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
package com.hojoai.webmvc.omniasst.translate.service;

import com.hojoai.common.page.IPage;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationConversationListResponse;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationConversationQueryRequest;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationMessagePageResponse;
import com.hojoai.webmvc.omniasst.translate.service.dto.TranslationMessageQueryRequest;

/**
 * 翻译会话查询服务：面向前端的会话与消息分页查询。
 */
public interface TranslationConversationService {

    /**
     * 按用户分页查询翻译会话列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<TranslationConversationListResponse> listByUser(TranslationConversationQueryRequest request);

    /**
     * 分页查询指定会话下的消息，并组装会话概要信息。
     *
     * @param request 会话与分页参数
     * @return 消息分页与会话信息
     */
    TranslationMessagePageResponse listMessages(TranslationMessageQueryRequest request);
}

