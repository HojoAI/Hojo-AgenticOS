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
package com.hojoai.webmvc.omniasst.translate.websocket.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.translate.websocket.IWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

/**
 * 带 {@code pdata} 包装的扩展消息：用于与端侧约定「payload.pdata」承载业务体。
 *
 * @param <T> 内层业务类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IMessageCustom<T> extends IMessage<T> {
    private static final Logger logger = LoggerFactory.getLogger(IMessageCustom.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /** 业务体在 JSON 中的字段名 */
    private static final String CUSTOM_KEY_PDATA = "pdata";

    /**
     * 构造下行消息（自定义事件字符串）。
     */
    public static <T> IMessageCustom<BackCustomPayloadMessage<T>> newInstanceCustom(String event, T eventContent) {
        try {
            IMessageCustom<BackCustomPayloadMessage<T>> response = new IMessageCustom<>();
            response.setEventType(event);
            BackCustomPayloadMessage<T> result = new BackCustomPayloadMessage<>();
            result.setPdata(eventContent);
            response.setEventContent(result);
            return response;
        } catch (Exception e) {
            logger.error("make business response exception", e);
            throw new UnsupportedOperationException("make business response exception");
        }
    }

    /**
     * 构造下行消息（使用 {@link IMessageEvent}）。
     */
    public static <T> IMessageCustom<BackCustomPayloadMessage<T>> newInstanceCustom(IMessageEvent event, T eventContent) {
        try {
            IMessageCustom<BackCustomPayloadMessage<T>> response = new IMessageCustom<>();
            response.setEventType(event.getEventType());
            BackCustomPayloadMessage<T> result = new BackCustomPayloadMessage<>();
            result.setPdata(eventContent);
            response.setEventContent(result);
            return response;
        } catch (Exception e) {
            logger.error("make business response exception", e);
            throw new UnsupportedOperationException("make business response exception");
        }
    }

    /**
     * 序列化并发送带 pdata 的 JSON 文本帧。
     */
    public static <T> void sendMessageCustom(WebSocketSession session, IMessageEvent event, T eventContent) {
        IMessageCustom<BackCustomPayloadMessage<Object>> messageCustom = IMessageCustom.newInstanceCustom(event, eventContent);
        IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(messageCustom));
    }

    /**
     * 从客户端消息中解析 {@code payload.pdata} 为指定类型。
     *
     * @param message    原始 JSON
     * @param targetClass 目标类型
     * @return 解析结果，缺字段时为 null
     * @throws IllegalArgumentException 入参非法时由 Jackson 抛出
     * @throws UnsupportedOperationException 解析失败时包装抛出
     */
    public static <T> T parseBusinessRequest(String message, Class<T> targetClass) {
        try {
            IMessageCustom<?> iMessageCustom = JacksonUtil.json2Bean(message, new TypeReference<>() {});
            if (Objects.isNull(iMessageCustom.getEventContent())) return null;
            String eventContentJson = JacksonUtil.bean2Json(iMessageCustom.getEventContent());
            JsonNode customPayloadNode = objectMapper.readTree(eventContentJson);
            if (Objects.isNull(customPayloadNode) || Objects.isNull(customPayloadNode.get(CUSTOM_KEY_PDATA))) return null;
            JsonNode pdataNode = customPayloadNode.get(CUSTOM_KEY_PDATA);
            return JacksonUtil.json2Bean(pdataNode.toString(), targetClass);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("parse business request exception", e);
            throw new UnsupportedOperationException("parse business request exception");
        }
    }
}

