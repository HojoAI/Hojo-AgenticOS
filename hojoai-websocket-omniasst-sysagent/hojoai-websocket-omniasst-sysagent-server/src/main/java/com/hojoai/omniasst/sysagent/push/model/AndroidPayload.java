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
package com.hojoai.omniasst.sysagent.push.model;

import java.util.Map;

/**
 * Android 推送 payload：展示类型 + 通知体 + 业务 extra。
 */
public class AndroidPayload {

    /** notification 或 message，见 {@link com.hojoai.omniasst.sysagent.push.constants.PushConstants}。 */
    private String displayType;
    /** 通知栏标题、正文等。 */
    private PayloadBody body;
    /** 点击后业务自定义参数。 */
    private Map<String, Object> extra;

    public AndroidPayload() {}

    public AndroidPayload(String displayType, PayloadBody body, Map<String, Object> extra) {
        this.displayType = displayType;
        this.body = body;
        this.extra = extra;
    }

    public String getDisplayType() {
        return displayType;
    }

    public PayloadBody getBody() {
        return body;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public void setBody(PayloadBody body) {
        this.body = body;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}