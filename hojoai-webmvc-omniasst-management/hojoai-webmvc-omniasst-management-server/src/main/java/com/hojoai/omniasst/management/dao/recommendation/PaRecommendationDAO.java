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
package com.hojoai.omniasst.management.dao.recommendation;

import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendation;

import java.util.Date;
import java.util.List;

public interface PaRecommendationDAO {

    void insert(PaRecommendation recommendation);

    PaRecommendation selectByRecommendationId(String recommendationId);

    /**
     * 查询同一用户+类型+关联业务的 active 推荐（用于去重）
     */
    PaRecommendation selectActiveByUserTypeRelated(String userId, String type, String relatedId);

    /**
     * 查询用户可展示的推荐列表：active、未过期、不在被抑制的类型中。
     * 每种 type 只取最新一条。
     */
    List<PaRecommendation> selectLatestPerType(String userId, List<String> suppressedTypes, List<String> filterTypes);

    /**
     * 统计用户某类型在指定时间之后的忽略次数
     */
    long countIgnoredSince(String userId, String type, Date since);

    int updateStatusToIgnored(String recommendationId, Date ignoredAt);
}
