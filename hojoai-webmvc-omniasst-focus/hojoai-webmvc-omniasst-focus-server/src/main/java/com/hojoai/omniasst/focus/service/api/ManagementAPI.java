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
package com.hojoai.omniasst.focus.service.api;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.common.api.conversation.UpdateMessageExtraRequest;
import com.hojoai.omniasst.focus.service.api.bean.CreateRecommendationRequest;
import com.hojoai.omniasst.focus.service.api.bean.DeleteRecommendationRequest;
import com.hojoai.omniasst.focus.service.api.bean.PreSignUrlResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hojoai-webmvc-omniasst-management")
public interface ManagementAPI {

    @PostMapping("/omniasst-management/api/v0/conversations/message/extra/update")
    ResponseEntity<Response<Boolean>> createConversationById(@RequestBody UpdateMessageExtraRequest request);

    @GetMapping("/omniasst-management/api/v0/storage/presign")
    Response<PreSignUrlResponse> getPresignUrl(@RequestParam("path") String path,
            @RequestParam(name = "contentType", required = false, defaultValue = "") String contentType,
            @RequestParam(name = "style", required = false, defaultValue = "") String style);

    /**
     * 创建「为你推荐」条目（日程模块在合适时机调用，type 固定为 "schedule"）
     */
    @PostMapping("/omniasst-management/api/v0/recommendations")
    ResponseEntity<Response<String>> createRecommendation(@RequestBody CreateRecommendationRequest request);

    /**
     * 删除「为你推荐」条目（日程发生变更或删除时调用）
     */
    @PostMapping("/omniasst-management/api/v0/recommendations/delete")
    ResponseEntity<Response<Void>> deleteRecommendation(@RequestBody DeleteRecommendationRequest request);
}