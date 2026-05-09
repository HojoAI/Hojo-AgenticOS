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

/**
 * 删除记忆请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteMemoriesRequest {

    /** 事件 ID；特殊值 {@code __all__} 表示删除该用户在该组下全部记忆 */
    @JsonProperty("event_id")
    private String eventId;

    /** 用户 ID */
    @JsonProperty("user_id")
    private String userId;

    /** 群组 ID */
    @JsonProperty("group_id")
    private String groupId;
}
