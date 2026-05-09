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
package com.hojoai.omniasst.weather.v2.stream;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.weather.entity.SAAgentRequest;
import com.hojoai.omniasst.weather.entity.SAAgentResponse;
import com.hojoai.omniasst.weather.v2.card.CardContext;
import com.hojoai.omniasst.weather.v2.card.CardHandlerRegistry;
import com.hojoai.omniasst.weather.v2.card.handler.WeatherCardHandler;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResponse;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResult;
import com.hojoai.omniasst.weather.v2.geo.GeoResult;
import com.hojoai.omniasst.weather.v2.geo.GeoService;
import com.hojoai.omniasst.weather.v2.service.caiyun.CaiyunDailyFilterService;
import com.hojoai.omniasst.weather.v2.service.caiyun.CaiyunWeatherService;
import com.hojoai.omniasst.weather.v2.date.DateParseService;
import com.hojoai.omniasst.weather.v2.slot.DayScope;
import com.hojoai.omniasst.weather.v2.slot.SlotExtractionResult;
import com.hojoai.omniasst.weather.v2.slot.SlotExtractionService;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 天气智能体 v2 流式编排：槽位解析 → 经纬度 → 彩云 API → 卡片映射 → SSE 返回
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Service
public class WeatherAgentStreamService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherAgentStreamService.class);

    /** 卡片处理器未自定义气泡时的占位文案 */
    private static final String BUBBLE_PLACEHOLDER = "正在为您查询天气…";

    /** 未获取到位置时的提示文案 */
    private static final String NO_LOCATION_MESSAGE =
            "未获取到位置信息，无法提供天气的查询，请前往设置开启位置权限，或直接告诉我你想要查询的地址。";

    /** 未解析到日期时的提示文案 */
    private static final String NO_DATE_MESSAGE =
            "未获取到对应日期的数据，无法提供天气的查询。";

    @Resource
    private SlotExtractionService slotExtractionService;
    @Resource
    private GeoService geoService;
    @Resource
    private CaiyunWeatherService caiyunWeatherService;
    @Resource
    private CardHandlerRegistry cardHandlerRegistry;
    @Resource
    private DateParseService dateParseService;
    @Resource
    private CaiyunDailyFilterService caiyunDailyFilterService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 处理 /chat/v2 请求：在异步线程中执行编排逻辑，并通过 SSE 流式写出结果。
     * 异常时统一走错误兜底（两帧 bubble 文案），并清理 MDC。
     *
     * @param request 智能体请求（含 query、inputs、conversationId 等）
     * @param emitter  SSE 发射器，用于推送多帧响应
     */
    public void handleChatV2(SAAgentRequest request, SseEmitter emitter) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                doHandleChatV2(request, emitter);
            } catch (Exception e) {
                logger.error("v2 天气智能体处理异常, userId={}, query={}", request.getUserId(), request.getQuery(), e);
                sendErrorFallback(emitter);
            } finally {
                MDC.clear();
            }
        });
    }

    /**
     * v2 编排主流程：按步骤执行槽位解析、地点校验、经纬度解析、彩云 API、日期过滤、卡片映射，最后通过 SSE 发送三帧。
     * 无位置时在 stepEnsureLocation 中已发送响应并结束，此处不再继续。
     *
     * @param request 智能体请求
     * @param emitter SSE 发射器
     */
    private void doHandleChatV2(SAAgentRequest request, SseEmitter emitter) {
        String query = request.getQuery();
        Map<String, Object> inputs = request.getInputs() != null ? request.getInputs() : Map.of();

        // 1. 槽位解析：从 query 中抽取地点、卡片类型等
        SlotExtractionResult slot = stepExtractSlot(query);
        if (!stepEnsureLocation(slot, inputs, request, emitter)) {
            return; // 无可用地点，已发送「未获取位置」并结束
        }

        // 2. 经纬度解析 → 3. 日期解析（为空则发送「未获取日期」并结束）
        GeoResult geo = stepResolveGeo(slot, inputs);
        List<LocalDate> parsedDates = stepParseDates(query);
        if (parsedDates == null || parsedDates.isEmpty()) {
            sendNoDateResponse(request, emitter);
            return;
        }

        // 4. 彩云 API → 按日期过滤
        CaiyunWeatherResponse caiyunResponse = stepFetchCaiyunWeather(geo.getLongitude(), geo.getLatitude());
        CaiyunWeatherResult resultForCard = stepFilterResultByDates(caiyunResponse.getResult(), parsedDates);
        if (isFilteredResultEmpty(resultForCard)) {
            sendNoDateResponse(request, emitter);
            return;
        }

        // 5. 卡片映射 → 6. 发送三帧 SSE（bubble + card + 完整报文）
        CardMappingResult mapping = stepBuildCardMapping(slot, resultForCard, geo, parsedDates);
        sendSuccessFrames(request, emitter, mapping);
    }

    /**
     * Step1: 从用户 query 中解析槽位（地点、卡片类型等）。
     *
     * @param query 用户输入的问句
     * @return 槽位解析结果，可能包含 location、cardType 等
     */
    private SlotExtractionResult stepExtractSlot(String query) {
        logger.info("v2 槽位解析前: query={}", query);
        SlotExtractionResult slot = slotExtractionService.extract(query);
        logger.info("v2 槽位解析后: query={}, slot={}", query, JSON.toJSONString(slot));
        return slot;
    }

    /**
     * Step1.5: 确保有可用地点。优先用槽位中的地点，否则从 inputs 的 location（格式：经度,纬度）取；若仍为空则发送「未获取位置」响应并结束。
     *
     * @param slot   槽位解析结果，可能含 location
     * @param inputs 请求入参，可能含 location
     * @param request 当前请求，用于会话 ID 等
     * @param emitter SSE 发射器，无位置时用于发送提示帧
     * @return true 表示有地点可继续后续步骤，false 表示已发送响应，调用方应直接 return
     */
    private boolean stepEnsureLocation(SlotExtractionResult slot, Map<String, Object> inputs,
                                       SAAgentRequest request, SseEmitter emitter) {
        String locationFromSlot = slot != null ? slot.getLocation() : null;
        if (locationFromSlot != null && !locationFromSlot.isBlank()) {
            return true;
        }
        String locationFromInputs = getLocationFromInputs(inputs);
        if (locationFromInputs != null && !locationFromInputs.isBlank()) {
            return true;
        }
        sendNoLocationResponse(request, emitter);
        return false;
    }

    /**
     * Step2: 根据槽位与 inputs 解析经纬度及地点名称（槽位有地点时走地理编码，否则用 inputs 的 location）。
     *
     * @param slot   槽位解析结果
     * @param inputs 请求入参（含 location 时可直接解析）
     * @return 经纬度与地点名称
     */
    private GeoResult stepResolveGeo(SlotExtractionResult slot, Map<String, Object> inputs) {
        logger.info("v2 经纬度获取前: slot={}, inputs={}", JSON.toJSONString(slot), inputs);
        GeoResult geo = geoService.resolve(slot, inputs);
        logger.info("v2 经纬度获取后: lon={}, lat={}, locationName={}", geo.getLongitude(), geo.getLatitude(), geo.getLocationName());
        return geo;
    }

    /**
     * Step3: 调用彩云天气 API，固定请求 7 天数据，下游再按 query 解析出的日期过滤。
     *
     * @param lon 经度
     * @param lat 纬度
     * @return 彩云 API 原始响应，result 不为空
     * @throws IllegalStateException 当 API 返回或 result 为空时
     */
    private CaiyunWeatherResponse stepFetchCaiyunWeather(double lon, double lat) {
        int dailySteps = 15;
        logger.info("v2 彩云API调用前: lon={}, lat={}, dailySteps={}", lon, lat, dailySteps);
        CaiyunWeatherResponse response = caiyunWeatherService.getWeather(lon, lat, dailySteps);
        if (response == null || response.getResult() == null) {
            throw new IllegalStateException("彩云天气 API 返回为空");
        }
        logger.info("v2 彩云API调用后: status={}, result={}", response.getStatus(), response.getResult() != null);
        return response;
    }

    /**
     * Step3.5: 从 query 中解析日期（如「明天」「下周」），单日返回单元素列表，区间返回区间内全部日期。
     *
     * @param query 用户输入
     * @return 解析出的日期列表，用于过滤彩云逐日数据
     */
    private List<LocalDate> stepParseDates(String query) {
        List<LocalDate> parsedDates = dateParseService.parseDates(query);
        logger.info("v2 日期解析后: query={}, parsedDates={}", query, parsedDates);
        return parsedDates;
    }

    /**
     * Step3.6: 按解析出的日期过滤彩云逐日数据，例如用户问「明天」则只保留明天对应的日数据。
     *
     * @param result      彩云 API 的逐日结果
     * @param parsedDates 从 query 解析出的目标日期
     * @return 过滤后的逐日结果，供卡片使用
     */
    private CaiyunWeatherResult stepFilterResultByDates(CaiyunWeatherResult result, List<LocalDate> parsedDates) {
        return caiyunDailyFilterService.filterByDates(result, parsedDates);
    }

    /**
     * 判断过滤后的彩云逐日结果是否为空（无任何目标日期的数据）。
     * 例如用户问的日期超出彩云 API 返回范围时，过滤后 daily.temperature 为空。
     */
    private boolean isFilteredResultEmpty(CaiyunWeatherResult resultForCard) {
        if (resultForCard == null) {
            return true;
        }
        var daily = resultForCard.getDaily();
        if (daily == null) {
            return true;
        }
        var temp = daily.getTemperature();
        return temp == null || temp.isEmpty();
    }

    /**
     * Step5: 构建卡片上下文、按槽位卡片类型获取处理器（缺省用 WeatherOverview）、生成卡片数据与气泡文案。
     *
     * @param slot         槽位解析结果（含 cardType）
     * @param resultForCard 按日期过滤后的彩云逐日结果
     * @param geo          经纬度与地点名称
     * @param parsedDates  解析出的日期列表
     * @return 卡片类型、卡片数据、气泡文案，供 Step6 发送
     */
    private CardMappingResult stepBuildCardMapping(SlotExtractionResult slot, CaiyunWeatherResult resultForCard,
                                                   GeoResult geo, List<LocalDate> parsedDates) {
        CardContext cardContext = new CardContext(slot, resultForCard, geo, parsedDates);
        WeatherCardType cardType = slot.getCardType();
        logger.info("v2 卡片映射前: cardType={}, slot={}", cardType, JSON.toJSONString(slot));

        WeatherCardHandler handler = cardHandlerRegistry.getHandler(cardType);
        if (handler == null) {
            logger.warn("未找到卡片处理器: cardType={}, 使用 WeatherOverview", cardType);
            handler = cardHandlerRegistry.getHandler(WeatherCardType.WeatherOverview);
        }
        Map<String, Object> cardData = handler.buildCardData(cardContext);
        String bubbleText = handler.buildBubbleText(cardContext).orElse(BUBBLE_PLACEHOLDER);
        logger.info("v2 卡片映射后: cardType={}, cardDataKeys={}", cardType, cardData != null ? cardData.keySet() : null);

        return new CardMappingResult(cardType, cardData, bubbleText, slot.getDayScope());
    }

    /**
     * Step5: 按协议发送三帧 SSE——第一帧仅 bubble（finished=false）、第二帧仅 card（finished=false）、第三帧完整 data（finished=true）并 complete。
     * 发送异常时走错误兜底。
     *
     * @param request 当前请求（取 conversationId）
     * @param emitter SSE 发射器
     * @param mapping 卡片映射结果（气泡文案、卡片 JSON、卡片类型）
     */
    private void sendSuccessFrames(SAAgentRequest request, SseEmitter emitter, CardMappingResult mapping) {
        SAAgentResponse.SAAgentDataItem bubbleItem = new SAAgentResponse.SAAgentDataItem();
        bubbleItem.setType("text");
        bubbleItem.setSubType("bubble");
        bubbleItem.setText(mapping.bubbleText());

        // WeatherAirQuality + 多日：不发送卡片帧，仅气泡
        boolean skipCard = mapping.cardType() == WeatherCardType.WeatherAirQuality
                && mapping.dayScope() == DayScope.MULTI;

        SAAgentResponse.SAAgentDataItem cardItem = new SAAgentResponse.SAAgentDataItem();
        cardItem.setType("text");
        cardItem.setSubType("card");
        cardItem.setText(buildCardJson(mapping.cardType().name(), mapping.cardData()));

        List<SAAgentResponse.SAAgentDataItem> dataList = new ArrayList<>();
        dataList.add(bubbleItem);
        if (!skipCard) {
            dataList.add(cardItem);
        }

        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        long timestamp = System.currentTimeMillis() / 1000;

        try {
            sendSingleFrame(emitter, conversationId, bubbleItem, false, timestamp);
            logger.info("v2 发送 SSE 第一帧 bubbleText: finished=false");

            if (!skipCard) {
                sendSingleFrame(emitter, conversationId, cardItem, false, timestamp);
                logger.info("v2 发送 SSE 第二帧 card: finished=false");
            }

            SAAgentResponse finalFrame = new SAAgentResponse();
            finalFrame.setConversationId(conversationId);
            finalFrame.setMessageId("");
            finalFrame.setData(dataList);
            finalFrame.setFinished(Boolean.TRUE);
            finalFrame.setTimestamp(timestamp);
            emitter.send(objectMapper.writeValueAsString(finalFrame));
            logger.info("v2 发送 SSE 第三帧完整报文: finished=true");
            emitter.complete();
        } catch (Exception e) {
            logger.error("v2 发送 SSE 失败", e);
            sendErrorFallback(emitter);
        }
    }

    /**
     * 发送单帧 SSE（bubble 或 card），用于成功流程的前两帧。
     *
     * @param emitter         SSE 发射器
     * @param conversationId  会话 ID
     * @param data            本帧数据（bubble 或 card）
     * @param finished        本帧是否标记为结束
     * @param timestamp       时间戳（秒）
     */
    private void sendSingleFrame(SseEmitter emitter, String conversationId,
                                 SAAgentResponse.SAAgentDataItem data, boolean finished, long timestamp) throws Exception {
        SAAgentResponse frame = new SAAgentResponse();
        frame.setConversationId(conversationId);
        frame.setMessageId("");
        frame.setData(data);
        frame.setFinished(finished);
        frame.setTimestamp(timestamp);
        emitter.send(objectMapper.writeValueAsString(frame));
    }

    /** 从 inputs 中取 location 字段（格式：经度,纬度），为空则返回 null */
    private String getLocationFromInputs(Map<String, Object> inputs) {
        if (inputs == null) return null;
        Object v = inputs.get("location");
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /** 未获取到位置时直接返回两帧 bubble 响应并结束 */
    private void sendNoLocationResponse(SAAgentRequest request, SseEmitter emitter) {
        String conversationId = request.getConversationId() != null ? request.getConversationId() : "";
        sendBubbleOnlyResponse(conversationId, NO_LOCATION_MESSAGE, emitter);
    }

    /** 未解析到日期时：与未获取位置相同方式，发送两帧 bubble 并结束 */
    private void sendNoDateResponse(SAAgentRequest request, SseEmitter emitter) {
        String conversationId = request.getConversationId() != null ? request.getConversationId() : "";
        sendBubbleOnlyResponse(conversationId, NO_DATE_MESSAGE, emitter);
    }

    /** 错误兜底：返回两帧 bubble 文案 */
    private void sendErrorFallback(SseEmitter emitter) {
        String errorMessage = "抱歉啊...出于某些原因，这个问题现在我没法回答你，也许你可以换个问法试试？";
        sendBubbleOnlyResponse("", errorMessage, emitter);
    }

    /**
     * 仅返回 bubble 文案的两帧：第一帧 finished=false，第二帧 finished=true。
     * 供未获取位置、错误兜底等场景复用。
     *
     * @param conversationId 会话 ID
     * @param text           bubble 文案
     * @param emitter        SSE 发射器
     */
    private void sendBubbleOnlyResponse(String conversationId, String text, SseEmitter emitter) {
        long timestamp = System.currentTimeMillis() / 1000;
        SAAgentResponse.SAAgentDataItem bubbleItem = new SAAgentResponse.SAAgentDataItem();
        bubbleItem.setType("text");
        bubbleItem.setSubType("bubble");
        bubbleItem.setText(text);

        SAAgentResponse first = new SAAgentResponse();
        first.setConversationId(conversationId);
        first.setMessageId("");
        first.setData(bubbleItem);
        first.setFinished(Boolean.FALSE);
        first.setTimestamp(timestamp);

        SAAgentResponse second = new SAAgentResponse();
        second.setConversationId(conversationId);
        second.setMessageId("");
        second.setData(bubbleItem);
        second.setFinished(Boolean.TRUE);
        second.setTimestamp(timestamp);

        try {
            emitter.send(objectMapper.writeValueAsString(first));
            emitter.send(objectMapper.writeValueAsString(second));
            emitter.complete();
        } catch (Exception e) {
            logger.error("发送 bubble 两帧响应失败", e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
        }
    }

    /** 卡片映射结果：卡片类型、卡片数据、气泡文案、单日/多日，用于 SSE 发送。 */
    private record CardMappingResult(WeatherCardType cardType, Map<String, Object> cardData, String bubbleText, DayScope dayScope) {}

    /** 组装卡片第二段 text：{"name":"WeatherOverview","type":"component","data":{...}} */
    private String buildCardJson(String cardName, Map<String, Object> data) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", cardName);
        card.put("type", "component");
        card.put("data", data);
        try {
            return objectMapper.writeValueAsString(card);
        } catch (JsonProcessingException e) {
            logger.error("序列化卡片 JSON 失败", e);
            return "{}";
        }
    }

    
}
