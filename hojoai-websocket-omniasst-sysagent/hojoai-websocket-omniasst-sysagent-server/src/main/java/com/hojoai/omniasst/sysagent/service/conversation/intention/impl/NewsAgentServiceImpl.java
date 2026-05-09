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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.sysagent.common.ServiceDiscoveryUtil;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.interaction.sse.SseClient;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.routing.engine.AgentService;
import com.hojoai.routing.engine.ConversationContext;
import com.hojoai.routing.engine.TaskSequence;
import com.hojoai.omniasst.sysagent.service.conversation.manager.SseEmitterManager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 播客/资讯推荐智能体：发现 news WebSocket 网关对应 HTTP 服务并调用推荐对话接口。
 */
@Service
public class NewsAgentServiceImpl extends AgentServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(NewsAgentServiceImpl.class);

    /** 资讯类微服务注册名（与部署约定一致）。 */
    private static final String NEWS_SERVICE_NAME = "hojoai-websocket-omniasst-news";

    /** 推荐对话 API 路径。 */
    private static final String NEWS_API_PATH = "/omniasst-news/v0/conversation/recommend";

    @Resource
    private ServiceDiscoveryUtil serviceDiscoveryUtil;

    @Resource
    private SseClient sseClient;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private ConversationUtils conversationUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewsAgentServiceImpl() {
        this.agentName = ConversationConstant.AGENT_PODCAST;
    }

    @Override
    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_NEWS_CN;
    }

    @Override
    public void callAgent(ConversationContext context, TaskSequence taskSequence,
                          SseEmitter emitter, Consumer<List<ConversationAddMessageRequest>> dataHandler) {
        String conversationId = context.getConversationId();
        String messageId = context.getMessageId();
        String userId = context.getUserId();

        sendAgentLoadingEvent(emitter);
        try {
            String newsServiceUrl = getNewsServiceUrl();
            if (StringUtils.isEmpty(newsServiceUrl)) {
                logger.error("News service not found in Nacos");
                throw new RuntimeException("News service unavailable");
            }

            logger.info("[{}] News service URL: {}", conversationId, newsServiceUrl);

            // 2. 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("query", taskSequence.getTask());
            requestBody.put("input", context.getInput());
            requestBody.put("conversationId", context.getConversationId());

            logger.info("[{}] Call News agent with params: {}", conversationId, requestBody);

            Map<String, StringBuilder> answerBuilderMap = new HashMap<>();
            Map<String, String> contentIdMap = new HashMap<>();
            AtomicReference<String> contentId = new AtomicReference<>("");

            sseClient.sendRequest(newsServiceUrl, requestBody, data -> {
                try {
                    if (sseEmitterManager.shouldStop(conversationId, messageId)) {
                        return;
                    }
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
                    logger.info("[{}, {}] News agent response data: {}, time: {}", conversationId, messageId, data, timestamp);
                    JsonNode jsonData = objectMapper.readTree(data);
                    JsonNode dataNode = jsonData.get("data");
                    JsonNode finishedNode = jsonData.get("finished");
                    boolean finished = finishedNode != null && finishedNode.asBoolean();

                    if (dataNode != null) {
                        // 单个数据对象处理（流式响应）
                        JsonNode answerNode = dataNode.get("text");
                        if (answerNode != null && StringUtils.isNotEmpty(answerNode.asText())) {
                            String subType = dataNode.has("subType") ? dataNode.get("subType").asText("") : "";
                            String answer = answerNode.asText();
                            emitTextChunkWithBubbleSpeechDuplicateIfNeeded(
                                    taskSequence, conversationId, messageId, contentIdMap, contentId,
                                    subType, answer, finished, emitter, answerBuilderMap);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse News SSE response data: " + data, e);
                }
            });

            if (!answerBuilderMap.isEmpty()) {
                dataHandler.accept(buildManagementConversationMessage(conversationId, messageId, answerBuilderMap, contentIdMap, context.getUserId()));
            }
        } catch (Exception e) {
            logger.error("Error occurred during News agent call", e);
            throw new RuntimeException("Error occurred during News agent call", e);
        }
    }

    private String getNewsServiceUrl() {
        String baseUrl = serviceDiscoveryUtil.getServiceBaseUrl(NEWS_SERVICE_NAME);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + NEWS_API_PATH;
    }
}
