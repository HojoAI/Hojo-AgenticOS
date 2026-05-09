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
package com.hojoai.omniasst.management.dao.recommendation.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * @author David
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@TableName(value = "pa_recommendation")
public class PaRecommendation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "recommendation_id")
    private String recommendationId;

    @TableField(value = "user_id")
    private String userId;

    /** schedule / meeting / knowledge / explore */
    @TableField(value = "type")
    private String type;

    @TableField(value = "title")
    private String title;

    @TableField(value = "body")
    private String body;

    @TableField(value = "related_id")
    private String relatedId;

    @TableField(value = "related_type")
    private String relatedType;

    @TableField(value = "landing_page")
    private String landingPage;

    /** 类型特有的业务数据，JSON */
    @TableField(value = "extra")
    private String extra;

    /** 文件附件列表，JSON 数组 */
    @TableField(value = "attachments")
    private String attachments;

    /** active / ignored / expired */
    @TableField(value = "status")
    private String status;

    @TableField(value = "created_at")
    private Date createdAt;

    /** null 表示不过期 */
    @TableField(value = "expires_at")
    private Date expiresAt;

    @TableField(value = "ignored_at")
    private Date ignoredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }
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
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public Date getIgnoredAt() { return ignoredAt; }
    public void setIgnoredAt(Date ignoredAt) { this.ignoredAt = ignoredAt; }
}
