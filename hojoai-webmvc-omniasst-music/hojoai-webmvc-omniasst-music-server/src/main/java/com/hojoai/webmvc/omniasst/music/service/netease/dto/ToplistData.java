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
package com.hojoai.webmvc.omniasst.music.service.netease.dto;

import java.util.List;

/**
 * 榜单列表数据
 * 
 * @author shengxianfei
 * @since 2026-01-15
 */
public class ToplistData {
    
    /** 榜单/歌单 ID */
    private String id;
    
    /** 名称 */
    private String name;
    
    /** 封面图 URL */
    private String coverImgUrl;
    
    /** 描述文案 */
    private String describe;
    
    /** 创建者昵称 */
    private String creatorNickName;
    
    /** 播放次数 */
    private Long playCount;
    
    /** 收藏人数 */
    private Long subscribedCount;
    
    /** 标签列表 */
    private List<String> tags;
    
    /** 创建时间（时间戳，单位以接口为准） */
    private Long createTime;
    
    /** 当前用户是否已收藏 */
    private Boolean subed;
    
    /** 曲目数量 */
    private Integer trackCount;
    
    /** 特殊类型（开放平台枚举） */
    private Integer specialType;
    
    /** 分类 */
    private String category;
    
    /** 更新频率说明（如「每日更新」） */
    private String updateFrequency;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCoverImgUrl() {
        return coverImgUrl;
    }
    
    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }
    
    public String getDescribe() {
        return describe;
    }
    
    public void setDescribe(String describe) {
        this.describe = describe;
    }
    
    public String getCreatorNickName() {
        return creatorNickName;
    }
    
    public void setCreatorNickName(String creatorNickName) {
        this.creatorNickName = creatorNickName;
    }
    
    public Long getPlayCount() {
        return playCount;
    }
    
    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }
    
    public Long getSubscribedCount() {
        return subscribedCount;
    }
    
    public void setSubscribedCount(Long subscribedCount) {
        this.subscribedCount = subscribedCount;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Boolean getSubed() {
        return subed;
    }
    
    public void setSubed(Boolean subed) {
        this.subed = subed;
    }
    
    public Integer getTrackCount() {
        return trackCount;
    }
    
    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }
    
    public Integer getSpecialType() {
        return specialType;
    }
    
    public void setSpecialType(Integer specialType) {
        this.specialType = specialType;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getUpdateFrequency() {
        return updateFrequency;
    }
    
    public void setUpdateFrequency(String updateFrequency) {
        this.updateFrequency = updateFrequency;
    }
}

