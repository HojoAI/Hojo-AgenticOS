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
package com.hojoai.omniasst.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IConfiguration {

    @Value("${central.account.base.url}")
    private String centralAccountBaseUrl;

    public String getCentralAccountBaseUrl() {
        return centralAccountBaseUrl;
    }

    public void setCentralAccountBaseUrl(String centralAccountBaseUrl) {
        this.centralAccountBaseUrl = centralAccountBaseUrl;
    }
}
