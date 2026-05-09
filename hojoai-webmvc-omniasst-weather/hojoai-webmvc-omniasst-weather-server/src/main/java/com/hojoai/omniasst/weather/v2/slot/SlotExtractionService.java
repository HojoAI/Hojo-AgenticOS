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
package com.hojoai.omniasst.weather.v2.slot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.weather.v2.config.LlmProperties;
import com.hojoai.omniasst.weather.v2.config.PromptProperties;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 意图/槽位解析服务：调用大模型提取地点、单日/多日、卡片类型；失败时回退到规则。
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Service
public class SlotExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(SlotExtractionService.class);

    private final CloseableHttpClient httpClient;
    private final LlmProperties llmProperties;
    private final PromptProperties promptProperties;
    private final ObjectMapper objectMapper;

    /**
     * @param httpClientManager  调用大模型 API 的 HTTP 客户端
     * @param llmProperties      模型 endpoint、apiKey 等
     * @param promptProperties   槽位抽取系统提示词（可刷新）
     * @param objectMapper       请求/响应 JSON
     */
    public SlotExtractionService(com.hojoai.omniasst.weather.service.dify.HttpClientManager httpClientManager,
                                 LlmProperties llmProperties,
                                 PromptProperties promptProperties,
                                 ObjectMapper objectMapper) {
        this.httpClient = httpClientManager.getHttpClient();
        this.llmProperties = llmProperties;
        this.promptProperties = promptProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据用户 query 解析槽位：优先调用大模型，失败或未配置时回退规则。
     *
     * @param query 用户输入
     * @return 槽位结果
     */
    public SlotExtractionResult extract(String query) {
        if (query == null || query.isBlank()) {
            return new SlotExtractionResult(null, DayScope.SINGLE, WeatherCardType.WeatherOverview);
        }
        String q = query.trim();

        SlotExtractionResult llmResult = callLlm(q);
        if (llmResult != null) {
            SlotExtractionResult corrected = ensureMultiDayWhenQuerySaysSo(q, llmResult);
            logger.debug("槽位解析(模型): query={}, result={}", query, corrected);
            return corrected;
        }

        WeatherCardType cardType = inferCardType(q);
        DayScope dayScope = inferDayScope(q);
        String location = inferLocation(q);
        SlotExtractionResult result = new SlotExtractionResult(location, dayScope, cardType);
        logger.debug("槽位解析(规则回退): query={}, result={}", query, result);
        return result;
    }

    /**
     * 调用 DashScope 兼容 Chat Completions API，解析返回 JSON 为 SlotExtractionResult；失败返回 null。
     */
    private SlotExtractionResult callLlm(String query) {
        String apiKey = llmProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            logger.debug("槽位抽取未配置 api_key，使用规则回退");
            return null;
        }
        String systemPrompt = promptProperties != null ? promptProperties.getSlotExtractionSystem() : null;
        if (systemPrompt == null || systemPrompt.isBlank()) {
            logger.debug("槽位抽取系统提示词未配置（prompt.slot-extraction-system），使用规则回退");
            return null;
        }

        String baseUrl = llmProperties.getBaseUrl();
        if (baseUrl == null) baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", llmProperties.getModelName());
        body.put("max_tokens", llmProperties.getMaxTokens());
        body.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", query)
        });

        try {
            String requestBody = objectMapper.writeValueAsString(body);
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + apiKey.trim());
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)));

            String responseBody = httpClient.execute(post, response -> {
                int code = response.getCode();
                String entity = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (code != 200) {
                    logger.warn("槽位抽取 API 非 200: code={}, body={}", code, entity);
                    return null;
                }
                return entity;
            });

            if (responseBody == null || responseBody.isBlank()) return null;

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) return null;
            JsonNode msg = choices.get(0).path("message").path("content");
            String content = msg.isMissingNode() || msg.isNull() ? "" : msg.asText();
            if (content.isBlank()) return null;

            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            int end = content.indexOf("```");
            if (end > 0) content = content.substring(0, end);
            content = content.trim();

            JsonNode node = objectMapper.readTree(content);
            String location = node.path("location").asText();
            if ("当前位置".equals(location) || location.isEmpty()) location = null;
            String dateRange = node.path("dateRange").asText();
            if (dateRange.isEmpty()) dateRange = "单日";
            DayScope dayScope = "多日".equals(dateRange) ? DayScope.MULTI : DayScope.SINGLE;
            String template = node.path("template").asText();
            if (template.isEmpty()) template = "WeatherOverview";
            WeatherCardType cardType = parseTemplate(template);

            return new SlotExtractionResult(location, dayScope, cardType);
        } catch (Exception e) {
            logger.warn("槽位抽取模型调用异常，使用规则回退: query={}", query, e);
            return null;
        }
    }

    private static WeatherCardType parseTemplate(String template) {
        if (template == null || "none".equalsIgnoreCase(template)) {
            return WeatherCardType.WeatherOverview;
        }
        try {
            return WeatherCardType.valueOf(template);
        } catch (IllegalArgumentException e) {
            return WeatherCardType.WeatherOverview;
        }
    }

    /**
     * 当 query 明确包含「几天」「未来…天」等多日表述，但模型返回了单日+概览时，纠正为多日+WeatherInfoMultiDay。
     */
    private static SlotExtractionResult ensureMultiDayWhenQuerySaysSo(String query, SlotExtractionResult result) {
        if (result == null) return result;
        boolean querySaysMultiDay = (query != null && (
                query.contains("几天") || (query.contains("未来") && query.contains("天"))));
        if (!querySaysMultiDay) return result;
        boolean currentlySingleOverview = result.getDayScope() == DayScope.SINGLE
                && result.getCardType() == WeatherCardType.WeatherOverview;
        if (!currentlySingleOverview) return result;
        return new SlotExtractionResult(
                result.getLocation(),
                DayScope.MULTI,
                WeatherCardType.WeatherInfoMultiDay);
    }

    private WeatherCardType inferCardType(String q) {
        if (q.contains("温度") || q.contains("几度")) {
            return WeatherCardType.WeatherDailyFallback;
        }
        if (q.contains("几天") || q.contains("未来") && (q.contains("天") || q.contains("周"))) {
            return WeatherCardType.WeatherInfoMultiDay;
        }
        if (q.contains("建议") || q.contains("穿衣") || q.contains("适合")) {
            return WeatherCardType.WeatherBehaviorAdvice;
        }
        if (q.contains("降雨") || q.contains("下雨") || q.contains("降水")) {
            return WeatherCardType.WeatherRain;
        }
        if (q.contains("风") && (q.contains("力") || q.contains("级") || q.contains("多大"))) {
            return WeatherCardType.WeatherWind;
        }
        if (q.contains("空气") || q.contains("空气质量") || q.contains("AQI") || q.contains("雾霾")) {
            return WeatherCardType.WeatherAirQuality;
        }
        return WeatherCardType.WeatherOverview;
    }

    private DayScope inferDayScope(String q) {
        if (q.contains("几天") || q.contains("未来") || q.contains("一周") || q.contains("一周内")) {
            return DayScope.MULTI;
        }
        return DayScope.SINGLE;
    }

    private String inferLocation(String q) {
        String[] cities = {"北京", "上海", "广州市", "深圳市", "杭州", "南京", "成都", "武汉", "西安", "重庆"};
        for (String city : cities) {
            if (q.contains(city)) {
                return city.endsWith("市") ? city : city;
            }
        }
        return null;
    }
}
