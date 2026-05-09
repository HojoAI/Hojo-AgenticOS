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
package com.hojoai.central.provider.domain.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hojoai.central.provider.service.common.constants.UMengConstants;
import com.hojoai.central.provider.domain.UMengDomainService;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * The xxx class for xxx.
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Service
public class UMengDomainServiceImpl implements UMengDomainService {

    private static final Logger logger = LoggerFactory.getLogger(UMengDomainServiceImpl.class);

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${umeng.push.app-key}")
    private String umengAppKey;

    @Value("${umeng.push.master-secret}")
    private String umengMasterSecret;

    @Value("${umeng.push.production-mode}")
    private String umengProductionMode;

    @Value("${umeng.push.host}")
    private String umengHost;

    @Value("${umeng.push.default-activity}")
    private String umengDefaultActivity;

    @Value("${umeng.push.xiaomi.channel-id}")
    private String xiaomiChannelId;

    @Value("${umeng.push.xiaomi.activity}")
    private String xiaomiActivity;

    /**
     * 发送通知给单个用户
     */
    @Override
    public boolean sendNotification(String userId, String title, String content, Map<String, Object> extra) {
        if (!StringUtils.hasText(userId)) {
            logger.warn("发送通知失败: userId 不能为空");
            return false;
        }
        return sendPush(UMengConstants.TYPE_CUSTOMIZED, userId, title, content, UMengConstants.MSG_TYPE_NOTIFICATION, null, extra);
    }

    /**
     * 发送通知给多个用户
     */
    @Override
    public boolean sendNotificationToUsers(List<String> userIds, String title, String content, Map<String, Object> extra) {
        if (CollectionUtils.isEmpty(userIds)) {
            logger.warn("发送通知失败: userIds 不能为空");
            return false;
        }
        String aliases = String.join(",", userIds);
        return sendPush(UMengConstants.TYPE_CUSTOMIZED, aliases, title, content, UMengConstants.MSG_TYPE_NOTIFICATION, null, extra);
    }

    /**
     * 广播通知给所有用户
     */
    @Override
    public boolean broadcastNotification(String title, String content, Map<String, Object> extra) {
        return sendPush(UMengConstants.TYPE_BROADCAST, null, title, content, UMengConstants.MSG_TYPE_NOTIFICATION, null, extra);
    }

    /**
     * 发送自定义消息给单个用户
     */
    @Override
    public boolean sendMessage(String userId, String title, String content, Map<String, Object> extra) {
        if (!StringUtils.hasText(userId)) {
            logger.warn("发送自定义消息失败: userId 不能为空");
            return false;
        }
        return sendPush(UMengConstants.TYPE_CUSTOMIZED, userId, title, content, UMengConstants.MSG_TYPE_MESSAGE, null, extra);
    }

    /**
     * 广播自定义消息给所有用户
     */
    @Override
    public boolean broadcastMessage(String title, String content, Map<String, Object> extra) {
        return sendPush(UMengConstants.TYPE_BROADCAST, null, title, content, UMengConstants.MSG_TYPE_MESSAGE, null, extra);
    }

    /**
     * 发送通知（简化版）
     */
    @Override
    public boolean sendNotification(String userId, String title, String content) {
        return sendNotification(userId, title, content, null);
    }

    /**
     * 广播通知（简化版）
     */
    @Override
    public boolean broadcastNotification(String title, String content) {
        return broadcastNotification(title, content, null);
    }

    @Override
    public boolean unicastMessage(String userId, String title, String content, String deviceToken, Map<String, Object> extra) {
        return sendPush(UMengConstants.TYPE_UNICAST, userId, title, content, UMengConstants.MSG_TYPE_MESSAGE, deviceToken, extra);
    }

    // =============================================== iOS 推送实现 ===============================================

    @Override
    public boolean sendIosNotification(String userId, String title, String subtitle, String content, Map<String, Object> extra) {
        if (!StringUtils.hasText(userId)) {
            logger.warn("iOS 发送通知失败: userId 不能为空");
            return false;
        }
        return sendIosPush(UMengConstants.TYPE_CUSTOMIZED, userId, title, subtitle, content, null, false, extra);
    }

