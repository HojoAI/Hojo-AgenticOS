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
package com.hojoai.omniasst.weather.service.dify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 数组流式累积与补全工具类。
 * <p>
 * 负责将 Dify 流式返回的 answer 片段累积成完整的 JSON 数组字符串，
 * 并在语法完整时解析为 {@link ArrayNode}。
 *
 * @author wangjian
 * @since 2026-01-22
 */
public class JsonArrayStreamAccumulator {

    private static final Logger logger = LoggerFactory.getLogger(JsonArrayStreamAccumulator.class);

    private final StringBuilder buffer = new StringBuilder();

    /**
     * 追加新的片段，并尝试解析为 JSON 数组。
     *
     * @param chunk       新增的片段
     * @param objectMapper Jackson ObjectMapper
     * @return 解析成功返回 ArrayNode，解析失败返回 null
     */
    public ArrayNode appendAndTryParse(String chunk, ObjectMapper objectMapper) {
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }
        buffer.append(chunk);
        try {
            JsonNode node = objectMapper.readTree(buffer.toString());
            if (node != null && node.isArray()) {
                return (ArrayNode) node;
            }
        } catch (Exception e) {
            // 解析失败说明 JSON 还不完整，继续累积
            logger.debug("JSON 仍不完整，等待更多数据补全: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取当前累积的buffer内容（用于部分解析）
     *
     * @return 当前累积的字符串
     */
    public String getBuffer() {
        return buffer.toString();
    }

    /**
     * 重置内部缓冲。
     */
    public void reset() {
        buffer.setLength(0);
    }
}

