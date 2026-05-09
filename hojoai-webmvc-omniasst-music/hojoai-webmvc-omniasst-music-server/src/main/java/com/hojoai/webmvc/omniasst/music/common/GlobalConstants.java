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
package com.hojoai.webmvc.omniasst.music.common;

/**
 * Global Constants
 * 全局常量定义
 * 
 * @author HojoAI
 * @since 2026-01-13
 */
public class GlobalConstants {
    
    /**
     * Redis Key Prefix
     */
    /** 用户歌单缓存键前缀 */
    public static final String REDIS_KEY_PREFIX_USER_PLAYLIST = "music:user:playlist:";
    /** 歌曲详情缓存键前缀 */
    public static final String REDIS_KEY_PREFIX_SONG_DETAIL = "music:song:detail:";
    /** 播放状态缓存键前缀 */
    public static final String REDIS_KEY_PREFIX_PLAYING_STATE = "music:playing:state:";
    
    /**
     * Cache Expiration Time (seconds)
     */
    /** 用户歌单缓存过期时间（秒），默认 1 小时 */
    public static final long CACHE_EXPIRE_USER_PLAYLIST = 3600L; // 1 hour
    /** 歌曲详情缓存过期时间（秒），默认 2 小时 */
    public static final long CACHE_EXPIRE_SONG_DETAIL = 7200L; // 2 hours
    /** 播放状态缓存过期时间（秒），默认 30 分钟 */
    public static final long CACHE_EXPIRE_PLAYING_STATE = 1800L; // 30 minutes
    
    /**
     * Music File Path Prefix
     */
    /** 对象存储中音乐文件路径前缀 */
    public static final String OSS_PATH_PREFIX_MUSIC = "music/";
    /** 对象存储中封面资源路径前缀 */
    public static final String OSS_PATH_PREFIX_COVER = "cover/";
    /** 对象存储中歌词文件路径前缀 */
    public static final String OSS_PATH_PREFIX_LYRIC = "lyric/";
    
    /**
     * API Response Status
     */
    /** HTTP/业务成功状态码 200 */
    public static final int STATUS_SUCCESS = 200;
    /** 服务端错误状态码 500 */
    public static final int STATUS_ERROR = 500;
    /** 资源不存在状态码 404 */
    public static final int STATUS_NOT_FOUND = 404;
    /** 未授权状态码 401 */
    public static final int STATUS_UNAUTHORIZED = 401;
}

