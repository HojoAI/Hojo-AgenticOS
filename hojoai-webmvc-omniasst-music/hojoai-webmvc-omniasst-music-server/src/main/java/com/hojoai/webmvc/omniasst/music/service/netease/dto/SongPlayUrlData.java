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
 * 歌曲播放URL数据（网易云API返回格式）
 * 对应接口：/openapi/music/basic/song/playurl/get/v2
 * 
 * @author shengxianfei
 * @since 2026-01-22
 */
public class SongPlayUrlData {
    
    /** 可播放资源 URL */
    private String url;
    
    /** 文件大小（字节） */
    private Long size;
    
    /** 文件 MD5 */
    private String md5;
    
    /** 码率（如 kbps 含义以开放平台为准） */
    private Integer br;
    
    /** 音效标识 */
    private String effects;
    
    /** 是否为云盘私有曲库资源 */
    private Boolean privateCloudSong;
    
    /** 音质档位/level */
    private String level;
    
    /** 试听时间片段（若有） */
    private FreeTrail freeTrail;
    
    // Getters and Setters
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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

    public Boolean getPrivateCloudSong() {
        return privateCloudSong;
    }

    public void setPrivateCloudSong(Boolean privateCloudSong) {
        this.privateCloudSong = privateCloudSong;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public FreeTrail getFreeTrail() {
        return freeTrail;
    }

    public void setFreeTrail(FreeTrail freeTrail) {
        this.freeTrail = freeTrail;
    }

    /**
     * 试听时间片段
     */
    public static class FreeTrail {
        /** 试听开始时间（秒） */
        private Integer start;
        
        /** 试听结束时间（秒） */
        private Integer end;

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
    }
}

