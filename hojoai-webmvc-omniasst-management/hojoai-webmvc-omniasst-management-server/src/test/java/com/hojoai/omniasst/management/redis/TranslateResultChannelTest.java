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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.ISpringApplication;
import com.hojoai.omniasst.management.common.RedisConstants;
import org.junit.jupiter.api.Test;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * The test for publishing translate result message to Redis channel.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@SpringBootTest(classes = ISpringApplication.class)
public class TranslateResultChannelTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void publishTranslateResultSample() {
        Map<String, Object> payload = new HashMap<>(8);
        payload.put("chatId", "C12345967890");
        payload.put("originalText", "Hello everyone, welcome to the launch event.");
        payload.put("translatedText", "大家好，欢迎来到发布会。");
        payload.put("timestamp", 1704067200032L);
        payload.put("sourceLanguage", "en-US");
        payload.put("targetLanguage", "zh-CN");
        payload.put("detectedLanguage", "en-US");
        payload.put("userId", "user_123");

        String json = JacksonUtil.bean2Json(payload);
        RTopic topic = redissonClient.getTopic(RedisConstants.CHANNEL_TRANSLATE_RESULT);
        topic.publish(json);
    }
}

