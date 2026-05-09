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
package com.hojoai.central.provider.service.common.constants;

/**
 *
 * @author My
 * @version 1.0, 2026/01/17
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface RedisConstants {

    /** 友盟等设备 token（国内默认） */
    String REDIS_KEY_USER = "user_device_token:";

    /** FCM Registration Token（海外），与 pushChannel=fcm 配套 */
    String REDIS_KEY_USER_FCM = "user_device_token:fcm:";

    Long REDIS_EXPIRE_TIME_ONE_YEAR = 365L;

}
