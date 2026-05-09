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
package com.hojoai.webmvc.omniasst.translate.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationMessageDAO;
import com.hojoai.webmvc.omniasst.translate.dao.mapper.TranslationMessageMapper;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationMessage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link TranslationMessageDAO} 的 MyBatis-Plus 实现。
 */
@Repository
public class TranslationMessageDAOImpl implements TranslationMessageDAO {

    @Resource
    private TranslationMessageMapper mapper;

    @Override
    public TranslationMessage selectByConversationIdAndTimestamp(String translationConversationId, Long timestamp) {
        LambdaQueryWrapper<TranslationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationMessage::getTranslationConversationId, translationConversationId)
                .eq(TranslationMessage::getTimestamp, timestamp);
        return mapper.selectOne(wrapper);
    }

    @Override
    public int insertMessage(TranslationMessage message) {
        return mapper.insert(message);
    }

    @Override
    public Page<TranslationMessage> selectMessagePage(Page<TranslationMessage> page, String translationConversationId, String sortField, boolean desc) {
        LambdaQueryWrapper<TranslationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationMessage::getTranslationConversationId, translationConversationId);

        if (StringUtils.isNotBlank(sortField)) {
            if ("timestamp".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, desc, TranslationMessage::getTimestamp).orderByAsc(TranslationMessage::getId);
            } else if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, TranslationMessage::getCreatedAt);
            } else {
                wrapper.orderByDesc(TranslationMessage::getTimestamp).orderByAsc(TranslationMessage::getId);
            }
        } else {
            wrapper.orderByDesc(TranslationMessage::getTimestamp).orderByAsc(TranslationMessage::getId);
        }

        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<TranslationMessage> selectByConversationId(String translationConversationId) {
        LambdaQueryWrapper<TranslationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationMessage::getTranslationConversationId, translationConversationId)
                .orderByAsc(TranslationMessage::getTimestamp)
                .orderByAsc(TranslationMessage::getId);
        return mapper.selectList(wrapper);
    }
}

