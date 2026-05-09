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
package com.hojoai.omniasst.sysagent.push.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hojoai.omniasst.sysagent.configure.UmengPushProperties;
import com.hojoai.omniasst.sysagent.push.constants.PushConstants;
import com.hojoai.omniasst.sysagent.push.model.*;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 友盟 OpenAPI 推送执行器：组装 JSON、签名、HTTP POST，并解析响应为 {@link PushResult}。
 * <p>依赖 {@link UmengPushProperties} 与全局 {@link RestTemplate}。</p>
 */
@Component
public class UmengPushUtil {
    private static final Logger logger = LoggerFactory.getLogger(UmengPushUtil.class);

    @Resource private UmengPushProperties properties;
    @Resource private RestTemplate restTemplate;
    @Resource private ObjectMapper objectMapper;

    /** 未传 description 时的默认前缀。 */
    private static final String DEFAULT_DESC_PREFIX = "推送_";
    /** Android 通知栏 ticker 默认值（若协议需要）。 */
    private static final String DEFAULT_TICKER = "新消息";

    /**
     * 发送一条友盟推送请求。
     *
     * @param request 推送类型、受众、payload 等；非法时返回失败 {@link PushResult}
     * @return 成功含 msgId/taskId；失败含 errorCode/errorMsg
     */
    public PushResult sendPush(PushRequest request) {
        // 参数验证
        if (request == null) {
            logger.warn("推送请求为空");
            return PushResult.fail("推送请求不能为空", "NULL_REQUEST");
        }
        if (request.getType() == null || request.getType().isEmpty()) {
            logger.warn("推送类型为空");
            return PushResult.fail("推送类型不能为空", "EMPTY_TYPE");
        }

        logger.info("推送开始: {}", request.getType());

        try {
            String requestBody = buildRequestBody(request);
            String sign = generateSign(requestBody);
            String url = buildUrl(sign);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(requestBody, createHeaders()), String.class);

            return handleResponse(response.getBody());

        } catch (JsonProcessingException e) {
            logger.warn("JSON处理失败: {}", e.getMessage());
            return PushResult.fail("请求格式错误", "JSON_ERROR");
        } catch (Exception e) {
            logger.warn("推送请求失败: {}", e.getMessage());
            return PushResult.fail("推送服务异常", "SERVICE_ERROR");
        }
    }

    private String buildRequestBody(PushRequest request) throws JsonProcessingException {
        ObjectNode body = objectMapper.createObjectNode();

        body.put("appkey", properties.getAppKey());
        body.put("timestamp", System.currentTimeMillis() / 1000);
        body.put("type", request.getType());
        body.put("production_mode", boolToStr(request.isProductionMode()));

        String desc = request.getDescription();
        body.put("description", desc != null && !desc.isEmpty() ? desc : DEFAULT_DESC_PREFIX + System.currentTimeMillis());

        addRecipientParams(body, request);
        addPayload(body, request);

        return objectMapper.writeValueAsString(body);
    }

    private void addRecipientParams(ObjectNode body, PushRequest request) {
        String type = request.getType();

        switch (type) {
            case PushConstants.PUSH_TYPE_UNICAST:
            case PushConstants.PUSH_TYPE_LISTCAST:
                if (request.getDeviceTokens() == null || request.getDeviceTokens().isEmpty()) {
                    throw new IllegalArgumentException("device_tokens不能为空");
                }
                body.put("device_tokens", request.getDeviceTokens());
                break;

            case PushConstants.PUSH_TYPE_CUSTOMIZED:
                if (request.getAliasType() == null || request.getAliasType().isEmpty()) {
                    throw new IllegalArgumentException("alias_type不能为空");
                }
                if (request.getAlias() == null || request.getAlias().isEmpty()) {
                    throw new IllegalArgumentException("alias不能为空");
                }
                body.put("alias_type", request.getAliasType());
                body.put("alias", request.getAlias());
                break;

            case PushConstants.PUSH_TYPE_FILECAST:
                if (request.getFileId() == null || request.getFileId().isEmpty()) {
                    throw new IllegalArgumentException("file_id不能为空");
                }
                body.put("file_id", request.getFileId());
                break;

            case PushConstants.PUSH_TYPE_GROUPCAST:
                if (request.getFilter() != null) {
                    body.set("filter", objectMapper.convertValue(request.getFilter(), ObjectNode.class));
                } else if (request.getGroupId() != null && !request.getGroupId().isEmpty()) {
                    body.put("group_id", request.getGroupId());
                } else {
                    throw new IllegalArgumentException("filter或group_id不能为空");
                }
                break;

            case PushConstants.PUSH_TYPE_BROADCAST:
                break;

            default:
                throw new IllegalArgumentException("不支持的推送类型");
        }
    }

    private void addPayload(ObjectNode body, PushRequest request) {
        body.set("payload", request.getPayload() != null ?
                convertPayload(request.getPayload()) : createDefaultPayload());
    }

    private ObjectNode convertPayload(AndroidPayload payload) {
        ObjectNode payloadNode = objectMapper.createObjectNode();

        String displayType = getOrDefault(payload.getDisplayType(), PushConstants.DISPLAY_TYPE_NOTIFICATION);
        payloadNode.put("display_type", displayType);

        if (PushConstants.DISPLAY_TYPE_NOTIFICATION.equals(displayType)) {
            payloadNode.set("body", createNotificationBody(payload.getBody()));
        } else if (!PushConstants.DISPLAY_TYPE_MESSAGE.equals(displayType)) {
            logger.debug("不支持的消息类型: {}", displayType);
            payloadNode.put("display_type", PushConstants.DISPLAY_TYPE_NOTIFICATION);
            payloadNode.set("body", createSimpleBody());
        }

        addExtraParams(payloadNode, payload.getExtra());
        return payloadNode;
    }

    private ObjectNode createNotificationBody(PayloadBody body) {
        ObjectNode bodyNode = objectMapper.createObjectNode();

        if (body != null) {
            bodyNode.put("title", getOrDefault(body.getTitle(), PushConstants.DEFAULT_TITLE));
            bodyNode.put("text", getOrDefault(body.getText(), PushConstants.DEFAULT_CONTENT));

            if (body.getTicker() != null && !body.getTicker().isEmpty()) {
                bodyNode.put("ticker", body.getTicker());
            }

            bodyNode.put("play_vibrate", boolToStr(body.isPlayVibrate()));
            bodyNode.put("play_lights", boolToStr(body.isPlayLights()));
            bodyNode.put("play_sound", boolToStr(body.isPlaySound()));
            bodyNode.put("after_open", getOrDefault(body.getAfterOpen(), PushConstants.DEFAULT_AFTER_OPEN));
        } else {
            createDefaultBody(bodyNode);
        }

        return bodyNode;
    }

    private void createDefaultBody(ObjectNode bodyNode) {
        bodyNode.put("title", PushConstants.DEFAULT_TITLE);
        bodyNode.put("text", PushConstants.DEFAULT_CONTENT);
        bodyNode.put("ticker", DEFAULT_TICKER);
        bodyNode.put("play_vibrate", boolToStr(PushConstants.DEFAULT_PLAY_VIBRATE));
        bodyNode.put("play_lights", boolToStr(PushConstants.DEFAULT_PLAY_LIGHTS));
        bodyNode.put("play_sound", boolToStr(PushConstants.DEFAULT_PLAY_SOUND));
        bodyNode.put("after_open", PushConstants.DEFAULT_AFTER_OPEN);
    }

    private ObjectNode createSimpleBody() {
        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("title", PushConstants.DEFAULT_TITLE);
        bodyNode.put("text", PushConstants.DEFAULT_CONTENT);
        bodyNode.put("after_open", PushConstants.DEFAULT_AFTER_OPEN);
        return bodyNode;
    }

    private ObjectNode createDefaultPayload() {
        ObjectNode payloadNode = objectMapper.createObjectNode();
        payloadNode.put("display_type", PushConstants.DISPLAY_TYPE_NOTIFICATION);

        ObjectNode bodyNode = objectMapper.createObjectNode();
        createDefaultBody(bodyNode);
        payloadNode.set("body", bodyNode);

        return payloadNode;
    }

    private void addExtraParams(ObjectNode payloadNode, Map<String, Object> extra) {
        if (extra != null && !extra.isEmpty()) {
            try {
                ObjectNode extraNode = objectMapper.convertValue(extra, ObjectNode.class);
                payloadNode.set("extra", extraNode);
            } catch (Exception e) {
                logger.debug("extra参数转换失败");
            }
        }
    }

    private String generateSign(String requestBody) {
        String url = properties.getHost() + PushConstants.API_SEND;
        String signString = "POST" + url + requestBody + properties.getMasterSecret();
        return DigestUtils.md5Hex(signString.getBytes(StandardCharsets.UTF_8));
    }

    private String buildUrl(String sign) {
        return properties.getHost() + PushConstants.API_SEND + "?sign=" + sign;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", PushConstants.USER_AGENT);
        return headers;
    }

    private PushResult handleResponse(String responseBody) {
        if (responseBody == null) {
            return PushResult.fail("响应为空", "EMPTY_RESPONSE");
        }

        try {
            ObjectNode result = objectMapper.readValue(responseBody, ObjectNode.class);
            String ret = result.get("ret").asText();

            if (PushConstants.SUCCESS_CODE.equals(ret)) {
                return extractSuccessData(result);
            } else {
                return extractErrorData(result);
            }
        } catch (Exception e) {
            logger.warn("解析响应失败: {}", e.getMessage());
            return PushResult.fail("解析响应失败", "PARSE_ERROR");
        }
    }

    private PushResult extractSuccessData(ObjectNode result) {
        String msgId = "", taskId = "";

        if (result.has("data")) {
            ObjectNode data = (ObjectNode) result.get("data");
            msgId = data.has("msg_id") ? data.get("msg_id").asText() : "";
            taskId = data.has("task_id") ? data.get("task_id").asText() : "";
        }

        return PushResult.success(msgId, taskId);
    }

    private PushResult extractErrorData(ObjectNode result) {
        String errorCode = "UNKNOWN";
        String errorMsg = "未知错误";

        if (result.has("data") && result.get("data").isObject()) {
            ObjectNode data = (ObjectNode) result.get("data");
            errorCode = data.has("error_code") ? data.get("error_code").asText() : "UNKNOWN";
            errorMsg = data.has("error_msg") ? data.get("error_msg").asText() : "未知错误";
        }

        logger.warn("推送失败: {} - {}", errorCode, errorMsg);
        return PushResult.fail(errorMsg, errorCode);
    }

    private String boolToStr(boolean value) {
        return value ? PushConstants.TRUE_STR : PushConstants.FALSE_STR;
    }

    private String getOrDefault(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
}