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
package com.hojoai.omniasst.management.redis;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.service.translation.TranslationPersistenceService;
import com.hojoai.omniasst.management.service.translation.bean.TranslateResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The handler for translate result channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Component
public class TranslateResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TranslateResultHandler.class);

    @Resource
    private TranslationPersistenceService translationPersistenceService;

    /**
     * Handle translate result message json.
     *
     * @param messageJson raw json string from Redis channel
     */
    public void handle(String messageJson) {
         Asserts.notBlank(messageJson, "messageJson");
         TranslateResultMessage message = JacksonUtil.json2Bean(messageJson, TranslateResultMessage.class);
         translationPersistenceService.persistTranslateResult(message);
    }
}
