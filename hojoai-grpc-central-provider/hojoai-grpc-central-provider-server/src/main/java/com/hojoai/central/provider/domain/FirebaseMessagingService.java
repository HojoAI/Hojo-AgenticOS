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
package com.hojoai.central.provider.domain;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Firebase Cloud Messaging 发送封装，支持静默推送（data-only）与系统通知（notification + data）。
 *
 * @author David
 * @version 1.1, 2026/03/27
 * @since hojoai-grpc-central-provider 1.0.0
 */
@Service
@ConditionalOnBean(FirebaseMessaging.class)
public class FirebaseMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * 发送 FCM 消息。根据 {@code silent} 决定是否携带 notification 字段。
     *
     * @param token   FCM Registration Token
     * @param title   通知标题（仅 silent=false 时展示在通知栏）
     * @param body    通知正文（仅 silent=false 时展示在通知栏）
     * @param data    自定义 data payload，key/value 均为 String；可空
     * @param silent  true → data-only（不弹通知栏），false → notification + data（弹通知栏）
     * @return 是否发送成功
     */
    public boolean sendToToken(String token, String title, String body,
                               Map<String, String> data, boolean silent) {
        if (StringUtils.isBlank(token)) {
            logger.warn("[central-push] FCM token 为空，跳过推送");
            return false;
        }

        logger.info("[central-push] 准备发送 FCM, tokenPrefix: {}, silent: {}, dataKeys: {}",
                abbreviateToken(token), silent, data != null ? data.keySet() : null);

        Message.Builder builder = Message.builder().setToken(token.trim());

        if (!silent && (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(body))) {
            Notification.Builder nb = Notification.builder();
            if (StringUtils.isNotBlank(title)) {
                nb.setTitle(title);
            }
            if (StringUtils.isNotBlank(body)) {
                nb.setBody(body);
            }
            builder.setNotification(nb.build());
        }

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        try {
            String messageId = firebaseMessaging.send(builder.build());
            logger.info("[central-push] FCM 发送成功, messageId: {}, silent: {}", messageId, silent);
            return true;
        } catch (FirebaseMessagingException e) {
            logger.error("[central-push] FCM 发送失败, tokenPrefix: {}, silent: {}", abbreviateToken(token), silent, e);
            return false;
        }
    }

    private static String abbreviateToken(String token) {
        if (token == null || token.length() <= 12) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
