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
 * 获取记忆响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FetchMemoriesResponse {

    /** 当前页记忆列表 */
    private List<Memory> memories;

    /** 总条数（或服务端声明的总命中数） */
    @JsonProperty("total_count")
    private int totalCount;

    /** 是否还有下一页 */
    @JsonProperty("has_more")
    private boolean hasMore;

    /** 服务端返回的附加元数据 */
    private Map<String, Object> metadata;
}
