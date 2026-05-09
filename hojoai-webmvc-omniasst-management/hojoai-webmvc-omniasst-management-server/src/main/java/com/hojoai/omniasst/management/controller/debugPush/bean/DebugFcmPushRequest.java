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
package com.hojoai.omniasst.management.controller.debugPush.bean;

import java.util.Map;

/**
 * FCM 推送测试接口请求体。
 * <p>
 * {@code params} 接受任意结构的 JSON 对象，服务端会自动序列化为 JSON 字符串后放入 FCM data payload，
 * 与正式推送链路的格式完全一致。
 * </p>
 *
 * @author David
 * @version 1.0, 2026/03/31
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class DebugFcmPushRequest {

    /** 目标用户 ID，必填 */
    private String userId;

    /**
     * FCM Registration Token（设备令牌）。
     * 传入后直接使用该 token 推送，不再从 Redis 查询；不传则按 userId 从 Redis 中取。
     */
    private String deviceToken;

    /**
     * 是否静默推送。{@code true} → data-only（不弹通知栏）；{@code false} → notification + data（弹通知栏）。
     * 默认 {@code true}。
     */
    private Boolean silent = Boolean.TRUE;

    /** 通知栏标题，仅 silent=false 时展示 */
    private String title;

    /** 通知栏正文，仅 silent=false 时展示 */
    private String content;

    /**
     * FCM data 中的 {@code action} 字段，例如 {@code action.web.message} 或 {@code action.web.navigation}。
     * 必填。
     */
    private String action;

    /**
     * FCM data 中的 {@code params} 字段原始结构，服务端会将其序列化为 JSON 字符串后发送。
     * 必填。示例：
     * <pre>
     * {
     *   "action": "message",
     *   "messageId": "MSG001",
     *   "data": { "totalUnread": 3, "type": "meeting", "title": "测试" }
     * }
     * </pre>
     */
    private Map<String, Object> params;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
