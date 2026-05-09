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
package com.hojoai.omniasst.sysagent.service.conversation.intention.impl;

import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.routing.engine.AgentService;
import com.hojoai.routing.engine.AgentTimestampRegistry;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.IntentionService;
import com.hojoai.routing.engine.StopSignalProvider;
import com.hojoai.routing.engine.TaskSequence;
import com.hojoai.routing.engine.impl.AgentRegistry;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * 开源意图调度实现：顺序执行任务序列、检查停止标志、下发 {@code agentType} 事件并调用各 {@link AgentService}。
 * <p>
 * 与闭源 JAR 行为对齐的主要点：耳机模式下对部分智能体启用 bubble→speech 复制、耳机主聊抑制 {@code agentType} 事件等（见 {@link #maybeEmitAgentType}）。
 * </p>
 */
@Service
public class IntentionServiceImpl implements IntentionService {

    private static final Logger logger = LoggerFactory.getLogger(IntentionServiceImpl.class);

    @Resource
    private AgentRegistry agentRegistry;

    @Resource
    private StopSignalProvider stopSignalProvider;

    @Override
    public void intentDispatch(
            ConversationContext context,
            List<TaskSequence> taskSequences,
            SseEmitter emitter,
            Consumer<List<ConversationAddMessageRequest>> dataHandler) {
        if (context == null || emitter == null || dataHandler == null) {
            return;
        }
        if (CollectionUtils.isEmpty(taskSequences)) {
            return;
        }
        String tsKey = context.getConversationId() + context.getMessageId();
        AgentTimestampRegistry.setTimestamp(tsKey, System.currentTimeMillis());
        try {
            for (TaskSequence taskSequence : taskSequences) {
                if (stopSignalProvider.shouldStop(context.getConversationId(), context.getMessageId())) {
                    logger.info("intentDispatch stopped: conversationId={}, messageId={}",
                            context.getConversationId(), context.getMessageId());
                    break;
                }
                if (taskSequence == null) {
                    continue;
                }
                taskSequence.setHeadphone(context.isHeadphoneMode());
                taskSequence.setMapBubbleSubtypeToSpeech(shouldMapBubbleSubtypeToSpeech(context, taskSequence.getTargetAgent()));

                String targetAgent = taskSequence.getTargetAgent();
                this.maybeEmitAgentType(context, targetAgent, emitter);

                AgentService agent = agentRegistry.getAgent(targetAgent);
                if (agent == null) {
                    logger.warn("No AgentService registered for targetAgent={}", targetAgent);
                    continue;
                }
                agent.callAgent(context, taskSequence, emitter, dataHandler);
            }
        } finally {
            AgentTimestampRegistry.removeTimestamp(tsKey);
        }
    }

    private static boolean shouldMapBubbleSubtypeToSpeech(ConversationContext context, String targetAgent) {
        if (!context.isHeadphoneMode() || StringUtils.isBlank(targetAgent)) {
            return false;
        }
        return switch (targetAgent) {
            case ConversationConstant.AGENT_TRANSLATOR,
                 ConversationConstant.AGENT_MEETING,
                 ConversationConstant.AGENT_FOCUS,
                 ConversationConstant.AGENT_MUSIC,
                 ConversationConstant.AGENT_WEATHER,
                 ConversationConstant.AGENT_PODCAST,
                 ConversationConstant.AGENT_DOC -> true;
            default -> false;
        };
    }

    private void maybeEmitAgentType(ConversationContext context, String targetAgent, SseEmitter emitter) {
        if (!context.isHeadphoneMode()) {
            this.sendAgentType(emitter, targetAgent);
            return;
        }
        if (ConversationConstant.AGENT_CHAT.equals(targetAgent)
                || ConversationConstant.AGENT_CHAT_WEBSEARCH.equals(targetAgent)
                || StringUtils.isBlank(targetAgent)) {
            return;
        }
        this.sendAgentType(emitter, targetAgent);
    }

    private void sendAgentType(SseEmitter emitter, String targetAgent) {
        try {
            emitter.send(SseEmitter.event()
                    .name(ConversationConstant.EVENT_NAME_AGENT_TYPE)
                    .data(targetAgent == null ? "" : targetAgent));
        } catch (IOException e) {
            logger.debug("send agentType event failed: {}", e.getMessage());
        }
    }
}
