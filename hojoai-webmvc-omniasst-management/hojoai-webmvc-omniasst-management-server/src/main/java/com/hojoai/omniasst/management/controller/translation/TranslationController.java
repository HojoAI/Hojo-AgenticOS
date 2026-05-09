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
package com.hojoai.omniasst.management.controller.translation;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.translation.TranslationConversationService;
import com.hojoai.omniasst.management.service.translation.bean.TranslationConversationListResponse;
import com.hojoai.omniasst.management.service.translation.bean.TranslationConversationQueryRequest;
import com.hojoai.omniasst.management.service.translation.bean.TranslationMessagePageResponse;
import com.hojoai.omniasst.management.service.translation.bean.TranslationMessageQueryRequest;
import com.hojoai.omniasst.management.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller for translation conversation and messages.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/translation/conversations")
public class TranslationController {

    @Resource
    private TranslationConversationService translationConversationService;

    /**
     * List translation conversations by user (POST).
     */
    @PostMapping("/list")
    public Response<IPage<TranslationConversationListResponse>> listConversations(@RequestBody TranslationConversationQueryRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        IPage<TranslationConversationListResponse> result = translationConversationService.listByUser(request);
        return ResultUtils.Ok(result);
    }

    /**
     * List translation messages by conversation id with conversation info (POST).
     */
    @PostMapping("/messages/list")
    public Response<TranslationMessagePageResponse> listMessages(@RequestBody TranslationMessageQueryRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getTranslationConversationId(), "translationConversationId");
        TranslationMessagePageResponse result = translationConversationService.listMessages(request);
        return ResultUtils.Ok(result);
    }
}
