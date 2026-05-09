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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储消息响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreMessageResponse {

    /** 已落库的记忆片段（结构由服务端决定，可能为数组或对象） */
    @JsonProperty("saved_memories")
    private Object savedMemories;

    /** 本次写入关联的记忆条数 */
    private int count;

    /** 处理状态说明，如 accumulated 表示尚在聚合未出记忆 */
    @JsonProperty("status_info")
    private String statusInfo;
}
