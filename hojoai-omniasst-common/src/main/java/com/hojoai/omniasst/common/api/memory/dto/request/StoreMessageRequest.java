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
 * 写入单条对话消息的请求体，对应 POST /memories。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreMessageRequest {

    /** 消息唯一 ID */
    @JsonProperty("message_id")
    private String messageId;

    /** 消息创建时间，ISO-8601 字符串 */
    @JsonProperty("create_time")
    private String createTime;

    /** 发送方 ID */
    private String sender;

    /** 消息正文 */
    private String content;

    /** 群组/会话作用域 ID（可与业务约定拼接维度） */
    @JsonProperty("group_id")
    private String groupId;

    /** 群组展示名 */
    @JsonProperty("group_name")
    private String groupName;

    /** 发送方展示名 */
    @JsonProperty("sender_name")
    private String senderName;

    /** API 角色字符串，如 user、assistant */
    private String role;

    /** 引用的历史消息 ID 列表 */
    @JsonProperty("refer_list")
    private List<String> referList;
}
