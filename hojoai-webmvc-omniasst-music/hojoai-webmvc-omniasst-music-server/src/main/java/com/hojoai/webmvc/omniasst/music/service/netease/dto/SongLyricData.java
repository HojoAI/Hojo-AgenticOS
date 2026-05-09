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
 * 歌曲歌词数据（网易云API返回格式）
 * 对应接口：/openapi/music/basic/song/lyric/get/v2
 * 
 * @author shengxianfei
 * @since 2026-01-30
 */
public class SongLyricData {
    
    /** 歌曲 ID */
    private String songId;
    
    /** 原版歌词内容（逐行 LRC 等格式，依接口返回） */
    private String lyric;
    
    /** 是否无歌词：true 表示纯音乐等无歌词，false 表示有歌词 */
    private Boolean noLyric;
    
    /** 翻译歌词 */
    private String transLyric;
    
    /** 非滚动/纯文本歌词 */
    private String txtLyric;
    
    // Getters and Setters
    
    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public Boolean getNoLyric() {
        return noLyric;
    }

    public void setNoLyric(Boolean noLyric) {
        this.noLyric = noLyric;
    }

    public String getTransLyric() {
        return transLyric;
    }

    public void setTransLyric(String transLyric) {
        this.transLyric = transLyric;
    }

    public String getTxtLyric() {
        return txtLyric;
    }

    public void setTxtLyric(String txtLyric) {
        this.txtLyric = txtLyric;
    }
}

