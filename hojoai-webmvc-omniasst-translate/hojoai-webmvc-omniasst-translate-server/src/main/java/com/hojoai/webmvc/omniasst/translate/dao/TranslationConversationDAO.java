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
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationConversation;

/**
 * 翻译会话数据访问：按 chatId 查询、分页列表及会话结束后的元数据更新。
 */
public interface TranslationConversationDAO {

    /**
     * 按业务会话 ID 查询单条记录。
     *
     * @param translationConversationId 与 WebSocket chatId 一致的业务 ID
     * @return 会话实体，不存在时为 null
     */
    TranslationConversation selectByConversationId(String translationConversationId);

    /**
     * 插入新会话。
     *
     * @param conversation 会话实体
     * @return 影响行数
     */
    int insertConversation(TranslationConversation conversation);

    /**
     * 更新标题与摘要。
     *
     * @param translationConversationId 会话 ID
     * @param title                       标题
     * @param summary                     摘要
     * @return 影响行数
     */
    int updateTitleAndSummary(String translationConversationId, String title, String summary);

    /**
     * 更新会话起止时间（连接建立/结束时写入）。
     *
     * @param translationConversationId 会话 ID
     * @param startTime                 开始时间戳（毫秒）
     * @param endTime                   结束时间戳（毫秒）
     * @return 影响行数
     */
    int updateTime(String translationConversationId, Long startTime, Long endTime);

    /**
     * 更新会话总时长与是否仍活跃。
     *
     * @param translationConversationId 会话 ID
     * @param duration                  时长（毫秒）
     * @param isActive                  是否活跃（如 0 表示已结束）
     * @return 影响行数
     */
    int updateDurationAndStatus(String translationConversationId, Long duration, Integer isActive);

    /**
     * 按用户分页查询会话，支持标题/摘要模糊与排序。
     *
     * @param page     分页参数
     * @param userId   用户 ID
     * @param title    标题或摘要关键字，可为空
     * @param sortField 排序字段名
     * @param desc     是否降序
     * @return 分页结果
     */
    Page<TranslationConversation> selectConversationPage(
            Page<TranslationConversation> page,
            String userId,
            String title,
            String sortField,
            boolean desc);

    /**
     * 会话结束后由 LLM 生成标题与摘要并回写。
     *
     * @param translationConversationId 会话 ID
     * @param title                       标题
     * @param summary                     摘要
     * @return 影响行数
     */
    int updateTitleAndSummaryByConversationId(String translationConversationId, String title, String summary);
}
