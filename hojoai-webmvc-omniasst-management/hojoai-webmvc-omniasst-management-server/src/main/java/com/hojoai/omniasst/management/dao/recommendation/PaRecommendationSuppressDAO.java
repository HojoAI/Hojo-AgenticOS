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

import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendationSuppress;

import java.util.List;

public interface PaRecommendationSuppressDAO {

    /**
     * 查询用户当前仍在抑制期内的类型列表
     */
    List<String> selectSuppressedTypes(String userId);

    PaRecommendationSuppress selectByUserIdAndType(String userId, String type);

    void insert(PaRecommendationSuppress suppress);

    void updateById(PaRecommendationSuppress suppress);
}
