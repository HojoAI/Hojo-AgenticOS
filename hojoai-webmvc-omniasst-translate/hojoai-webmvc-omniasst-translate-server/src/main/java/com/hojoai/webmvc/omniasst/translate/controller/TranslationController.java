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
package com.hojoai.webmvc.omniasst.translate.controller;

import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.translate.service.TranslationConversationService;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationConversationListResponse;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationConversationQueryRequest;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationMessagePageResponse;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationMessageQueryRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 翻译会话 HTTP 接口：分页查询用户会话列表及会话内消息。
 */
@RestController
@RequestMapping("/v0/translation/conversations")
public class TranslationController {

    @Resource
    private TranslationConversationService translationConversationService;

    /**
     * 按用户分页查询翻译会话列表。
     *
     * @param request 查询条件（用户 ID、标题模糊、分页与排序）
     * @return 分页会话摘要列表
     */
    @PostMapping("/list")
    public Response<IPage<TranslationConversationListResponse>> listConversations(@RequestBody TranslationConversationQueryRequest request) {
        IPage<TranslationConversationListResponse> result = translationConversationService.listByUser(request);
        return Response.newInstance(ResponseStatusBasic.OK,result);
    }

    /**
     * 分页查询某会话下的翻译消息，并附带会话元信息（标题、摘要、时长等）。
     *
     * @param request 会话 ID 与分页、排序参数
     * @return 消息分页数据与会话头信息
     */
    @PostMapping("/messages/list")
    public Response<TranslationMessagePageResponse> listMessages(@RequestBody TranslationMessageQueryRequest request) {
        TranslationMessagePageResponse result = translationConversationService.listMessages(request);
        return Response.newInstance(ResponseStatusBasic.OK, result);
    }
}

