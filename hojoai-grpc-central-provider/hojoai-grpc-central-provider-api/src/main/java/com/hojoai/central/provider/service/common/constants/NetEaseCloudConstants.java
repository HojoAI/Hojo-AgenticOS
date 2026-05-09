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
package com.hojoai.central.provider.service.common.constants;

/**
 *
 * @author My
 * @version 1.0, 2025/12/22
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface NetEaseCloudConstants {

    String SUCCESS = "200";

    // =============================================== 用户 ===============================================

    /**
     * 匿名登录
     */
    String ANONYMOUS_LOGIN_URL = "/openapi/music/basic/oauth2/login/anonymous";

    // =============================================== 推荐 ===============================================

    /**
     * 获取场景音乐标签
     */
    String GET_SCENE_TAGS_URL = "/openapi/music/basic/scene/radio/tags/get";

    /**
     * 获取榜单列表
     */
    String GET_TOPLIST_URL = "/openapi/music/basic/toplist/get/v2";

    /**
     * 获取场景音乐标签下的歌曲
     */
    String GET_RADIO_BY_TAG_URL = "/openapi/music/basic/scene/radio/get";

    // =============================================== 歌曲 ===============================================

    /**
     * 获取播放歌曲url
     */
    String GET_SONG_PLAY_URL = "/openapi/music/basic/song/playurl/get/v2";

    /**
     * 批量获取播放歌曲url
     */
    String GET_BATCH_SONG_PLAY_URL = "/openapi/music/basic/batch/song/playurl/get";

    /**
     * 获取歌曲详情
     */
    String GET_SONG_DETAIL_URL = "/openapi/music/basic/song/detail/get/v2";

    /**
     * 批量获取歌曲详情
     */
    String GET_BATCH_SONG_DETAIL_URL = "/openapi/music/basic/song/list/get/v2";

    /**
     * 获取专辑下歌曲列表
     */
    String GET_ALBUM_SONG_URL = "/openapi/music/basic/album/song/list/get/v3";

    /**
     * 获取歌单下歌曲列表
     */
    String GET_PLAYLIST_SONG_URL = "/openapi/music/basic/playlist/song/list/get/v3";

    // =============================================== 搜索 ===============================================

    /**
     * 根据关键字搜索艺人
     */
    String GET_ARTIST_URL = "/openapi/music/basic/search/artists/get/v2";

    /**
     * 根据关键字搜索专辑
     */
    String GET_ALBUM_URL = "/openapi/music/basic/search/album/get/v2";

    /**
     * 根据艺人、专辑关键字搜索专辑列表
     */
    String GET_ARTIST_BY_ALBUM_ARTIST_URL = "/openapi/music/basic/search/song/by/album/artist/get/v2";

    /**
     * 根据关键字搜索歌曲
     */
    String GET_SONG_URL = "/openapi/music/basic/search/song/get/v3";

    /**
     * 根据艺人关键字搜索歌曲
     */
    String GET_SONG_BY_ARTIST_URL = "/openapi/music/basic/search/song/byartist/get/v2";

    /**
     * 根据歌曲名、艺人名搜索歌曲信息
     */
    String GET_SONG_INFO_URL = "/openapi/music/basic/search/song/by/artist/song/get/v2";

    /**
     * 根据多槽位搜索歌曲
     */
    String GET_SONG_INFO_BY_ASSISTANT_MULTI_URL = "/openapi/music/basic/voice/assistant/multi/search";

    // =============================================== 艺人 ===============================================

    /**
     * 获取艺人下的热门歌曲列表
     */
    String GET_HOT_SONG_BY_ARTIST_URL = "/openapi/music/basic/artists/hotsong/list/get/v2";

}
