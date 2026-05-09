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
package com.hojoai.omniasst.management.service.recommendation;

import com.hojoai.omniasst.management.service.recommendation.bean.CreateRecommendationRequest;
import com.hojoai.omniasst.management.service.recommendation.bean.RecommendationItemResponse;

import java.util.List;

/**
 * 为你推荐 - 服务接口
 *
 * @author David
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
public interface RecommendationService {

    /**
     * 查询用户的推荐列表，每种类型最多返回一条
     *
     * @param userId 用户ID
     * @param types  类型过滤（可选），为空时返回所有类型
     */
    List<RecommendationItemResponse> listRecommendations(String userId, List<String> types);

    /**
     * 忽略单条推荐，并触发抑制判断
     */
    void ignoreRecommendation(String userId, String recommendationId);

    /**
     * 创建推荐条目（供业务模块调用），自动去重
     *
     * @return 推荐ID
     */
    String createRecommendation(CreateRecommendationRequest request);
}
