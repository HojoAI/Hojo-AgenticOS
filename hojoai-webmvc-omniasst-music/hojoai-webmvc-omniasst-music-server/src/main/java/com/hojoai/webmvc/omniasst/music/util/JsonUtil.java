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
package com.hojoai.webmvc.omniasst.music.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Util
 * JSON工具类
 * 
 * @author HojoAI
 * @since 2026-01-13
 */
public class JsonUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj 待序列化对象
     * @return JSON 字符串，失败时返回 null（并打日志）
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转JSON失败", e);
            return null;
        }
    }
    
    /**
     * 将 JSON 字符串反序列化为指定类型。
     *
     * @param json  JSON 文本
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 反序列化结果，失败时返回 null（并打日志）
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("JSON转对象失败", e);
            return null;
        }
    }
}

