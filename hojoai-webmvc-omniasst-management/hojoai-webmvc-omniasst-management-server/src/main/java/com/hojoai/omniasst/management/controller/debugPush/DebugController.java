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
package com.hojoai.omniasst.management.controller.debugPush;

import com.hojoai.central.provider.service.common.constants.PushChannelConstants;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.controller.debugPush.bean.DebugFcmPushRequest;
import com.hojoai.omniasst.management.service.notification.PushService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试接口，仅用于开发联调阶段，生产环境请关闭或鉴权保护。
 *
 * @author David
 * @version 1.0, 2026/03/31
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Resource
    private PushService pushService;

    /**
     * FCM 推送测试接口。
     * <p>
     * 直接将入参组装为 Firebase data payload 并推送，不写消息中心数据库，不影响线上数据。
     * 支持静默推送（data-only）和系统通知（notification + data）两种模式。
     * </p>
     *
     * <p><b>静默推送示例（silent=true）：</b></p>
     * <pre>
     * POST /omniasst-management/api/v0/debug/fcm-push
     * {
     *   "userId": "1308670880322211840",
     *   "silent": true,
     *   "action": "action.web.message",
     *   "params": {
     *     "action": "message",
     *     "messageId": "MSG_TEST_001",
     *     "data": {
     *       "totalUnread": 3,
     *       "type": "meeting",
     *       "title": "测试会议纪要",
     *       "body": "这是一条测试消息"
     *     }
     *   }
     * }
     * </pre>
     *
     * <p><b>系统通知示例（silent=false）：</b></p>
     * <pre>
     * {
     *   "userId": "1308670880322211840",
     *   "silent": false,
     *   "title": "云空间不足",
     *   "content": "您的云空间即将满额，请及时清理",
     *   "action": "action.web.navigation",
     *   "params": { "route": "/cloud" }
     * }
     * </pre>
     */
    @PostMapping("/fcm-push")
    public Response<String> testFcmPush(@RequestBody DebugFcmPushRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getAction(), "action");
        Asserts.notNull(request.getParams(), "params");

        Map<String, String> firebaseData = new HashMap<>();
        firebaseData.put("action", request.getAction());
        firebaseData.put("params", JacksonUtil.bean2Json(request.getParams()));

        PushRequestDTO dto = new PushRequestDTO();
        dto.setUserIdList(List.of(request.getUserId()));
        dto.setPushChannel(PushChannelConstants.FCM);
        dto.setSilent(Boolean.TRUE.equals(request.getSilent()));
        dto.setTitle(request.getTitle());
        dto.setContent(request.getContent());
        dto.setBroadcast(Boolean.FALSE);
        dto.setFirebaseData(firebaseData);
        dto.setDeviceToken(request.getDeviceToken());

        logger.info("[debug] FCM 推送测试, userId: {}, silent: {}, action: {}, params: {}",
                request.getUserId(), dto.getSilent(), request.getAction(),
                JacksonUtil.bean2Json(request.getParams()));

        return pushService.push(dto);
    }
}
