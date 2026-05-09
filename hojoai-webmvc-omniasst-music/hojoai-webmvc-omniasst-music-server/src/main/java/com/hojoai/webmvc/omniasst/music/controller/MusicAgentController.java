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
package com.hojoai.webmvc.omniasst.music.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.webmvc.omniasst.music.service.dify.DifyService;
import com.hojoai.webmvc.omniasst.music.service.dify.dto.SAAgentRequest;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 音乐智能体控制器
 * 提供给SA服务调用的接口
 * 
 * @author shengxianfei
 * @since 2026-01-20
 */
@RestController
@RequestMapping("/music-agent")
public class MusicAgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicAgentController.class);
    
    @Resource
    private DifyService difyService;
    
    /**
     * 音乐智能体查询接口
     * 接收SA的query，转发给Dify音乐智能体，并流式返回结果
     * 
     * @param request 包含userId和query的请求
     * @return SSE流式响应（长连接，由客户端订阅事件）
     */
    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter query(@RequestBody SAAgentRequest request) {
        logger.info("收到音乐智能体查询请求 - userId: {}, query: {}", 
                request != null ? request.getUserId() : null,
                request != null ? request.getQuery() : null);
        
        // 参数校验
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        Asserts.notNull(request.getQuery(), "query");
        
        // 创建SSE发射器，设置超时时间为60秒
        SseEmitter emitter = new SseEmitter(60000L);
        
        // 设置完成和超时回调
        emitter.onCompletion(() -> {
            logger.info("SSE连接完成 - userId: {}", request.getUserId());
        });
        
        emitter.onTimeout(() -> {
            logger.warn("SSE连接超时 - userId: {}", request.getUserId());
            emitter.complete();
        });
        
        emitter.onError((ex) -> {
            logger.error("SSE连接错误 - userId: {}", request.getUserId(), ex);
        });
        
        // 异步调用Dify服务
        difyService.callDifyMusicAgent(request.getUserId(), request.getQuery(), emitter);
        
        return emitter;
    }
}

