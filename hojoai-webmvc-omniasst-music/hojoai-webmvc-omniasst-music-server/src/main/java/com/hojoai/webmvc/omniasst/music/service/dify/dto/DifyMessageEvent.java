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
package com.hojoai.webmvc.omniasst.music.service.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dify消息事件DTO
 * 
 * @author shengxianfei
 * @since 2026-01-20
 */
public class DifyMessageEvent {
    
    /**
     * 事件类型：agent_thought, agent_message, message_end等
     */
    private String event;
    
    /**
     * 会话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;
    
    /**
     * 消息ID
     */
    @JsonProperty("message_id")
    private String messageId;
    
    /**
     * 创建时间戳
     */
    @JsonProperty("created_at")
    private Long createdAt;
    
    /**
     * 任务ID
     */
    @JsonProperty("task_id")
    private String taskId;
    
    /**
     * 事件ID
     */
    private String id;
    
    /**
     * 回答内容（仅在agent_message事件中有值）
     */
    private String answer;
    
    /**
     * 位置
     */
    private Integer position;
    
    /**
     * 思考内容
     */
    private String thought;
    
    /**
     * 观察内容
     */
    private String observation;
    
    /**
     * 工具
     */
    private String tool;
    
    /**
     * 工具输入
     */
    @JsonProperty("tool_input")
    private String toolInput;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getToolInput() {
        return toolInput;
    }

    public void setToolInput(String toolInput) {
        this.toolInput = toolInput;
    }
}

