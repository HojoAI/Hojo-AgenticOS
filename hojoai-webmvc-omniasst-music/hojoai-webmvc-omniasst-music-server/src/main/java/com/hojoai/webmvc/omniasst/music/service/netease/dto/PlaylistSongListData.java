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
 * 歌单歌曲列表响应数据（直接是列表）
 * <p>继承 {@link java.util.ArrayList}，元素类型为 {@link SongSearchData.SongRecord}，与开放平台歌单曲目接口一致。
 * 
 * @author shengxianfei
 * @since 2026-01-14
 */
public class PlaylistSongListData extends java.util.ArrayList<SongSearchData.SongRecord> {
}

