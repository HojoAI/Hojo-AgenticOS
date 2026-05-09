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

import java.util.List;
import java.util.Map;

/**
 * 友盟推送服务接口
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface UMengDomainService {

    /**
     * 发送通知给单个用户
     *
     * @param userId  用户ID（必填）
     * @param title   通知标题
     * @param content 通知内容
     * @param extra   额外参数（可选）
     * @return 是否发送成功
     */
    boolean sendNotification(String userId, String title, String content, Map<String, Object> extra);

    /**
     * 发送通知给单个用户（简化版）
     *
     * @param userId  用户ID（必填）
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendNotification(String userId, String title, String content);

    /**
     * 发送通知给多个用户
     *
     * @param userIds 用户ID列表（必填）
     * @param title   通知标题
     * @param content 通知内容
     * @param extra   额外参数（可选）
     * @return 是否发送成功
     */
    boolean sendNotificationToUsers(List<String> userIds, String title, String content, Map<String, Object> extra);

    /**
     * 广播通知给所有用户
     *
     * @param title   通知标题
     * @param content 通知内容
     * @param extra   额外参数（可选）
     * @return 是否发送成功
     */
    boolean broadcastNotification(String title, String content, Map<String, Object> extra);

    /**
     * 广播通知给所有用户（简化版）
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean broadcastNotification(String title, String content);

    /**
     * 发送自定义消息给单个用户（单播）
     *
     * @param userId      用户ID（必填）
     * @param title       消息标题
     * @param content     消息内容
     * @param deviceToken 设备Token（可选）
     * @param extra       额外参数（可选）
     * @return 是否发送成功
     */
    boolean unicastMessage(String userId, String title, String content, String deviceToken, Map<String, Object> extra);

    /**
     * 发送自定义消息给单个用户
     *
     * @param userId  用户ID（必填）
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   额外参数（可选）
     * @return 是否发送成功
     */
    boolean sendMessage(String userId, String title, String content, Map<String, Object> extra);

    /**
     * 广播自定义消息给所有用户
     *
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   额外参数（可选）
     * @return 是否发送成功
     */
    boolean broadcastMessage(String title, String content, Map<String, Object> extra);

    // =============================================== iOS 推送 ===============================================

    /**
     * 发送 iOS 通知给单个用户
     *
     * @param userId   用户ID（必填）
     * @param title    通知标题
     * @param subtitle 通知副标题（可选）
     * @param content  通知内容
     * @param extra    自定义键值对，与 aps 平级（可选）
     * @return 是否发送成功
     */
    boolean sendIosNotification(String userId, String title, String subtitle, String content, Map<String, Object> extra);

    /**
     * 发送 iOS 通知给单个用户（简化版）
     *
     * @param userId  用户ID（必填）
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendIosNotification(String userId, String title, String content);

    /**
     * 发送 iOS 通知给多个用户
     *
     * @param userIds  用户ID列表（必填）
     * @param title    通知标题
     * @param subtitle 通知副标题（可选）
     * @param content  通知内容
     * @param extra    自定义键值对，与 aps 平级（可选）
     * @return 是否发送成功
     */
    boolean sendIosNotificationToUsers(List<String> userIds, String title, String subtitle, String content, Map<String, Object> extra);

    /**
     * 广播 iOS 通知给所有用户
     *
     * @param title    通知标题
     * @param subtitle 通知副标题（可选）
     * @param content  通知内容
     * @param extra    自定义键值对，与 aps 平级（可选）
     * @return 是否发送成功
     */
    boolean broadcastIosNotification(String title, String subtitle, String content, Map<String, Object> extra);

    /**
     * 广播 iOS 通知给所有用户（简化版）
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean broadcastIosNotification(String title, String content);

    /**
     * iOS 单播（通过 deviceToken）
     *
     * @param userId      用户ID（必填）
     * @param title       通知标题
     * @param content     通知内容
     * @param deviceToken 设备Token（必填）
     * @param extra       自定义键值对，与 aps 平级（可选）
     * @return 是否发送成功
     */
    boolean unicastIosMessage(String userId, String title, String content, String deviceToken, Map<String, Object> extra);

    /**
     * iOS 静默推送（content-available=1，不弹通知栏）
     *
     * @param userId 用户ID（必填）
     * @param extra  自定义键值对，与 aps 平级（可选）
     * @return 是否发送成功
     */
    boolean sendIosSilentNotification(String userId, Map<String, Object> extra);

}
