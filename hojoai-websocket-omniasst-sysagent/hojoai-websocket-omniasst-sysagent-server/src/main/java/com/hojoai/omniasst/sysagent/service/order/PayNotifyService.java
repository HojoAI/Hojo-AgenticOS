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
package com.hojoai.omniasst.sysagent.service.order;

import java.util.Map;

/**
 * The PayNotifyService class for pay notify.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public interface PayNotifyService {

    /**
     * alipay notify
     *
     * @param paramMap
     * @return
     */
    Boolean alipayNotify(Map<String, String> paramMap);

    /**
     * panhe notify
     *
     * @param paramMap
     * @return
     */
    Boolean panheNotify(Map<String, String> paramMap);

}
