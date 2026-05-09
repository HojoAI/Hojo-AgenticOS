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
 * 对话元数据请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMetaRequest {

    /** 元数据版本号 */
    private String version;

    /** 场景标识，如 assistant */
    private String scene;

    /** 场景扩展描述，结构由业务约定 */
    @JsonProperty("scene_desc")
    private Map<String, Object> sceneDesc;

    /** 对话或会话展示名称 */
    private String name;

    /** 描述文案 */
    private String description;

    /** 群组 ID */
    @JsonProperty("group_id")
    private String groupId;

    /** 创建时间（ISO-8601） */
    @JsonProperty("created_at")
    private String createdAt;

    /** 默认时区，如 Asia/Shanghai */
    @JsonProperty("default_timezone")
    private String defaultTimezone;

    /** 参与方用户 ID 到详情的映射 */
    @JsonProperty("user_details")
    private Map<String, UserDetail> userDetails;

    /** 标签列表，用于分类或检索 */
    private List<String> tags;

    /**
     * 用户详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDetail {
        
        /** 展示全名 */
        @JsonProperty("full_name")
        private String fullName;

        /** 角色，如 assistant、user */
        private String role;

        /** 自定义角色文案 */
        @JsonProperty("custom_role")
        private String customRole;

        /** 业务扩展字段 */
        private Map<String, Object> extra;
    }
}
