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
package com.hojoai.central.provider.service.push;

import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.grpc.PushRespDTO;
import com.hojoai.central.provider.grpc.PushServiceGrpc;
import com.hojoai.central.provider.service.CentralProviderChannelProvider;

/**
 * 推送 gRPC 阻塞客户端，基于 {@link CentralProviderChannelProvider} 获取 Channel 并调用 {@code PushService}。
 */
public class PushClient {

    private final CentralProviderChannelProvider centralProviderChannelProvider;

    public PushClient(CentralProviderChannelProvider centralProviderChannelProvider) {
        this.centralProviderChannelProvider = centralProviderChannelProvider;
    }

    /**
     * 同步调用推送接口。
     *
     * @param requestDTO gRPC 请求体
     * @return 推送是否成功等结果
     */
    public PushRespDTO push(PushReqDTO requestDTO) {
        return centralProviderChannelProvider.executeWithChannel(channel -> PushServiceGrpc.newBlockingStub(channel).push(requestDTO));
    }

}
