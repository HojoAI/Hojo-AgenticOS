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
package com.hojoai.omniasst.sysagent.service.conversation.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.routing.engine.StopSignalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理对话流式输出的 {@link SseEmitter} 注册表与停止标志，支持 HTTP 侧与 Redis 广播协同中止生成。
 */
@Component
public class SseEmitterManager implements StopSignalProvider {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterManager.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储活跃的 SSE 连接: key = conversationId:messageId, value = SseEmitter
    private volatile Map<String, SseEmitter> activeEmitters = new ConcurrentHashMap<>();
    
    // 存储停止标志: conversationId:messageId
    private volatile Set<String> stopFlags = ConcurrentHashMap.newKeySet();

    /**
     * 生成 key
     */
    private String generateKey(String conversationId, String messageId) {
        return conversationId + ":" + messageId;
    }

    /**
     * 注册 SSE 连接
     */
    public void registerEmitter(String conversationId, String messageId, SseEmitter emitter) {
        String key = generateKey(conversationId, messageId);
        activeEmitters.put(key, emitter);
        logger.info("Register SSE emitter: {}, total active: {}", key, activeEmitters.size());
    }

    /**
     * 注销 SSE 连接
     */
    public void unregisterEmitter(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        activeEmitters.remove(key);
        stopFlags.remove(key);
        logger.info("Unregister SSE emitter: {}, total active: {}", key, activeEmitters.size());
    }

    /**
     * 获取 SSE 连接
     */
    public SseEmitter getEmitter(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        return activeEmitters.get(key);
    }

    /**
     * 检查连接是否存在
     */
    public boolean hasEmitter(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        return activeEmitters.containsKey(key);
    }

    /**
     * 设置停止标志
     */
    public void setStopFlag(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        stopFlags.add(key);
        logger.info("Set stop flag: {}, list {}", key, stopFlags.toString());
    }

    /**
     * 检查是否应该停止
     */
    public boolean shouldStop(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        return stopFlags.contains(key);
    }

    /**
     * 清除停止标志
     */
    public void clearStopFlag(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        stopFlags.remove(key);
    }

    /**
     * 停止指定的对话
     * 发送停止消息并完成连接
     */
    public boolean stopConversation(String conversationId, String messageId) {
        String key = generateKey(conversationId, messageId);
        
        // 获取 emitter
        SseEmitter emitter = activeEmitters.get(key);
        if (emitter != null) {
            // 设置停止标志
            setStopFlag(conversationId, messageId);
            
            try {
                Thread.sleep(200);
                ConversationResponse response = new ConversationResponse();
                response.setConversationId(conversationId);
                response.setMessageId(messageId);
                response.setFinished(true);
                
                emitter.send(SseEmitter.event()
                        .name(ConversationConstant.EVENT_NAME_MESSAGE)
                        .data(objectMapper.writeValueAsString(response)));
                
                emitter.complete();
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                logger.info("Stopped conversation: {}, time: {}", key, timestamp);
                return true;
            } catch (Exception e) {
                logger.error("Failed to stop conversation: {}", key, e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    logger.error("Failed to complete with error: {}", key, ex);
                }
            }
        } else {
            logger.warn("No active emitter found for: {}", key);
        }
        
        return false;
    }

    /**
     * 获取活跃连接数量
     */
    public int getActiveCount() {
        return activeEmitters.size();
    }
}
