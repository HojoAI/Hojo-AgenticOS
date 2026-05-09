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
import com.hojoai.routing.engine.AgentRoutingConstants;
import com.hojoai.routing.engine.AgentService;
import com.hojoai.routing.engine.impl.AgentRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sysagent 模块的 Agent 注册表实现。
 * <p>
 * 在 {@link #init()} 中向 {@link AgentRegistry} 注册全部 9 个 Agent 实例，
 * 供 {@link IntentionServiceImpl} 按 {@code targetAgent} 字符串解析各 {@link AgentService}。
 * </p>
 */
@Service
public class SysagentAgentRegistry implements AgentRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SysagentAgentRegistry.class);

    private final Map<String, AgentService> registry = new ConcurrentHashMap<>();

    @Resource(type = AgentServiceImpl.class)
    private AgentService chatAgentService;

    @Resource(type = DocAgentServiceImpl.class)
    private AgentService docAgentService;

    @Resource(type = TranslatorAgentServiceImpl.class)
    private AgentService translatorAgentService;

    @Resource(type = MeetingAgentServiceImpl.class)
    private AgentService meetingAgentService;

    @Resource(type = WebsearchAgentServiceImpl.class)
    private AgentService websearchAgentService;

    @Resource(type = FocusAgentServiceImpl.class)
    private AgentService focusAgentService;

    @Resource(type = MusicAgentServiceImpl.class)
    private AgentService musicAgentService;

    @Resource(type = WeatherAgentServiceImpl.class)
    private AgentService weatherAgentService;

    @Resource(type = NewsAgentServiceImpl.class)
    private AgentService newsAgentService;

    @PostConstruct
    public void init() {
        registry.put(ConversationConstant.AGENT_CHAT, chatAgentService);
        registry.put(ConversationConstant.AGENT_DOC, docAgentService);
        registry.put(ConversationConstant.AGENT_TRANSLATOR, translatorAgentService);
        registry.put(ConversationConstant.AGENT_MEETING, meetingAgentService);
        registry.put(ConversationConstant.AGENT_CHAT_WEBSEARCH, websearchAgentService);
        registry.put(ConversationConstant.AGENT_FOCUS, focusAgentService);
        registry.put(ConversationConstant.AGENT_MUSIC, musicAgentService);
        registry.put(ConversationConstant.AGENT_WEATHER, weatherAgentService);
        registry.put(ConversationConstant.AGENT_PODCAST, newsAgentService);
        logger.info("SysagentAgentRegistry initialized with {} agents", registry.size());
    }

    @Override
    public AgentService getAgent(String agentType) {
        return registry.get(agentType);
    }
}
