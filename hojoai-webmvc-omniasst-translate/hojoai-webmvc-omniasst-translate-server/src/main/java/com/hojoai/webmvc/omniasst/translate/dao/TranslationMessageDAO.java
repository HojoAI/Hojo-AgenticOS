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
package com.hojoai.webmvc.omniasst.translate.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationMessage;

import java.util.List;

/**
 * 翻译消息数据访问：按会话与时间戳去重、分页查询及全量拉取用于 LLM 摘要。
 */
public interface TranslationMessageDAO {

    /**
     * 按会话 ID 与消息时间戳查询，用于幂等插入前判重。
     *
     * @param translationConversationId 会话 ID
     * @param timestamp                 消息时间戳
     * @return 已存在消息或 null
     */
    TranslationMessage selectByConversationIdAndTimestamp(String translationConversationId, Long timestamp);

    /**
     * 插入一条翻译消息。
     *
     * @param message 消息实体
     * @return 影响行数
     */
    int insertMessage(TranslationMessage message);

    /**
     * 分页查询某会话下的消息。
     *
     * @param page                      分页参数
     * @param translationConversationId 会话 ID
     * @param sortField                 排序字段
     * @param desc                      是否降序
     * @return 分页结果
     */
    Page<TranslationMessage> selectMessagePage(
            Page<TranslationMessage> page,
            String translationConversationId,
            String sortField,
            boolean desc);

    /**
     * 查询会话下全部消息（按时间升序），供生成标题摘要。
     *
     * @param translationConversationId 会话 ID
     * @return 消息列表
     */
    List<TranslationMessage> selectByConversationId(String translationConversationId);
}