    @Override
    public boolean sendIosNotification(String userId, String title, String content) {
        return sendIosNotification(userId, title, null, content, null);
    }

    @Override
    public boolean sendIosNotificationToUsers(List<String> userIds, String title, String subtitle, String content, Map<String, Object> extra) {
        if (CollectionUtils.isEmpty(userIds)) {
            logger.warn("iOS 发送通知失败: userIds 不能为空");
            return false;
        }
        String aliases = String.join(",", userIds);
        return sendIosPush(UMengConstants.TYPE_CUSTOMIZED, aliases, title, subtitle, content, null, false, extra);
    }

    @Override
    public boolean broadcastIosNotification(String title, String subtitle, String content, Map<String, Object> extra) {
        return sendIosPush(UMengConstants.TYPE_BROADCAST, null, title, subtitle, content, null, false, extra);
    }

    @Override
    public boolean broadcastIosNotification(String title, String content) {
        return broadcastIosNotification(title, null, content, null);
    }

    @Override
    public boolean unicastIosMessage(String userId, String title, String content, String deviceToken, Map<String, Object> extra) {
        return sendIosPush(UMengConstants.TYPE_UNICAST, userId, title, null, content, deviceToken, false, extra);
    }

    @Override
    public boolean sendIosSilentNotification(String userId, Map<String, Object> extra) {
        if (!StringUtils.hasText(userId)) {
            logger.warn("iOS 静默推送失败: userId 不能为空");
            return false;
        }
        return sendIosPush(UMengConstants.TYPE_CUSTOMIZED, userId, null, null, null, null, true, extra);
    }

    // =============================================== Android 内部方法 ===============================================

    private boolean sendPush(String pushType, String target, String title, String content,
                             String msgType, String deviceToken, Map<String, Object> extra) {
        try {
            String jsonBody = buildRequestBody(pushType, target, title, content, msgType, deviceToken, extra);
            String sign = generateSign(jsonBody);
            String url = buildUrl(sign);

            logger.info("push begin request is -> {}", jsonBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, createHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getBody() == null) {
                logger.error("推送失败: 响应体为空, type={}, target={}", pushType, target);
                return false;
            }

            return parseResponse(response.getBody());

        } catch (Exception e) {
            logger.error("推送失败: type={}, target={}, title={}", pushType, target, title, e);
            return false;
        }
    }

    // =============================================== iOS 内部方法 ===============================================

    private boolean sendIosPush(String pushType, String target, String title, String subtitle,
                                String content, String deviceToken, boolean silent, Map<String, Object> extra) {
        try {
            String jsonBody = buildIosRequestBody(pushType, target, title, subtitle, content, deviceToken, silent, extra);
            String sign = generateSign(jsonBody);
            String url = buildUrl(sign);

            logger.info("iOS push begin request is -> {}", jsonBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, createHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getBody() == null) {
                logger.error("iOS 推送失败: 响应体为空, type={}, target={}", pushType, target);
                return false;
            }

            return parseResponse(response.getBody());

        } catch (Exception e) {
            logger.error("iOS 推送失败: type={}, target={}, title={}", pushType, target, title, e);
            return false;
        }
    }

    /**
     * 构建友盟 iOS APNs 请求体 JSON
     */
    private String buildIosRequestBody(String pushType, String target, String title, String subtitle,
                                       String content, String deviceToken, boolean silent,
                                       Map<String, Object> extra) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();

        body.put(UMengConstants.FIELD_APPKEY, umengAppKey);
        body.put(UMengConstants.FIELD_TIMESTAMP, System.currentTimeMillis() / 1000);
        body.put(UMengConstants.FIELD_TYPE, pushType);
        body.put(UMengConstants.FIELD_PRODUCTION_MODE, umengProductionMode);

