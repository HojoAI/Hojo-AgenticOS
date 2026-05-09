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
package com.hojoai.webmvc.omniasst.music.service.bean;

import java.util.List;

/**
 * Playlist DTO (Data Transfer Object)
 * 歌单数据传输对象
 * 
 * @author HojoAI
 * @since 2026-01-13
 */
public class PlaylistDTO {
    
    /**
     * 歌单ID
     */
    private Long playlistId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 歌单名称
     */
    private String name;
    
    /**
     * 歌单描述
     */
    private String description;
    
    /**
     * 封面URL
     */
    private String coverUrl;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 歌曲列表
     */
    private List<SongDTO> songs;
    
    /**
     * 歌曲数量
     */
    private Integer songCount;

    // Getters and Setters
    
    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<SongDTO> getSongs() {
        return songs;
    }

    public void setSongs(List<SongDTO> songs) {
        this.songs = songs;
    }

    public Integer getSongCount() {
        return songCount;
    }

    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
    }
}

