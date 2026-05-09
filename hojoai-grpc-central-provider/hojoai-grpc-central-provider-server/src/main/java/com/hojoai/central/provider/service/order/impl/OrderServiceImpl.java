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
package com.hojoai.central.provider.service.order.impl;

import com.google.protobuf.util.JsonFormat;
import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.dto.response.movie.*;
import com.hojoai.central.provider.service.common.constants.NotifyConstants;
import com.hojoai.central.provider.service.common.constants.OrderConstants;
import com.hojoai.central.provider.service.common.enums.AlipayTradeStatusEnum;
import com.hojoai.central.provider.service.common.enums.OrderStatusEnum;
import com.hojoai.central.provider.domain.OrderDomainService;
import com.hojoai.central.provider.domain.PanheDomainService;
import com.hojoai.central.provider.grpc.*;
import com.hojoai.central.provider.repository.po.BillPO;
import com.hojoai.central.provider.repository.po.MovieOrderDetailPO;
import com.hojoai.central.provider.repository.po.OrderPO;
import com.hojoai.common.jackson.JacksonUtil;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 订单相关 gRPC 实现：查单、创单、支付宝/盘禾回调与状态同步，依赖订单领域与 Redis 防重。
 */
@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Resource
    private OrderDomainService orderDomainService;

    @Resource
    private PanheDomainService panheDomainService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final Map<String, String> PAY_STAUS_MAP = new HashMap<>();

    static {
        PAY_STAUS_MAP.put(AlipayTradeStatusEnum.TRADE_SUCCESS.getCode(), OrderStatusEnum.PAID_WAITING_ISSUE.getCode());
        PAY_STAUS_MAP.put(AlipayTradeStatusEnum.TRADE_CLOSED.getCode(), OrderStatusEnum.CANCELLED.getCode());
    }

    @Override
    public void getOrderDetail(GetOrderDetailRequestDTO requestDTO, StreamObserver<GetOrderDetailRespDTO> responseObserver) {
        try {
            logger.info("OrderServiceImpl.getOrderDetail requestDTO -> {}", JsonFormat.printer().print(requestDTO));

            GetOrderDetailRespDTO.Builder builder = GetOrderDetailRespDTO.newBuilder();

            MovieOrderDetailPO movieOrderDetailPO = orderDomainService.getOrderDetail(requestDTO.getUserId(), requestDTO.getOrderNo());

            HttpPanheGetOrderDetailResponse httpPanheGetOrderDetailResponse = panheDomainService.getOrderDetail(movieOrderDetailPO.getOutOrderNo());

            logger.info("OrderServiceImpl.getOrderDetail httpPanheGetOrderDetailResponse -> {}", JacksonUtil.bean2Json(httpPanheGetOrderDetailResponse));

            movieOrderDetailPO = orderDomainService.updateMovieOrderDetail(movieOrderDetailPO, httpPanheGetOrderDetailResponse);

            logger.info("OrderServiceImpl.getOrderDetail movieOrderDetailPO -> {}", JacksonUtil.bean2Json(movieOrderDetailPO));

            GetOrderDetailResponse response = GetOrderDetailResponse.convert2GetOrderDetailResponse(movieOrderDetailPO);

            builder.setRespJson(JacksonUtil.bean2Json(response));
            
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("OrderServiceImpl.getOrderDetail error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void createOrder(CreateOrderRequestDTO requestDTO, StreamObserver<CreateOrderRespDTO> responseObserver) {
        try {
            logger.info("OrderServiceImpl.createOrder requestDTO -> {}", JsonFormat.printer().print(requestDTO));
            CreateOrderRespDTO.Builder builder = CreateOrderRespDTO.newBuilder();

            String redisKey = OrderConstants.CREATE_ORDER_PREFIX + requestDTO.getUserId() + requestDTO.getShowId();

            if (redisTemplate.hasKey(redisKey)) {
                logger.info("OrderServiceImpl.createOrder redisKey exists, redisKey is -> {}", redisKey);

                builder.setSuccess(true).setErrorMsg("request repeat");

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

                return;
            }

            redisTemplate.opsForValue().set(redisKey, Boolean.TRUE, OrderConstants.REDIS_EXPIRE_TIME, TimeUnit.SECONDS);

            // 1.查询流水
            if (StringUtils.isNotBlank(requestDTO.getOrderNo())) {
                List<BillPO> billPOList = orderDomainService.getBillByOrderNo(requestDTO.getOrderNo());

                BillPO getBillPO = billPOList.stream()
                        .filter(billPO -> billPO.getExpireTime() > Instant.now().toEpochMilli())
                        .findFirst()
                        .orElse(null);

                if (Objects.nonNull(getBillPO)) {
                    builder.setOrderNo(getBillPO.getOrderNo()).setOrderStr(getBillPO.getOrderStr()).setExpireTime(getBillPO.getExpireTime()).setSuccess(true);

                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();

                    return;
                }
            }

            // 2.查询订单金额
            String amount = panheDomainService.getShowList(requestDTO.getShowId(), requestDTO.getSeatIdListList());

            CommonResponse<GetShowSeatListResponse> showSeatListResp = panheDomainService.getShowSeatList(requestDTO.getShowId());
            CommonResponse<GetCinemaFilmListResponse> cinemaFilmListResp = panheDomainService.getCinemaFilmList(requestDTO.getCinemaId());

            if (StringUtils.isBlank(amount) || BigDecimal.ZERO.compareTo(new BigDecimal(amount)) == 0) {
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

                return;
            }

            CreateOrderRequestDTO buildDTO = requestDTO.toBuilder().setOrderAmount(amount).build();

            // 3.创建本地订单
            OrderPO orderPO = orderDomainService.createOrder(buildDTO);
            logger.info("OrderServiceImpl.createOrder orderPO -> {}", JacksonUtil.bean2Json(orderPO));

            // 4.创建电影订单
            orderDomainService.createMovieOrderDetail(orderPO, requestDTO, showSeatListResp.getData(), cinemaFilmListResp.getData());

            // 5.调用支付宝支付
            Pair<String, Long> alipayPair = orderDomainService.alipayPay(orderPO, buildDTO.getAlipayType());

            // 6.创建支付流水表
            orderDomainService.createBill(orderPO, alipayPair);

            builder.setOrderNo(orderPO.getOrderNo())
                    .setOrderStr(alipayPair.getLeft())
                    .setExpireTime(alipayPair.getRight())
                    .setSuccess(true);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("OrderServiceImpl.createOrdr error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void alipayNotify(AlipayNotifyRequestDTO requestDTO, StreamObserver<AlipayNotifyRespDTO> responseObserver) {
        try {
            logger.info("OrderServiceImpl.alipayNotify requestDTO -> {}", JsonFormat.printer().print(requestDTO));

            AlipayNotifyRespDTO.Builder builder = AlipayNotifyRespDTO.newBuilder();

            // 1.处理支付宝回调
            Map<String, String> paramMap = new HashMap<>(requestDTO.getParamMapMap());

            Boolean isSuccess = orderDomainService.validate(paramMap);

            if (!isSuccess) {
                builder.setSuccess(false);

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            String tradeStatus = paramMap.get(NotifyConstants.ALIPAY_TRADE_STATUS);
            String orderNo = paramMap.get(NotifyConstants.ALIPAY_OUT_TRADE_NO);
            String totalAmount = paramMap.get(NotifyConstants.ALIPAY_TOTAL_AMOUNT);

            if (redisTemplate.hasKey(OrderConstants.ALIPAY_NOTIFY_PREFIX + orderNo)) {
                logger.info("OrderServiceImpl.alipayNotify redisKey exists, orderNo is -> {}", orderNo);

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            redisTemplate.opsForValue().set(OrderConstants.ALIPAY_NOTIFY_PREFIX + orderNo, Boolean.TRUE, OrderConstants.REDIS_EXPIRE_TIME, TimeUnit.SECONDS);

            // 3.调用磐河接口创建支付订单
            HttpPanheGetOrderDetailResponse getOrderDetailResponse = null;
            if (AlipayTradeStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus)) {
                String panheOrderNo = panheDomainService.createPayOrder(orderNo);
                logger.info("OrderServiceImpl.alipayNotify panheOrderNo -> {}", panheOrderNo);

                Boolean success = panheDomainService.pay(panheOrderNo, totalAmount);

                getOrderDetailResponse = panheDomainService.getOrderDetail(panheOrderNo);

                logger.info("OrderServiceImpl.alipayNotify getOrderDetailResponse -> {}", JacksonUtil.bean2Json(getOrderDetailResponse));

                if (!success) {
                    builder.setSuccess(false);

                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                    return;
                }
            }

            // 4.更新订单状态
            orderDomainService.payNotifyUpdateStatus(orderNo, PAY_STAUS_MAP.get(tradeStatus), getOrderDetailResponse, Boolean.FALSE);

            builder.setSuccess(true);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("OrderServiceImpl.alipayNotify error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            throw new RuntimeException(e);
        }

    }

    @Override
    @Transactional
    public void panheNotify(PanheNotifyRequestDTO requestDTO, StreamObserver<PanheNotifyRespDTO> responseObserver) {
        try {
            logger.info("OrderServiceImpl.panheNotify requestDTO -> {}", JsonFormat.printer().print(requestDTO));

            PanheNotifyRespDTO.Builder builder = PanheNotifyRespDTO.newBuilder();

            Map<String, String> paramMap = requestDTO.getParamMapMap();

            String orderNo = paramMap.get(NotifyConstants.PANHE_ORDER_NO);
            String status = paramMap.get(NotifyConstants.PANHE_STATUS);

            if (redisTemplate.hasKey(OrderConstants.PANHE_NOTIFY_PREFIX + orderNo)) {
                logger.info("OrderServiceImpl.panheNotify redisKey exists, orderNo is -> {}", orderNo);

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            redisTemplate.opsForValue().set(OrderConstants.PANHE_NOTIFY_PREFIX + orderNo, Boolean.TRUE, OrderConstants.REDIS_EXPIRE_TIME, TimeUnit.SECONDS);

            // 1.调用磐河接口获取订单详情接口
            HttpPanheGetOrderDetailResponse response = panheDomainService.getOrderDetail(orderNo);

            logger.info("OrderServiceImpl.panheNotify response -> {}", JacksonUtil.bean2Json(response));

            // 2.支付回调处理，更新订单状态
            MovieOrderDetailPO movieOrderDetailPO = orderDomainService.payNotifyUpdateStatus(orderNo, status, response, Boolean.TRUE);

            logger.info("OrderServiceImpl.panheNotify movieOrderDetailPO -> {}", JacksonUtil.bean2Json(movieOrderDetailPO));

            builder.setMovieDetailStr(JacksonUtil.bean2Json(movieOrderDetailPO));

            builder.setSuccess(true);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("OrderServiceImpl.panheNotify error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            throw new RuntimeException(e);
        }
    }

}
