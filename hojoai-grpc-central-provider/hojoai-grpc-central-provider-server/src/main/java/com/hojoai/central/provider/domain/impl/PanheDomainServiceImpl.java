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
package com.hojoai.central.provider.domain.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.central.provider.dto.response.movie.*;
import com.hojoai.central.provider.service.common.constants.PanheConstants;
import com.hojoai.central.provider.domain.PanheDomainService;
import com.hojoai.central.provider.dto.request.movie.HttpPanheCreateOrderRequest;
import com.hojoai.central.provider.repository.MovieOrderDetailRepository;
import com.hojoai.central.provider.repository.po.MovieOrderDetailPO;
import com.hojoai.common.jackson.JacksonUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 盘禾电影票务 HTTP 对接实现：场次/下单/支付/订单详情等，依赖配置中的 URL、AppKey 与签名字段。
 */
@Service
public class PanheDomainServiceImpl implements PanheDomainService {

    private static final Logger logger = LoggerFactory.getLogger(PanheDomainServiceImpl.class);

    @Resource
    private String panheUrl;

    @Resource
    private String panheAppKey;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MovieOrderDetailRepository movieOrderDetailRepository;

    @Value("${panhe.request.http.secretKey}")
    private String panheSecretKey;

    @Override
    public String getShowList(String showId, List<String> seatIdList) {
        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getShowSeats")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_SHOW_ID, showId)
                .build()
                .toUriString();

        CommonResponse<GetShowSeatListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.POST, null,
                new ParameterizedTypeReference<CommonResponse<GetShowSeatListResponse>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        if (!httpResponse.getSuccess()) {
            return BigDecimal.ZERO.toString();
        }

        GetShowSeatListResponse response = httpResponse.getData();

        return response.getSeatsInfo().getSeatInfoList().stream()
                .filter(seatInfo -> seatIdList.contains(seatInfo.getSeatId())).map(seatInfo -> {
                    if (Objects.nonNull(seatInfo.getSalePrice()) && seatInfo.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
                        return seatInfo.getSalePrice();
                    } else {
                        return seatInfo.getNetPrice();
                    }
                }).reduce(BigDecimal.ZERO, BigDecimal::add).toString();

    }

    @Override
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public String createPayOrder(String orderNo) {
        LambdaQueryWrapper<MovieOrderDetailPO> movieOrderWrapper = new LambdaQueryWrapper<>();
        movieOrderWrapper.eq(MovieOrderDetailPO::getOrderNo, orderNo);

        MovieOrderDetailPO movieOrderDetailPO = movieOrderDetailRepository.selectOne(movieOrderWrapper);

        String url = panheUrl + "/movie/createOrder";

        HttpPanheCreateOrderRequest httpRequest = HttpPanheCreateOrderRequest.convert2HttpPanheCreateOrderRequest(movieOrderDetailPO);
        httpRequest.setAppKey(panheAppKey);
        httpRequest.setCustomerOrderNo(orderNo);
        httpRequest.setCinemaId(movieOrderDetailPO.getCinemaId());
        httpRequest.setFilmId(movieOrderDetailPO.getFilmId());
        httpRequest.setShowId(movieOrderDetailPO.getShowId());
        httpRequest.setOrderAmount(movieOrderDetailPO.getOrderAmount());
        httpRequest.setAcceptChangeSeat(movieOrderDetailPO.getAcceptChangeSeat());
        httpRequest.setMobile(movieOrderDetailPO.getPhone());
        httpRequest.setAcceptChangeSeat(movieOrderDetailPO.getAcceptChangeSeat());
        httpRequest.setSeatIds(Arrays.asList(movieOrderDetailPO.getSeatIds().split(",")));
        httpRequest.setCallBackUrl(movieOrderDetailPO.getCallbackUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JacksonUtil.bean2Json(httpRequest), headers);

        CommonResponse<HttpPanheCreateOrderResponse> httpResponse = restTemplate.exchange(url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<CommonResponse<HttpPanheCreateOrderResponse>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        return httpResponse.getSuccess() ? httpResponse.getData().getOrderNo() : null;
    }

    @Recover
    public String createPayOrderRecover(Exception e, String orderNo) {
        // 重试耗尽后的降级：仅记录日志；出票/订单状态落库由主流程或其它补偿任务处理，避免在此处与 @Retryable 主路径重复写库
        logger.warn("PanheDomainServiceImpl.createPayOrderRecover, orderNo={}, error={}", orderNo, e.getMessage());

        return null;
    }

    @Override
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public Boolean pay(String panheOrderNo, String totalAmount) {
        String str = panheSecretKey + panheOrderNo + totalAmount + panheSecretKey;
        String signStr = DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));

        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/autoPay")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_ORDER_NO, panheOrderNo)
                .queryParam(PanheConstants.PARAM_PAY_AMOUNT, totalAmount)
                .queryParam(PanheConstants.PARAM_SIGN, signStr)
                .build()
                .toUriString();

        CommonResponse<Boolean> httpResponse = restTemplate.exchange(url, HttpMethod.POST, null,
                new ParameterizedTypeReference<CommonResponse<Boolean>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        return Boolean.TRUE;
    }

    @Recover
    public Boolean payRecover(Exception e, String panheOrderNo, String totalAmount) {
        logger.warn("PanheDomainServiceImpl.payRecover, panheOrderNo={}, error={}", panheOrderNo, e.getMessage());

        return null;
    }

    @Override
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 1.5))
    public HttpPanheGetOrderDetailResponse getOrderDetail(String orderNo) {
        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getOrderDetail")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_ORDER_NO, orderNo)
                .build()
                .toUriString();

        CommonResponse<HttpPanheGetOrderDetailResponse> httpResponse = restTemplate.exchange(url, HttpMethod.POST, null,
                new ParameterizedTypeReference<CommonResponse<HttpPanheGetOrderDetailResponse>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        return httpResponse.getData();
    }

    @Recover
    public HttpPanheGetOrderDetailResponse getOrderDetailRecover(Exception e, String orderNo) {
        logger.warn("PanheDomainServiceImpl.getOrderDetailRecover, orderNo={}, error={}", orderNo, e.getMessage());

        return null;
    }

    @Override
    public CommonResponse<GetShowSeatListResponse> getShowSeatList(String showId) {
        logger.info("PanheDomainServiceImpl.getShowSeatList showId is -> {}", showId);

        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getShowSeats")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_SHOW_ID, showId)
                .build().toUriString();

        CommonResponse<GetShowSeatListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.POST, null,
                new ParameterizedTypeReference<CommonResponse<GetShowSeatListResponse>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        logger.info("PanheDomainServiceImpl.getShowSeatList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

        return httpResponse;
    }

    @Override
    public CommonResponse<GetCinemaFilmListResponse> getCinemaFilmList(String cinemaId) {
        String url = UriComponentsBuilder.fromUriString(panheUrl + "/movie/getCinemaFilms")
                .queryParam(PanheConstants.PARAM_APP_KEY, panheAppKey)
                .queryParam(PanheConstants.PARAM_CINEMA_ID, cinemaId)
                .build()
                .toUriString();

        CommonResponse<GetCinemaFilmListResponse> httpResponse = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<CommonResponse<GetCinemaFilmListResponse>>() {}).getBody();

        Assert.notNull(httpResponse, "Response is null!");

        logger.info("PanheDomainServiceImpl.getCinemaFilmList http response is -> {}", JacksonUtil.bean2Json(httpResponse));

        return httpResponse;
    }
}
