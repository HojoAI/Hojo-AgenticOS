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
package com.hojoai.omniasst.weather.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.omniasst.weather.entity.SAAgentRequest;
import com.hojoai.omniasst.weather.service.dify.DifyService;
import com.hojoai.omniasst.weather.v2.stream.WeatherAgentStreamService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 天气智能体控制器
 * 提供给SA服务调用的接口
 * 
 * @author wangjian
 * @since 2026-01-22
 */
@RestController
@RequestMapping("/weather-agent")
public class WeatherAgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherAgentController.class);
    
    @Resource
    private DifyService difyService;

    @Resource
    private WeatherAgentStreamService weatherAgentStreamService;

    /**
     * 天气智能体 v2：槽位解析、彩云天气与卡片编排，结果以 SSE 流式返回。
     *
     * @param request 智能体请求，{@code userId}、{@code query} 必填，{@code conversationId}、{@code inputs} 可选
     * @return SSE 发射器，由 {@link WeatherAgentStreamService} 异步写入多帧响应
     * @throws com.hojoai.common.asserts.AssertException 当 request、userId 或 query 为空时（由 {@link Asserts} 抛出）
     */
    @PostMapping(value = "/chat",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter queryV2(@RequestBody SAAgentRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        Asserts.notNull(request.getQuery(), "query");

        SseEmitter sseEmitter = buildSseEmitter(request.getUserId());
        weatherAgentStreamService.handleChatV2(request, sseEmitter);
        return sseEmitter;
    }
    
    /**
     * 天气智能体 v1：转发至 Dify 流式对话，结果以 SSE 返回。
     *
     * @param request 智能体请求，{@code userId}、{@code query} 必填
     * @return SSE 发射器，由 {@link DifyService} 异步写入流式事件
     * @throws com.hojoai.common.asserts.AssertException 当 request、userId 或 query 为空时
     */
    @PostMapping(value = "/chat/v1",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter query(@RequestBody SAAgentRequest request) {
        // 参数校验
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        Asserts.notNull(request.getQuery(), "query");

        SseEmitter sseEmitter = buildSseEmitter(request.getUserId());

        // 异步调用Dify服务，传递conversationId（如果存在）和inputs（如果存在）
        difyService.callWeatherAgent(request.getUserId(), request.getQuery(), request.getConversationId(), request.getInputs(), sseEmitter);
        
        return sseEmitter;
    }

    private SseEmitter buildSseEmitter(String userId){
        // 创建SSE发射器，设置超时时间为3分钟
        SseEmitter emitter = new SseEmitter(180000L);

        // 设置完成和超时回调
        emitter.onCompletion(() -> {
            logger.info("SSE连接完成 - userId: {}", userId);
        });
        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时 - userId: {}", userId);
            emitter.complete();
        });
        emitter.onError((ex) -> {
            logger.error("SSE连接错误 - userId: {}", userId, ex);
        });

        return emitter;
    }
}

