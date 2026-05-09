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
package com.hojoai.omniasst.management.controller.recommendation;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.recommendation.RecommendationService;
import com.hojoai.omniasst.management.service.recommendation.bean.CreateRecommendationRequest;
import com.hojoai.omniasst.management.service.recommendation.bean.RecommendationItemResponse;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 为你推荐
 *
 * @author David
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/recommendations")
public class RecommendationController {

    @Resource
    private RecommendationService recommendationService;

    /**
     * 推荐列表（每种类型最多返回一条）
     */
    @GetMapping
    public Response<List<RecommendationItemResponse>> list(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(value = "types", required = false) String types) {
        Asserts.notBlank(userId, "userId");

        List<String> typeFilter = null;
        if (StringUtils.isNotBlank(types)) {
            typeFilter = Arrays.asList(types.split(","));
        }

        List<RecommendationItemResponse> result = recommendationService.listRecommendations(userId, typeFilter);
        return ResultUtils.Ok(result);
    }

    /**
     * 忽略单条推荐
     */
    @PostMapping("/{recommendationId}/ignore")
    public Response<Void> ignore(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("recommendationId") String recommendationId) {
        Asserts.notBlank(userId, "userId");
        Asserts.notBlank(recommendationId, "recommendationId");

        recommendationService.ignoreRecommendation(userId, recommendationId);
        return ResultUtils.Ok();
    }

    /**
     * 创建推荐条目（供业务模块调用）
     */
    @PostMapping
    public Response<String> create(@RequestBody CreateRecommendationRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getType(), "type");
        Asserts.notBlank(request.getTitle(), "title");

        String recId = recommendationService.createRecommendation(request);
        return ResultUtils.Ok(recId);
    }
}
