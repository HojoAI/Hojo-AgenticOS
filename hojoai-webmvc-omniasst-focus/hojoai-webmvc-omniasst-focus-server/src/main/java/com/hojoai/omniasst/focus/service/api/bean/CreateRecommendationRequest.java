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
package com.hojoai.omniasst.focus.service.api.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 创建「为你推荐」条目请求，通过 ManagementAPI 调 management 服务落库。
 * 日程模块在触发推荐时组装此对象后调用 {@code ManagementAPI#createRecommendation}。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRecommendationRequest {

    private String userId;
    /** schedule / meeting / knowledge / explore */
    private String type;
    private String title;
    private String body;
    private String relatedId;
    private String relatedType;
    private String landingPage;
    /** 类型特有的业务数据，按 type 自定义结构（如 scheduleTime、scheduleEndTime、scheduleTitle） */
    private Object extra;
    private List<AttachmentItem> attachments;
    /** 过期时间（毫秒时间戳），不传则不过期 */
    private Long expiresAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public String getLandingPage() { return landingPage; }
    public void setLandingPage(String landingPage) { this.landingPage = landingPage; }
    public Object getExtra() { return extra; }
    public void setExtra(Object extra) { this.extra = extra; }
    public List<AttachmentItem> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentItem> attachments) { this.attachments = attachments; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
