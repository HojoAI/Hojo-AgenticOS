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
package com.hojoai.omniasst.sysagent.push.model.dto;

/**
 * 对外推送 HTTP 接口入参，由 {@link com.hojoai.omniasst.sysagent.controller.umeng.PushController} 转为 {@link com.hojoai.omniasst.sysagent.push.model.PushRequest}。
 */
public class PushRequestDTO {

    /** 业务用户 ID，映射友盟 alias。 */
    private String uid;

    /** 客户端自定义动作名，写入 payload.extra。 */
    private String action;

    /** JSON 字符串参数，写入 payload.extra。 */
    private String params;

    /** notification 或 message。 */
    private String displayType = "notification";

    public PushRequestDTO() {}

    public PushRequestDTO(String uid, String action, String params, String displayType) {
        this.uid = uid;
        this.action = action;
        this.params = params;
        this.displayType = displayType;
    }

    public String getUid() {
        return uid;
    }

    public String getAction() {
        return action;
    }

    public String getParams() {
        return params;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }
}