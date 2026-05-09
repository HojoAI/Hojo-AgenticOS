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
 * 歌曲详情数据（网易云API返回格式）
 * <p>各字段与开放平台「歌曲详情」接口返回一致；本服务主要消费播放地址、码率、试听区间 {@link FreeTrail}、艺人列表等。
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
public class SongDetailData {
    
    private String id;
    private String name;
    private Long duration;
    private String albumName;
    private String albumId;
    private String albumArtistId;
    private String albumArtistName;
    private String artistId;
    private String artistName;
    private String coverImgUrl;
    private String mvId;
    private String playUrl;
    private Long playUrlExpireTime;
    private Integer br;
    private Boolean playFlag;
    private Boolean downloadFlag;
    private Boolean payPlayFlag;
    private Boolean payDownloadFlag;
    private Boolean vipFlag;
    private Boolean vipPlayFlag;
    private Boolean freeTrailFlag;
    private Boolean songFtFlag;
    private Boolean liked;
    private Integer songMaxBr;
    private Integer userMaxBr;
    private String maxBrLevel;
    private String plLevel;
    private String dlLevel;
    private Double gain;
    private Integer peak;
    private String type;
    private String level;
    private Long songSize;
    private String songMd5;
    private List<String> songTag;
    private String emotionTag;
    private List<ArtistInfo> artists;
    private List<ArtistInfo> fullArtists;
    private Integer songFee;
    private List<String> qualities;
    private Boolean visible;
    private String effects;
    
    // 试听相关
    private FreeTrialPrivilege freeTrialPrivilege;
    private FreeTrail freeTrail;  // 试听时间片段
    private ChorusMeta chorusMeta;
    
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

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getMvId() {
        return mvId;
    }

    public void setMvId(String mvId) {
        this.mvId = mvId;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public Long getPlayUrlExpireTime() {
        return playUrlExpireTime;
    }

    public void setPlayUrlExpireTime(Long playUrlExpireTime) {
        this.playUrlExpireTime = playUrlExpireTime;
    }

    public Integer getBr() {
        return br;
    }

    public void setBr(Integer br) {
        this.br = br;
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

    public Boolean getVipPlayFlag() {
        return vipPlayFlag;
    }

    public void setVipPlayFlag(Boolean vipPlayFlag) {
        this.vipPlayFlag = vipPlayFlag;
    }

    public Boolean getFreeTrailFlag() {
        return freeTrailFlag;
    }

    public void setFreeTrailFlag(Boolean freeTrailFlag) {
        this.freeTrailFlag = freeTrailFlag;
    }

    public Boolean getSongFtFlag() {
        return songFtFlag;
    }

    public void setSongFtFlag(Boolean songFtFlag) {
        this.songFtFlag = songFtFlag;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
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

    public Double getGain() {
        return gain;
    }

    public void setGain(Double gain) {
        this.gain = gain;
    }

    public Integer getPeak() {
        return peak;
    }

    public void setPeak(Integer peak) {
        this.peak = peak;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getSongSize() {
        return songSize;
    }

    public void setSongSize(Long songSize) {
        this.songSize = songSize;
    }

    public String getSongMd5() {
        return songMd5;
    }

    public void setSongMd5(String songMd5) {
        this.songMd5 = songMd5;
    }

    public List<String> getSongTag() {
        return songTag;
    }

    public void setSongTag(List<String> songTag) {
        this.songTag = songTag;
    }

    public String getEmotionTag() {
        return emotionTag;
    }

    public void setEmotionTag(String emotionTag) {
        this.emotionTag = emotionTag;
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

    public Integer getSongFee() {
        return songFee;
    }

    public void setSongFee(Integer songFee) {
        this.songFee = songFee;
    }

    public List<String> getQualities() {
        return qualities;
    }

    public void setQualities(List<String> qualities) {
        this.qualities = qualities;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getEffects() {
        return effects;
    }

    public void setEffects(String effects) {
        this.effects = effects;
    }

    public FreeTrialPrivilege getFreeTrialPrivilege() {
        return freeTrialPrivilege;
    }

    public void setFreeTrialPrivilege(FreeTrialPrivilege freeTrialPrivilege) {
        this.freeTrialPrivilege = freeTrialPrivilege;
    }

    public FreeTrail getFreeTrail() {
        return freeTrail;
    }

    public void setFreeTrail(FreeTrail freeTrail) {
        this.freeTrail = freeTrail;
    }

    public ChorusMeta getChorusMeta() {
        return chorusMeta;
    }

    public void setChorusMeta(ChorusMeta chorusMeta) {
        this.chorusMeta = chorusMeta;
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

    public static class FreeTrialPrivilege {
        private String cannotListenReason;
        private Boolean resConsumable;
        private Boolean userConsumable;

        public String getCannotListenReason() {
            return cannotListenReason;
        }

        public void setCannotListenReason(String cannotListenReason) {
            this.cannotListenReason = cannotListenReason;
        }

        public Boolean getResConsumable() {
            return resConsumable;
        }

        public void setResConsumable(Boolean resConsumable) {
            this.resConsumable = resConsumable;
        }

        public Boolean getUserConsumable() {
            return userConsumable;
        }

        public void setUserConsumable(Boolean userConsumable) {
            this.userConsumable = userConsumable;
        }
    }

    public static class FreeTrail {
        private Integer start;  // 试听开始时间（毫秒）
        private Integer end;    // 试听结束时间（毫秒）

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

    public static class ChorusMeta {
        private Integer startTime;
        private Integer endTime;

        public Integer getStartTime() {
            return startTime;
        }

        public void setStartTime(Integer startTime) {
            this.startTime = startTime;
        }

        public Integer getEndTime() {
            return endTime;
        }

        public void setEndTime(Integer endTime) {
            this.endTime = endTime;
        }
    }
}

