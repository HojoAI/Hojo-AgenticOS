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
package com.hojoai.omniasst.sysagent.service.conversation.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 客户端发起对话（首轮 SSE）的请求体：多模态内容 + 用户侧信息。
 */
public class ConversationRequest {

    /** 响应模式，通常为 streaming。 */
    private String responseMode;

    /** 会话 ID，新会话可为空由服务端分配。 */
    private String conversationId;

    /** 数字人形象 ID。 */
    private String avatarId;

    /** 用户输入的多段内容（文本或多媒体）。 */
    private List<ContentItem> contents;

    /** 用户 ID、地理位置等。 */
    private Inputs inputs;

    /**
     * 耳机模式：为 true 时同一用户按业务日（见 {@link Inputs#userNowDate}，缺省为服务器当日）仅使用一个会话。
     */
    private boolean headphoneMode = false;

    public String getResponseMode() {
        return responseMode;
    }

    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public List<ContentItem> getContents() {
        return contents;
    }

    public void setContents(List<ContentItem> contents) {
        this.contents = contents;
    }

    public Inputs getInputs() {
        return inputs;
    }

    public void setInputs(Inputs inputs) {
        this.inputs = inputs;
    }

    public boolean isHeadphoneMode() {
        return headphoneMode;
    }

    public void setHeadphoneMode(boolean headphoneMode) {
        this.headphoneMode = headphoneMode;
    }

    /** 用户单条输入：文本或多媒体引用。 */
    public static class ContentItem {
        /** 类型：text、image、pdf、audio、video 等。 */
        private String type;
        /** 非 text 时为 OSS 等资源地址。 */
        private String url;
        /** type 为 text 时的正文。 */
        private String text;
        /** 业务扩展字段。 */
        private Object extra;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Object getExtra() {
            return extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        @Override
        public String toString() {
            return "ContentItem{" +
                    "type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", text='" + text + '\'' +
                    ", extra=" + extra +
                    '}';
        }
    }

    /** 客户端上报的用户与环境信息（JSON 字段 snake_case）。 */
    public static class Inputs {
        /** 粗粒度位置描述。 */
        private String location;

        /** 用户 ID。 */
        @JsonProperty("user_id")
        private String userId;

        /** 用户昵称。 */
        @JsonProperty("user_nickname")
        private String userNickname;

        /** 客户端当前日期（字符串，由端上传）。 */
        @JsonProperty("user_now_date")
        private String userNowDate;

        /** 省。 */
        @JsonProperty("user_province")
        private String userProvince;

        /** 市。 */
        @JsonProperty("user_city")
        private String userCity;

        /** 区/县。 */
        @JsonProperty("user_district")
        private String userDistrict;

        /** 经度。 */
        @JsonProperty("user_longitude")
        private String userLongitude;

        /** 纬度。 */
        @JsonProperty("user_latitude")
        private String userLatitude;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public void setUserNickname(String userNickname) {
            this.userNickname = userNickname;
        }

        public String getUserNowDate() {
            return userNowDate;
        }

        public void setUserNowDate(String userNowDate) {
            this.userNowDate = userNowDate;
        }

        public String getUserProvince() {
            return userProvince;
        }

        public void setUserProvince(String userProvince) {
            this.userProvince = userProvince;
        }

        public String getUserCity() {
            return userCity;
        }

        public void setUserCity(String userCity) {
            this.userCity = userCity;
        }

        public String getUserDistrict() {
            return userDistrict;
        }

        public void setUserDistrict(String userDistrict) {
            this.userDistrict = userDistrict;
        }

        public String getUserLongitude() {
            return userLongitude;
        }

        public void setUserLongitude(String userLongitude) {
            this.userLongitude = userLongitude;
        }

        public String getUserLatitude() {
            return userLatitude;
        }

        public void setUserLatitude(String userLatitude) {
            this.userLatitude = userLatitude;
        }

        @Override
        public String toString() {
            return "Inputs{" +
                    "location='" + location + '\'' +
                    ", userId='" + userId + '\'' +
                    ", userNickname='" + userNickname + '\'' +
                    ", userNowDate='" + userNowDate + '\'' +
                    ", userProvince='" + userProvince + '\'' +
                    ", userCity='" + userCity + '\'' +
                    ", userDistrict='" + userDistrict + '\'' +
                    ", userLongitude='" + userLongitude + '\'' +
                    ", userLatitude='" + userLatitude + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ConversationRequest{" +
                "responseMode='" + responseMode + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", avatarId='" + avatarId + '\'' +
                ", contents=" + contents +
                ", inputs=" + inputs +
                ", headphoneMode=" + headphoneMode +
                '}';
    }
}