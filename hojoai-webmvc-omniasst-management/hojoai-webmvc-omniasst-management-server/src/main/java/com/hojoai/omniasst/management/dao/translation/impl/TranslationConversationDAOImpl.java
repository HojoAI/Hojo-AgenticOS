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
package com.hojoai.omniasst.management.dao.translation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hojoai.omniasst.management.dao.translation.TranslationConversationDAO;
import com.hojoai.omniasst.management.dao.translation.TranslationConversationMapper;
import com.hojoai.omniasst.management.dao.translation.po.TranslationConversation;
import com.hojoai.omniasst.management.service.translation.impl.TranslationConversationServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * The DAO implementation for translation conversation.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Repository
public class TranslationConversationDAOImpl implements TranslationConversationDAO {

    @Resource
    private TranslationConversationMapper translationConversationMapper;

    @Override
    public TranslationConversation selectByConversationId(String translationConversationId) {
        LambdaQueryWrapper<TranslationConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId);
        return translationConversationMapper.selectOne(wrapper);
    }

    @Override
    public int insertConversation(TranslationConversation conversation) {
        return translationConversationMapper.insert(conversation);
    }

    @Override
    public int updateTitleAndSummaryByConversationId(String translationConversationId, String title, String summary) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getTitle, title)
                .set(TranslationConversation::getSummary, summary);
        return translationConversationMapper.update(null, wrapper);
    }

    @Override
    public int updateTimeByConversationId(String translationConversationId, Long startTime, Long endTime) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getStartTime, startTime)
                .set(TranslationConversation::getEndTime, endTime);
        return translationConversationMapper.update(null, wrapper);
    }

    @Override
    public int updateDurationAndStatus(String translationConversationId, Long duration, Integer isActive) {
        LambdaUpdateWrapper<TranslationConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TranslationConversation::getTranslationConversationId, translationConversationId)
                .set(TranslationConversation::getDuration, duration)
                .set(TranslationConversation::getIsActive, isActive);
        return translationConversationMapper.update(null, wrapper);
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

        return translationConversationMapper.selectPage(page, wrapper);
    }
}

