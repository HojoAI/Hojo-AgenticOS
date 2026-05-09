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
package com.hojoai.central.provider.service.push.impl;

import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.domain.FirebaseMessagingService;
import com.hojoai.central.provider.domain.UMengDomainService;
import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.grpc.PushRespDTO;
import com.hojoai.central.provider.grpc.PushServiceGrpc;
import com.hojoai.central.provider.service.common.constants.PushChannelConstants;
import com.hojoai.central.provider.service.common.constants.RedisConstants;
import com.hojoai.central.provider.service.common.constants.UMengConstants;
import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * gRPC 推送服务实现：按渠道（友盟 / FCM）分发，依赖 Redis 中的设备 token 缓存与可选的 Firebase Admin。
 */
@GrpcService
public class PushServiceImpl extends PushServiceGrpc.PushServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    @Resource
    private UMengDomainService umengDomainService;

    @Autowired(required = false)
    private FirebaseMessagingService firebaseMessagingService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 解析 JSON 推送请求并执行推送；非 FCM 渠道在成功后可能下发 Web 刷新类友盟消息。
     *
     * @param requestDTO     gRPC 请求，{@code pushReqStr} 为 {@link PushRequestDTO} 的 JSON
     * @param responseObserver gRPC 响应流
     */
    @Override
    public void push(PushReqDTO requestDTO, StreamObserver<PushRespDTO> responseObserver) {
        try {
            PushRespDTO.Builder builder = PushRespDTO.newBuilder();

            String pushReqStr = requestDTO.getPushReqStr();
            Asserts.notBlank(pushReqStr, "pushReqStr is blank");

            PushRequestDTO pushRequestDTO = JacksonUtil.json2Bean(pushReqStr, PushRequestDTO.class);

            logger.info("[central-push] 收到推送请求, pushChannel: {}, messageType: {}, silent: {}, userIdList: {}",
                    pushRequestDTO.getPushChannel(), pushRequestDTO.getMessageType(),
                    pushRequestDTO.getSilent(), pushRequestDTO.getUserIdList());

            boolean pushOk = this.executePush(pushRequestDTO);
            logger.info("[central-push] 推送执行结果: {}", pushOk);
            builder.setSuccess(pushOk);

            if (!PushChannelConstants.FCM.equalsIgnoreCase(StringUtils.trimToEmpty(pushRequestDTO.getPushChannel()))) {
                this.refreshHandlePush(pushRequestDTO);
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("推送异常", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private void refreshHandlePush(PushRequestDTO pushRequestDTO) {
        Map<String, Object> extra = new HashMap<>();

        extra.put(UMengConstants.FIELD_ACTION, AgentActionEnum.WEB_REFRESH_PAGE.getValue());

        Map<String, Object> pageMap = new HashMap<>();

        if (AgentTypeEnum.MEETING.equals(pushRequestDTO.getType())) {
            pageMap.put(UMengConstants.FIELD_PAGE_NAME, UMengConstants.MEETING_REFRESH);
        } else if (AgentTypeEnum.FOCUS_AI_DETAIL.equals(pushRequestDTO.getType())) {
            pageMap.put(UMengConstants.FIELD_PAGE_NAME, UMengConstants.AI_LOADING_FINISH);
        } else {
            return;
        }

        extra.put(UMengConstants.FIELD_PARAMS, pageMap);

        if (CollectionUtils.isNotEmpty(pushRequestDTO.getUserIdList())) {
            umengDomainService.unicastMessage(pushRequestDTO.getUserIdList().getFirst(), pushRequestDTO.getTitle(),
                    pushRequestDTO.getContent(), pushRequestDTO.getDeviceToken(), extra);
        }

    }

    /**
     * 执行推送（策略模式）
     *
     * @param pushRequestDTO 推送请求
     * @return 是否成功
     */
    private boolean executePush(PushRequestDTO pushRequestDTO) {
        if (PushChannelConstants.FCM.equalsIgnoreCase(StringUtils.trimToEmpty(pushRequestDTO.getPushChannel()))) {
            return executeFcmPush(pushRequestDTO);
        }

        String title = pushRequestDTO.getTitle();
        String content = pushRequestDTO.getContent();
        Map<String, Object> extra = buildExtra(pushRequestDTO);

        List<String> userIdList = pushRequestDTO.getUserIdList();
        Boolean isBroadcast = pushRequestDTO.getBroadcast();

        // 根据不同条件调用不同的推送方法
        if (Boolean.TRUE.equals(isBroadcast) || CollectionUtils.isEmpty(userIdList)) {
            // 广播推送
            logger.info("执行广播推送: title={}, content={}", title, content);
            return umengDomainService.broadcastNotification(title, content, extra);
        } else if (userIdList.size() == 1) {
            // 单个用户推送
            String userId = userIdList.getFirst();

            logger.info("执行单用户推送: userId={}, title={}, content={}", userId, title, content);

            if (redisTemplate.hasKey(RedisConstants.REDIS_KEY_USER.concat(userId))) {
                pushRequestDTO.setDeviceToken(redisTemplate.opsForValue().get(RedisConstants.REDIS_KEY_USER.concat(userId)));
            }

            if (AgentTypeEnum.MEETING.equals(pushRequestDTO.getType()) || AgentTypeEnum.TRANSLATION.equals(pushRequestDTO.getType())) {
                return umengDomainService.unicastMessage(userId, title, content, pushRequestDTO.getDeviceToken(), extra);
            } else if (AgentTypeEnum.FOCUS.equals(pushRequestDTO.getType())) {
                return umengDomainService.sendNotification(userId, title, content, extra);
            } else {
                return true;
            }
        } else {
            // 多个用户推送
            logger.info("执行多用户推送: userCount={}, title={}, content={}", userIdList.size(), title, content);
            return umengDomainService.sendNotificationToUsers(userIdList, title, content, extra);
        }
    }

    /**
     * Firebase Cloud Messaging：单播或多播。
     * 优先使用 management 组装的 {@code firebaseData}；若未提供则回退到 buildExtra 兜底。
     */
    private boolean executeFcmPush(PushRequestDTO pushRequestDTO) {
        if (firebaseMessagingService == null) {
            logger.error("pushChannel=fcm but FirebaseMessagingService not injected, check firebase.service-account.enabled");
            return false;
        }

        List<String> userIdList = pushRequestDTO.getUserIdList();
        if (Boolean.TRUE.equals(pushRequestDTO.getBroadcast()) || CollectionUtils.isEmpty(userIdList)) {
            logger.warn("FCM does not support broadcast or empty userIdList");
            return false;
        }

        boolean silent = Boolean.TRUE.equals(pushRequestDTO.getSilent());
        String title = pushRequestDTO.getTitle();
        String content = pushRequestDTO.getContent();

        Map<String, String> data = pushRequestDTO.getFirebaseData();
        if (data == null || data.isEmpty()) {
            data = toStringMap(buildExtra(pushRequestDTO));
        }

        logger.info("[central-push] 执行 FCM 推送, userCount: {}, silent: {}, dataKeys: {}", userIdList.size(), silent, data.keySet());

        boolean allOk = true;
        for (String userId : userIdList) {
            String token = resolveFcmDeviceToken(userId, pushRequestDTO);
            logger.info("[central-push] FCM token 解析, userId: {}, tokenResolved: {}", userId, token != null);
            boolean ok = firebaseMessagingService.sendToToken(token, title, content, data, silent);
            if (!ok) {
                allOk = false;
            }
        }
        return allOk;
    }

    private static Map<String, String> toStringMap(Map<String, Object> src) {
        if (src == null || src.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new HashMap<>();
        src.forEach((k, v) -> {
            if (k == null) return;
            if (v == null) out.put(k, "");
            else if (v instanceof String s) out.put(k, s);
            else out.put(k, JacksonUtil.bean2Json(v));
        });
        return out;
    }

    /**
     * 单用户且请求带 deviceToken 时优先使用；否则按 userId 读 Redis {@link RedisConstants#REDIS_KEY_USER_FCM}。
     */
    private String resolveFcmDeviceToken(String userId, PushRequestDTO pushRequestDTO) {
        if (userIdListSizeOne(pushRequestDTO) && StringUtils.isNotBlank(pushRequestDTO.getDeviceToken())) {
            return pushRequestDTO.getDeviceToken();
        }
        String key = RedisConstants.REDIS_KEY_USER_FCM.concat(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    private static boolean userIdListSizeOne(PushRequestDTO pushRequestDTO) {
        return pushRequestDTO.getUserIdList() != null && pushRequestDTO.getUserIdList().size() == 1;
    }

    /**
     * 构建额外参数
     *
     * @param pushRequestDTO 推送请求
     * @return 额外参数 Map
     */
    private Map<String, Object> buildExtra(PushRequestDTO pushRequestDTO) {
        Map<String, Object> extra = new HashMap<>();

        Optional.ofNullable(pushRequestDTO.getAction())
                .ifPresent(action -> extra.put(UMengConstants.FIELD_ACTION, action.getValue()));

        if (AgentActionEnum.WEB_NAVIGATION.equals(pushRequestDTO.getAction())) {
            Map<String, String> routeMap = new HashMap<>();

            routeMap.put(UMengConstants.FIELD_ROUTE, pushRequestDTO.getRoute());

            extra.put(UMengConstants.FIELD_PARAMS, routeMap);
        } else {
            Optional.ofNullable(pushRequestDTO.getConversationInfo())
                    .ifPresent(param -> extra.put(UMengConstants.FIELD_PARAMS, pushRequestDTO.getConversationInfo()));
        }

        return extra;
    }

}