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
 * 专辑搜索数据（网易云API返回格式）
 * <p>{@link AlbumRecord} 等嵌套类型字段与开放平台专辑搜索接口返回一致。
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
public class AlbumSearchData {
    
    /** 总记录数 */
    private Integer recordCount;
    
    /** 专辑记录列表 */
    private List<AlbumRecord> records;

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public List<AlbumRecord> getRecords() {
        return records;
    }

    public void setRecords(List<AlbumRecord> records) {
        this.records = records;
    }

    public static class AlbumRecord {
        private String id;
        private String name;
        private String language;
        private String coverImgUrl;
        private String company;
        private String transName;
        private String aliaName;
        private String genre;
        private List<ArtistInfo> artists;
        private String briefDesc;
        private String description;

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

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCoverImgUrl() {
            return coverImgUrl;
        }

        public void setCoverImgUrl(String coverImgUrl) {
            this.coverImgUrl = coverImgUrl;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getTransName() {
            return transName;
        }

        public void setTransName(String transName) {
            this.transName = transName;
        }

        public String getAliaName() {
            return aliaName;
        }

        public void setAliaName(String aliaName) {
            this.aliaName = aliaName;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public List<ArtistInfo> getArtists() {
            return artists;
        }

        public void setArtists(List<ArtistInfo> artists) {
            this.artists = artists;
        }

        public String getBriefDesc() {
            return briefDesc;
        }

        public void setBriefDesc(String briefDesc) {
            this.briefDesc = briefDesc;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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
}

