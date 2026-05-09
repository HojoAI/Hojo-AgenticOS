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
package com.hojoai.webmvc.omniasst.music.service.netease;

import com.hojoai.webmvc.omniasst.music.service.netease.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 网易云音乐API Feign客户端
 * <p>调用配置项 {@code netease.cloud.music.api.base-url} 指向的开放平台网关。
 * 各方法公共查询参数含义一致：{@code appId}、{@code signType}、{@code sign}、{@code timestamp}、
 * {@code bizContent}、{@code encryptType}、{@code device}；需登录态的接口另传 {@code accessToken}。
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
@FeignClient(name = "netease-cloud-music-client",
             url = "${netease.cloud.music.api.base-url}")
public interface NeteaseCloudMusicAPI {
    
    /**
     * 匿名登录
     */
    @PostMapping("/openapi/music/basic/oauth2/login/anonymous")
    NeteaseApiResponse<AnonymousLoginData> anonymousLogin(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据歌名关键字搜索歌曲
     */
    @GetMapping("/openapi/music/basic/search/song/get/v3")
    NeteaseApiResponse<SongSearchData> searchSongByName(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据歌手搜索歌曲
     */
    @GetMapping("/openapi/music/basic/search/song/byartist/get/v2")
    NeteaseApiResponse<SongSearchData> searchSongByArtist(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据歌名、歌手搜索歌曲
     */
    @GetMapping("/openapi/music/basic/search/song/by/artist/song/get/v2")
    NeteaseApiResponse<SongSearchData> searchSongByArtistAndSong(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据专辑关键字搜索专辑
     */
    @GetMapping("/openapi/music/basic/search/album/get/v2")
    NeteaseApiResponse<AlbumSearchData> searchAlbum(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据专辑ID获取歌曲列表
     */
    @GetMapping("/openapi/music/basic/album/song/list/get/v3")
    NeteaseApiResponse<SongSearchData> getAlbumSongList(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 获取歌曲详情
     */
    @GetMapping("/openapi/music/basic/song/detail/get/v2")
    NeteaseApiResponse<SongDetailData> getSongDetail(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 根据标签搜索歌单
     */
    @GetMapping("/openapi/music/basic/search/playlist/bytag/get/v2")
    NeteaseApiResponse<PlaylistSearchData> searchPlaylistByTag(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 获取歌单里的歌曲列表
     */
    @GetMapping("/openapi/music/basic/playlist/song/list/get/v3")
    NeteaseApiResponse<PlaylistSongListData> getPlaylistSongList(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 多槽位搜索
     */
    @PostMapping("/openapi/music/basic/voice/assistant/multi/search")
    NeteaseApiResponse<MultiSearchData> multiSearch(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 获取榜单列表
     */
    @GetMapping("/openapi/music/basic/toplist/get/v2")
    NeteaseApiResponse<java.util.List<ToplistData>> getToplist(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 用code兑换token
     */
    @GetMapping("/openapi/music/basic/user/oauth2/token/get/v2")
    NeteaseApiResponse<OAuth2TokenData> getTokenByCode(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 刷新accessToken
     */
    @PostMapping("/openapi/music/basic/user/oauth2/token/refresh/v2")
    NeteaseApiResponse<OAuth2TokenData> refreshToken(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 获取歌曲播放URL
     */
    @GetMapping("/openapi/music/basic/song/playurl/get/v2")
    NeteaseApiResponse<SongPlayUrlData> getSongPlayUrl(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 获取歌曲歌词
     */
    @GetMapping("/openapi/music/basic/song/lyric/get/v2")
    NeteaseApiResponse<SongLyricData> getSongLyric(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
    
    /**
     * 播放数据回传
     */
    @PostMapping("/openapi/music/basic/play/data/record")
    NeteaseApiResponse<Boolean> playDataRecord(
            @RequestParam("appId") String appId,
            @RequestParam("signType") String signType,
            @RequestParam("sign") String sign,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("bizContent") String bizContent,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("encryptType") String encryptType,
            @RequestParam("device") String device);
}

