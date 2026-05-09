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

import com.hojoai.webmvc.omniasst.translate.websocket.bean.dto.TranslateResultMessage;

/**
 * 翻译结果持久化与下游通知：将 WebSocket 产生的翻译结果写入数据库，并在会话结束时生成摘要、推送客户端。
 */
public interface TranslationPersistenceService {

    /**
     * 异步持久化单次翻译结果或会话结束事件；会话结束时会抢分布式锁并触发标题摘要生成与推送。
     *
     * @param message         WebSocket 侧组装的翻译结果或连接结束事件
     * @param conversationId  业务侧会话 ID（用于推送），可为空
     */
    void persistTranslateResult(TranslateResultMessage message, String conversationId);
}

