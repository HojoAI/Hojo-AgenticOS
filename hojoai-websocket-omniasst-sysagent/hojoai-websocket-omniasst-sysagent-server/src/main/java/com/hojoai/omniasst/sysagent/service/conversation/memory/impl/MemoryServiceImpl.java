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
package com.hojoai.omniasst.sysagent.service.conversation.memory.impl;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.common.RedisService;
import com.hojoai.omniasst.sysagent.service.conversation.bean.DeleteChatRequest;
import com.hojoai.omniasst.sysagent.service.api.ConversationAPI;
import com.hojoai.omniasst.sysagent.service.conversation.ConversationUtils;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ManagementConversationMessageQueryRequest;
import com.hojoai.omniasst.sysagent.service.conversation.memory.MemoryService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link MemoryService} 实现：Redis 缓存 + management 查询/删除。
 */
@Service
public class MemoryServiceImpl implements MemoryService {
    private static final Logger logger = LoggerFactory.getLogger(MemoryServiceImpl.class);
    
    @Resource
    private RedisService redisService;

    @Resource
    private ConversationAPI conversationAPI;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageContentItem> getMemory(String conversationId, String filterMessageId, boolean useCache) {
        String key = ConversationUtils.CONVERSATION_CONTEXT_PRE + conversationId;
        List<MessageContentItem> items = null;
        if (useCache) {
            try {
                items = (List<MessageContentItem>) redisService.get(key);
            } catch (Exception e) {
                if (ExceptionUtils.indexOfThrowable(e, SerializationException.class) >= 0) {
                    logger.error(
                            "Redis conversation context deserialize failed (invalid or legacy cache value), key={}, conversationId={}, detail={}",
                            key, conversationId, e.getMessage(), e);
                    redisService.delete(key);
                    items = null;
                } else {
                    throw e;
                }
            }
        }
        if (items == null){
            ManagementConversationMessageQueryRequest request = new ManagementConversationMessageQueryRequest();
            request.setConversationId(conversationId);
            ResponseEntity<Response<Map>> responseEntity = conversationAPI.getManagementConversationMessageById(request);
            if (responseEntity.getStatusCode().is2xxSuccessful()){
                Response<Map> reponse = responseEntity.getBody();
                if (reponse != null && reponse.getResCode().equals(ResponseStatusBasic.OK.getResCode())) {
                    items = getMessageList(reponse.getResContent(), filterMessageId);
                }
            }

            if (CollectionUtils.isNotEmpty(items)){
                redisService.set(key, items, ConversationUtils.CONVERSATION_CONTEXT_TIMEOUT, TimeUnit.MINUTES);
            }
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMessageAndReloadToRedis(String conversationId, String messageId) {
        try {
            DeleteChatRequest deleteRequest = new DeleteChatRequest();
            deleteRequest.setConversationId(conversationId);
            deleteRequest.setMessageId(messageId);
            
            ResponseEntity<Response<Void>> deleteResponse = conversationAPI.deleteConversationMessage(deleteRequest);
            if (!deleteResponse.getStatusCode().is2xxSuccessful()) {
                logger.error("Failed to delete message from DB: conversationId={}, messageId={}", conversationId, messageId);
                return false;
            }
            
            Response<Void> responseBody = deleteResponse.getBody();
            if (responseBody == null || !ResponseStatusBasic.OK.getResCode().equals(responseBody.getResCode())) {
                logger.error("Delete message response failed: conversationId={}, messageId={}, response={}", 
                    conversationId, messageId, responseBody);
                return false;
            }
            
            logger.info("Successfully deleted message from DB: conversationId={}, messageId={}", conversationId, messageId);
            
            List<MessageContentItem> items = getMemory(conversationId, null, true);
            if (items != null) {
                logger.info("Successfully reloaded messages to Redis: conversationId={}, messageCount={}", 
                    conversationId, items.size());
                return true;
            } else {
                logger.info("No messages left after deletion: conversationId={}", conversationId);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error deleting message and reloading to Redis: conversationId={}, messageId={}", 
                conversationId, messageId, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteChatConversation(DeleteChatRequest request) {
        try {
            logger.info("Processing delete chat request: conversationId={}, messageId={}", 
                request.getConversationId(), request.getMessageId());

            ResponseEntity<Response<Void>> deleteResponse = conversationAPI.deleteConversation(request);
            if (!deleteResponse.getStatusCode().is2xxSuccessful()) {
                logger.error("Failed to delete conversation: conversationId={}, messageId={}", 
                    request.getConversationId(), request.getMessageId());
                return false;
            }
            
            Response<Void> responseBody = deleteResponse.getBody();
            if (responseBody == null || !ResponseStatusBasic.OK.getResCode().equals(responseBody.getResCode())) {
                logger.error("Delete conversation response failed: conversationId={}, messageId={}, response={}", 
                    request.getConversationId(), request.getMessageId(), responseBody);
                return false;
            }
            
            logger.info("Successfully deleted conversation: conversationId={}, messageId={}", 
                request.getConversationId(), request.getMessageId());
            
            // 删除 Redis 中的缓存
            String redisKey = ConversationUtils.CONVERSATION_CONTEXT_PRE + request.getConversationId();
            Boolean deleted = redisService.delete(redisKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Successfully deleted Redis cache for conversationId: {}", request.getConversationId());
            } else {
                logger.warn("Redis cache not found or already deleted for conversationId: {}", request.getConversationId());
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error deleting chat: conversationId={}, messageId={}", 
                request.getConversationId(), request.getMessageId(), e);
            return false;
        }
    }

    private List<MessageContentItem> getMessageList(Map resContent, String filterMessageId){
        List<MessageContentItem> messages = new ArrayList<>();
        List<Map> pageContent = (List<Map>) resContent.get("pageContent");
        for (Map content : pageContent){
            if (StringUtils.isNotEmpty(filterMessageId) && content.getOrDefault("messageId", "").equals(filterMessageId))
                continue;

            List<Map> contentItems = (List<Map>) content.get("contentJson");
            Collections.reverse(contentItems);
            for(Map item : contentItems){
                MessageContentItem messageContentItem = new MessageContentItem();
                messageContentItem.setRole((String) item.getOrDefault("role", ""));
                messageContentItem.setType((String) item.getOrDefault("type", ""));
                messageContentItem.setText((String) item.getOrDefault("text", ""));
                messageContentItem.setUrl((String) item.getOrDefault("url", ""));
                messageContentItem.setAgent((String) item.getOrDefault("agent", ""));
                messageContentItem.setSubType((String) item.getOrDefault("subType", ""));
                messageContentItem.setContentId((String) item.getOrDefault("contentId", ""));
                messages.add(messageContentItem);
            }
        }
        Collections.reverse(messages);
        return messages;
    }
}