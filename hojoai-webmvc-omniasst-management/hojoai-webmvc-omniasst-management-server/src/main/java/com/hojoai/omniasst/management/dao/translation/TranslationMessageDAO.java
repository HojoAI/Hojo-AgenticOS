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

import com.hojoai.omniasst.management.dao.translation.po.TranslationMessage;

/**
 * The DAO interface for translation message.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface TranslationMessageDAO {

    /**
     * Select translation message by conversation id and timestamp.
     *
     * @param translationConversationId translation conversation id
     * @param timestamp                 message timestamp
     * @return translation message or null if not exists
     */
    TranslationMessage selectByConversationIdAndTimestamp(String translationConversationId, Long timestamp);

    /**
     * Insert translation message.
     *
     * @param message translation message entity
     * @return affected rows
     */
    int insertMessage(TranslationMessage message);

    /**
     * Select translation messages page by conversation id with sorting.
     *
     * @param page                     page object
     * @param translationConversationId conversation id
     * @param sortField                sort field
     * @param desc                     sort order, true for desc
     * @return page result
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranslationMessage> selectMessagePage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<TranslationMessage> page,
            String translationConversationId,
            String sortField,
            boolean desc);

    /**
     * Select all translation messages by conversation id ordered by timestamp.
     *
     * @param translationConversationId conversation id
     * @return list of translation messages ordered by timestamp
     */
    java.util.List<TranslationMessage> selectByConversationId(String translationConversationId);
}

