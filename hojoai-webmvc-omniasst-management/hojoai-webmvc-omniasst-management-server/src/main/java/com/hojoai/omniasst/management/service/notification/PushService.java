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
package com.hojoai.omniasst.management.service.notification;

import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.dto.request.PushUserDeviceTokenRequestDTO;

/**
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-websocket-omniasst-management 1.0.0
 */
public interface PushService {

    /**
     * 发送推送消息
     * @param requestDTO 推送参数
     * @return 是否发送成功
     */
    Response<String> push(PushRequestDTO requestDTO);

    /**
     * 保存用户设备Token
     * @param requestDTO 用户设备Token参数
     * @return 是否保存成功
     */
    Response<String> userDeviceToken(PushUserDeviceTokenRequestDTO requestDTO);

}
