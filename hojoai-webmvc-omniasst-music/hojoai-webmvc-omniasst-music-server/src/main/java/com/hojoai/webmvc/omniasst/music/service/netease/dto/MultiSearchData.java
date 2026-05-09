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
 * 多槽位搜索返回数据
 * <p>嵌套类型 {@link AlbumItem}、{@link SongItem}、{@link PlaylistItem} 等字段与网易云语音助手多槽位搜索接口返回一致。
 * 
 * @author shengxianfei
 * @since 2026-01-15
 */
public class MultiSearchData {
    
    /** 专辑结果列表 */
    private List<AlbumItem> albums;
    
    /** 歌曲结果列表 */
    private List<SongItem> songs;
    
    /** 歌单结果列表 */
    private List<PlaylistItem> playlists;
    
    /** 本次返回涉及的资源类型标识 */
    private List<String> resTypes;
    
    public List<AlbumItem> getAlbums() {
        return albums;
    }
    
    public void setAlbums(List<AlbumItem> albums) {
        this.albums = albums;
    }
    
    public List<SongItem> getSongs() {
        return songs;
    }
    
    public void setSongs(List<SongItem> songs) {
        this.songs = songs;
    }
    
    public List<PlaylistItem> getPlaylists() {
        return playlists;
    }
    
    public void setPlaylists(List<PlaylistItem> playlists) {
        this.playlists = playlists;
    }
    
    public List<String> getResTypes() {
        return resTypes;
    }
    
    public void setResTypes(List<String> resTypes) {
        this.resTypes = resTypes;
    }
    
    /**
     * 专辑项
     */
    public static class AlbumItem {
        private String id;
        private String name;
        private String coverImgUrl;
        private List<ArtistInfo> artists;
        
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
        
        public List<ArtistInfo> getArtists() {
            return artists;
        }
        
        public void setArtists(List<ArtistInfo> artists) {
            this.artists = artists;
        }
    }
    
    /**
     * 歌曲项
     */
    public static class SongItem {
        private String id;
        private String name;
        private Long duration;
        private List<ArtistInfo> artists;
        private AlbumInfo album;
        private Boolean playFlag;
        private Boolean visible;
        private String coverImgUrl;
        
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
        
        public Long getDuration() {
            return duration;
        }
        
        public void setDuration(Long duration) {
            this.duration = duration;
        }
        
        public List<ArtistInfo> getArtists() {
            return artists;
        }
        
        public void setArtists(List<ArtistInfo> artists) {
            this.artists = artists;
        }
        
        public AlbumInfo getAlbum() {
            return album;
        }
        
        public void setAlbum(AlbumInfo album) {
            this.album = album;
        }
        
        public Boolean getPlayFlag() {
            return playFlag;
        }
        
        public void setPlayFlag(Boolean playFlag) {
            this.playFlag = playFlag;
        }
        
        public Boolean getVisible() {
            return visible;
        }
        
        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
        
        public String getCoverImgUrl() {
            return coverImgUrl;
        }
        
        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }
    }
    
    /**
     * 歌单项
     */
    public static class PlaylistItem {
        private String id;
        private String name;
        private String coverImgUrl;
        
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
    }
    
    /**
     * 艺人信息
     */
    public static class ArtistInfo {
        private String id;
        private String name;
        
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
    }
    
    /**
     * 专辑信息
     */
    public static class AlbumInfo {
        private String id;
        private String name;
        
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
    }
}

