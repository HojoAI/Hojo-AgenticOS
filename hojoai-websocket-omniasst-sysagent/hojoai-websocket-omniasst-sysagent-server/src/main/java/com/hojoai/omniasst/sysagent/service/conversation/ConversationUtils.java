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
package com.hojoai.omniasst.sysagent.service.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.common.api.avatar.AvatarItem;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.sysagent.common.RedisService;
import com.hojoai.omniasst.sysagent.constans.ConversationConstant;
import com.hojoai.omniasst.sysagent.service.api.ConversationAPI;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationRequest;
import com.hojoai.omniasst.sysagent.service.conversation.bean.ConversationResponse;
import com.hojoai.omniasst.sysagent.service.conversation.bean.IntentionModelRequest;
import com.hojoai.omniasst.common.api.conversation.ConversationAddMessageRequest;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.sysagent.service.speech.ASRService;
import com.hojoai.omniasst.sysagent.service.speech.impl.HojoASRServiceImp;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对话辅助组件：构造 SSE/落库消息体、上下文与意图模型入参转换、头像性别缓存等。
 */
@Component
public class ConversationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConversationUtils.class);

    /** Redis 中会话上下文列表 key 前缀。 */
    public final static String CONVERSATION_CONTEXT_PRE = "Omniasst:Conversation:Context:";

    /** 会话上下文在 Redis 中的过期时间（分钟）。 */
    public final static Long CONVERSATION_CONTEXT_TIMEOUT = 10L;

    /** 头像详情缓存 key 前缀。 */
    private static final String AVATAR_REDIS_KEY_PREFIX = "Omniasst:Avatar:";
    /** 头像缓存过期时间（小时）。 */
    private static final long AVATAR_REDIS_EXPIRE_HOURS = 2L;

    @Resource
    private Snowflake snowflake;
    
    @Resource
    private ConversationAPI conversationAPI;
    
    @Resource(type = HojoASRServiceImp.class)
    private ASRService asrService;
    
    @Resource
    private RedisService redisService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 组装下发给前端的纯文本 SSE 消息体。
     *
     * @param conversationId 会话 ID
     * @param messageId      消息 ID
     * @param contentId      内容块 ID
     * @param answer         文本内容
     * @param subType        子类型（如 bubble/body）
     * @param finished       是否本轮结束
     */
    public ConversationResponse buildConversationTextResponse(String conversationId, String messageId, String contentId, String answer, String subType, boolean finished){
        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setConversationId(conversationId);
        conversationResponse.setMessageId(messageId);
        ConversationResponse.DataItem dataItem = new ConversationResponse.DataItem();
        dataItem.setType(ConversationConstant.CONTENT_TYPE_TEXT);
        dataItem.setText(answer);
        dataItem.setSubType(subType);
        dataItem.setContentId(contentId);
        conversationResponse.setData(List.of(dataItem));
        conversationResponse.setFinished(finished);
        return conversationResponse;
    }

    /**
     * 构建写入 management 的单条文本消息（多 subtype 聚合为多条 {@link MessageContentItem}）。
     *
     * @see #buildManagementConversationTextMessage(String, String, String, String, String, Map, Map, String)
     */
    public ConversationAddMessageRequest buildManagementConversationTextMessage(String conversationId, String messageId, String role, String contextRole, String agent, Map<String, String> typeToTextMap){
        return buildManagementConversationTextMessage(conversationId, messageId, role, contextRole, agent, typeToTextMap, null, null);
    }

    /**
     * 同上，支持为各 subtype 指定 contentId。
     */
    public ConversationAddMessageRequest buildManagementConversationTextMessage(String conversationId, String messageId, String role, String contextRole, String agent, Map<String, String> typeToTextMap, Map<String, String> subtypeToContentIdMap){
        return buildManagementConversationTextMessage(conversationId, messageId, role, contextRole, agent, typeToTextMap, subtypeToContentIdMap, null);
    }

    /**
     * 构建 management 落库消息，可带 userId。
     */
    public ConversationAddMessageRequest buildManagementConversationTextMessage(String conversationId, String messageId, String role, String contextRole, String agent, Map<String, String> typeToTextMap, Map<String, String> subtypeToContentIdMap, String userId){
        ConversationAddMessageRequest message = new ConversationAddMessageRequest();
        message.setConversationId(conversationId);
        message.setMessageId(messageId);
        message.setRole(role);
        if (userId != null) {
            message.setUserId(userId);
        }

        List<MessageContentItem> contentItems = new ArrayList<>();
        for (Map.Entry<String, String> entry : typeToTextMap.entrySet()) {
            String subtype = entry.getKey();
            String text = entry.getValue();
            if (text != null && !text.isEmpty()) {
                MessageContentItem messageContentItem = new MessageContentItem();
                messageContentItem.setRole(contextRole);
                messageContentItem.setAgent(agent);
                messageContentItem.setText(text);
                messageContentItem.setType(ConversationConstant.CONTENT_TYPE_TEXT);
                messageContentItem.setSubType(subtype);
                if (subtypeToContentIdMap != null) {
                    messageContentItem.setContentId(subtypeToContentIdMap.getOrDefault(subtype, ""));
                }
                contentItems.add(messageContentItem);
            }
        }

        message.setContent(contentItems);
        return message;
    }

    /**
     * 将单条内容项转为意图模型上下文条目（含文件 URL 等）。
     */
    public IntentionModelRequest.MessageContext messageContentItem2MessageContext(MessageContentItem item){
        IntentionModelRequest.MessageContext message = new IntentionModelRequest.MessageContext();
        message.setAgent(item.getAgent());
        message.setFile(item.getUrl());
        message.setContentType(item.getType());
        message.setText(item.getText());
        message.setRole(item.getRole());
        message.setSubType(item.getSubType());
        return message;
    }

    /**
     * 音频类内容项转意图模型上下文（文本走 {@link ConversationConstant#CONTENT_TYPE_TEXT} 以兼容模型入参）。
     */
    public IntentionModelRequest.MessageContext audioContentItem2MessageContext(MessageContentItem item){
        IntentionModelRequest.MessageContext message = new IntentionModelRequest.MessageContext();
        message.setAgent(item.getAgent());
        message.setFile("");
        message.setContentType(ConversationConstant.CONTENT_TYPE_TEXT);
        message.setText(item.getText());
        message.setRole(item.getRole());
        message.setSubType(item.getSubType());
        return message;
    }

    /**
     * 生成前端/落库用的内容块 ID（Snowflake 前缀 {@code C}）。
     *
     * @return 唯一 contentId
     */
    public String generaConversationContentId(){
        return "C" + snowflake.nextId();
    }

    /**
     * 将 ConversationRequest.Inputs 转换为 IntentionModelRequest.Input
     * 
     * @param inputs ConversationRequest 的 Inputs 对象
     * @param avatarId 头像ID，用于获取助手性别
     * @return IntentionModelRequest.Input 对象
     */
    public IntentionModelRequest.Input convertInputsToInput(ConversationRequest.Inputs inputs, String avatarId) {
        if (inputs == null) {
            return null;
        }
        
        // 通过 avatarId 获取助手性别
        String assistantGender = getAssistantGenderByAvatarId(avatarId);
        
        IntentionModelRequest.Input input = new IntentionModelRequest.Input();
        input.setAssistantGender(assistantGender);
        input.setUserId(inputs.getUserId());
        input.setUserName(inputs.getUserNickname());
        input.setCityName(inputs.getUserCity());
        input.setDistrictName(inputs.getUserDistrict());
        input.setProvinceName(inputs.getUserProvince());
        input.setLongitude(inputs.getUserLongitude());
        input.setLatitude(inputs.getUserLatitude());
        
        return input;
    }

    /**
     * 将 ConversationRequest.Inputs 转换为 IntentionModelRequest.Input（不包含 avatarId）
     * 
     * @param inputs ConversationRequest 的 Inputs 对象
     * @return IntentionModelRequest.Input 对象
     */
    public IntentionModelRequest.Input convertInputsToInput(ConversationRequest.Inputs inputs) {
        return convertInputsToInput(inputs, null);
    }

    /**
     * 通过 avatarId 获取助手性别
     * 优先从 Redis 缓存获取，如果不存在则调用 API 并缓存到 Redis
     * 
     * @param avatarId 头像ID
     * @return 助手性别，如果获取失败返回 null
     */
    private String getAssistantGenderByAvatarId(String avatarId) {
        if (StringUtils.isEmpty(avatarId)) {
            return null;
        }
        
        String redisKey = AVATAR_REDIS_KEY_PREFIX + avatarId;
        
        try {
            // 1. 先从 Redis 获取
            Object cachedAvatar = redisService.get(redisKey);
            if (cachedAvatar != null) {
                AvatarItem avatarItem = null;
                try {
                    if (cachedAvatar instanceof AvatarItem) {
                        avatarItem = (AvatarItem) cachedAvatar;
                    } else if (cachedAvatar instanceof Map) {
                        // 如果 Redis 返回的是 Map，转换为 AvatarItem
                        avatarItem = objectMapper.convertValue(cachedAvatar, AvatarItem.class);
                    } else {
                        // 尝试 JSON 字符串转换
                        String jsonStr = cachedAvatar.toString();
                        avatarItem = objectMapper.readValue(jsonStr, AvatarItem.class);
                    }
                    
                    if (avatarItem != null && StringUtils.isNotEmpty(avatarItem.getGender())) {
                        logger.debug("Get avatar gender from Redis cache: avatarId={}, gender={}", avatarId, avatarItem.getGender());
                        return avatarItem.getGender();
                    }
                } catch (Exception e) {
                    logger.warn("Failed to deserialize AvatarItem from Redis cache: avatarId={}", avatarId, e);
                }
            }
            
            // 2. Redis 中没有，调用 API 获取
            logger.debug("Avatar not found in Redis, calling API: avatarId={}", avatarId);
            ResponseEntity<Response<AvatarItem>> response = conversationAPI.getAvatarById(avatarId);
            
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                Response<AvatarItem> responseBody = response.getBody();
                if (responseBody != null && responseBody.getResContent() != null) {
                    AvatarItem avatarItem = responseBody.getResContent();
                    
                    // 3. 保存到 Redis，过期时间 2 小时
                    // 使用 JSON 字符串存储以确保兼容性
                    String avatarJson = objectMapper.writeValueAsString(avatarItem);
                    redisService.set(redisKey, avatarJson, AVATAR_REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
                    logger.debug("Avatar saved to Redis: avatarId={}, gender={}", avatarId, avatarItem.getGender());
                    
                    return avatarItem.getGender();
                }
            }
            
            logger.warn("Failed to get avatar from API: avatarId={}", avatarId);
            return null;
        } catch (Exception e) {
            logger.error("Error getting assistant gender by avatarId: avatarId={}", avatarId, e);
            return null;
        }
    }
}
