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
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationSuppressDAO;
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationSuppressMapper;
import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendationSuppress;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * PA 推荐「重复曝光抑制」记录数据访问实现。
 */
@Repository
public class PaRecommendationSuppressDAOImpl implements PaRecommendationSuppressDAO {

    @Resource
    private PaRecommendationSuppressMapper mapper;

    @Override
    public List<String> selectSuppressedTypes(String userId) {
        LambdaQueryWrapper<PaRecommendationSuppress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendationSuppress::getUserId, userId)
               .gt(PaRecommendationSuppress::getSuppressedUntil, new Date())
               .select(PaRecommendationSuppress::getType);
        return mapper.selectList(wrapper).stream()
                .map(PaRecommendationSuppress::getType)
                .toList();
    }

    @Override
    public PaRecommendationSuppress selectByUserIdAndType(String userId, String type) {
        LambdaQueryWrapper<PaRecommendationSuppress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaRecommendationSuppress::getUserId, userId)
               .eq(PaRecommendationSuppress::getType, type);
        return mapper.selectOne(wrapper);
    }

    @Override
    public void insert(PaRecommendationSuppress suppress) {
        mapper.insert(suppress);
    }

    @Override
    public void updateById(PaRecommendationSuppress suppress) {
        mapper.updateById(suppress);
    }
}