        if (UMengConstants.TYPE_CUSTOMIZED.equals(pushType)) {
            body.put(UMengConstants.FIELD_ALIAS_TYPE, UMengConstants.ALIAS_TYPE);
            body.put(UMengConstants.FIELD_ALIAS, target);
        } else if (UMengConstants.TYPE_UNICAST.equals(pushType)) {
            body.put(UMengConstants.FIELD_DEVICE_TOKENS, deviceToken);
        }

        ObjectNode payload = objectMapper.createObjectNode();

        ObjectNode aps = objectMapper.createObjectNode();
        if (silent) {
            aps.put(UMengConstants.FIELD_CONTENT_AVAILABLE, 1);
        } else {
            ObjectNode alert = objectMapper.createObjectNode();
            alert.put(UMengConstants.FIELD_TITLE, getOrDefault(title, UMengConstants.DEFAULT_TITLE));
            if (StringUtils.hasText(subtitle)) {
                alert.put(UMengConstants.FIELD_SUBTITLE, subtitle);
            }
            alert.put(UMengConstants.FIELD_BODY, getOrDefault(content, UMengConstants.DEFAULT_CONTENT));
            aps.set(UMengConstants.FIELD_ALERT, alert);
            aps.put(UMengConstants.FIELD_SOUND, UMengConstants.DEFAULT_SOUND);
        }
        payload.set(UMengConstants.FIELD_APS, aps);

        if (extra != null && !extra.isEmpty()) {
            ObjectNode extraNode = objectMapper.valueToTree(extra);
            extraNode.fields().forEachRemaining(entry -> payload.set(entry.getKey(), entry.getValue()));
        }

        body.set(UMengConstants.FIELD_PAYLOAD, payload);

