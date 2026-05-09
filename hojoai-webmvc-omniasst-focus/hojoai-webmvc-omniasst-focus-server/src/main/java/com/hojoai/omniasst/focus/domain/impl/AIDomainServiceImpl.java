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
package com.hojoai.omniasst.focus.domain.impl;

import com.hojoai.central.provider.service.common.enums.CommonEnum;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.domain.AIDomainService;
import com.hojoai.omniasst.focus.dto.response.StreamChunkResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;

/**
 * AI Domain Service 实现类
 * 使用 Spring AI 标准接口：ChatClient 和 ChatModel
 * 
 * @author hojoai
 * @date 2026-01-09
 */
@Component
public class AIDomainServiceImpl implements AIDomainService {

    private static final Logger log = LoggerFactory.getLogger(AIDomainServiceImpl.class);

    @Resource
    private ResourceLoader resourceLoader;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Resource
    private Snowflake snowflake;

    private static final String X_TRACE_ID = "X-Trace-ID";

    /**
     * 非流式输出 - 使用 WebClient（带返回类型）
     * 用于意图识别等场景
     *
     * @param typeEnum prompt类型 (default/schedule-create/creative/technical等)
     * @param requestDTO 用户输入
     * @param responseClass 响应类型
     * @param <T> 响应类型泛型
     * @return AI回复内容
     */
    @Override
    public <T> T chat(CommonEnum typeEnum, Object requestDTO, Class<T> responseClass) {
        log.info("调用 chat 方法, type={}, requestDTO={}, responseClass={}", typeEnum, JacksonUtil.bean2Json(requestDTO), responseClass);

        String traceId = String.valueOf(snowflake.nextId());

        log.info("调用chat方法，生成traceId -> {}", traceId);

        // 创建 WebClient 实例
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(X_TRACE_ID, "Bearer " + traceId)
                .build();

        // 发送请求并接收响应
        return webClient.post()
                .uri(String.valueOf(typeEnum.getValue()))
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(responseClass)
                .block();
    }

    /**
     * 流式输出 - 使用 WebClient
     * 用于创建日程等需要流式返回的场景
     * 自动解析流式响应的 JSON 格式，提取 choices[0].delta.content 字段
     *
     * @param typeEnum prompt类型 (default/schedule-create/creative/technical等)
     * @param requestDTO 用户输入
     * @return 流式响应Flux，返回提取的 content 字符串
     */
    @Override
    public Flux<String> chatStream(CommonEnum typeEnum, Object requestDTO) {
        log.info("调用 chatStream 方法, type={}, requestDTO={}", typeEnum, requestDTO);

        String traceId = String.valueOf(snowflake.nextId());

        log.info("chatStream，生成traceId -> {}", traceId);

        // 创建 WebClient 实例
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(X_TRACE_ID, "Bearer " + traceId)
                .build();

        // 使用 WebClient 发送 POST 请求并接收流式响应
        return webClient.post()
                .uri(String.valueOf(typeEnum.getValue()))
                .contentType(MediaType.APPLICATION_JSON)  // 设置请求体 Content-Type
                .accept(MediaType.TEXT_EVENT_STREAM)       // 设置期望的响应类型
                .body(BodyInserters.fromValue(requestDTO))
                .retrieve()
                .bodyToFlux(String.class)
                .handle((chunk, sink) -> {
                    // 解析 SSE 格式并提取 JSON
                    String content = parseStreamChunk(chunk);
                    // 只有当 content 不为 null 且不为空时才发出
                    if (content != null && !content.isEmpty()) {
                        log.debug("提取到 content: {}", content);
                        sink.next(content);
                    }
                })
                .cast(String.class)
                .doOnError(error -> log.error("解析流式响应失败", error));
    }
    
    /**
     * 解析流式响应的 chunk，提取 content 字段
     * 处理 SSE 格式：data: {...} 或直接是 JSON
     * 
     * @param chunk 原始 chunk 字符串
     * @return content 字段的值，如果解析失败或不存在则返回 null
     */
    private String parseStreamChunk(String chunk) {
        if (chunk == null || chunk.trim().isEmpty()) {
            return null;
        }
        
        try {
            String jsonStr = chunk.trim();
            
            // 处理 SSE 格式：移除 "data: " 前缀
            if (jsonStr.startsWith("data: ")) {
                jsonStr = jsonStr.substring(6).trim();
            }
            
            // 如果是 "[DONE]" 或其他非 JSON 内容，返回 null
            if (jsonStr.equals("[DONE]") || !jsonStr.startsWith("{")) {
                return null;
            }
            
            // 解析 JSON
            StreamChunkResponseDTO chunkResponse = JacksonUtil.json2Bean(jsonStr, StreamChunkResponseDTO.class);
            
            // 提取 content
            return chunkResponse.getContent();
            
        } catch (Exception e) {
            log.warn("解析流式响应 chunk 失败: {}, 错误: {}", chunk, e.getMessage());
            return null;
        }
    }

}
