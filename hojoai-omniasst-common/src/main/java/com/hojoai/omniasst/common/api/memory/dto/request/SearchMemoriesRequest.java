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
package com.hojoai.omniasst.common.api.memory.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索记忆请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchMemoriesRequest {

    /** 检索关键词或自然语言查询 */
    private String query;

    /** 用户 ID */
    @JsonProperty("user_id")
    private String userId;

    /** 群组 ID */
    @JsonProperty("group_id")
    private String groupId;

    /** 检索方法：keyword、vector 等 */
    @JsonProperty("retrieve_method")
    private String retrieveMethod;

    /** 参与检索的记忆类型列表 */
    @JsonProperty("memory_types")
    private List<String> memoryTypes;

    /** 返回条数上限 */
    @JsonProperty("top_k")
    private Integer topK;

    /** 时间窗口起始 */
    @JsonProperty("start_time")
    private String startTime;

    /** 时间窗口结束 */
    @JsonProperty("end_time")
    private String endTime;

    /** 向量检索半径等数值参数 */
    private Double radius;

    /** 是否在结果中带元数据 */
    @JsonProperty("include_metadata")
    private Boolean includeMetadata;

    /** 当前时间（部分策略使用） */
    @JsonProperty("current_time")
    private String currentTime;
}
