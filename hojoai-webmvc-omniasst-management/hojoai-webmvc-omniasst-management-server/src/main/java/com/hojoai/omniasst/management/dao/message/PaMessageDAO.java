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
package com.hojoai.omniasst.management.dao.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.message.po.PaMessage;

import java.util.List;

/**
 * 消息中心 - 消息 DAO
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface PaMessageDAO {

    void insert(PaMessage message);

    PaMessage selectByMessageId(String messageId);

    /**
     * 分页查询用户的消息列表，按创建时间倒序
     */
    Page<PaMessage> selectPageByUserId(Page<PaMessage> page, String userId);

    /**
     * 统计用户未读消息数
     */
    long countUnreadByUserId(String userId);

    /**
     * 标记单条消息已读
     *
     * @return 影响行数
     */
    int markAsRead(String userId, String messageId);

    /**
     * 批量标记消息已读
     *
     * @return 影响行数
     */
    int batchMarkAsRead(String userId, List<String> messageIds);

    /**
     * 删除指定用户的全部消息
     *
     * @return 影响行数
     */
    int deleteAllByUserId(String userId);
}
