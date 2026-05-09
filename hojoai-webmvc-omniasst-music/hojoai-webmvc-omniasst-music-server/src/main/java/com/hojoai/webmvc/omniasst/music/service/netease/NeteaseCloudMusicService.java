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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.*;
import com.hojoai.webmvc.omniasst.music.util.NetEaseCloudSignUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 网易云音乐业务服务
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
@Service
public class NeteaseCloudMusicService {
    
    private static final Logger logger = LoggerFactory.getLogger(NeteaseCloudMusicService.class);
    
    @Resource
    private NeteaseCloudMusicAPI neteaseCloudMusicAPI;
    
    @Resource
    private NetEaseCloudSignUtil signUtil;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private OAuth2Service oauth2Service;
    
    @Value("${netease.cloud.app-id}")
    private String appId;
    
    @Value("${netease.cloud.app-secret}")
    private String appSecret;
    
    /** 调用开放平台使用的签名类型 */
    private static final String SIGN_TYPE = "RSA_SHA256";
    
    /** 列表类接口默认每页条数 */
    private static final Integer DEFAULT_LIMIT = 30;
    
    /** 列表类接口默认偏移 */
    private static final Integer DEFAULT_OFFSET = 0;
    
    // 用于并发获取歌曲详情的线程池，最大20个并发
    private final ExecutorService detailExecutor = Executors.newFixedThreadPool(20);
    
    
    /**
     * 按歌名/歌手/专辑组合规则搜索歌曲，并并发拉取详情、过滤无播放地址的结果。
     *
     * @param request 须含 userId；songName、artistName、albumName 至少一项非空
     * @return 成功时携带 {@link MusicSearchResponse}；参数错误 400，异常 500
     */
    public Response<MusicSearchResponse> searchMusic(MusicSearchRequest request) {
        logger.info("搜索歌曲请求: userId={}, songName={}, artistName={}, albumName={}", 
                request.getUserId(), request.getSongName(), request.getArtistName(), request.getAlbumName());
        
        try {
            // 参数校验：songName、artistName、albumName至少需要一个
            if (StringUtils.isBlank(request.getSongName()) && 
                StringUtils.isBlank(request.getArtistName()) && 
                StringUtils.isBlank(request.getAlbumName())) {
                MusicSearchResponse emptyResponse = new MusicSearchResponse();
                emptyResponse.setMusicList(new ArrayList<>());
                // 正确方式：使用newInstance静态方法创建错误响应
                return Response.newInstance("400", "参数错误：songName、artistName、albumName至少需要一个", emptyResponse);
            }
            
            // 获取accessToken
            String accessToken = getAccessToken(request.getUserId());
            
            List<SongSearchData.SongRecord> songRecords;
            
            // 根据不同的参数组合调用不同的API
            if (StringUtils.isNotBlank(request.getSongName()) && StringUtils.isNotBlank(request.getArtistName())) {
                // 情况4: 有songName和artistName，用接口3
                songRecords = searchBySongAndArtist(request.getSongName(), request.getArtistName(), accessToken);
            } else if (StringUtils.isNotBlank(request.getSongName())) {
                // 情况1: 仅有songName，用接口1
                songRecords = searchBySongName(request.getSongName(), accessToken);
            } else if (StringUtils.isNotBlank(request.getArtistName())) {
                // 情况2: 仅有artistName，用接口2
                songRecords = searchByArtist(request.getArtistName(), accessToken);
            } else {
                // 情况3: 仅有albumName，用接口4+接口5
                songRecords = searchByAlbum(request.getAlbumName(), accessToken);
            }
            
            // 批量并发获取歌曲详情（包含播放URL等）并过滤掉playUrl为null的歌曲
            List<MusicSearchResponse.Music> musicList = batchGetSongDetailsWithRecord(songRecords, accessToken);
            
            MusicSearchResponse musicResponse = new MusicSearchResponse();
            musicResponse.setMusicList(musicList);
            
            logger.info("搜索歌曲成功: 共找到{}首有效歌曲", musicList.size());
            // 正确方式：使用newInstance静态方法创建成功响应
            return Response.newInstance("200", "成功", musicResponse);
            
        } catch (Exception e) {
            logger.error("搜索歌曲失败", e);
            MusicSearchResponse emptyResponse = new MusicSearchResponse();
            emptyResponse.setMusicList(new ArrayList<>());
            // 正确方式：使用newInstance静态方法创建异常响应
            return Response.newInstance("500", "搜索失败: " + e.getMessage(), emptyResponse);
        }
    }
    
