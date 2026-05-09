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
 * 歌单搜索响应数据
 * <p>{@link PlaylistRecord} 嵌套类型字段与开放平台歌单搜索接口返回一致。
 * 
 * @author shengxianfei
 * @since 2026-01-14
 */
public class PlaylistSearchData {
    
    /** 总记录数 */
    private Integer recordCount;
    
    /** 歌单记录列表 */
    private List<PlaylistRecord> records;
    
    public Integer getRecordCount() {
        return recordCount;
    }
    
    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }
    
    public List<PlaylistRecord> getRecords() {
        return records;
    }
    
    public void setRecords(List<PlaylistRecord> records) {
        this.records = records;
    }
    
    /**
     * 歌单记录
     */
    public static class PlaylistRecord {
        private String id;
        private String name;
        private String coverImgUrl;
        private String describe;
        private String creatorNickName;
        private Long playCount;
        private Long subscribedCount;
        private List<String> tags;
        private Long createTime;
        private Boolean subed;
        
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
    }
}

