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

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.sysagent.service.api.ConversationAPI;
import com.hojoai.omniasst.sysagent.service.conversation.bean.CreateConversationIdResponse;
import com.hojoai.omniasst.sysagent.service.conversation.bean.CreateConversationIdRequest;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过 management 服务分配或确认会话 ID 与首条消息 ID。
 */
@Service
public class ConversationManagement {
    @Resource
    private ConversationAPI conversationAPI;

    /**
     * 调用 {@link ConversationAPI#createConversationById}，返回单元素 Map：key 为 conversationId，value 为 messageId。
     *
     * @param conversationId 客户端传入或空的会话 ID，由对端决定是否新建
     * @param userId         用户 ID
     * @param headphoneMode 是否耳机模式
     * @return 成功为非空 Map；失败为空 Map
     */
    public Map<String, String> createConversationAndMessageId(String conversationId, String userId,
                                                              boolean headphoneMode) {
        CreateConversationIdRequest request = new CreateConversationIdRequest();
        request.setUserId(userId);
        request.setConversationId(conversationId);
        request.setHeadphoneMode(headphoneMode);
        ResponseEntity<Response<CreateConversationIdResponse>> response = conversationAPI.createConversationById(request);
        if (response.getStatusCode().is2xxSuccessful()){
            Response<CreateConversationIdResponse> conversationResponse = response.getBody();
            if (conversationResponse != null && conversationResponse.getResCode().equals("OK")) {
                Map<String, String> map = new HashMap<>();
                map.put(conversationResponse.getResContent().getConversationId(), conversationResponse.getResContent().getMessageId());
                return map;
            }
        }
        return new HashMap<>();
    }
}