    /**
     * 先按标签找歌单，再取歌单内歌曲并并发拉详情、过滤无 playUrl 的条目。
     *
     * @param userId 业务用户 ID（用于取 token）
     * @param tag    歌单标签
     * @return 歌曲列表封装；失败时 resCode 非 200
     */
    public Response<MusicSearchResponse> searchMusicByTag(String userId, String tag) {
        logger.info("根据标签搜索歌曲请求: userId={}, tag={}", userId, tag);
        
        try {
            // 获取accessToken
            String accessToken = getAccessToken(userId);
            
            // 根据标签搜索歌单
            List<SongSearchData.SongRecord> songRecords = searchByTag(tag, accessToken);
            
            // 批量并发获取歌曲详情并过滤掉playUrl为null的歌曲
            List<MusicSearchResponse.Music> musicList = batchGetSongDetailsWithRecord(songRecords, accessToken);
            
            MusicSearchResponse musicResponse = new MusicSearchResponse();
            musicResponse.setMusicList(musicList);
            
            logger.info("根据标签搜索歌曲成功: 共找到{}首有效歌曲", musicList.size());
            return Response.newInstance("200", "成功", musicResponse);
            
        } catch (Exception e) {
            logger.error("根据标签搜索歌曲失败", e);
            MusicSearchResponse emptyResponse = new MusicSearchResponse();
            emptyResponse.setMusicList(new ArrayList<>());
            return Response.newInstance("500", "搜索失败: " + e.getMessage(), emptyResponse);
        }
    }
    
    /**
     * 多槽位语音搜索：按 mediaType 从专辑/歌曲/歌单等结果中解析歌曲 ID，再批量取详情。
     *
     * @param request 须含 userId、mediaName
     * @return 歌曲列表封装；失败时 resCode 非 200
     */
    public Response<MusicSearchResponse> searchMusicByFuzzy(FuzzySearchRequest request) {
        logger.info("模糊搜索请求: userId={}, mediaName={}, artistName={}, albumName={}, mediaType={}, sortOrder={}", 
                request.getUserId(), request.getMediaName(), request.getArtistName(), 
                request.getAlbumName(), request.getMediaType(), request.getSortOrder());
        
        try {
            // 获取accessToken
            String accessToken = getAccessToken(request.getUserId());
            
            // 调用多槽位搜索接口
            MultiSearchData multiSearchData = multiSearch(request, accessToken);
            
            // 根据mediaType处理返回结果
            List<String> songIds = extractSongIds(multiSearchData, request.getMediaType(), accessToken);
            
            // 批量并发获取歌曲详情并过滤掉playUrl为null的歌曲
            List<MusicSearchResponse.Music> musicList = batchGetSongDetailsById(songIds, accessToken);
            
            MusicSearchResponse musicResponse = new MusicSearchResponse();
            musicResponse.setMusicList(musicList);
            
            logger.info("模糊搜索成功: 共找到{}首有效歌曲", musicList.size());
            return Response.newInstance("200", "成功", musicResponse);
            
        } catch (Exception e) {
            logger.error("模糊搜索失败", e);
            MusicSearchResponse emptyResponse = new MusicSearchResponse();
            emptyResponse.setMusicList(new ArrayList<>());
            return Response.newInstance("500", "搜索失败: " + e.getMessage(), emptyResponse);
        }
    }
    
    /**
     * 拉取榜单列表后取第一个榜单，取前 {@link #DEFAULT_LIMIT} 首并批量拉详情。
     *
     * @param userId 业务用户 ID
     * @return 歌曲列表；榜单为空时仍可能返回 200 与空列表
     */
    public Response<MusicSearchResponse> searchMusicByToplist(String userId) {
        logger.info("榜单搜索请求: userId={}", userId);
        
        try {
            // 获取accessToken
            String accessToken = getAccessToken(userId);
            
            // 获取榜单列表
            List<ToplistData> toplists = getToplist(accessToken);
            
            if (toplists == null || toplists.isEmpty()) {
                logger.warn("未找到榜单");
                MusicSearchResponse emptyResponse = new MusicSearchResponse();
                emptyResponse.setMusicList(new ArrayList<>());
                return Response.newInstance("200", "未找到榜单", emptyResponse);
            }
            
            // 获取第一个榜单的ID
            String playlistId = toplists.get(0).getId();
            logger.info("使用榜单: id={}, name={}", playlistId, toplists.get(0).getName());
            
            // 获取榜单歌曲列表
            List<SongSearchData.SongRecord> songRecords = getPlaylistSongs(playlistId, accessToken);
            
            // 取前30首，批量并发获取歌曲详情并过滤掉playUrl为null的歌曲
            List<SongSearchData.SongRecord> limitedRecords = songRecords.stream()
                    .limit(DEFAULT_LIMIT)
                    .collect(Collectors.toList());
            
            // 提取歌曲ID列表
            List<String> songIds = limitedRecords.stream()
                    .map(SongSearchData.SongRecord::getId)
                    .collect(Collectors.toList());
            
            List<MusicSearchResponse.Music> musicList = batchGetSongDetailsById(songIds, accessToken);
            
            MusicSearchResponse musicResponse = new MusicSearchResponse();
            musicResponse.setMusicList(musicList);
            
            logger.info("榜单搜索成功: 共找到{}首有效歌曲", musicList.size());
            return Response.newInstance("200", "成功", musicResponse);
            
        } catch (Exception e) {
            logger.error("榜单搜索失败", e);
            MusicSearchResponse emptyResponse = new MusicSearchResponse();
            emptyResponse.setMusicList(new ArrayList<>());
            return Response.newInstance("500", "搜索失败: " + e.getMessage(), emptyResponse);
        }
    }
    
