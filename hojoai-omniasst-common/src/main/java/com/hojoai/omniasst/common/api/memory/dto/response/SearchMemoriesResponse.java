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
package com.hojoai.omniasst.common.api.memory.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hojoai.omniasst.common.api.memory.dto.Memory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 搜索记忆响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchMemoriesResponse {

    /** 按类型或分组组织的记忆命中列表 */
    private List<Map<String, List<Memory>>> memories;

    /** 与各记忆条目对应的相似度或排序分数 */
    private List<Map<String, List<Double>>> scores;

    /** 重要性分数列表（若服务端返回） */
    @JsonProperty("importance_scores")
    private List<Double> importanceScores;

    /** 原始检索数据或调试信息 */
    @JsonProperty("original_data")
    private List<Object> originalData;

    /** 命中总条数 */
    @JsonProperty("total_count")
    private int totalCount;

    /** 是否还有更多结果 */
    @JsonProperty("has_more")
    private boolean hasMore;

    /** 与本次查询相关的元信息 */
    @JsonProperty("query_metadata")
    private Map<String, Object> queryMetadata;

    /** 通用元数据 */
    private Map<String, Object> metadata;

    /** 尚未写入记忆、仍在队列中的消息 */
    @JsonProperty("pending_messages")
    private List<Object> pendingMessages;
}
