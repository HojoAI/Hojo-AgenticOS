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
package com.hojoai.omniasst.management.dto.request;

import com.hojoai.central.provider.service.dto.request.push.ConversationInfo;

import java.util.List;
import java.util.Map;

/**
 * The PAPushRequestDTO class for PA push.
 *
 * @author My
 * @version 1.0, 2026/01/13
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public class PAPushRequestDTO {

    private List<String> userIdList;

    private String type;

    private Boolean isBroadcast;

    private String action;

    private String title;

    private String content;

    private ConversationInfo conversationInfo;

    private String route;

    private String messageType;

    private String deviceToken;

    /** fcm：Firebase；未传或 umeng：友盟（默认） */
    private String pushChannel;

    /** true 为静默推送（不弹通知栏），false 为系统通知（弹通知栏） */
    private Boolean silent;

    /** 关联业务 ID（纪要 ID、翻译 ID、taskId 等） */
    private String relatedId;

    /** 关联业务标题 */
    private String relatedTitle;

    /** 跳转落地页路径 */
    private String landingPage;

    /**
     * 预组装的 Firebase data payload（key/value 均为 String）。
     * 传入后直接使用，跳过 management 侧的数据聚合组装逻辑。
     */
    private Map<String, String> firebaseData;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(Boolean broadcast) {
        isBroadcast = broadcast;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
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

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
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

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public Map<String, String> getFirebaseData() {
        return firebaseData;
    }

    public void setFirebaseData(Map<String, String> firebaseData) {
        this.firebaseData = firebaseData;
    }
}
