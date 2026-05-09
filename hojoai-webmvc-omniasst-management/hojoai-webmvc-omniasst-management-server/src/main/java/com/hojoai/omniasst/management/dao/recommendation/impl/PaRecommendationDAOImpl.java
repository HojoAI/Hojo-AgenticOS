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
package com.hojoai.omniasst.management.dao.recommendation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.omniasst.management.common.RecommendationStatusEnum;
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationDAO;
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationMapper;
import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendation;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PA 推荐记录数据访问实现（MyBatis-Plus）。
 */
@Repository
public class PaRecommendationDAOImpl implements PaRecommendationDAO {

    @Resource
    private PaRecommendationMapper mapper;

    @Override
    public void insert(PaRecommendation recommendation) {
        mapper.insert(recommendation);
    }

    @Override
    public PaRecommendation selectByRecommendationId(String recommendationId) {
        LambdaQueryWrapper<PaRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendation::getRecommendationId, recommendationId);
        return mapper.selectOne(wrapper);
    }

    @Override
    public PaRecommendation selectActiveByUserTypeRelated(String userId, String type, String relatedId) {
        LambdaQueryWrapper<PaRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendation::getUserId, userId)
               .eq(PaRecommendation::getType, type)
               .eq(PaRecommendation::getRelatedId, relatedId)
               .eq(PaRecommendation::getStatus, RecommendationStatusEnum.ACTIVE.getValue());
        return mapper.selectOne(wrapper);
    }

    @Override
    public List<PaRecommendation> selectLatestPerType(String userId, List<String> suppressedTypes, List<String> filterTypes) {
        LambdaQueryWrapper<PaRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendation::getUserId, userId)
               .eq(PaRecommendation::getStatus, RecommendationStatusEnum.ACTIVE.getValue())
               .and(w -> w.isNull(PaRecommendation::getExpiresAt).or().gt(PaRecommendation::getExpiresAt, new Date()));

        if (CollectionUtils.isNotEmpty(suppressedTypes)) {
            wrapper.notIn(PaRecommendation::getType, suppressedTypes);
        }
        if (CollectionUtils.isNotEmpty(filterTypes)) {
            wrapper.in(PaRecommendation::getType, filterTypes);
        }

        wrapper.orderByDesc(PaRecommendation::getCreatedAt);
        List<PaRecommendation> all = mapper.selectList(wrapper);

        // 按 type 分组，每组取第一条（已按 created_at 降序，即最新的）
        Map<String, PaRecommendation> grouped = new LinkedHashMap<>();
        for (PaRecommendation item : all) {
            grouped.putIfAbsent(item.getType(), item);
        }
        return new ArrayList<>(grouped.values());
    }

    @Override
    public long countIgnoredSince(String userId, String type, Date since) {
        LambdaQueryWrapper<PaRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendation::getUserId, userId)
               .eq(PaRecommendation::getType, type)
               .eq(PaRecommendation::getStatus, RecommendationStatusEnum.IGNORED.getValue())
               .ge(PaRecommendation::getIgnoredAt, since);
        return mapper.selectCount(wrapper);
    }

    @Override
    public int updateStatusToIgnored(String recommendationId, Date ignoredAt) {
        LambdaUpdateWrapper<PaRecommendation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PaRecommendation::getRecommendationId, recommendationId)
               .eq(PaRecommendation::getStatus, RecommendationStatusEnum.ACTIVE.getValue())
               .set(PaRecommendation::getStatus, RecommendationStatusEnum.IGNORED.getValue())
               .set(PaRecommendation::getIgnoredAt, ignoredAt);
        return mapper.update(null, wrapper);
    }
}
