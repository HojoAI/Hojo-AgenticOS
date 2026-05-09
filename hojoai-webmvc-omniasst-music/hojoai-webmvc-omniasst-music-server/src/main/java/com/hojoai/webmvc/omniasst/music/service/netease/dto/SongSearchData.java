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
 * 歌曲搜索数据（网易云API返回格式）
 * <p>嵌套类型 {@link SongRecord}、{@link ArtistInfo}、{@link AlbumInfo} 等字段与开放平台搜索/歌单类接口返回一致。
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
public class SongSearchData {
    
    /** 总记录数（分页场景） */
    private Integer recordCount;
    
    /** 当前页歌曲记录列表 */
    private List<SongRecord> records;

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public List<SongRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SongRecord> records) {
        this.records = records;
    }

    public static class SongRecord {
        private String id;
        private String name;
        private Long duration;
        private List<ArtistInfo> artists;
        private List<ArtistInfo> fullArtists;
        private AlbumInfo album;
        private Boolean playFlag;
        private Boolean downloadFlag;
        private Boolean payPlayFlag;
        private Boolean payDownloadFlag;
        private Boolean vipFlag;
        private Boolean liked;
        private String coverImgUrl;
        private Boolean vipPlayFlag;
        private Integer songMaxBr;
        private Integer userMaxBr;
        private String maxBrLevel;
        private String plLevel;
        private String dlLevel;
        private List<String> songTag;
        private String alg;
        private Boolean visible;

        // Getters and Setters
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

        public List<ArtistInfo> getFullArtists() {
            return fullArtists;
        }

        public void setFullArtists(List<ArtistInfo> fullArtists) {
            this.fullArtists = fullArtists;
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

        public Boolean getDownloadFlag() {
            return downloadFlag;
        }

        public void setDownloadFlag(Boolean downloadFlag) {
            this.downloadFlag = downloadFlag;
        }

        public Boolean getPayPlayFlag() {
            return payPlayFlag;
        }

        public void setPayPlayFlag(Boolean payPlayFlag) {
            this.payPlayFlag = payPlayFlag;
        }

        public Boolean getPayDownloadFlag() {
            return payDownloadFlag;
        }

        public void setPayDownloadFlag(Boolean payDownloadFlag) {
            this.payDownloadFlag = payDownloadFlag;
        }

        public Boolean getVipFlag() {
            return vipFlag;
        }

        public void setVipFlag(Boolean vipFlag) {
            this.vipFlag = vipFlag;
        }

        public Boolean getLiked() {
            return liked;
        }

        public void setLiked(Boolean liked) {
            this.liked = liked;
        }

        public String getCoverImgUrl() {
            return coverImgUrl;
        }

        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }

        public Boolean getVipPlayFlag() {
            return vipPlayFlag;
        }

        public void setVipPlayFlag(Boolean vipPlayFlag) {
            this.vipPlayFlag = vipPlayFlag;
        }

        public Integer getSongMaxBr() {
            return songMaxBr;
        }

        public void setSongMaxBr(Integer songMaxBr) {
            this.songMaxBr = songMaxBr;
        }

        public Integer getUserMaxBr() {
            return userMaxBr;
        }

        public void setUserMaxBr(Integer userMaxBr) {
            this.userMaxBr = userMaxBr;
        }

        public String getMaxBrLevel() {
            return maxBrLevel;
        }

        public void setMaxBrLevel(String maxBrLevel) {
            this.maxBrLevel = maxBrLevel;
        }

        public String getPlLevel() {
            return plLevel;
        }

        public void setPlLevel(String plLevel) {
            this.plLevel = plLevel;
        }

        public String getDlLevel() {
            return dlLevel;
        }

        public void setDlLevel(String dlLevel) {
            this.dlLevel = dlLevel;
        }

        public List<String> getSongTag() {
            return songTag;
        }

        public void setSongTag(List<String> songTag) {
            this.songTag = songTag;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }
    }

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

