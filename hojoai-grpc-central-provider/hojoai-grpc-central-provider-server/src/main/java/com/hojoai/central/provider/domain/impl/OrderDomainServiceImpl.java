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

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.central.provider.service.common.constants.NotifyConstants;
import com.hojoai.central.provider.service.common.constants.OrderConstants;
import com.hojoai.central.provider.service.common.enums.AlipayTradeStatusEnum;
import com.hojoai.central.provider.service.common.enums.BillStatusEnum;
import com.hojoai.central.provider.service.common.enums.OrderStatusEnum;
import com.hojoai.central.provider.utils.OrderNoGenerateUtil;
import com.hojoai.central.provider.utils.SnowflakeIdGenerator;
import com.hojoai.central.provider.domain.AlipayContextFactory;
import com.hojoai.central.provider.domain.OrderDomainService;
import com.hojoai.central.provider.dto.response.movie.GetCinemaFilmListResponse;
import com.hojoai.central.provider.dto.response.movie.GetShowSeatListResponse;
import com.hojoai.central.provider.dto.response.movie.HttpPanheGetOrderDetailResponse;
import com.hojoai.central.provider.dto.response.movie.SeatInfo;
import com.hojoai.central.provider.grpc.CreateOrderRequestDTO;
import com.hojoai.central.provider.repository.BillRepository;
import com.hojoai.central.provider.repository.MovieOrderDetailRepository;
import com.hojoai.central.provider.repository.OrderRepository;
import com.hojoai.central.provider.repository.po.BillPO;
import com.hojoai.central.provider.repository.po.MovieOrderDetailPO;
import com.hojoai.central.provider.repository.po.OrderPO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The OrderDomainServiceImpl class for order domain service.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Service
public class OrderDomainServiceImpl implements OrderDomainService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private BillRepository billRepository;

    @Resource
    private MovieOrderDetailRepository movieOrderDetailRepository;

    @Resource
    private OrderNoGenerateUtil orderNoGenerator;

    @Resource
    private AlipayContextFactory alipayContextFactory;

    @Value("${alipay.public.key}")
    private String ALIPAY_PUBLIC_KEY;

    @Value("${alipay.app.id}")
    private String APP_ID;

    @Value("${panhe.callback.url}")
    private String panheCallbackUrl;

    private static final String[] ignoreProperties = {"cinemaId", "orderNo"};

    private static final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    private static final Map<String, String> PAY_STAUS_MAP = new HashMap<>();

    static {
        PAY_STAUS_MAP.put(OrderStatusEnum.PENDING_PAYMENT.getValue(), BillStatusEnum.UNPAID.getValue());
        PAY_STAUS_MAP.put(OrderStatusEnum.PAID_WAITING_ISSUE.getValue(), BillStatusEnum.PAID.getValue());
        PAY_STAUS_MAP.put(OrderStatusEnum.CANCELLED.getValue(), BillStatusEnum.CANCELED.getValue());
        PAY_STAUS_MAP.put(OrderStatusEnum.REFUNDING.getValue(), BillStatusEnum.REFUNDING.getValue());
        PAY_STAUS_MAP.put(OrderStatusEnum.REFUNDED.getValue(), BillStatusEnum.REFUNDED.getValue());
    }

    @Override
    public OrderPO createOrder(CreateOrderRequestDTO requestDTO) {
        OrderPO orderPO = new OrderPO();

        Date date = new Date();

        orderPO.setId(snowflakeIdGenerator.nextId());
        orderPO.setOrderNo(orderNoGenerator.generateDailyOrderNo(OrderConstants.ORDER_PREFIX));
        orderPO.setUserId(requestDTO.getUserId());
        orderPO.setAmount(new BigDecimal(requestDTO.getOrderAmount()));
        orderPO.setOrderTime(date);
        orderPO.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());

        orderPO.setCreateTime(date);
        orderPO.setUpdateTime(date);
        orderPO.setDeleteFlag(Boolean.FALSE);
        orderPO.setVersion(0);

        orderRepository.insert(orderPO);

        return orderPO;
    }

    @Override
    public Pair<String, Long> alipayPay(OrderPO orderPO, String aliPayType) throws AlipayApiException {
        return alipayContextFactory.getAlipayStrategy(aliPayType).alipayPay(orderPO);
    }

    @Override
    public void createMovieOrderDetail(OrderPO orderPO, CreateOrderRequestDTO requestDTO, GetShowSeatListResponse getShowSeatListResponse, GetCinemaFilmListResponse getCinemaFilmListResponse) {
        MovieOrderDetailPO movieOrderDetailPO = new MovieOrderDetailPO();

        Date date = new Date();
        movieOrderDetailPO.setId(snowflakeIdGenerator.nextId());
        movieOrderDetailPO.setOrderId(orderPO.getId());
        movieOrderDetailPO.setOrderNo(orderPO.getOrderNo());
        movieOrderDetailPO.setOrderAmount(orderPO.getAmount());
        movieOrderDetailPO.setUserId(orderPO.getUserId());
        movieOrderDetailPO.setFilmId(requestDTO.getFilmId());
        movieOrderDetailPO.setCinemaId(requestDTO.getCinemaId());
        movieOrderDetailPO.setShowId(requestDTO.getShowId());
        movieOrderDetailPO.setPhone(requestDTO.getPhone());
        movieOrderDetailPO.setTicketCount(requestDTO.getSeatIdListCount());
        movieOrderDetailPO.setAcceptChangeSeat(requestDTO.getAcceptChangeSeat());
        movieOrderDetailPO.setCallbackUrl(panheCallbackUrl);
        movieOrderDetailPO.setTicketsData("");

        Optional.ofNullable(getCinemaFilmListResponse).ifPresent(response -> {
            movieOrderDetailPO.setCinemaName(response.getCinemaInfo().getCinemaName());
            movieOrderDetailPO.setCinemaAddress(response.getCinemaInfo().getCinemaAddress());
            movieOrderDetailPO.setCinemaTel(response.getCinemaInfo().getTel());
            movieOrderDetailPO.setCityName(response.getCinemaInfo().getCityName());
            movieOrderDetailPO.setLongitude(new BigDecimal(response.getCinemaInfo().getLongitude()));
            movieOrderDetailPO.setLatitude(new BigDecimal(response.getCinemaInfo().getLatitude()));
        });

        Optional.ofNullable(getShowSeatListResponse).ifPresent(response -> {
            movieOrderDetailPO.setFilmName(response.getFilmShowInfo().getFilmName());
            movieOrderDetailPO.setHallName(response.getFilmShowInfo().getHallName());
            movieOrderDetailPO.setFilmImgUrl(response.getFilmInfo().getPic());
            movieOrderDetailPO.setShowId(response.getFilmShowInfo().getShowId());

            try {
                movieOrderDetailPO.setShowTime(DateUtils.parseDate(response.getFilmShowInfo().getShowTime(), "yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            String seatName = response.getSeatsInfo().getSeatInfoList().stream()
                    .filter(seatInfo -> requestDTO.getSeatIdListList().contains(seatInfo.getSeatId()))
                    .map(SeatInfo::getSeatNo)
                    .collect(Collectors.joining(","));

            movieOrderDetailPO.setSeatNos(seatName);
        });

        String seatStr = String.join(",", requestDTO.getSeatIdListList());
        movieOrderDetailPO.setSeatIds(seatStr);

        movieOrderDetailPO.setCreateTime(date);
        movieOrderDetailPO.setUpdateTime(date);
        movieOrderDetailPO.setVersion(0);
        movieOrderDetailPO.setDeleteFlag(Boolean.FALSE);

        movieOrderDetailRepository.insert(movieOrderDetailPO);
    }

    @Override
    public void createBill(OrderPO orderPO, Pair<String, Long> alipayPair) {
        BillPO billPO = new BillPO();

        Date date = new Date();
        billPO.setId(snowflakeIdGenerator.nextId());
        billPO.setBillNo(orderNoGenerator.generateDailyOrderNo(OrderConstants.BILL_PREFIX));
        billPO.setOrderId(orderPO.getId());
        billPO.setOrderNo(orderPO.getOrderNo());
        billPO.setAmount(orderPO.getAmount());
        billPO.setBillStatus(BillStatusEnum.PAYING.getCode());
        billPO.setBillCreateTime(orderPO.getOrderTime());
        billPO.setOrderStr(alipayPair.getLeft());
        billPO.setExpireTime(alipayPair.getRight());

        billPO.setCreateTime(date);
        billPO.setUpdateTime(date);
        billPO.setVersion(0);
        billPO.setDeleteFlag(Boolean.FALSE);

        billRepository.insert(billPO);
    }

    @Override
    public Boolean validate(Map<String, String> paramMap) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, ALIPAY_PUBLIC_KEY, OrderConstants.CHARSET_UTF_8,OrderConstants.SIGN_TYPE_RSA2);

        if (!signVerified) {
            return Boolean.FALSE;
        }

        String tradeStatus = paramMap.get(NotifyConstants.ALIPAY_TRADE_STATUS);
        String orderNo = paramMap.get(NotifyConstants.ALIPAY_OUT_TRADE_NO);
        String appId = paramMap.get(NotifyConstants.ALIPAY_APP_ID);

        if (!APP_ID.equals(appId)) {
            return Boolean.FALSE;
        }

        if (!AlipayTradeStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus)
                || !AlipayTradeStatusEnum.TRADE_CLOSED.getCode().equals(tradeStatus)
                || !AlipayTradeStatusEnum.TRADE_FINISHED.getCode().equals(tradeStatus)) {
            return Boolean.FALSE;
        }

        LambdaQueryWrapper<OrderPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderPO::getOrderNo, orderNo);

        boolean exists = orderRepository.exists(lambdaQueryWrapper);

        if (!exists) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public MovieOrderDetailPO payNotifyUpdateStatus(String orderNo, String orderStatus, HttpPanheGetOrderDetailResponse response, Boolean isPanhe) {
        LambdaQueryWrapper<OrderPO> orderWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MovieOrderDetailPO> movieOrderWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<BillPO> billWrapper = new LambdaQueryWrapper<>();

        if (isPanhe) {
            orderWrapper.eq(OrderPO::getOutOrderNo, orderNo);
            movieOrderWrapper.eq(MovieOrderDetailPO::getOutOrderNo, orderNo);
        } else {
            orderWrapper.eq(OrderPO::getOrderNo, orderNo);
            movieOrderWrapper.eq(MovieOrderDetailPO::getOrderNo, orderNo);
        }

        OrderPO orderPO = orderRepository.selectOne(orderWrapper);

        MovieOrderDetailPO movieOrderDetailPO = movieOrderDetailRepository.selectOne(movieOrderWrapper);

        billWrapper.eq(BillPO::getOrderNo, orderPO.getOrderNo());

        BillPO billPO = billRepository.selectOne(billWrapper);

        orderPO.setOrderStatus(orderStatus);
        billPO.setBillStatus(PAY_STAUS_MAP.getOrDefault(orderStatus, BillStatusEnum.PAID.getCode()));

        if (Objects.nonNull(response)) {
            orderPO.setOutOrderNo(response.getOrderNo());

            BeanUtils.copyProperties(response, movieOrderDetailPO, ignoreProperties);
            movieOrderDetailPO.setOutOrderNo(response.getOrderNo());
        }

        movieOrderDetailPO.setOrderStatus(Integer.valueOf(orderStatus));

        orderRepository.update(orderPO, orderWrapper);
        movieOrderDetailRepository.update(movieOrderDetailPO, movieOrderWrapper);
        billRepository.update(billPO, billWrapper);

        return movieOrderDetailPO;
    }

    @Override
    public MovieOrderDetailPO getOrderDetail(String userId, String orderNo) {
        LambdaQueryWrapper<MovieOrderDetailPO> movieOrderWrapper = new LambdaQueryWrapper<>();
        movieOrderWrapper.eq(MovieOrderDetailPO::getUserId, userId)
                .eq(MovieOrderDetailPO::getOrderNo, orderNo);

        return movieOrderDetailRepository.selectOne(movieOrderWrapper);
    }

    @Override
    public MovieOrderDetailPO updateMovieOrderDetail(MovieOrderDetailPO movieOrderDetailPO, HttpPanheGetOrderDetailResponse httpPanheGetOrderDetailResponse) {
        if (Objects.nonNull(httpPanheGetOrderDetailResponse)) {
            BeanUtils.copyProperties(httpPanheGetOrderDetailResponse, movieOrderDetailPO, ignoreProperties);
        }

        movieOrderDetailRepository.updateById(movieOrderDetailPO);

        return movieOrderDetailPO;
    }

    @Override
    public List<BillPO> getBillByOrderNo(String orderNo) {
        LambdaQueryWrapper<BillPO> billWrapper = new LambdaQueryWrapper<>();

        billWrapper.eq(BillPO::getOrderNo, orderNo);

        return billRepository.selectList(billWrapper);
    }

}
