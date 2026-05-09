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
package com.hojoai.omniasst.common.api.avatar;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 数字人/虚拟形象配置项，用于会话或用户维度的形象与音色选择。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvatarItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 形象唯一 ID */
    private String avatarId;
    /** 展示名称 */
    private String name;
    /** 形象简介 */
    private String description;
    /** 性别或风格标签 */
    private String gender;
    /** 形象预览图或模型资源 URL */
    private String avatarUrl;
    /** 形象资源或渲染键 */
    private String avatarKey;
    /** 绑定音色资源键 */
    private String voiceKey;
    /** 试听音频地址 */
    private String audioUrl;
    /** 是否为默认推荐形象 */
    private Boolean isDefault;
    /** 所属平台或渠道标识 */
    private Integer platform;

    public AvatarItem() {
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public String getVoiceKey() {
        return voiceKey;
    }

    public void setVoiceKey(String voiceKey) {
        this.voiceKey = voiceKey;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }
}
