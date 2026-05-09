/*
 * Copyright 2026 HojoAI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hojoai.central.provider.service.movie.impl;

import com.google.protobuf.util.JsonFormat;
import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.dto.response.movie.*;
import com.hojoai.central.provider.service.common.constants.PanheConstants;
import com.hojoai.central.provider.domain.PanheDomainService;
import com.hojoai.central.provider.grpc.*;
import com.hojoai.central.provider.grpc.FilmInfo;
import com.hojoai.central.provider.grpc.FilmShowInfo;
import com.hojoai.central.provider.grpc.ScheduleInfo;
import com.hojoai.central.provider.grpc.SeatsInfo;
import com.hojoai.common.jackson.JacksonUtil;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 电影票务 gRPC 实现：影片/场次/座位、盘禾 HTTP 对接与 Redis 缓存等。
 */
@GrpcService
public class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private String panheUrl;

    @Resource
    private String panheAppKey;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PanheDomainService panheDomainService;

    @Override
    public void getFilmList(GetFilmListRequestDTO requestDTO, StreamObserver<GetFilmListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getFilmList request is -> {}", JsonFormat.printer().print(requestDTO));

            GetFilmListRespDTO.Builder builder = GetFilmListRespDTO.newBuilder();

            String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getFilmList")
                    .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                    .queryParam(PanheConstants.PARAM_TYPE, requestDTO.getType())
                    .queryParam(PanheConstants.PARAM_KEYWORD, requestDTO.getKeyWord())
                    .queryParam(PanheConstants.PARAM_PAGE_INDEX, requestDTO.getPageIndex())
                    .queryParam(PanheConstants.PARAM_PAGE_SIZE, requestDTO.getPageSize())
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            CommonResponse<GetFilmListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<CommonResponse<GetFilmListResponse>>() {}).getBody();

            Assert.notNull(httpResponse, "httpResponse is null!");

            logger.info("MovieServiceImpl.getFilmList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

            if (httpResponse.getSuccess()) {
                GetFilmListResponse getFilmListResponse = httpResponse.getData();

                builder.setTotal(getFilmListResponse.getTotal());

                if (CollectionUtils.isNotEmpty(getFilmListResponse.getFilmList())) {
                    getFilmListResponse.getFilmList().forEach(film -> builder.addFilmList(this.getFilmInfo(film)));
                }
            }

            logger.info("MovieServiceImpl.getFilmList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getFilmList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getScheduleCinemaList(GetScheduleCinemaListRequestDTO requestDTO, StreamObserver<GetScheduleCinemaListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getScheduleCinemaList request is -> {}", JsonFormat.printer().print(requestDTO));

            String cityCode = requestDTO.getCityCode();

            if (StringUtils.isEmpty(cityCode)) {

                if (!redisTemplate.hasKey(PanheConstants.CITY_REDIS_KEY)) {
                    this.allCityRedisHandle();
                }

                cityCode = String.valueOf(redisTemplate.opsForHash().get(PanheConstants.CITY_REDIS_KEY, requestDTO.getCityName()));
            }

            logger.info("MovieServiceImpl.getScheduleCinemaList cityCode is -> {}", cityCode);

            String regionCode = requestDTO.getRegionCode();

            if (StringUtils.isEmpty(requestDTO.getRegionCode()) && StringUtils.isNotEmpty(requestDTO.getRegionName())) {
                regionCode = this.getRegionCode(requestDTO.getRegionName(), cityCode);
            }

            logger.info("MovieServiceImpl.getScheduleCinemaList regionCode is -> {}", regionCode);

            GetScheduleCinemaListRespDTO.Builder builder = GetScheduleCinemaListRespDTO.newBuilder();

            String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getScheduleCinemaList")
                    .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                    .queryParam(PanheConstants.PARAM_FILM_ID, requestDTO.getFilmId())
                    .queryParam(PanheConstants.PARAM_CITY_CODE, cityCode)
                    .queryParam(PanheConstants.PARAM_REGION_CODE, StringUtils.isEmpty(requestDTO.getRegionCode()) ? regionCode : requestDTO.getRegionCode())
                    .queryParam(PanheConstants.PARAM_LOW_KEYWORD, requestDTO.getKeyword())
                    .queryParam(PanheConstants.PARAM_LONGITUDE, requestDTO.getLongitude())
                    .queryParam(PanheConstants.PARAM_LATITUDE, requestDTO.getLatitude())
                    .queryParam(PanheConstants.PARAM_DATE, requestDTO.getDate())
                    .queryParam(PanheConstants.PARAM_PAGE_INDEX, requestDTO.getPageIndex())
                    .queryParam(PanheConstants.PARAM_PAGE_SIZE, requestDTO.getPageSize())
                    .build()
                    .toUriString();

            CommonResponse<GetScheduleCinemaListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<CommonResponse<GetScheduleCinemaListResponse>>() {}).getBody();

            Assert.notNull(httpResponse, "Response is null!");

            logger.info("MovieServiceImpl.getScheduleCinemaList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

            if (httpResponse.getSuccess()) {
                GetScheduleCinemaListResponse getFilmSchedulesResponse = httpResponse.getData();

                if (Objects.isNull(getFilmSchedulesResponse.getTotal()) || 0 == getFilmSchedulesResponse.getTotal()) {
                    String newUrl = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getScheduleCinemaList")
                            .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                            .queryParam(PanheConstants.PARAM_FILM_ID, requestDTO.getFilmId())
                            .queryParam(PanheConstants.PARAM_CITY_CODE, cityCode)
                            .queryParam(PanheConstants.PARAM_REGION_CODE, regionCode)
                            .queryParam(PanheConstants.PARAM_LOW_KEYWORD, StringUtils.EMPTY)
                            .queryParam(PanheConstants.PARAM_LONGITUDE, requestDTO.getLongitude())
                            .queryParam(PanheConstants.PARAM_LATITUDE, requestDTO.getLatitude())
                            .queryParam(PanheConstants.PARAM_DATE, requestDTO.getDate())
                            .queryParam(PanheConstants.PARAM_PAGE_INDEX, requestDTO.getPageIndex())
                            .queryParam(PanheConstants.PARAM_PAGE_SIZE, requestDTO.getPageSize())
                            .build()
                            .toUriString();

                    httpResponse = restTemplate.exchange(newUrl, HttpMethod.GET, null,
                            new ParameterizedTypeReference<CommonResponse<GetScheduleCinemaListResponse>>() {}).getBody();

                    Assert.notNull(httpResponse, "Response is null!");
                }

                getFilmSchedulesResponse = httpResponse.getData();

                builder.setTotal(getFilmSchedulesResponse.getTotal());

                if (CollectionUtils.isNotEmpty(getFilmSchedulesResponse.getCinemaList())) {
                    getFilmSchedulesResponse.getCinemaList().forEach(cinema -> builder.addData(this.getCinemaVal(cinema)));
                }
            }

            logger.info("MovieServiceImpl.getScheduleCinemaList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getScheduleCinemaList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private String getRegionCode(String regionName, String cityCode) {
        String regionUrl = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getCityRegions")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_CITY_CODE, cityCode)
                .build()
                .toUriString();

        CommonResponse<List<GetCityRegionListResponse>> regionHttpResponse = restTemplate.exchange(regionUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<CommonResponse<List<GetCityRegionListResponse>>>() {}).getBody();

        Assert.notNull(regionHttpResponse, "regionHttpResponse is null!");

        logger.info("MovieServiceImpl.getScheduleCinemaList region http response is -> {}", JacksonUtil.bean2Json(regionHttpResponse));

        if (regionHttpResponse.getSuccess()) {
            List<GetCityRegionListResponse> getCityRegionListResponseList = regionHttpResponse.getData();

            return getCityRegionListResponseList.stream()
                    .filter(region -> region.getRegionName().contains(regionName))
                    .map(GetCityRegionListResponse::getRegionCode)
                    .findAny()
                    .orElse(StringUtils.EMPTY);
        }

        return StringUtils.EMPTY;
    }

    @Override
    public void getFilmShowList(GetFilmShowListRequestDTO requestDTO, StreamObserver<GetFilmShowListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getFilmShowList request is -> {}", JsonFormat.printer().print(requestDTO));

            GetFilmShowListRespDTO.Builder builder = GetFilmShowListRespDTO.newBuilder();

            String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getFilmShowList")
                    .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                    .queryParam(PanheConstants.PARAM_CINEMA_ID, requestDTO.getCinemaId())
                    .queryParam(PanheConstants.PARAM_FILM_ID, requestDTO.getFilmId())
                    .queryParam(PanheConstants.PARAM_DATE, requestDTO.getDate())
                    .build()
                    .toUriString();

            CommonResponse<GetFilmShowListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<CommonResponse<GetFilmShowListResponse>>() {}).getBody();

            Assert.notNull(httpResponse, "Response is null!");

            logger.info("MovieServiceImpl.getFilmShowList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

            if (httpResponse.getSuccess()) {
                GetFilmShowListResponse getFilmShowListResponse = httpResponse.getData();

                getFilmShowListResponse.getFilmShowInfoList().forEach(cinema -> {
                    cinema.setCinemaId(requestDTO.getCinemaId());

                    redisTemplate.opsForValue().set(cinema.getShowId(), requestDTO.getCinemaId(), PanheConstants.REDIS_EXPIRE_TIME_CINEMA_ID, TimeUnit.DAYS);

                    builder.addShowList(this.getFilmShowInfo(cinema));
                });

                if (CollectionUtils.isNotEmpty(getFilmShowListResponse.getScheduleInfoList())) {
                    getFilmShowListResponse.getScheduleInfoList().forEach(scheduleInfo -> {
                        ScheduleInfo schedule = ScheduleInfo.newBuilder().setShowTime(StringUtils.defaultString(scheduleInfo.getShowTime())).build();

                        builder.addScheduleList(schedule);
                    });
                }
            }

            logger.info("MovieServiceImpl.getFilmShowList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getFilmShowList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getShowSeatList(GetShowSeatListRequestDTO requestDTO, StreamObserver<GetShowSeatListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getShowSeatList request is -> {}", JsonFormat.printer().print(requestDTO));

            GetShowSeatListRespDTO.Builder builder = GetShowSeatListRespDTO.newBuilder();

            CommonResponse<GetShowSeatListResponse> httpResponse = panheDomainService.getShowSeatList(requestDTO.getShowId());

            if (httpResponse.getSuccess()) {
                GetShowSeatListResponse getShowSeatListResponse = httpResponse.getData();

                builder.setFilmInfo(this.getFilmInfo(getShowSeatListResponse));

                builder.setShowInfo(this.getFilmShowInfo(getShowSeatListResponse));

                SeatsInfo.Builder seatsInfoBuilder = SeatsInfo.newBuilder();

                com.hojoai.central.provider.dto.response.movie.SeatsInfo seatsInfo = getShowSeatListResponse.getSeatsInfo();

                if (CollectionUtils.isNotEmpty(seatsInfo.getSeatInfoList())) {
                    seatsInfo.getSeatInfoList().forEach(seatInfo -> seatsInfoBuilder.addSeats(this.getSeat(seatInfo)));
                }

                seatsInfoBuilder.setRestrictions(Objects.isNull(seatsInfo.getRestrictions()) ? 0 : seatsInfo.getRestrictions())
                        .setMaxColumnNo(Objects.isNull(seatsInfo.getMaxColumnNo()) ? 0 : seatsInfo.getMaxColumnNo())
                        .setMinColumnNo(Objects.isNull(seatsInfo.getMinColumnNo()) ? 0 : seatsInfo.getMinColumnNo())
                        .setMaxRowNo(Objects.isNull(seatsInfo.getMaxRowNo()) ? 0 : seatsInfo.getMaxRowNo())
                        .setMinRowNo(Objects.isNull(seatsInfo.getMinRowNo()) ? 0 : seatsInfo.getMinRowNo())
                        .setRealSeatRowCount(Objects.isNull(seatsInfo.getRealSeatRowCount()) ? 0 : seatsInfo.getRealSeatRowCount())
                        .setIsExistLoveSeat(Objects.nonNull(seatsInfo.getExistLoveSeat()) && seatsInfo.getExistLoveSeat());

                if (CollectionUtils.isNotEmpty(seatsInfo.getAreaPriceInfoList())) {
                    seatsInfo.getAreaPriceInfoList().forEach(areaPriceInfo -> seatsInfoBuilder.addAreaPrices(this.getAreaPrice(areaPriceInfo)));
                }

                builder.setSeatsInfo(seatsInfoBuilder.build());
            }

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            logger.info("MovieServiceImpl.getShowSeatList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getShowSeatList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getCinemaList(GetCinemaListRequestDTO requestDTO, StreamObserver<GetCinemaListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getCinemaList request is -> {}", JsonFormat.printer().print(requestDTO));

            String cityCode = requestDTO.getCityCode();

            if (StringUtils.isEmpty(cityCode)) {

                if (!redisTemplate.hasKey(PanheConstants.CITY_REDIS_KEY)) {
                    this.allCityRedisHandle();
                }

                cityCode = String.valueOf(redisTemplate.opsForHash().get(PanheConstants.CITY_REDIS_KEY, requestDTO.getCityName()));
            }

            logger.info("MovieServiceImpl.getCinemaList cityCode is -> {}", cityCode);

            String regionCode = requestDTO.getRegionCode();

            if (StringUtils.isEmpty(requestDTO.getRegionCode()) && StringUtils.isNotEmpty(requestDTO.getRegionName())) {
                regionCode = this.getRegionCode(requestDTO.getRegionName(), cityCode);
            }

            logger.info("MovieServiceImpl.getCinemaList regionCode is -> {}", regionCode);

            GetCinemaListRespDTO.Builder builder = GetCinemaListRespDTO.newBuilder();

            String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getCinemaList")
                    .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                    .queryParam(PanheConstants.PARAM_CITY_CODE, cityCode)
                    .queryParam(PanheConstants.PARAM_REGION_CODE, regionCode)
                    .queryParam(PanheConstants.PARAM_LOW_KEYWORD, requestDTO.getKeyword())
                    .queryParam(PanheConstants.PARAM_LONGITUDE, requestDTO.getLongitude())
                    .queryParam(PanheConstants.PARAM_LATITUDE, requestDTO.getLatitude())
                    .queryParam(PanheConstants.PARAM_PAGE_INDEX, requestDTO.getPageIndex())
                    .queryParam(PanheConstants.PARAM_PAGE_SIZE, requestDTO.getPageSize())
                    .build()
                    .toUriString();

            CommonResponse<GetCinemaListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<CommonResponse<GetCinemaListResponse>>() {}).getBody();

            Assert.notNull(httpResponse, "Response is null!");

            logger.info("MovieServiceImpl.getCinemaList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

            if (httpResponse.getSuccess()) {
                GetCinemaListResponse response = httpResponse.getData();

                builder.setTotal(response.getTotal());

                if (CollectionUtils.isNotEmpty(response.getCinemaInfoList())) {
                    response.getCinemaInfoList().forEach(cinema -> builder.addCinemaList(this.getCinemaVal(cinema)));
                }
            }

            logger.info("MovieServiceImpl.getCinemaList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getCinemaList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getCinemaFilmList(GetCinemaFilmListRequestDTO requestDTO, StreamObserver<GetCinemaFilmListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getCinemaFilmList request is -> {}", JsonFormat.printer().print(requestDTO));

            GetCinemaFilmListRespDTO.Builder builder = GetCinemaFilmListRespDTO.newBuilder();

            CommonResponse<GetCinemaFilmListResponse> httpResponse = panheDomainService.getCinemaFilmList(requestDTO.getCinemaId());

            if (httpResponse.getSuccess()) {
                GetCinemaFilmListResponse response = httpResponse.getData();

                builder.setCinemaVal(this.getCinemaVal(response.getCinemaInfo()));

                if (CollectionUtils.isNotEmpty(response.getFilmInfoList())) {
                    response.getFilmInfoList().forEach(filmInfo -> builder.addFilmList((this.getFilmInfo(filmInfo))));
                }
            }

            logger.info("MovieServiceImpl.getCinemaFilmList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getCinemaFilmList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getFilmScheduleList(GetFilmScheduleListRequestDTO requestDTO, StreamObserver<GetFilmScheduleListRespDTO> responseObserver) {
        try {
            logger.info("MovieServiceImpl.getFilmScheduleList request is -> {}", JsonFormat.printer().print(requestDTO));

            String cityCode = requestDTO.getCityCode();

            if (StringUtils.isEmpty(cityCode)) {

                if (!redisTemplate.hasKey(PanheConstants.CITY_REDIS_KEY)) {
                    this.allCityRedisHandle();
                }

                cityCode = String.valueOf(redisTemplate.opsForHash().get(PanheConstants.CITY_REDIS_KEY, requestDTO.getCityName()));
            }

            logger.info("MovieServiceImpl.getFilmScheduleList cityCode is -> {}", cityCode);

            GetFilmScheduleListRespDTO.Builder builder = GetFilmScheduleListRespDTO.newBuilder();

            String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getFilmSchedules")
                    .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                    .queryParam(PanheConstants.PARAM_FILM_ID, requestDTO.getFilmId())
                    .queryParam(PanheConstants.PARAM_CITY_CODE, cityCode)
                    .build()
                    .toUriString();

            CommonResponse<GetFilmScheduleListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<CommonResponse<GetFilmScheduleListResponse>>() {}).getBody();

            Assert.notNull(httpResponse, "Response is null!");

            logger.info("MovieServiceImpl.getFilmScheduleList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

            if (httpResponse.getSuccess()) {
                GetFilmScheduleListResponse response = httpResponse.getData();

                builder.setFilmInfo(this.getFilmInfo(response.getFilmInfo()));

                if (CollectionUtils.isNotEmpty(response.getScheduleInfoList())) {
                    response.getScheduleInfoList().forEach(scheduleInfo -> {
                        ScheduleInfo schedule = ScheduleInfo.newBuilder().setShowTime(scheduleInfo.getShowTime()).build();

                        builder.addScheduleInfoList(schedule);
                    });
                }
            }

            logger.info("MovieServiceImpl.getFilmScheduleList grpc response is -> {}", JsonFormat.printer().print(builder.build()));

            builder.setSuccess(httpResponse.getSuccess()).setMsg(httpResponse.getMsg());

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("MovieServiceImpl.getFilmScheduleList error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private void allCityRedisHandle() {
        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getAllCity")
                .queryParam("appKey", panheAppKey)
                .build()
                .toUriString();

        CommonResponse<List<GetAllCityResponse>> cityResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<CommonResponse<List<GetAllCityResponse>>>() {}).getBody();

        Assert.notNull(cityResponse, "cityResponse is null!");

        logger.info("MovieServiceImpl.allCityRedisHandle cityResponse is -> {}", JacksonUtil.bean2Json(cityResponse));

        if (cityResponse.getSuccess() && CollectionUtils.isNotEmpty(cityResponse.getData())) {

            Map<String, String> cityMap = cityResponse.getData().stream()
                    .collect(Collectors.toMap(GetAllCityResponse::getCityName, GetAllCityResponse::getCityCode));

            redisTemplate.opsForHash().putAll(PanheConstants.CITY_REDIS_KEY, cityMap);
            redisTemplate.expire(PanheConstants.CITY_REDIS_KEY, PanheConstants.REDIS_EXPIRE_TIME, TimeUnit.DAYS);
        }
    }

    private FilmInfo getFilmInfo(com.hojoai.central.provider.dto.response.movie.FilmInfo film) {
        return FilmInfo.newBuilder()
                .setFilmId(StringUtils.defaultString(film.getFilmId()))
                .setFilmCode(StringUtils.defaultString(film.getFilmCode()))
                .setName(StringUtils.defaultString(film.getName()))
                .setPic(StringUtils.defaultString(film.getPic()))
                .setGrade(StringUtils.defaultString(film.getGrade()))
                .setLike(Objects.isNull(film.getLike()) ? 0 : film.getLike())
                .setDirector(StringUtils.defaultString(film.getDirector()))
                .setLeadingRole(StringUtils.defaultString(film.getLeadingRole()))
                .setPublishDate(StringUtils.defaultString(film.getPublishDate()))
                .setDuration(Objects.isNull(film.getDuration()) ? 0 : film.getDuration())
                .setFilmTypes(StringUtils.defaultString(film.getFilmTypes()))
                .build();
    }

    private CinemaVal getCinemaVal(CinemaInfo cinema) {
        return CinemaVal.newBuilder()
                .setCinemaId(StringUtils.defaultString(cinema.getCinemaId()))
                .setCinemaName(StringUtils.defaultString(cinema.getCinemaName()))
                .setCinemaAddress(StringUtils.defaultString(cinema.getCinemaAddress()))
                .setCityName(StringUtils.defaultString(cinema.getCityName()))
                .setDistanceText(StringUtils.defaultString(cinema.getDistanceText()))
                .setDownPrice(StringUtils.defaultString(cinema.getDownPrice()))
                .setLatitude(StringUtils.defaultString(cinema.getLatitude()))
                .setLongitude(StringUtils.defaultString(cinema.getLongitude()))
                .setTel(StringUtils.defaultString(cinema.getTel()))
                .build();
    }

    private FilmShowInfo getFilmShowInfo(com.hojoai.central.provider.dto.response.movie.FilmShowInfo cinema) {
        return FilmShowInfo.newBuilder()
                .setFilmId(StringUtils.defaultString(cinema.getFilmId()))
                .setShowId(StringUtils.defaultString(cinema.getShowId()))
                .setCinemaId(StringUtils.defaultString(cinema.getCinemaId()))
                .setDuration(Objects.isNull(cinema.getDuration()) ? 0 : cinema.getDuration())
                .setFilmCode(StringUtils.defaultString(cinema.getFilmCode()))
                .setFilmName(StringUtils.defaultString(cinema.getFilmName()))
                .setHallName(StringUtils.defaultString(cinema.getHallName()))
                .setLanguage(StringUtils.defaultString(cinema.getLanguage()))
                .setPlanType(StringUtils.defaultString(cinema.getPlanType()))
                .setShowTime(StringUtils.defaultString(cinema.getShowTime()))
                .setShowVersionType(StringUtils.defaultString(cinema.getShowVersionType()))
                .setStopSellTime(StringUtils.defaultString(cinema.getStopSellTime()))
                .setDownNetPrice(StringUtils.defaultString(cinema.getDownNetPrice().toString()))
                .setDownSalePrice(StringUtils.defaultString(cinema.getDownSalePrice().toString()))
                .build();
    }

    private AreaPrice getAreaPrice(AreaPriceInfo getShowSeat) {
        return AreaPrice.newBuilder()
                .setAreaId(StringUtils.defaultString(getShowSeat.getAreaId()))
                .setNetPrice(StringUtils.defaultString(getShowSeat.getNetPrice().toString()))
                .setSalePrice(StringUtils.defaultString(getShowSeat.getSalePrice().toString()))
                .build();
    }

    private Seat getSeat(SeatInfo seatInfo) {
        return Seat.newBuilder()
                .setSeatId(StringUtils.defaultString(seatInfo.getSeatId()))
                .setColumnNo(StringUtils.defaultString(seatInfo.getColumnNo()))
                .setRowNo(StringUtils.defaultString(seatInfo.getRowNo()))
                .setSeatNo(StringUtils.defaultString(seatInfo.getSeatNo()))
                .setStatus(StringUtils.defaultString(seatInfo.getStatus()))
                .setLoveStatus(Objects.isNull(seatInfo.getLoveStatus()) ? 0 : seatInfo.getLoveStatus())
                .setAreaId(StringUtils.defaultString(seatInfo.getAreaId()))
                .setNetPrice(Objects.isNull(seatInfo.getNetPrice()) ? StringUtils.EMPTY : seatInfo.getNetPrice().toString())
                .setSalePrice(StringUtils.defaultString(seatInfo.getSalePrice().toString()))
                .setRowName(StringUtils.defaultString(seatInfo.getRowName()))
                .build();
    }

    private FilmShowInfo getFilmShowInfo(GetShowSeatListResponse getShowSeatListResponse) {
        String cinemaId = String.valueOf(redisTemplate.opsForValue().get(getShowSeatListResponse.getFilmShowInfo().getShowId()));
        logger.info("OrderServiceImpl.getFilmShowInfo showId is -> {}, cinemaId is -> {}", getShowSeatListResponse.getFilmShowInfo().getShowId(), cinemaId);

        return FilmShowInfo.newBuilder()
                .setFilmId(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getFilmId()))
                .setShowId(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getShowId()))
                .setCinemaId(StringUtils.defaultString(cinemaId))
                .setDuration(Objects.isNull(getShowSeatListResponse.getFilmShowInfo().getDuration()) ? 0 : getShowSeatListResponse.getFilmShowInfo().getDuration())
                .setFilmCode(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getFilmCode()))
                .setFilmName(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getFilmName()))
                .setHallName(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getHallName()))
                .setLanguage(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getLanguage()))
                .setPlanType(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getPlanType()))
                .setShowTime(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getShowTime()))
                .setShowVersionType(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getShowVersionType()))
                .setStopSellTime(StringUtils.defaultString(getShowSeatListResponse.getFilmShowInfo().getStopSellTime()))
                .setDownNetPrice(Objects.isNull(getShowSeatListResponse.getFilmShowInfo().getDownNetPrice()) ? StringUtils.EMPTY : getShowSeatListResponse.getFilmShowInfo().getDownNetPrice().toString())
                .setDownSalePrice(Objects.isNull(getShowSeatListResponse.getFilmShowInfo().getDownSalePrice()) ? StringUtils.EMPTY : getShowSeatListResponse.getFilmShowInfo().getDownSalePrice().toString())
                .build();
    }

    private FilmInfo getFilmInfo(GetShowSeatListResponse getShowSeatListResponse) {
        return FilmInfo.newBuilder()
                .setFilmId(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getFilmId()))
                .setFilmCode(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getFilmCode()))
                .setName(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getName()))
                .setPic(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getPic()))
                .setGrade(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getGrade()))
                .setLike(Objects.isNull(getShowSeatListResponse.getFilmInfo().getLike()) ? 0 : getShowSeatListResponse.getFilmInfo().getLike())
                .setDirector(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getDirector()))
                .setLeadingRole(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getLeadingRole()))
                .setPublishDate(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getPublishDate()))
                .setDuration(Objects.isNull(getShowSeatListResponse.getFilmInfo().getDuration()) ? 0 : getShowSeatListResponse.getFilmInfo().getDuration())
                .setFilmTypes(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getFilmTypes()))
                .setMovieArea(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getMovieArea()))
                .setLanguage(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getLanguage()))
                .setIntro(StringUtils.defaultString(getShowSeatListResponse.getFilmInfo().getIntro()))
                .build();
    }

}
