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
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.hojoai.central.provider.service.common.constants.OrderConstants;
import com.hojoai.central.provider.service.common.enums.AlipaySubjectEnum;
import com.hojoai.central.provider.domain.AlipayStrategy;
import com.hojoai.central.provider.repository.po.OrderPO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The AlipayWapStrategyImpl class for alipay wap.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Service
public class AlipayWapStrategyImpl implements AlipayStrategy {

    @Resource
    private AlipayConfig alipayConfig;

    @Value("${alipay.callback.url}")
    private String alipayCallbackUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Override
    public Pair<String, Long> alipayPay(OrderPO orderPO) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();

        model.setOutTradeNo(orderPO.getOrderNo());

        if (OrderConstants.ACTIVE_PROFILE_SAN.equals(activeProfile)) {
            model.setTotalAmount(OrderConstants.SAN_AMOUNT);
        } else {
            model.setTotalAmount(orderPO.getAmount().toString());
        }

        model.setSubject(AlipaySubjectEnum.MOVIE_SUBJECT.getValue());
        model.setProductCode(OrderConstants.ALIPAY_PRODUCT_WAP_WAY);

        LocalDateTime dateTime = LocalDateTime.now().plusMinutes(OrderConstants.FEATURE_TIME_MINUTES);
        long expireTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern(OrderConstants.TIME_FORMAT));

        model.setTimeExpire(dateTimeStr);

        // 设置针对用户授权接口
        // model.setAuthToken("appopenBb64d181d0146481ab6a762c00714cC27");

        // 设置用户付款中途退出返回商户网站的地址
        // model.setQuitUrl("http://www.taobao.com/product/113714.html");

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();

        request.setBizModel(model);
        request.setNotifyUrl(alipayCallbackUrl);

        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, OrderConstants.REQUEST_POST);

        return Pair.of(response.getBody(), expireTime);
    }

}
