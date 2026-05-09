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

import java.util.Map;

/**
 * 对话元数据响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMetaResponse {

    /** 服务端生成的元数据记录 ID */
    private String id;

    /** 群组 ID */
    @JsonProperty("group_id")
    private String groupId;

    /** 场景标识 */
    private String scene;

    /** 展示名称 */
    private String name;

    /** 参与方详情（结构为 Map，便于扩展） */
    @JsonProperty("user_details")
    private Map<String, Object> userDetails;

    /** 是否为默认会话元数据 */
    @JsonProperty("is_default")
    private boolean isDefault;

    /** 描述文案 */
    private String description;

    /** 场景扩展描述 */
    @JsonProperty("scene_desc")
    private Map<String, Object> sceneDesc;

    /** 元数据版本 */
    private String version;

    /** 创建时间 */
    @JsonProperty("created_at")
    private String createdAt;

    /** 默认时区 */
    @JsonProperty("default_timezone")
    private String defaultTimezone;

    /** PATCH 更新时服务端返回的已更新字段名列表 */
    @JsonProperty("updated_fields")
    private String[] updatedFields;

    /** 最后更新时间 */
    @JsonProperty("updated_at")
    private String updatedAt;
}
