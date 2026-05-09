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
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
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
 * The AlipayAppStrategyImpl class for app alipay.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Service
public class AlipayAppStrategyImpl implements AlipayStrategy {

    @Resource
    private AlipayConfig alipayConfig;

    @Value("${alipay.callback.url}")
    private String alipayCallbackUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public Pair<String, Long> alipayPay(OrderPO orderPO) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

        model.setOutTradeNo(orderPO.getOrderNo());

        if (OrderConstants.ACTIVE_PROFILE_SAN.equals(activeProfile)) {
            model.setTotalAmount(OrderConstants.SAN_AMOUNT);
        } else {
            model.setTotalAmount(orderPO.getAmount().toString());
        }

        model.setSubject(AlipaySubjectEnum.MOVIE_SUBJECT.getValue());
        model.setProductCode(OrderConstants.ALIPAY_PRODUCT_MSECURITY_PAY);

        LocalDateTime dateTime = LocalDateTime.now().plusMinutes(OrderConstants.FEATURE_TIME_MINUTES);
        long expireTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern(OrderConstants.TIME_FORMAT));

        model.setTimeExpire(dateTimeStr);

        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();

        request.setNotifyUrl(alipayCallbackUrl);
        request.setBizModel(model);

        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

        return Pair.of(response.getBody(), expireTime);
    }

}
