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
package com.hojoai.common.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * The common class for jackson util.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-common 1.0.0
 */
public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 空 Bean 不触发序列化失败；忽略 JSON 中未知字段，便于对接字段演进中的外部接口
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串。
     *
     * @param value 待序列化的对象，可为 null（将序列化为 JSON null）
     * @return JSON 字符串
     * @throws UnsupportedOperationException 当序列化失败时包装原始 {@link JsonProcessingException}
     */
    public static String bean2Json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException("bean 2 json exception", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的 Java 对象。
     *
     * @param content   JSON 文本，不可为 null
     * @param valueType 目标类型 Class
     * @param <T>       目标类型
     * @return 反序列化后的实例
     * @throws UnsupportedOperationException 当反序列化失败时包装原始 {@link IOException}
     */
    public static <T> T json2Bean(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new UnsupportedOperationException("json 2 bean exception", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为带泛型结构的对象（如 {@code List<Map<String,Object>>}）。
     *
     * @param content        JSON 文本，不可为 null
     * @param valueTypeRef   Jackson {@link TypeReference}，用于保留泛型类型信息
     * @param <T>            目标类型
     * @return 反序列化后的实例
     * @throws UnsupportedOperationException 当反序列化失败时包装原始 {@link IOException}
     */
    public static <T> T json2Bean(String content, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new UnsupportedOperationException("json 2 bean exception", e);
        }
    }
}
