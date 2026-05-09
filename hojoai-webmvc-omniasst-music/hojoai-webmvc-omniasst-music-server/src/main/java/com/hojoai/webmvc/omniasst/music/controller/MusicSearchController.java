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
package com.hojoai.webmvc.omniasst.music.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.music.service.netease.NeteaseCloudMusicService;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.FuzzySearchRequest;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.MusicSearchRequest;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.MusicSearchResponse;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.PlayDataRecordRequest;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.SongLyricData;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.SongPlayUrlData;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


/**
 * 音乐搜索控制器
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
@RestController
@RequestMapping("/netease-cloud")
public class MusicSearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicSearchController.class);
    
    @Resource
    private NeteaseCloudMusicService neteaseCloudMusicService;
    
    /**
     * 搜索歌曲
     *
     * @param request 搜索条件，须含 userId，且 songName、artistName、albumName 至少填一项
     * @return 统一响应体，成功时携带歌曲列表
     */
    @PostMapping("/songs")
    public Response<MusicSearchResponse> searchSongs(@RequestBody MusicSearchRequest request) {
        logger.info("收到歌曲搜索请求 - userId: {}, songName: {}, artistName: {}, albumName: {}", 
                request != null ? request.getUserId() : null,
                request != null ? request.getSongName() : null,
                request != null ? request.getArtistName() : null,
                request != null ? request.getAlbumName() : null);
        
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        
        return neteaseCloudMusicService.searchMusic(request);
    }
    
    /**
     * 根据标签搜索歌曲
     *
     * @param userId 业务用户 ID
     * @param tag    歌单标签
     * @return 歌曲列表响应
     */
    @GetMapping("/songs/bytag")
    public Response<MusicSearchResponse> searchSongsByTag(
            @RequestParam("userId") String userId,
            @RequestParam("tag") String tag) {
        logger.info("收到根据标签搜索歌曲请求 - userId: {}, tag: {}", userId, tag);
        
        Asserts.notNull(userId, "userId");
        Asserts.notNull(tag, "tag");
        
        return neteaseCloudMusicService.searchMusicByTag(userId, tag);
    }
    
    /**
     * 模糊搜索（多槽位搜索）
     *
     * @param request 须含 userId、mediaName，可选艺人/专辑/类型等
     * @return 歌曲列表响应
     */
    @PostMapping("/songs/fuzzy")
    public Response<MusicSearchResponse> searchSongsByFuzzy(@RequestBody FuzzySearchRequest request) {
        logger.info("收到模糊搜索请求 - userId: {}, mediaName: {}, artistName: {}, albumName: {}, mediaType: {}, sortOrder: {}", 
                request != null ? request.getUserId() : null,
                request != null ? request.getMediaName() : null,
                request != null ? request.getArtistName() : null,
                request != null ? request.getAlbumName() : null,
                request != null ? request.getMediaType() : null,
                request != null ? request.getSortOrder() : null);
        
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        Asserts.notNull(request.getMediaName(), "mediaName");
        
        return neteaseCloudMusicService.searchMusicByFuzzy(request);
    }
    
    /**
     * 榜单搜索（歌曲）
     *
     * @param userId 业务用户 ID
     * @return 默认取第一个榜单内前若干首歌曲
     */
    @GetMapping("/songs/toplist")
    public Response<MusicSearchResponse> searchSongsByToplist(@RequestParam("userId") String userId) {
        logger.info("收到榜单搜索请求 - userId: {}", userId);
        
        Asserts.notNull(userId, "userId");
        
        return neteaseCloudMusicService.searchMusicByToplist(userId);
    }
    
    /**
     * 获取歌曲播放URL
     *
     * @param userId 业务用户 ID
     * @param songId 歌曲 ID
     * @return 播放地址等信息
     */
    @GetMapping("/song/playurl")
    public Response<SongPlayUrlData> getSongPlayUrl(
            @RequestParam("userId") String userId,
            @RequestParam("songId") String songId) {
        logger.info("收到获取歌曲播放URL请求 - userId: {}, songId: {}", userId, songId);
        
        Asserts.notNull(userId, "userId");
        Asserts.notNull(songId, "songId");
        
        return neteaseCloudMusicService.getSongPlayUrl(userId, songId);
    }
    
    /**
     * 获取歌曲歌词
     *
     * @param userId 业务用户 ID
     * @param songId 歌曲 ID
     * @return 歌词各字段数据
     */
    @GetMapping("/song/lyric")
    public Response<SongLyricData> getSongLyric(
            @RequestParam("userId") String userId,
            @RequestParam("songId") String songId) {
        logger.info("收到获取歌曲歌词请求 - userId: {}, songId: {}", userId, songId);
        
        Asserts.notNull(userId, "userId");
        Asserts.notNull(songId, "songId");
        
        return neteaseCloudMusicService.getSongLyric(userId, songId);
    }
    
    /**
     * 播放数据回传
     *
     * @param request 播放行为与资源信息，字段见 DTO 注释
     * @return 平台是否成功接收
     */
    @PostMapping("/play/data/record")
    public Response<Boolean> playDataRecord(@RequestBody PlayDataRecordRequest request) {
        logger.info("收到播放数据回传请求 - userId: {}, action: {}, id: {}", 
                request != null ? request.getUserId() : null,
                request != null ? request.getAction() : null,
                request != null ? request.getId() : null);
        
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        
        return neteaseCloudMusicService.playDataRecord(request);
    }
}

