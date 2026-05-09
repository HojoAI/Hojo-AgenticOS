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

/**
 * Song DTO (Data Transfer Object)
 * 歌曲数据传输对象
 * 
 * @author HojoAI
 * @since 2026-01-13
 */
public class SongDTO {
    
    /**
     * 歌曲ID
     */
    private String songId;
    
    /**
     * 歌曲名称
     */
    private String name;
    
    /**
     * 艺术家
     */
    private String artist;
    
    /**
     * 专辑
     */
    private String album;
    
    /**
     * 专辑封面URL
     */
    private String coverUrl;
    
    /**
     * 歌曲URL
     */
    private String musicUrl;
    
    /**
     * 歌词URL
     */
    private String lyricUrl;
    
    /**
     * 时长（秒）
     */
    private Integer duration;

    // Getters and Setters
    
    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}

