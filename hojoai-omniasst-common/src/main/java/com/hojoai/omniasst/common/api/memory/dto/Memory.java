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
package com.hojoai.omniasst.common.api.memory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 单条记忆在 API 中的传输模型，对应检索/推送返回中的记忆条目。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Memory {

    /** 记忆类型标识，与 {@link MemoryType} 中 JSON 取值一致 */
    @JsonProperty("memory_type")
    private String memoryType;

    /** 所属用户 ID */
    @JsonProperty("user_id")
    private String userId;

    /** 记忆发生或记录时间 */
    private String timestamp;

    /** 原始内容片段 */
    private String content;

    /** 摘要或压缩表述 */
    private String summary;

    /** 主题/标题类信息 */
    private String subject;

    /** 所属情节或会话片段标识 */
    private String episode;

    /** 群组/会话作用域 ID */
    @JsonProperty("group_id")
    private String groupId;
    
    /** 群组展示名称 */
    @JsonProperty("group_name")
    private String groupName;

    /**
     * Memory 服务支持的记忆类型（请求/查询参数中的 memory_type）。
     */
    public enum MemoryType {

        /** User profile information；用户画像类长期记忆 */
        PROFILE("profile"),

        /** Conversation episodes (default)；情景/对话片段类记忆（默认类型） */
        EPISODIC_MEMORY("episodic_memory"),

        /** Prospective memory；前瞻/计划类记忆 */
        FORESIGHT("foresight"),

        /** Atomic facts；原子事实/事件日志类记忆 */
        EVENT_LOG("event_log"),
        ;

        MemoryType(String memoryTypeName) {
            this.memoryTypeName = memoryTypeName;
        }

        @Getter
        private String memoryTypeName;

        /**
         * @return 默认记忆类型（情景记忆）
         */
        public static Memory.MemoryType getDefaultMemoryType() {
            return MemoryType.EPISODIC_MEMORY;
        }
    }

    /**
     * 存储消息时的发言方角色（映射到 API 的 role 字符串）。
     */
    public enum Role {
        /** 终端用户 */
        HUMAN("user"),
        /** 模型或助手 */
        AI("assistant"),
        ;

        Role(String roleName) {
            this.roleName = roleName;
        }

        /**
         * @return 默认按用户侧发言处理
         */
        public static Role getDefaultRole() {
            return Role.HUMAN;
        }

        @Getter
        private String roleName;
    }

    /**
     * 记忆检索策略，对应 search 接口的 retrieve_method。
     */
    public enum RetrieveMethod {
        /** 关键词检索 */
        KEYWORD("keyword"),
        /** 向量相似度检索 */
        VECTOR("vector"),
        /** 混合检索 */
        HYBRID("hybrid"),
        /** RRF 融合排序 */
        RRF("rrf"),
        /** 智能体驱动检索 */
        AGENTIC("agentic"),
        ;

        RetrieveMethod(String methodName) {
            this.methodName = methodName;
        }

        /**
         * @return 默认检索方式为关键词
         */
        public static RetrieveMethod getDefaultRetrieveMethod() {
            return RetrieveMethod.KEYWORD;
        }

        @Getter
        private String methodName;
    }
}
