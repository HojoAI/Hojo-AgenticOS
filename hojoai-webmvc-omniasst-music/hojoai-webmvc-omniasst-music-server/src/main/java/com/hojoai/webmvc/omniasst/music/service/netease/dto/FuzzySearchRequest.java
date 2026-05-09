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

/**
 * 模糊搜索请求参数
 * 
 * @author shengxianfei
 * @since 2026-01-15
 */
public class FuzzySearchRequest {
    
    /** 用户 ID，用于换取网易云 accessToken */
    private String userId;
    
    /** 媒体名称/搜索关键词（多槽位主查询词） */
    private String mediaName;
    
    /** 艺人名，可选 */
    private String artistName;
    
    /** 专辑名，可选 */
    private String albumName;
    
    /**
     * 媒体类型：0-开放泛词（默认）、1-歌曲、2-专辑、3-艺人、5-歌单
     */
    private String mediaType;
    
    /**
     * 排序方式；资源按时间排序时传 1（如新歌、新专辑），可选
     */
    private String sortOrder;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getMediaName() {
        return mediaName;
    }
    
    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }
    
    public String getArtistName() {
        return artistName;
    }
    
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    
    public String getAlbumName() {
        return albumName;
    }
    
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}

