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
@TableName(value = "pa_recommendation_suppress")
public class PaRecommendationSuppress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private String userId;

    /** schedule / meeting / knowledge / explore */
    @TableField(value = "type")
    private String type;

    /** 近 7 天内的忽略累计次数 */
    @TableField(value = "ignore_count")
    private Integer ignoreCount;

    /** 该类型在此时间之前不推荐 */
    @TableField(value = "suppressed_until")
    private Date suppressedUntil;

    @TableField(value = "created_at")
    private Date createdAt;

    @TableField(value = "updated_at")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getIgnoreCount() { return ignoreCount; }
    public void setIgnoreCount(Integer ignoreCount) { this.ignoreCount = ignoreCount; }
    public Date getSuppressedUntil() { return suppressedUntil; }
    public void setSuppressedUntil(Date suppressedUntil) { this.suppressedUntil = suppressedUntil; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
