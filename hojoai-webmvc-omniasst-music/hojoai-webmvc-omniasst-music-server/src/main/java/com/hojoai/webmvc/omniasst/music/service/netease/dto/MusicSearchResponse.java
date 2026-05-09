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
 * 音乐搜索响应
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
public class MusicSearchResponse {
    
    /**
     * 歌曲列表
     */
    private List<Music> musicList;
    
    public List<Music> getMusicList() {
        return musicList;
    }
    
    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }
    
    /**
     * 歌曲信息
     */
    public static class Music {
        /**
         * 歌曲ID
         */
        private String songId;
        
        /**
         * 歌曲名称
         */
        private String songName;
        
        /**
         * 时长（毫秒）
         */
        private Long duration;
        
        /**
         * 歌曲封面URL
         */
        private String coverImgUrl;
        
        /**
         * 播放URL
         */
        private String playUrl;
        
        /**
         * 歌曲大小（字节）
         */
        private Long size;
        
        /**
         * 歌曲码率
         */
        private Integer br;
        
        /**
         * 音效
         */
        private String effects;
        
        /**
         * 当前返回的歌曲码率对应的level
         */
        private String level;
        
        /**
         * 是否有版权
         */
        private Boolean visible;
        
        /**
         * 是否可以播放
         */
        private Boolean playFlag;
        
        /**
         * 试听开始时间
         */
        private Integer start;
        
        /**
         * 试听结束时间
         */
        private Integer end;
        
        /**
         * 专辑名
         */
        private String albumName;
        
        /**
         * 专辑ID
         */
        private String albumId;
        
        /**
         * 专辑艺人ID
         */
        private String albumArtistId;
        
        /**
         * 专辑艺人名
         */
        private String albumArtistName;
        
        /**
         * 艺人列表
         */
        private List<Artist> artistList;

        // Getters and Setters
        
        public String getSongId() {
            return songId;
        }

        public void setSongId(String songId) {
            this.songId = songId;
        }

        public String getSongName() {
            return songName;
        }

        public void setSongName(String songName) {
            this.songName = songName;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public String getCoverImgUrl() {
            return coverImgUrl;
        }

        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }

        public String getPlayUrl() {
            return playUrl;
        }

        public void setPlayUrl(String playUrl) {
            this.playUrl = playUrl;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public Integer getBr() {
            return br;
        }

        public void setBr(Integer br) {
            this.br = br;
        }

        public String getEffects() {
            return effects;
        }

        public void setEffects(String effects) {
            this.effects = effects;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }

        public Boolean getPlayFlag() {
            return playFlag;
        }

        public void setPlayFlag(Boolean playFlag) {
            this.playFlag = playFlag;
        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getEnd() {
            return end;
        }

        public void setEnd(Integer end) {
            this.end = end;
        }

        public String getAlbumName() {
            return albumName;
        }

        public void setAlbumName(String albumName) {
            this.albumName = albumName;
        }

        public String getAlbumId() {
            return albumId;
        }

        public void setAlbumId(String albumId) {
            this.albumId = albumId;
        }

        public String getAlbumArtistId() {
            return albumArtistId;
        }

        public void setAlbumArtistId(String albumArtistId) {
            this.albumArtistId = albumArtistId;
        }

        public String getAlbumArtistName() {
            return albumArtistName;
        }

        public void setAlbumArtistName(String albumArtistName) {
            this.albumArtistName = albumArtistName;
        }

        public List<Artist> getArtistList() {
            return artistList;
        }

        public void setArtistList(List<Artist> artistList) {
            this.artistList = artistList;
        }
    }
    
    /**
     * 艺人信息
     */
    public static class Artist {
        /**
         * 艺人ID
         */
        private String artistId;
        
        /**
         * 艺人名称
         */
        private String artistName;

        public String getArtistId() {
            return artistId;
        }

        public void setArtistId(String artistId) {
            this.artistId = artistId;
        }

        public String getArtistName() {
            return artistName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }
    }
}

