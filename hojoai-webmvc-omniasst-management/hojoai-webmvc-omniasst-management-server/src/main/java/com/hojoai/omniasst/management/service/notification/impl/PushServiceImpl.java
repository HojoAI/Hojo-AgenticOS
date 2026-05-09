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
package com.hojoai.omniasst.management.service.notification.impl;

import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.grpc.PushRespDTO;
import com.hojoai.central.provider.service.common.constants.PushChannelConstants;
import com.hojoai.central.provider.service.common.constants.RedisConstants;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.management.dao.message.PaMessageTaskDAO;
import com.hojoai.omniasst.management.dao.message.po.PaMessageTask;
import com.hojoai.omniasst.management.dto.request.PushUserDeviceTokenRequestDTO;
import com.hojoai.omniasst.management.service.message.MessageCenterService;
import com.hojoai.omniasst.management.service.message.bean.CreateMessageRequest;
import com.hojoai.omniasst.management.service.notification.PushService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 推送服务实现。FCM 推送前会根据 messageType 拦截落库、组装 Firebase data payload。
 *
 * @author My
 * @version 1.1, 2026/03/27
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class PushServiceImpl implements PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    private static final String MSG_TYPE_MESSAGE = "message";
    private static final String MSG_TYPE_TASK = "task";

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private MessageCenterService messageCenterService;

    @Resource
    private PaMessageTaskDAO paMessageTaskDAO;

    private static final Long REDIS_EXPIRE_TIME_ONE_YEAR = 365L;

    @Override
    public Response<String> push(PushRequestDTO requestDTO) {
        if (Objects.isNull(requestDTO)) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT);
        }

        if (isFcmChannel(requestDTO) && requestDTO.getFirebaseData() == null) {
            assembleFirebaseData(requestDTO);
            logger.info("[management-push] FCM payload 组装完成, pushChannel: {}, messageType: {}, silent: {}, firebaseData: {}",
                    requestDTO.getPushChannel(), requestDTO.getMessageType(), requestDTO.getSilent(),
                    JacksonUtil.bean2Json(requestDTO.getFirebaseData()));
        }

        String grpcPayload = JacksonUtil.bean2Json(requestDTO);
        logger.info("[management-push] 发起 gRPC 推送, payload: {}", grpcPayload);

        PushReqDTO.Builder builder = PushReqDTO.newBuilder();
        builder.setPushReqStr(grpcPayload);

        PushRespDTO respDTO = iConfigurationForGrpc.pushClient().push(builder.build());

        logger.info("[management-push] gRPC 推送结果: {}", respDTO.getSuccess());
        return respDTO.getSuccess() ? Response.newInstance(ResponseStatusBasic.OK) :
                Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
    }

    @Override
    public Response<String> userDeviceToken(PushUserDeviceTokenRequestDTO requestDTO) {
        String prefix = RedisConstants.REDIS_KEY_USER;
        if (PushChannelConstants.FCM.equalsIgnoreCase(StringUtils.trimToEmpty(requestDTO.getPushChannel()))) {
            prefix = RedisConstants.REDIS_KEY_USER_FCM;
        }
        redisTemplate.opsForValue().set(prefix.concat(requestDTO.getUserId()),
                requestDTO.getDeviceToken(), REDIS_EXPIRE_TIME_ONE_YEAR, TimeUnit.DAYS);

        return Response.newInstance(ResponseStatusBasic.OK);
    }

    // ==================== FCM payload 组装 ====================

    private boolean isFcmChannel(PushRequestDTO dto) {
        return PushChannelConstants.FCM.equalsIgnoreCase(StringUtils.trimToEmpty(dto.getPushChannel()));
    }

    /**
     * 根据 messageType 组装 Firebase data payload 并设置 silent 标志。
     * <ul>
     *   <li>messageType=message → 3.2.2 格式：落库消息、查未读数、组装 action.web.message</li>
     *   <li>messageType=task → 3.2.3 格式：查任务数与任务详情、组装 action.web.message</li>
     *   <li>其他 → 3.3 格式：系统通知、组装 action.web.navigation</li>
     * </ul>
     */
    private void assembleFirebaseData(PushRequestDTO dto) {
        String messageType = dto.getMessageType();
        Map<String, String> firebaseData = new HashMap<>();

        if (MSG_TYPE_MESSAGE.equals(messageType)) {
            assembleMessagePayload(dto, firebaseData);
            if (dto.getSilent() == null) {
                dto.setSilent(true);
            }
        } else if (MSG_TYPE_TASK.equals(messageType)) {
            assembleTaskPayload(dto, firebaseData);
            if (dto.getSilent() == null) {
                dto.setSilent(true);
            }
        } else {
            assembleNavigationPayload(dto, firebaseData);
            if (dto.getSilent() == null) {
                dto.setSilent(false);
            }
        }

        dto.setFirebaseData(firebaseData);
    }

    /**
     * 3.2.2：新消息推送 —— 先在消息表落库，再查未读总数，组装完整 payload。
     */
    private void assembleMessagePayload(PushRequestDTO dto, Map<String, String> firebaseData) {
        String userId = resolveFirstUserId(dto);
        if (userId == null) {
            logger.warn("assembleMessagePayload: userIdList 为空，跳过 payload 组装");
            return;
        }

        CreateMessageRequest createReq = new CreateMessageRequest();
        createReq.setUserId(userId);
        createReq.setType(dto.getType() != null ? dto.getType().getCode() : "system");
        createReq.setTitle(dto.getTitle());
        createReq.setBody(dto.getContent());
        createReq.setRelatedId(dto.getRelatedId());
        createReq.setRelatedTitle(dto.getRelatedTitle());
        createReq.setLandingPage(dto.getRoute());

        String messageId = messageCenterService.createMessage(createReq);
        long totalUnread = messageCenterService.getUnreadCount(userId).getTotalUnread();
        logger.info("[management-push] 消息落库完成, userId: {}, messageId: {}, totalUnread: {}", userId, messageId, totalUnread);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUnread", totalUnread);
        data.put("messageId", messageId);
        data.put("type", createReq.getType());
        data.put("title", dto.getTitle());
        data.put("body", dto.getContent());
        putIfNotBlank(data, "relatedId", dto.getRelatedId());
        putIfNotBlank(data, "relatedTitle", dto.getRelatedTitle());
        putIfNotBlank(data, "landingPage", dto.getRoute());
        data.put("createdAt", DateUtil.formatAsSecond(new Date()));

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("action", MSG_TYPE_MESSAGE);
        params.put("messageId", messageId);
        params.put("data", data);

        firebaseData.put("action", "action.web.message");
        firebaseData.put("params", JacksonUtil.bean2Json(params));
    }

    /**
     * 3.2.3：任务变更推送 —— 通过 relatedId（taskId）查任务详情，查进行中任务数，组装 payload。
     */
    private void assembleTaskPayload(PushRequestDTO dto, Map<String, String> firebaseData) {
        String userId = resolveFirstUserId(dto);
        if (userId == null) {
            logger.warn("assembleTaskPayload: userIdList 为空，跳过 payload 组装");
            return;
        }

        long runningCount = messageCenterService.getRunningTaskCount(userId).getCount();
        logger.info("[management-push] 查询进行中任务数, userId: {}, taskId: {}, runningCount: {}", userId, dto.getRelatedId(), runningCount);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", runningCount);

        String taskId = dto.getRelatedId();
        if (StringUtils.isNotBlank(taskId)) {
            PaMessageTask task = paMessageTaskDAO.selectByTaskId(taskId);
            if (task != null) {
                boolean isCompleted = "completed".equals(task.getStatus());
                data.put("taskId", task.getTaskId());
                data.put("type", task.getType());
                data.put("title", task.getTitle());
                // completed 对端侧展示为 finished，与进行中的 running 语义对称
                data.put("status", isCompleted ? "finished" : task.getStatus());
                if (isCompleted) {
                    data.put("progressText", "已完成");
                } else {
                    putIfNotBlank(data, "progressText", task.getProgressText());
                }
                if (task.getEstimatedSeconds() != null) {
                    data.put("estimatedSeconds", task.getEstimatedSeconds());
                }
                putIfNotBlank(data, "relatedId", task.getRelatedId());
                data.put("createdAt", DateUtil.formatAsSecond(task.getCreatedAt()));
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("action", MSG_TYPE_TASK);
        params.put("messageId", taskId);
        params.put("data", data);

        firebaseData.put("action", "action.web.message");
        firebaseData.put("params", JacksonUtil.bean2Json(params));
    }

    /**
     * 3.3：系统通知 —— 简单路由跳转。
     */
    private void assembleNavigationPayload(PushRequestDTO dto, Map<String, String> firebaseData) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(dto.getRoute())) {
            params.put("route", dto.getRoute());
        }

        firebaseData.put("action", "action.web.navigation");
        firebaseData.put("params", JacksonUtil.bean2Json(params));
    }

    private static String resolveFirstUserId(PushRequestDTO dto) {
        List<String> userIdList = dto.getUserIdList();
        return (userIdList != null && !userIdList.isEmpty()) ? userIdList.getFirst() : null;
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            map.put(key, value);
        }
    }
}