        return objectMapper.writeValueAsString(body);
    }

    // =============================================== Android 内部方法 ===============================================

    /**
     * 构建友盟API请求体JSON
     */
    private String buildRequestBody(String pushType, String target, String title, String content,
                                    String msgType, String deviceToken, Map<String, Object> extra) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();

        // 友盟必填参数
        body.put(UMengConstants.FIELD_APPKEY, umengAppKey);
        body.put(UMengConstants.FIELD_TIMESTAMP, System.currentTimeMillis() / 1000);
        // customizedcast 或 broadcast
        body.put(UMengConstants.FIELD_TYPE, pushType);
        body.put(UMengConstants.FIELD_PRODUCTION_MODE, umengProductionMode);

        //消息分类
        body.put(UMengConstants.FIELD_CATEGORY, UMengConstants.MESSAGE_CATEGORY);

        // 接收者参数（broadcast不需要alias）
        if (UMengConstants.TYPE_CUSTOMIZED.equals(pushType)) {
            body.put(UMengConstants.FIELD_ALIAS_TYPE, UMengConstants.ALIAS_TYPE);
            body.put(UMengConstants.FIELD_ALIAS, target);
        } else if (UMengConstants.TYPE_UNICAST.equals(pushType)) {
            body.put(UMengConstants.FIELD_DEVICE_TOKENS, deviceToken);
        }

        // 消息体
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put(UMengConstants.FIELD_DISPLAY_TYPE, msgType);  // notification 或 message

        ObjectNode msgBody = createMessageBody(msgType, title, content);
        payload.set(UMengConstants.FIELD_BODY, msgBody);

        // 额外参数
        if (extra != null && !extra.isEmpty()) {
            payload.set(UMengConstants.FIELD_EXTRA, objectMapper.valueToTree(extra));
        }

        body.set(UMengConstants.FIELD_PAYLOAD, payload);

        // 离线： 判断如果是通知消息，就添加channel_activity
        if (UMengConstants.MSG_TYPE_NOTIFICATION.equals(msgType)) {
            if (StringUtils.hasText(umengDefaultActivity)) {
                ObjectNode channelProps = objectMapper.createObjectNode();
                channelProps.put(UMengConstants.FIELD_CHANNEL_ACTIVITY, umengDefaultActivity);
                channelProps.put(UMengConstants.FIELD_XIAOMI_CHANNEL_ID, xiaomiChannelId);
                body.set(UMengConstants.FIELD_CHANNEL_PROPERTIES, channelProps);
                body.put(UMengConstants.FIELD_CHANNEL_FCM, "0");
                body.put(UMengConstants.FIELD_MI_PUSH, Boolean.TRUE);
                body.put(UMengConstants.FIELD_MI_ACTIVITY, xiaomiActivity);
            }
        }

        return objectMapper.writeValueAsString(body);
    }

    /**
     * 创建消息体，区分通知消息和自定义消息
     */
    private ObjectNode createMessageBody(String msgType, String title, String content) {
        ObjectNode msgBody = objectMapper.createObjectNode();

        if (UMengConstants.MSG_TYPE_NOTIFICATION.equals(msgType)) {
            // 1. 必填字段
            msgBody.put(UMengConstants.FIELD_TITLE, getOrDefault(title, UMengConstants.DEFAULT_TITLE));
            msgBody.put(UMengConstants.FIELD_TEXT, getOrDefault(content, UMengConstants.DEFAULT_CONTENT));
            // 2. 可选字段
            msgBody.put(UMengConstants.FIELD_TICKER, UMengConstants.DEFAULT_TICKER);
            msgBody.put(UMengConstants.FIELD_PLAY_VIBRATE, UMengConstants.VALUE_TRUE);
            msgBody.put(UMengConstants.FIELD_PLAY_LIGHTS, UMengConstants.VALUE_TRUE);
            msgBody.put(UMengConstants.FIELD_PLAY_SOUND, UMengConstants.VALUE_TRUE);
            msgBody.put(UMengConstants.FIELD_AFTER_OPEN, UMengConstants.AFTER_OPEN_GO_APP);
        } else {
            // 自定义消息：body里只需要custom字段
            if (StringUtils.hasText(title) || StringUtils.hasText(content)) {
                // 把title和content包装成JSON对象作为custom的值
                ObjectNode customContent = objectMapper.createObjectNode();

                if (StringUtils.hasText(title)) {
                    customContent.put(UMengConstants.FIELD_TITLE, title.trim());
                }
                if (StringUtils.hasText(content)) {
                    customContent.put(UMengConstants.FIELD_CONTENT, content.trim());
                }
                // custom字段的值是一个JSON对象
                msgBody.set(UMengConstants.FIELD_CUSTOM, customContent);
            } else {
                // 确保custom字段存在
                msgBody.put(UMengConstants.FIELD_CUSTOM, UMengConstants.EMPTY_JSON_OBJECT);
            }
        }
        return msgBody;
    }

    /**
     * 生成友盟API签名
     */
    private String generateSign(String requestBody) {
        String url = umengHost + UMengConstants.API_PATH;
        String signString = UMengConstants.HTTP_METHOD_POST + url + requestBody + umengMasterSecret;
        return DigestUtils.md5Hex(signString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构建完整的API URL
     */
    private String buildUrl(String sign) {
        return UriComponentsBuilder
                .fromUriString(umengHost + UMengConstants.API_PATH)
                .queryParam(UMengConstants.FIELD_SIGN, sign)
                .build()
                .toUriString();
    }

    /**
     * 创建HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", UMengConstants.USER_AGENT);
        return headers;
    }

    /**
     * 解析友盟API响应
     */
    private boolean parseResponse(String responseBody) {
        try {
            ObjectNode result = objectMapper.readValue(responseBody, ObjectNode.class);
            boolean success = UMengConstants.SUCCESS_CODE.equals(result.path(UMengConstants.FIELD_RET).asText());
            if (!success) {
                logger.error("推送失败: {}", responseBody);
            }
            logger.info("推送结果: {}", responseBody);
            return  success;
        } catch (Exception e) {
            logger.error("解析响应失败: {}", responseBody, e);
            return false;
        }
    }

    /**
     * 获取值，如果为空则返回默认值
     */
    private String getOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

}
