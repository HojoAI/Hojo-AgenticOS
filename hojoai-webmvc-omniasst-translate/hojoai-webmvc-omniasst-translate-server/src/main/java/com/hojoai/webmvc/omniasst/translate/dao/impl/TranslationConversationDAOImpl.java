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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.webmvc.omniasst.translate.dao.TranslationConversationDAO;
import com.hojoai.webmvc.omniasst.translate.dao.mapper.TranslationConversationMapper;
import com.hojoai.webmvc.omniasst.translate.dao.po.TranslationConversation;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * {@link TranslationConversationDAO} 的 MyBatis-Plus 实现。
 */
@Repository
public class TranslationConversationDAOImpl implements TranslationConversationDAO {

    @Resource
    private TranslationConversationMapper mapper;

    @Override
    public TranslationConversation selectByConversationId(String translationConversationId) {
        LambdaQueryWrapper<TranslationConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId);
        return mapper.selectOne(wrapper);
    }

    @Override
    public int insertConversation(TranslationConversation conversation) {
        return mapper.insert(conversation);
    }

    @Override
    public int updateTitleAndSummary(String translationConversationId, String title, String summary) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getTitle, title)
                .set(TranslationConversation::getSummary, summary);
        return mapper.update(null, wrapper);
    }

    @Override
    public int updateTime(String translationConversationId, Long startTime, Long endTime) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getStartTime, startTime)
                .set(TranslationConversation::getEndTime, endTime);
        return mapper.update(null, wrapper);
    }

    @Override
    public int updateDurationAndStatus(String translationConversationId, Long duration, Integer isActive) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getDuration, duration)
                .set(TranslationConversation::getIsActive, isActive);
        return mapper.update(null, wrapper);
    }

    @Override
    public Page<TranslationConversation> selectConversationPage(Page<TranslationConversation> page, String userId, String title, String sortField, boolean desc) {
        LambdaQueryWrapper<TranslationConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationConversation::getUserId, userId)
                .and(StringUtils.isNotBlank(title), w -> w.like(TranslationConversation::getTitle, title)
                        .or().like(TranslationConversation::getSummary, title));

        if (StringUtils.isNotBlank(sortField)) {
            if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, TranslationConversation::getCreatedAt);
            } else if ("updatedAt".equalsIgnoreCase(sortField) || "updated_at".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, TranslationConversation::getUpdatedAt);
            } else if ("title".equalsIgnoreCase(sortField)) {
                wrapper.orderBy(true, !desc, TranslationConversation::getTitle);
            } else {
                wrapper.orderByDesc(TranslationConversation::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(TranslationConversation::getCreatedAt);
        }

        return mapper.selectPage(page, wrapper);
    }

    @Override
    public int updateTitleAndSummaryByConversationId(String translationConversationId, String title, String summary) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getTitle, title)
                .set(TranslationConversation::getSummary, summary);
        return mapper.update(null, wrapper);
    }
}

