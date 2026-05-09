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
package com.hojoai.omniasst.management.dao.translation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.omniasst.management.dao.translation.po.TranslationConversation;

/**
 * The DAO interface for translation conversation.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface TranslationConversationDAO {

    /**
     * Select translation conversation by conversation id.
     *
     * @param translationConversationId translation conversation id
     * @return translation conversation or null if not exists
     */
    TranslationConversation selectByConversationId(String translationConversationId);

    /**
     * Insert translation conversation.
     *
     * @param conversation translation conversation entity
     * @return affected rows
     */
    int insertConversation(TranslationConversation conversation);

    /**
     * Update title and summary by translation conversation id.
     *
     * @param translationConversationId translation conversation id
     * @param title                     title
     * @param summary                   summary
     * @return affected rows
     */
    int updateTitleAndSummaryByConversationId(String translationConversationId, String title, String summary);

    /**
     * Update start time and end time by translation conversation id.
     *
     * @param translationConversationId translation conversation id
     * @param startTime                 start time (millisecond timestamp)
     * @param endTime                   end time (millisecond timestamp)
     * @return affected rows
     */
    int updateTimeByConversationId(String translationConversationId, Long startTime, Long endTime);

    /**
     * Update duration and is_active status by translation conversation id.
     *
     * @param translationConversationId translation conversation id
     * @param duration                  conversation duration (milliseconds)
     * @param isActive                  is active status (1=active, 0=ended)
     * @return affected rows
     */
    int updateDurationAndStatus(String translationConversationId, Long duration, Integer isActive);

    /**
     * Select translation conversations page by user with optional title fuzzy and sorting.
     *
     * @param page      page object
     * @param userId    user id
     * @param title     title keyword (nullable)
     * @param sortField sort field
     * @param desc      sort order, true for desc
     * @return page result
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranslationConversation> selectConversationPage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranslationConversation> page,
            String userId,
            String title,
            String sortField,
            boolean desc);
}

