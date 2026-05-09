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
import java.util.Map;

/**
 * 更新对话元数据请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConversationMetaRequest {

    /** 待更新的群组 ID */
    @JsonProperty("group_id")
    private String groupId;

    /** 新名称，可选 */
    private String name;

    /** 新描述，可选 */
    private String description;

    /** 场景描述增量，可选 */
    @JsonProperty("scene_desc")
    private Map<String, Object> sceneDesc;

    /** 标签全量或增量由服务端约定，此处为列表形态 */
    private List<String> tags;

    /** 用户详情映射，可选 */
    @JsonProperty("user_details")
    private Map<String, ConversationMetaRequest.UserDetail> userDetails;

    /** 默认时区，可选 */
    @JsonProperty("default_timezone")
    private String defaultTimezone;
}
