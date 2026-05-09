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
package com.hojoai.webmvc.omniasst.meeting.api.dto;

import java.util.List;
import java.util.Map;

public class PAPushRequestDTO {

    private List<String> userIdList;

    private String type;

    private Boolean isBroadcast;

    private String action;

    private String title;

    private String content;

    private ConversationInfo conversationInfo;

    private String pushChannel;

    private String messageType;

    private String relatedId;

    private String relatedTitle;

    private String landingPage;

    private Boolean silent;

    /**
     * 预组装的 Firebase data payload（key/value 均为 String）。
     * 传入后 management 侧直接使用，不再进行数据聚合组装。
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

    public String getPushChannel() {
        return pushChannel;
    }

    public void setPushChannel(String pushChannel) {
        this.pushChannel = pushChannel;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
