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
package com.hojoai.omniasst.management.dao.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.message.PaMessageDAO;
import com.hojoai.omniasst.management.dao.message.PaMessageMapper;
import com.hojoai.omniasst.management.dao.message.po.PaMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 消息中心 - 消息 DAO 实现
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class PaMessageDAOImpl implements PaMessageDAO {

    @Resource
    private PaMessageMapper paMessageMapper;

    @Override
    public void insert(PaMessage message) {
        paMessageMapper.insert(message);
    }

    @Override
    public PaMessage selectByMessageId(String messageId) {
        LambdaQueryWrapper<PaMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessage::getMessageId, messageId);
        return paMessageMapper.selectOne(wrapper);
    }

    @Override
    public Page<PaMessage> selectPageByUserId(Page<PaMessage> page, String userId) {
        LambdaQueryWrapper<PaMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessage::getUserId, userId)
               .orderByDesc(PaMessage::getCreatedAt);
        return paMessageMapper.selectPage(page, wrapper);
    }

    @Override
    public long countUnreadByUserId(String userId) {
        LambdaQueryWrapper<PaMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessage::getUserId, userId)
               .isNull(PaMessage::getReadAt);
        return paMessageMapper.selectCount(wrapper);
    }

    @Override
    public int markAsRead(String userId, String messageId) {
        LambdaUpdateWrapper<PaMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PaMessage::getUserId, userId)
               .eq(PaMessage::getMessageId, messageId)
               .isNull(PaMessage::getReadAt)
               .set(PaMessage::getReadAt, new Date());
        return paMessageMapper.update(null, wrapper);
    }

    @Override
    public int batchMarkAsRead(String userId, List<String> messageIds) {
        LambdaUpdateWrapper<PaMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PaMessage::getUserId, userId)
               .in(PaMessage::getMessageId, messageIds)
               .isNull(PaMessage::getReadAt)
               .set(PaMessage::getReadAt, new Date());
        return paMessageMapper.update(null, wrapper);
    }

    @Override
    public int deleteAllByUserId(String userId) {
        LambdaQueryWrapper<PaMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaMessage::getUserId, userId);
        return paMessageMapper.delete(wrapper);
    }
}
