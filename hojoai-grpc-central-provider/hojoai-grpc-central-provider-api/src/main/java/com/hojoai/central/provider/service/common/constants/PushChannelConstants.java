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
 * 推送通道：与 management 侧 {@code pushChannel} 字段取值一致。
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-grpc-central-provider-api 1.0.0
 */
public interface PushChannelConstants {

    /** Firebase Cloud Messaging（海外） */
    String FCM = "fcm";

    /** 友盟（国内，默认） */
    String UMENG = "umeng";

    /** 友盟 iOS（国内） */
    String UMENG_IOS = "umeng_ios";

}
