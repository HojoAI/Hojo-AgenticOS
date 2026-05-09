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
package com.hojoai.omniasst.sysagent.service.order.impl;

import com.hojoai.central.provider.grpc.AlipayNotifyRequestDTO;
import com.hojoai.central.provider.grpc.AlipayNotifyRespDTO;
import com.hojoai.central.provider.grpc.PanheNotifyRequestDTO;
import com.hojoai.central.provider.grpc.PanheNotifyRespDTO;
import com.hojoai.omniasst.sysagent.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.sysagent.service.order.PayNotifyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * The AlipayServiceImpl class for alipay.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class PayNotifyServiceImpl implements PayNotifyService {

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    /**
     * 将支付宝异步通知参数转为 gRPC 请求，由 central-provider 验签并更新订单。
     *
     * @param paramMap 支付宝 POST 表单参数
     * @return 下游处理是否成功
     */
    @Override
    public Boolean alipayNotify(Map<String, String> paramMap) {
        AlipayNotifyRequestDTO requestDTO = AlipayNotifyRequestDTO.newBuilder()
                .putAllParamMap(paramMap)
                .build();

        AlipayNotifyRespDTO respDTO = iConfigurationForGrpc.orderClient().alipayNotify(requestDTO);

        return respDTO.getSuccess();
    }

    /**
     * 磐河支付回调参数转发至订单 gRPC 服务。
     *
     * @param paramMap 渠道回调参数
     * @return 下游处理是否成功
     */
    @Override
    public Boolean panheNotify(Map<String, String> paramMap) {
        PanheNotifyRequestDTO requestDTO = PanheNotifyRequestDTO.newBuilder()
                .putAllParamMap(paramMap)
                .build();

        PanheNotifyRespDTO respDTO = iConfigurationForGrpc.orderClient().panheNotify(requestDTO);

        return respDTO.getSuccess();
    }

}
