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
import org.springframework.stereotype.Service;

/**
 * 联网搜索聊天智能体，对应 {@link com.hojoai.omniasst.sysagent.constans.ConversationConstant#AGENT_CHAT_WEBSEARCH}。
 */
@Service
public class WebsearchAgentServiceImpl extends AgentServiceImpl {

    public WebsearchAgentServiceImpl() {
        this.agentName = ConversationConstant.AGENT_CHAT_WEBSEARCH;
    }

    @Override
    protected String getAgentChineseName() {
        return ConversationConstant.AGENT_CHAT_WEBSEARCH_CN;
    }
}
