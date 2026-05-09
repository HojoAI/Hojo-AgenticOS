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
package com.hojoai.central.provider.domain;

import com.alipay.api.AlipayApiException;
import com.hojoai.central.provider.repository.po.OrderPO;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The AlipayStrategy interface for alipay.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface AlipayStrategy {

    /**
     * alipay
     * @param orderPO
     * @return
     */
    Pair<String, Long> alipayPay(OrderPO orderPO) throws AlipayApiException;

}
