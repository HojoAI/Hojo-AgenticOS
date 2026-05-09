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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 部分JSON文本提取器
 * 用于从流式传输的不完整JSON中提取text字段
 *
 * @author wangjian
 * @since 2026-01-23
 */
public class PartialJsonTextExtractor {

    /**
     * 从部分JSON buffer中提取text字段信息
     * 只提取数组元素内部的text字段，不提取JSON结构本身的text字段
     *
     * @param buffer 累积的JSON buffer（可能不完整）
     * @return 提取到的text字段信息列表
     */
    public List<TextFieldInfo> extractTextFields(String buffer) {
        List<TextFieldInfo> textFields = new ArrayList<>();
        
        if (buffer == null || buffer.isEmpty()) {
            return textFields;
        }

        // 找到数组的开始位置
        int arrayStart = buffer.indexOf('[');
        if (arrayStart == -1) {
            return textFields; // 没有数组开始标记，无法提取
        }

        // 从数组开始位置之后查找对象
        String arrayContent = buffer.substring(arrayStart + 1);
        
        // 查找第一个对象的开始位置
        int objectStart = arrayContent.indexOf('{');
        if (objectStart == -1) {
            return textFields; // 没有对象开始标记，无法提取
        }

        // 从对象开始位置查找text字段
        String objectContent = arrayContent.substring(objectStart);
        
        // 查找text字段的位置（确保在对象内部）
        // 使用正则表达式精确匹配 "text": 后面跟着引号
        Pattern textPattern = Pattern.compile("\"text\"\\s*:\\s*\"");
        Matcher textMatcher = textPattern.matcher(objectContent);
        if (!textMatcher.find()) {
            return textFields; // 没有找到text字段
        }

        // text字段值的开始位置（跳过"text":和引号）
        int valueStart = textMatcher.end();

        // 提取text字段的值（可能未闭合）
        // 需要找到text值的结束位置，或者如果未闭合则取到buffer末尾
        String textValue = extractTextValue(objectContent, valueStart);
        
        if (textValue == null || textValue.isEmpty()) {
            return textFields;
        }

        // 验证提取的text值不是JSON结构的一部分
        // 如果text值包含JSON结构字符（如":, {, }等），可能是提取错误
        if (isJsonStructure(textValue)) {
            return textFields; // 可能是JSON结构，不提取
        }

        // 处理转义字符
        String extractedText = unescapeJsonString(textValue);
        
        // 提取type和subType（从对象内容中提取）
        String type = extractFieldValueFromObject(objectContent, "type");
        String subType = extractFieldValueFromObject(objectContent, "subType");
        
        if (type != null && subType != null) {
            textFields.add(new TextFieldInfo(0, type, subType, extractedText));
        }
        
        return textFields;
    }
    
    /**
     * 从对象内容中提取text字段的值
     * 处理未闭合的字符串情况
     *
     * @param objectContent 对象内容
     * @param valueStart    text字段值的开始位置
     * @return text字段的值，如果无法提取则返回null
     */
    private String extractTextValue(String objectContent, int valueStart) {
        if (valueStart >= objectContent.length()) {
            return null;
        }
        
        // 查找闭合的引号（考虑转义字符）
        int endPos = valueStart;
        boolean foundEnd = false;
        
        while (endPos < objectContent.length()) {
            char c = objectContent.charAt(endPos);
            if (c == '\\') {
                // 转义字符，跳过下一个字符
                endPos += 2;
                continue;
            } else if (c == '"') {
                // 找到闭合引号
                foundEnd = true;
                break;
            }
            endPos++;
        }
        
        if (foundEnd) {
            // 提取闭合的text值
            return objectContent.substring(valueStart, endPos);
        } else {
            // 未闭合，提取到末尾（流式传输中）
            return objectContent.substring(valueStart);
        }
    }
    
    /**
     * 从对象内容中提取指定字段的值
     *
     * @param objectContent 对象内容
     * @param fieldName     字段名
     * @return 字段值，如果不存在则返回null
     */
    private String extractFieldValueFromObject(String objectContent, String fieldName) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]+)\""
        );
        Matcher matcher = pattern.matcher(objectContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 从buffer中提取指定字段的值
     *
     * @param buffer    JSON buffer
     * @param fieldName 字段名
     * @return 字段值，如果不存在则返回null
     */
    public String extractFieldValue(String buffer, String fieldName) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]+)\""
        );
        Matcher matcher = pattern.matcher(buffer);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 判断字符串是否是JSON结构的一部分
     * 如果包含JSON结构字符（如":, {, }等），可能是提取错误
     *
     * @param text 待检查的文本
     * @return 如果是JSON结构则返回true
     */
    private boolean isJsonStructure(String text) {
        // 检查是否包含明显的JSON结构模式
        // 如果包含":（字段分隔符，后面跟着空格和引号），可能是JSON结构
        // 例如："subType\": \"bubble\"
        if (text.contains("\": \"") || text.contains("\":\"")) {
            return true;
        }
        // 如果包含未转义的{或}，可能是JSON结构
        if (text.contains("{") || text.contains("}")) {
            return true;
        }
        return false;
    }
    
    /**
     * 处理JSON字符串中的转义字符
     *
     * @param jsonString JSON字符串（可能包含转义字符）
     * @return 处理后的字符串
     */
    private String unescapeJsonString(String jsonString) {
        return jsonString.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
    
    /**
     * 从不完整 JSON 中解析出的单个数组元素的 text 及元数据（当前实现固定索引 0）。
     */
    public static class TextFieldInfo {
        /** 在 answer 数组中的下标 */
        private final int itemIndex;
        /** 与 Dify 返回的 type 一致 */
        private final String type;
        /** 与 Dify 返回的 subType 一致（如 bubble、card） */
        private final String subType;
        /** 已解码的 text 片段（可能仍在增长） */
        private final String text;

        /**
         * @param itemIndex 数组元素下标
         * @param type      元素 type
         * @param subType   元素 subType
         * @param text      当前 text 内容
         */
        public TextFieldInfo(int itemIndex, String type, String subType, String text) {
            this.itemIndex = itemIndex;
            this.type = type;
            this.subType = subType;
            this.text = text;
        }
        
        public int getItemIndex() {
            return itemIndex;
        }
        
        public String getType() {
            return type;
        }
        
        public String getSubType() {
            return subType;
        }
        
        public String getText() {
            return text;
        }
    }
}
