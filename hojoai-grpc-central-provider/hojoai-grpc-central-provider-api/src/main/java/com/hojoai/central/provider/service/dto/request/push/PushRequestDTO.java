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
package com.hojoai.central.provider.service.dto.request.push;

import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;

import java.util.List;
import java.util.Map;

/**
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class PushRequestDTO {

    private List<String> userIdList;

    private AgentTypeEnum type;

    private Boolean isBroadcast;

    private AgentActionEnum action;

    private String title;

    private String content;

    private ConversationInfo conversationInfo;

    private String route;

    private String messageType;

    private String deviceToken;

    /**
     * 推送通道：{@code fcm} 走 Firebase，否则走友盟；未传或空则按友盟处理。
     */
    private String pushChannel;

    /** 关联业务 ID（纪要 ID、翻译 ID、taskId 等），用于消息中心落库与 Firebase payload 组装 */
    private String relatedId;

    /** 关联业务标题（如纪要标题），可选 */
    private String relatedTitle;

    /**
     * 是否静默推送：true 则仅发 data message（不弹通知栏），false 则发 notification + data。
     */
    private Boolean silent;

    /**
     * Firebase data payload，由 management 侧组装好后直接透传给 FCM 的 data 字段。
     * key/value 均为 String，符合 FCM data message 规范。
     */
    private Map<String, String> firebaseData;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public AgentTypeEnum getType() {
        return type;
    }

    public void setType(AgentTypeEnum type) {
        this.type = type;
    }

    public Boolean getBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(Boolean broadcast) {
        isBroadcast = broadcast;
    }

    public AgentActionEnum getAction() {
        return action;
    }

    public void setAction(AgentActionEnum action) {
        this.action = action;
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

    public ConversationInfo getConversationInfo() {
        return conversationInfo;
    }

    public void setConversationInfo(ConversationInfo conversationInfo) {
        this.conversationInfo = conversationInfo;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPushChannel() {
        return pushChannel;
    }

    public void setPushChannel(String pushChannel) {
        this.pushChannel = pushChannel;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedTitle() {
        return relatedTitle;
    }

    public void setRelatedTitle(String relatedTitle) {
        this.relatedTitle = relatedTitle;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    public Map<String, String> getFirebaseData() {
        return firebaseData;
    }

    public void setFirebaseData(Map<String, String> firebaseData) {
        this.firebaseData = firebaseData;
    }
}
