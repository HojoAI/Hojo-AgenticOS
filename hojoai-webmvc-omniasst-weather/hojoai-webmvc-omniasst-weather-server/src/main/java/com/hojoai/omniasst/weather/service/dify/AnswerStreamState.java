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
package com.hojoai.omniasst.weather.service.dify;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify answer 流式解析的状态机状态。
 *
 * 负责记录当前正在处理的数组元素索引、已发送文本长度等，
 * 用于实现 a、b、c 的增量式输出，而不是 a、ab、abc 的累积输出。
 *
 * @author wangjian
 * @since 2026-01-22
 */
public class AnswerStreamState {

    /**
     * 当前正在处理的数组下标（-1 表示尚未初始化）
     */
    private int currentItemIndex = -1;

    /**
     * 当前元素的类型
     */
    private String currentType;

    /**
     * 当前元素的子类型
     */
    private String currentSubType;

    /**
     * 当前元素已发送的文本长度
     */
    private int sentTextLength;

    /**
     * 当前会话 ID
     */
    private String conversationId;

    /**
     * 当前消息 ID
     */
    private String messageId;

    /**
     * 已处理的数组大小（用于检测新增元素）
     */
    private int processedArraySize = 0;

    /**
     * 每个数组元素的text字段已发送长度
     * Key: 数组索引，Value: 该元素text字段已发送的文本长度
     */
    private final Map<Integer, Integer> itemTextLengthMap = new HashMap<>();

    /**
     * 每个数组元素的完整text内容（用于增量计算）
     * Key: 数组索引，Value: 该元素当前的完整text内容
     */
    private final Map<Integer, String> itemTextContentMap = new HashMap<>();

    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    public void setCurrentItemIndex(int currentItemIndex) {
        this.currentItemIndex = currentItemIndex;
    }

    public String getCurrentType() {
        return currentType;
    }

    public void setCurrentType(String currentType) {
        this.currentType = currentType;
    }

    public String getCurrentSubType() {
        return currentSubType;
    }

    public void setCurrentSubType(String currentSubType) {
        this.currentSubType = currentSubType;
    }

    public int getSentTextLength() {
        return sentTextLength;
    }

    public void setSentTextLength(int sentTextLength) {
        this.sentTextLength = sentTextLength;
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

    public int getProcessedArraySize() {
        return processedArraySize;
    }

    public void setProcessedArraySize(int processedArraySize) {
        this.processedArraySize = processedArraySize;
    }

    /**
     * 获取指定数组元素的text字段已发送长度
     *
     * @param itemIndex 数组索引
     * @return 已发送的文本长度，如果不存在则返回0
     */
    public int getItemTextLength(int itemIndex) {
        return itemTextLengthMap.getOrDefault(itemIndex, 0);
    }

    /**
     * 设置指定数组元素的text字段已发送长度
     *
     * @param itemIndex 数组索引
     * @param length     已发送的文本长度
     */
    public void setItemTextLength(int itemIndex, int length) {
        itemTextLengthMap.put(itemIndex, length);
    }

    /**
     * 获取指定数组元素的完整text内容
     *
     * @param itemIndex 数组索引
     * @return 完整的text内容，如果不存在则返回null
     */
    public String getItemTextContent(int itemIndex) {
        return itemTextContentMap.get(itemIndex);
    }

    /**
     * 设置指定数组元素的完整text内容
     *
     * @param itemIndex 数组索引
     * @param text      完整的text内容
     */
    public void setItemTextContent(int itemIndex, String text) {
        itemTextContentMap.put(itemIndex, text);
    }

    /**
     * 重置状态（用于新的会话或消息）
     */
    public void reset() {
        itemTextLengthMap.clear();
        itemTextContentMap.clear();
        processedArraySize = 0;
        currentItemIndex = -1;
        currentType = null;
        currentSubType = null;
        sentTextLength = 0;
    }
}