    /**
     * 获取指定歌曲的可播放 URL（需用户已授权）。
     *
     * @param userId 业务用户 ID
     * @param songId 网易云歌曲 ID
     * @return 播放地址数据；参数错误 400，未授权或接口失败 500
     */
    public Response<SongPlayUrlData> getSongPlayUrl(String userId, String songId) {
        logger.info("获取歌曲播放URL请求: userId={}, songId={}", userId, songId);
        
        try {
            // 参数校验
            if (StringUtils.isBlank(userId)) {
                return Response.newInstance("400", "参数错误：userId不能为空", null);
            }
            if (StringUtils.isBlank(songId)) {
                return Response.newInstance("400", "参数错误：songId不能为空", null);
            }
            
            // 获取accessToken
            String accessToken = getAccessToken(userId);
            
            // 调用获取播放URL接口
            SongPlayUrlData playUrlData = getSongPlayUrlInternal(songId, accessToken);
            
            logger.info("获取歌曲播放URL成功: songId={}, url={}", songId, 
                    playUrlData.getUrl() != null ? "已返回" : "为空");
            return Response.newInstance("200", "成功", playUrlData);
            
        } catch (Exception e) {
            logger.error("获取歌曲播放URL失败: userId={}, songId={}", userId, songId, e);
            return Response.newInstance("500", "获取播放URL失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取歌曲歌词（含逐行、翻译等字段，依平台返回为准）。
     *
     * @param userId 业务用户 ID
     * @param songId 网易云歌曲 ID
     * @return 歌词数据；参数错误 400，未授权或接口失败 500
     */
    public Response<SongLyricData> getSongLyric(String userId, String songId) {
        logger.info("获取歌曲歌词请求: userId={}, songId={}", userId, songId);
        
        try {
            // 参数校验
            if (StringUtils.isBlank(userId)) {
                return Response.newInstance("400", "参数错误：userId不能为空", null);
            }
            if (StringUtils.isBlank(songId)) {
                return Response.newInstance("400", "参数错误：songId不能为空", null);
            }
            
            // 获取accessToken
            String accessToken = getAccessToken(userId);
            
            // 调用获取歌词接口
            SongLyricData lyricData = getSongLyricInternal(songId, accessToken);
            
            logger.info("获取歌曲歌词成功: songId={}, noLyric={}", songId, lyricData.getNoLyric());
            return Response.newInstance("200", "成功", lyricData);
            
        } catch (Exception e) {
            logger.error("获取歌曲歌词失败: userId={}, songId={}", userId, songId, e);
            return Response.newInstance("500", "获取歌词失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 调用网易云API获取歌曲播放URL
     */
    private SongPlayUrlData getSongPlayUrlInternal(String songId, String accessToken) throws Exception {
        logger.debug("调用获取播放URL接口: songId={}", songId);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("songId", songId);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongPlayUrlData> response = neteaseCloudMusicAPI.getSongPlayUrl(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取播放URL失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData();
    }
    
    /**
     * 调用网易云API获取歌曲歌词
     */
    private SongLyricData getSongLyricInternal(String songId, String accessToken) throws Exception {
        logger.debug("调用获取歌词接口: songId={}", songId);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("songId", songId);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongLyricData> response = neteaseCloudMusicAPI.getSongLyric(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取歌词失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData();
    }
    
    /**
     * 获取用户AccessToken（从Redis获取，如果过期则刷新）
     */
    private String getAccessToken(String userId) throws Exception {
        logger.debug("获取用户AccessToken: userId={}", userId);
        
        // 从Redis获取用户token
        UserTokenData tokenData = oauth2Service.getUserToken(userId);
        
        // 检查是否有token数据
        if (tokenData == null || StringUtils.isBlank(tokenData.getRefreshToken())) {
            throw new RuntimeException("用户未授权，请先完成网易云音乐授权");
        }
        
        // 检查refreshToken是否过期
        long currentTime = System.currentTimeMillis();
        if (tokenData.getRefreshTokenExpireTime() != null && 
            currentTime > tokenData.getRefreshTokenExpireTime()) {
            throw new RuntimeException("授权已过期，请重新授权");
        }
        
        // 检查accessToken是否过期
        if (StringUtils.isNotBlank(tokenData.getAccessToken()) &&
            tokenData.getAccessTokenExpireTime() != null &&
            currentTime < tokenData.getAccessTokenExpireTime()) {
            // accessToken有效，直接返回
            logger.debug("使用有效的accessToken: userId={}", userId);
            return tokenData.getAccessToken();
        }
        
        // accessToken已过期或不存在，使用refreshToken刷新
        logger.info("accessToken已过期，使用refreshToken刷新: userId={}", userId);
        Response<UserTokenData> refreshResponse = oauth2Service.refreshUserToken(userId);
        
        if (!"200".equals(refreshResponse.getResCode()) || refreshResponse.getResContent() == null) {
            throw new RuntimeException("刷新token失败: " + refreshResponse.getResMessage());
        }
        
        UserTokenData newTokenData = refreshResponse.getResContent();
        logger.info("token刷新成功: userId={}", userId);
        return newTokenData.getAccessToken();
    }
    
    /**
     * 根据歌名搜索
     */
    private List<SongSearchData.SongRecord> searchBySongName(String songName, String accessToken) throws Exception {
        logger.info("根据歌名搜索: {}", songName);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("keyword", songName);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongSearchData> response = neteaseCloudMusicAPI.searchSongByName(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("搜索失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData().getRecords() != null ? response.getData().getRecords() : new ArrayList<>();
    }
    
    /**
     * 根据歌手搜索
     */
    private List<SongSearchData.SongRecord> searchByArtist(String artistName, String accessToken) throws Exception {
        logger.info("根据歌手搜索: {}", artistName);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("keyword", artistName);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongSearchData> response = neteaseCloudMusicAPI.searchSongByArtist(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("搜索失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData().getRecords() != null ? response.getData().getRecords() : new ArrayList<>();
    }
    
    /**
     * 根据歌名和歌手搜索
     */
    private List<SongSearchData.SongRecord> searchBySongAndArtist(String songName, String artistName, String accessToken) throws Exception {
        logger.info("根据歌名和歌手搜索: songName={}, artistName={}", songName, artistName);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("songName", songName);
        bizContentMap.put("artistName", artistName);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongSearchData> response = neteaseCloudMusicAPI.searchSongByArtistAndSong(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("搜索失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData().getRecords() != null ? response.getData().getRecords() : new ArrayList<>();
    }
    
    /**
     * 根据专辑搜索
     */
    private List<SongSearchData.SongRecord> searchByAlbum(String albumName, String accessToken) throws Exception {
        logger.info("根据专辑搜索: {}", albumName);
        
        // 先搜索专辑
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("keyword", albumName);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<AlbumSearchData> albumResponse = neteaseCloudMusicAPI.searchAlbum(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (albumResponse == null || albumResponse.getCode() != 200 || 
            albumResponse.getData() == null || albumResponse.getData().getRecords() == null || 
            albumResponse.getData().getRecords().isEmpty()) {
            logger.warn("未找到专辑: {}", albumName);
            return new ArrayList<>();
        }
        
        // 获取第一个专辑的歌曲列表
        String albumId = albumResponse.getData().getRecords().get(0).getId();
        logger.info("找到专辑ID: {}", albumId);
        
        // 根据专辑ID获取歌曲列表
        commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        bizContentMap = new HashMap<>();
        bizContentMap.put("albumId", albumId);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongSearchData> songResponse = neteaseCloudMusicAPI.getAlbumSongList(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (songResponse == null || songResponse.getCode() != 200 || songResponse.getData() == null) {
            throw new RuntimeException("获取专辑歌曲失败: " + (songResponse != null ? songResponse.getMessage() : "响应为空"));
        }
        
        return songResponse.getData().getRecords() != null ? songResponse.getData().getRecords() : new ArrayList<>();
    }
    
    /**
     * 根据标签搜索
     */
    private List<SongSearchData.SongRecord> searchByTag(String tag, String accessToken) throws Exception {
        logger.info("根据标签搜索: {}", tag);
        
        // 先根据标签搜索歌单
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("tag", tag);
        bizContentMap.put("limit", 1);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<PlaylistSearchData> playlistResponse = neteaseCloudMusicAPI.searchPlaylistByTag(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (playlistResponse == null || playlistResponse.getCode() != 200 || 
            playlistResponse.getData() == null || playlistResponse.getData().getRecords() == null || 
            playlistResponse.getData().getRecords().isEmpty()) {
            logger.warn("未找到标签为{}的歌单", tag);
            return new ArrayList<>();
        }
        
        // 获取第一个歌单的ID
        String playlistId = playlistResponse.getData().getRecords().get(0).getId();
        logger.info("找到歌单ID: {}", playlistId);
        
        // 根据歌单ID获取歌曲列表
        commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        bizContentMap = new HashMap<>();
        bizContentMap.put("playlistId", playlistId);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<PlaylistSongListData> songResponse = neteaseCloudMusicAPI.getPlaylistSongList(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (songResponse == null || songResponse.getCode() != 200 || songResponse.getData() == null) {
            throw new RuntimeException("获取歌单歌曲失败: " + (songResponse != null ? songResponse.getMessage() : "响应为空"));
        }
        
        return songResponse.getData() != null ? songResponse.getData() : new ArrayList<>();
    }
    
    /**
     * 获取歌曲详情
     */
    private SongDetailData getSongDetail(String songId, String accessToken) throws Exception {
        logger.debug("获取歌曲详情: {}", songId);
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("songId", songId);
        bizContentMap.put("withUrl", true);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongDetailData> response = neteaseCloudMusicAPI.getSongDetail(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取歌曲详情失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        SongDetailData data = response.getData();
        logger.debug("歌曲详情获取成功: songId={}, freeTrail={}", 
                songId, data.getFreeTrail() != null ? "存在" : "不存在");
        
        return data;
    }
    
    /**
     * 批量并发获取歌曲详情并转换为Music对象（过滤无playUrl的歌曲）
     * 
     * @param records 歌曲记录列表
     * @param accessToken 访问令牌
     * @return 有效的音乐列表（有playUrl的歌曲）
     */
    private List<MusicSearchResponse.Music> batchGetSongDetailsWithRecord(
            List<SongSearchData.SongRecord> records, String accessToken) {
        
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.info("批量并发获取{}首歌曲详情", records.size());
        long startTime = System.currentTimeMillis();
        
        // 创建并发任务列表
        List<CompletableFuture<MusicSearchResponse.Music>> futures = records.stream()
                .map(record -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SongDetailData detail = getSongDetail(record.getId(), accessToken);
                        MusicSearchResponse.Music music = convertToMusic(record, detail);
                        // 只返回有playUrl的歌曲
                        return StringUtils.isNotBlank(music.getPlayUrl()) ? music : null;
                    } catch (Exception e) {
                        logger.error("获取歌曲详情失败: songId={}", record.getId(), e);
                        return null;
                    }
                }, detailExecutor))
                .collect(Collectors.toList());
        
        // 等待所有任务完成并收集结果
        List<MusicSearchResponse.Music> musicList = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // 过滤掉null（失败或无playUrl的）
                .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("批量获取歌曲详情完成，耗时{}ms，有效歌曲{}首", duration, musicList.size());
        
        return musicList;
    }
    
    /**
     * 批量并发获取歌曲详情（仅根据歌曲ID）
     * 
     * @param songIds 歌曲ID列表
     * @param accessToken 访问令牌
     * @return 有效的音乐列表（有playUrl的歌曲）
     */
    private List<MusicSearchResponse.Music> batchGetSongDetailsById(
            List<String> songIds, String accessToken) {
        
        if (songIds == null || songIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.info("批量并发获取{}首歌曲详情", songIds.size());
        long startTime = System.currentTimeMillis();
        
        // 创建并发任务列表
        List<CompletableFuture<MusicSearchResponse.Music>> futures = songIds.stream()
                .map(songId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SongDetailData detail = getSongDetail(songId, accessToken);
                        MusicSearchResponse.Music music = convertToMusicFromDetail(detail);
                        // 只返回有playUrl的歌曲
                        return StringUtils.isNotBlank(music.getPlayUrl()) ? music : null;
                    } catch (Exception e) {
                        logger.error("获取歌曲详情失败: songId={}", songId, e);
                        return null;
                    }
                }, detailExecutor))
                .collect(Collectors.toList());
        
        // 等待所有任务完成并收集结果
        List<MusicSearchResponse.Music> musicList = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // 过滤掉null（失败或无playUrl的）
                .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("批量获取歌曲详情完成，耗时{}ms，有效歌曲{}首", duration, musicList.size());
        
        return musicList;
    }
    
    /**
     * 构建公共请求参数
     */
    private NeteaseCloudCommonRequest buildCommonRequest() throws Exception {
        NeteaseCloudCommonRequest request = new NeteaseCloudCommonRequest();
        request.setAppId(appId);
        request.setSignType(SIGN_TYPE);
        request.setTimestamp(System.currentTimeMillis());
        request.setEncryptType("server");
        
        // 构建设备信息
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("channel", "hojo");
        deviceMap.put("deviceId", "321");
        deviceMap.put("deviceType", "andrcar");
        deviceMap.put("appVer", "0.1");
        deviceMap.put("os", "andrcar");
        deviceMap.put("osVer", "8.1.0");
        deviceMap.put("brand", "hojo");
        deviceMap.put("model", "kys");
        deviceMap.put("clientIp", "192.168.0.1");
        request.setDevice(objectMapper.writeValueAsString(deviceMap));
        
        return request;
    }
    
    /**
     * 转换为Music对象
     */
    private MusicSearchResponse.Music convertToMusic(SongSearchData.SongRecord record, SongDetailData detail) {
        MusicSearchResponse.Music music = new MusicSearchResponse.Music();
        
        music.setSongId(record.getId());
        music.setSongName(record.getName());
        music.setDuration(record.getDuration());
        music.setCoverImgUrl(record.getCoverImgUrl());
        music.setVisible(record.getVisible());
        music.setPlayFlag(record.getPlayFlag());
        
        // 专辑信息
        if (record.getAlbum() != null) {
            music.setAlbumId(record.getAlbum().getId());
            music.setAlbumName(record.getAlbum().getName());
        }
        
        // 艺人信息
        if (record.getArtists() != null && !record.getArtists().isEmpty()) {
            List<MusicSearchResponse.Artist> artistList = record.getArtists().stream()
                    .map(a -> {
                        MusicSearchResponse.Artist artist = new MusicSearchResponse.Artist();
                        artist.setArtistId(a.getId());
                        artist.setArtistName(a.getName());
                        return artist;
                    })
                    .collect(Collectors.toList());
            music.setArtistList(artistList);
            
            // 设置主艺人
            music.setAlbumArtistId(artistList.get(0).getArtistId());
            music.setAlbumArtistName(artistList.get(0).getArtistName());
        }
        
        // 从详情中获取更多信息
        if (detail != null) {
            music.setPlayUrl(detail.getPlayUrl());
            music.setSize(detail.getSongSize());
            music.setBr(detail.getBr());
            music.setLevel(detail.getLevel());
            music.setEffects(detail.getEffects());
            
            // 试听时间（来自FreeTrail）
            if (detail.getFreeTrail() != null) {
                music.setStart(detail.getFreeTrail().getStart());
                music.setEnd(detail.getFreeTrail().getEnd());
                logger.debug("歌曲 {} 试听时间: start={}, end={}", 
                        record.getName(), 
                        detail.getFreeTrail().getStart(), 
                        detail.getFreeTrail().getEnd());
            } else {
                logger.debug("歌曲 {} 没有试听时间(freeTrail)数据", record.getName());
            }
        }
        
        return music;
    }
    
    /**
     * 从歌曲详情转换为Music对象
     */
    private MusicSearchResponse.Music convertToMusicFromDetail(SongDetailData detail) {
        MusicSearchResponse.Music music = new MusicSearchResponse.Music();
        
        music.setSongId(detail.getId());
        music.setSongName(detail.getName());
        music.setDuration(detail.getDuration());
        music.setCoverImgUrl(detail.getCoverImgUrl());
        music.setVisible(detail.getVisible());
        music.setPlayFlag(detail.getPlayFlag());
        music.setPlayUrl(detail.getPlayUrl());
        music.setSize(detail.getSongSize());
        music.setBr(detail.getBr());
        music.setLevel(detail.getLevel());
        music.setEffects(detail.getEffects());
        
        // 专辑信息
        music.setAlbumId(detail.getAlbumId());
        music.setAlbumName(detail.getAlbumName());
        music.setAlbumArtistId(detail.getAlbumArtistId());
        music.setAlbumArtistName(detail.getAlbumArtistName());
        
        // 艺人信息
        if (detail.getArtists() != null && !detail.getArtists().isEmpty()) {
            List<MusicSearchResponse.Artist> artistList = detail.getArtists().stream()
                    .map(a -> {
                        MusicSearchResponse.Artist artist = new MusicSearchResponse.Artist();
                        artist.setArtistId(a.getId());
                        artist.setArtistName(a.getName());
                        return artist;
                    })
                    .collect(Collectors.toList());
            music.setArtistList(artistList);
        }
        
        // 试听时间
        if (detail.getFreeTrail() != null) {
            music.setStart(detail.getFreeTrail().getStart());
            music.setEnd(detail.getFreeTrail().getEnd());
        }
        
        return music;
    }
    
    /**
     * 多槽位搜索
     */
    private MultiSearchData multiSearch(FuzzySearchRequest request, String accessToken) throws Exception {
        logger.info("调用多槽位搜索API");
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("mediaName", request.getMediaName());
        if (StringUtils.isNotBlank(request.getMediaType())) {
            bizContentMap.put("mediaType", request.getMediaType());
        }
        if (StringUtils.isNotBlank(request.getArtistName())) {
            bizContentMap.put("artistName", request.getArtistName());
        }
        if (StringUtils.isNotBlank(request.getAlbumName())) {
            bizContentMap.put("albumName", request.getAlbumName());
        }
        if (StringUtils.isNotBlank(request.getSortOrder())) {
            bizContentMap.put("sortOrder", request.getSortOrder());
        }
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<MultiSearchData> response = neteaseCloudMusicAPI.multiSearch(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("多槽位搜索失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData();
    }
    
    /**
     * 从多槽位搜索结果中提取歌曲ID列表
     */
    private List<String> extractSongIds(MultiSearchData data, String mediaType, String accessToken) throws Exception {
        List<String> songIds = new ArrayList<>();
        
        // 1. 归一化 mediaType（空视为开放泛词 "0"）
        // 2. 按类型从 songs / 首张专辑 / 首张歌单等提取 ID，必要时再调专辑曲目、歌单曲目接口
        // 3. 艺人类型（3）在缺艺人列表时按「歌曲 → 专辑 → 歌单」优先级兜底
        
        // 默认mediaType为0
        if (StringUtils.isBlank(mediaType)) {
            mediaType = "0";
        }
        
        switch (mediaType) {
            case "0": // 开放泛词（默认）
            case "1": // 歌曲
                // 直接从songs列表取前10首歌曲的ID
                if (data.getSongs() != null && !data.getSongs().isEmpty()) {
                    songIds = data.getSongs().stream()
                            .limit(30)
                            .map(MultiSearchData.SongItem::getId)
                            .collect(Collectors.toList());
                    logger.info("从歌曲列表提取{}首歌曲ID", songIds.size());
                }
                break;
                
            case "2": // 专辑
                // 根据专辑列表取第一个，调用接口5获取专辑歌曲
                if (data.getAlbums() != null && !data.getAlbums().isEmpty()) {
                    String albumId = data.getAlbums().get(0).getId();
                    logger.info("从专辑{}获取歌曲列表", albumId);
                    List<SongSearchData.SongRecord> albumSongs = getAlbumSongs(albumId, accessToken);
                    songIds = albumSongs.stream()
                            .limit(30)
                            .map(SongSearchData.SongRecord::getId)
                            .collect(Collectors.toList());
                    logger.info("从专辑提取{}首歌曲ID", songIds.size());
                }
                break;
                
            case "3": // 艺人
                // 因返回参数中没有艺人列表，查看其他列表（歌曲优先、其次专辑、歌单）
                if (data.getSongs() != null && !data.getSongs().isEmpty()) {
                    songIds = data.getSongs().stream()
                            .limit(30)
                            .map(MultiSearchData.SongItem::getId)
                            .collect(Collectors.toList());
                    logger.info("艺人搜索-从歌曲列表提取{}首歌曲ID", songIds.size());
                } else if (data.getAlbums() != null && !data.getAlbums().isEmpty()) {
                    String albumId = data.getAlbums().get(0).getId();
                    logger.info("艺人搜索-从专辑{}获取歌曲列表", albumId);
                    List<SongSearchData.SongRecord> albumSongs = getAlbumSongs(albumId, accessToken);
                    songIds = albumSongs.stream()
                            .limit(30)
                            .map(SongSearchData.SongRecord::getId)
                            .collect(Collectors.toList());
                    logger.info("艺人搜索-从专辑提取{}首歌曲ID", songIds.size());
                } else if (data.getPlaylists() != null && !data.getPlaylists().isEmpty()) {
                    String playlistId = data.getPlaylists().get(0).getId();
                    logger.info("艺人搜索-从歌单{}获取歌曲列表", playlistId);
                    List<SongSearchData.SongRecord> playlistSongs = getPlaylistSongs(playlistId, accessToken);
                    songIds = playlistSongs.stream()
                            .limit(30)
                            .map(SongSearchData.SongRecord::getId)
                            .collect(Collectors.toList());
                    logger.info("艺人搜索-从歌单提取{}首歌曲ID", songIds.size());
                }
                break;
                
            case "5": // 歌单
                // 根据歌单列表取第一个，调用接口8获取歌单歌曲
                if (data.getPlaylists() != null && !data.getPlaylists().isEmpty()) {
                    String playlistId = data.getPlaylists().get(0).getId();
                    logger.info("从歌单{}获取歌曲列表", playlistId);
                    List<SongSearchData.SongRecord> playlistSongs = getPlaylistSongs(playlistId, accessToken);
                    songIds = playlistSongs.stream()
                            .limit(30)
                            .map(SongSearchData.SongRecord::getId)
                            .collect(Collectors.toList());
                    logger.info("从歌单提取{}首歌曲ID", songIds.size());
                }
                break;
                
            default:
                logger.warn("未知的mediaType: {}", mediaType);
        }
        
        return songIds;
    }
    
    /**
     * 根据专辑ID获取歌曲列表
     */
    private List<SongSearchData.SongRecord> getAlbumSongs(String albumId, String accessToken) throws Exception {
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("albumId", albumId);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<SongSearchData> response = neteaseCloudMusicAPI.getAlbumSongList(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取专辑歌曲失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData().getRecords() != null ? response.getData().getRecords() : new ArrayList<>();
    }
    
    /**
     * 根据歌单ID获取歌曲列表
     */
    private List<SongSearchData.SongRecord> getPlaylistSongs(String playlistId, String accessToken) throws Exception {
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        bizContentMap.put("playlistId", playlistId);
        bizContentMap.put("limit", DEFAULT_LIMIT);
        bizContentMap.put("offset", DEFAULT_OFFSET);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<PlaylistSongListData> response = neteaseCloudMusicAPI.getPlaylistSongList(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取歌单歌曲失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData() != null ? response.getData() : new ArrayList<>();
    }
    
    /**
     * 获取榜单列表
     */
    private List<ToplistData> getToplist(String accessToken) throws Exception {
        logger.info("获取榜单列表");
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        commonRequest.setAccessToken(accessToken);
        
        Map<String, Object> bizContentMap = new HashMap<>();
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<java.util.List<ToplistData>> response = neteaseCloudMusicAPI.getToplist(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getAccessToken(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("获取榜单列表失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        return response.getData();
    }
    
    /**
     * 向网易云回传播放行为（开始/结束），用于推荐与版权统计等；结束播放时须带 time、end。
     *
     * @param request 回传参数，字段含义见 {@link PlayDataRecordRequest}
     * @return 是否被平台接受；校验失败 400，未授权或调用失败 500
     */
    public Response<Boolean> playDataRecord(PlayDataRecordRequest request) {
        logger.info("播放数据回传: userId={}, action={}, id={}", 
                request.getUserId(), request.getAction(), request.getId());
        
        try {
            // 参数校验
            if (StringUtils.isBlank(request.getUserId())) {
                return Response.newInstance("400", "参数错误：userId不能为空", false);
            }
            if (StringUtils.isBlank(request.getAction())) {
                return Response.newInstance("400", "参数错误：action不能为空", false);
            }
            if (request.getBitrate() == null) {
                return Response.newInstance("400", "参数错误：bitrate不能为空", false);
            }
            if (request.getFile() == null) {
                return Response.newInstance("400", "参数错误：file不能为空", false);
            }
            if (StringUtils.isBlank(request.getType())) {
                return Response.newInstance("400", "参数错误：type不能为空", false);
            }
            if (request.getStartLogTime() == null) {
                return Response.newInstance("400", "参数错误：startLogTime不能为空", false);
            }
            if (StringUtils.isBlank(request.getId())) {
                return Response.newInstance("400", "参数错误：id不能为空", false);
            }
            
            // 结束播放时需要校验time和end参数
            if ("play".equals(request.getAction())) {
                if (request.getTime() == null) {
                    return Response.newInstance("400", "参数错误：结束播放时time不能为空", false);
                }
                if (StringUtils.isBlank(request.getEnd())) {
                    return Response.newInstance("400", "参数错误：结束播放时end不能为空", false);
                }
            }
            
            // 获取accessToken
            String accessToken = getAccessToken(request.getUserId());
            
            // 构建公共请求参数
            NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
            commonRequest.setAccessToken(accessToken);
            
            // 构建业务参数
            Map<String, Object> bizContentMap = new HashMap<>();
            bizContentMap.put("action", request.getAction());
            bizContentMap.put("bitrate", request.getBitrate());
            bizContentMap.put("file", request.getFile());
            bizContentMap.put("type", request.getType());
            bizContentMap.put("startLogTime", request.getStartLogTime());
            bizContentMap.put("id", request.getId());
            
            // 可选参数
            if (StringUtils.isNotBlank(request.getAlg())) {
                bizContentMap.put("alg", request.getAlg());
            }
            
            // 结束播放的额外参数
            if ("play".equals(request.getAction())) {
                bizContentMap.put("time", request.getTime());
                bizContentMap.put("end", request.getEnd());
            }
            
            commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
            
            // 生成签名
            String sign = signUtil.rsa256Sign(commonRequest);
            commonRequest.setSign(sign);
            
            // 调用API
            NeteaseApiResponse<Boolean> response = neteaseCloudMusicAPI.playDataRecord(
                    commonRequest.getAppId(),
                    commonRequest.getSignType(),
                    commonRequest.getSign(),
                    commonRequest.getTimestamp(),
                    commonRequest.getBizContent(),
                    commonRequest.getAccessToken(),
                    commonRequest.getEncryptType(),
                    commonRequest.getDevice());
            
            if (response == null) {
                logger.error("播放数据回传失败: 响应为空");
                return Response.newInstance("500", "播放数据回传失败: 响应为空", false);
            }
            
            if (response.getCode() != 200) {
                logger.error("播放数据回传失败: code={}, message={}", response.getCode(), response.getMessage());
                return Response.newInstance(String.valueOf(response.getCode()), 
                        "播放数据回传失败: " + response.getMessage(), false);
            }
            
            Boolean result = response.getData() != null ? response.getData() : false;
            logger.info("播放数据回传成功: result={}", result);
            return Response.newInstance("200", "成功", result);
            
        } catch (Exception e) {
            logger.error("播放数据回传失败", e);
            return Response.newInstance("500", "播放数据回传失败: " + e.getMessage(), false);
        }
    }
}

