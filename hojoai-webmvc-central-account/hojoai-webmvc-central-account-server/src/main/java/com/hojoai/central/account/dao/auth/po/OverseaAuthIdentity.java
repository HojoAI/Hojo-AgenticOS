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
package com.hojoai.central.account.dao.auth.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 第三方身份绑定（provider + providerUserId），映射表 oversea_auth_identity。
 */
@TableName("oversea_auth_identity")
public class OverseaAuthIdentity implements Serializable {

    /** 主键 */
    @TableId("id")
    private Long id;

    /** 关联 oversea_user_info.id */
    @TableField("user_id")
    private Long userId;

    /** 第三方标识，如 google、apple */
    @TableField("provider")
    private String provider;

    /** 第三方用户唯一 ID */
    @TableField("provider_user_id")
    private String providerUserId;

    /** 绑定时的邮箱快照 */
    @TableField("email")
    private String email;

    /** 创建时间 */
    @TableField("created_at")
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
