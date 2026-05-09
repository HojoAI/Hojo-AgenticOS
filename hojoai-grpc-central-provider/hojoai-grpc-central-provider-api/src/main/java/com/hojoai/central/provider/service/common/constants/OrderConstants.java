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
 * The OrderConstants class for order.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface OrderConstants {

    /**
     * The order prefix.
     */
    String ORDER_PREFIX = "PO_";

    /**
     * The bill prefix.
     */
    String BILL_PREFIX = "PB_";

    /**
     * The alipay product code QUICK_MSECURITY_PAY.
     */
    String ALIPAY_PRODUCT_MSECURITY_PAY = "QUICK_MSECURITY_PAY";

    /**
     * The alipay product code QUICK_WAP_WAY.
     */
    String ALIPAY_PRODUCT_WAP_WAY = "QUICK_WAP_WAY";

    /**
     * The feature time minutes.
     */
    Long FEATURE_TIME_MINUTES = 15L;

    /**
     * The time format.
     */
    String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * The charset utf 8.
     */
    String CHARSET_UTF_8 = "UTF-8";

    /**
     * The sign type rsa2.
     */
    String SIGN_TYPE_RSA2 = "RSA2";

    /**
     * The san active profile.
     */
    String ACTIVE_PROFILE_SAN = "san";

    /**
     * The san amount.
     */
    String SAN_AMOUNT = "0.01";

    /**
     * The request post.
     */
    String REQUEST_POST = "POST";

    /**
     * The create order prefix.
     */
    String CREATE_ORDER_PREFIX = "create_order_";

    /**
     * The alipay notify prefix.
     */
    String ALIPAY_NOTIFY_PREFIX = "alipay_notify_";

    /**
     * The panhe notify prefix.
     */
    String PANHE_NOTIFY_PREFIX = "panhe_notify_";

    /**
     * The alipay expire time.
     */
    String ALIPAY_EXPIRE_TIME = "alipay_expire_time_";

    /**
     * The redis expire time.
     */
    Long REDIS_EXPIRE_TIME = 10L;

}
