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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 友盟 OpenAPI 推送请求体，字段名与官方 JSON 一致。
 */
public class PushRequest {

    /** 推送类型：unicast、listcast、customizedcast、broadcast 等。 */
    @JsonProperty("type")
    private String type;

    /** 设备 token 列表（单播/列播时必填，格式依友盟文档）。 */
    @JsonProperty("device_tokens")
    private String deviceTokens;

    /** 别名（customizedcast 等）。 */
    @JsonProperty("alias")
    private String alias;

    /** 别名类型，如 user_id。 */
    @JsonProperty("alias_type")
    private String aliasType;

    /** 受众文件 ID（filecast）。 */
    @JsonProperty("file_id")
    private String fileId;

    /** Android 消息体。 */
    @JsonProperty("payload")
    private AndroidPayload payload;

    /** 是否生产环境通道。 */
    @JsonProperty("production_mode")
    private boolean productionMode;

    /** 任务描述，用于友盟后台展示。 */
    @JsonProperty("description")
    private String description;

    /** 0 运营消息，1 服务消息。 */
    @JsonProperty("category")
    private Integer category = 0;

    /** 发送策略（限速、定时等）。 */
    @JsonProperty("policy")
    private Map<String, Object> policy;

    /** 厂商通道属性。 */
    @JsonProperty("channel_properties")
    private Map<String, Object> channelProperties;

    /** 受众过滤条件。 */
    @JsonProperty("filter")
    private Map<String, Object> filter;

    /** 分组推送 ID。 */
    @JsonProperty("group_id")
    private String groupId;

    /** 回调透传参数。 */
    @JsonProperty("callback_params")
    private Map<String, Object> callbackParams;

    public PushRequest() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceTokens() {
        return deviceTokens;
    }

    public void setDeviceTokens(String deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAliasType() {
        return aliasType;
    }

    public void setAliasType(String aliasType) {
        this.aliasType = aliasType;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public AndroidPayload getPayload() {
        return payload;
    }

    public void setPayload(AndroidPayload payload) {
        this.payload = payload;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Map<String, Object> getPolicy() {
        return policy;
    }

    public void setPolicy(Map<String, Object> policy) {
        this.policy = policy;
    }

    public Map<String, Object> getChannelProperties() {
        return channelProperties;
    }

    public void setChannelProperties(Map<String, Object> channelProperties) {
        this.channelProperties = channelProperties;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Map<String, Object> getCallbackParams() {
        return callbackParams;
    }

    public void setCallbackParams(Map<String, Object> callbackParams) {
        this.callbackParams = callbackParams;
    }
}