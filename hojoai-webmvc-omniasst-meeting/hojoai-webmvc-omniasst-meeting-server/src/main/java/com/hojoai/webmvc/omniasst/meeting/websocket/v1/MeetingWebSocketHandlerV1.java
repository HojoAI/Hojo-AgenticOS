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
package com.hojoai.webmvc.omniasst.meeting.websocket.v1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.meeting.api.VoiceBaseServiceClient;
import com.hojoai.webmvc.omniasst.meeting.common.GlobalConstants;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingPersistenceService;
import com.hojoai.webmvc.omniasst.meeting.websocket.IWebSocketHandler;
import com.hojoai.webmvc.omniasst.meeting.websocket.WebSocketConstants;
import com.hojoai.webmvc.omniasst.meeting.websocket.WebSocketHeartbeatManager;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.IMessage;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.IMessageCustom;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.IMessageEvent;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.*;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrConfigRequest;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketRequest;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketResponse;
import com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.AsrWebSocketClient;
import com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.MeetingAudioManager;
import com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.MeetingVoiceprintManager;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingPersistenceAsyncService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSegmentMessage;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import com.hojoai.webmvc.omniasst.meeting.util.AudioConverter;
import com.hojoai.webmvc.omniasst.meeting.util.StorageUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.downgoon.snowflake.Snowflake;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 会议 WebSocket v1 处理器：音频帧、ASR 转发、段落与声纹、与 persistence 协同落库。
 * 依赖 Nacos 发现 ASR 服务、{@link com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.MeetingAudioManager}、
 * {@link com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.MeetingVoiceprintManager} 等。
 */
@Component
public class MeetingWebSocketHandlerV1 extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MeetingWebSocketHandlerV1.class);
    private static final long AUDIO_FRAME_DURATION_MS = 320;
    private static final String ASR_SERVICE_NAME = "hojoai-webmvc-omniasst-voice";
    private static final String ASR_WEBSOCKET_PATH = "/omniasst-voice/asr/v1";
    private static final Random RANDOM = new Random();
    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    /** offset 落在当前段末尾之后时的容忍（毫秒），避免端上轻微超前 */
    private static final long MEETING_MARK_OFFSET_TOLERANCE_MS = 2000L;
    /** offset 与最近段落时间轴距离超过该值则视为非法 */
    private static final long MEETING_MARK_MAX_OFFSET_DISTANCE_MS = 60_000L;
    /** 端侧上报续接进度可领先服务端的最大容忍（毫秒），用于补齐断开瞬间未落库的进度 */
    private static final long RESUME_CLIENT_ELAPSED_LEAD_TOLERANCE_MS = 10_000L;
    /** 端侧上报续接进度的上限，防止异常/恶意跳跃（毫秒） */
    private static final long RESUME_CLIENT_ELAPSED_HARD_MAX_MS = 12L * 60 * 60 * 1000; // 12h

    @Resource
    private DiscoveryClient discoveryClient;
    @Resource
    private Snowflake snowflake;
    @Resource
    private WebSocketHeartbeatManager heartbeatManager;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private MeetingVoiceprintManager voiceprintManager;
    @Resource
    private MeetingPersistenceAsyncService meetingPersistenceAsyncService;
    @Resource
    private AudioConverter audioConverter;
    @Resource
    private VoiceBaseServiceClient voiceBaseServiceClient;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private MeetingPersistenceService meetingPersistenceService;
    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;
    @Resource
    private MeetingDAO meetingDAO;

    @Value("${voiceprint.similarity.threshold:0.50}")
    private double voiceprintThreshold;

    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    private static class SessionState {
        String meetingId;
        String userId;
        long meetingStartTime;
        AtomicLong audioIndex = new AtomicLong(0);
        String segmentId;
        AsrWebSocketClient asrClient;
        MeetingAudioManager audioManager;
        Long previousSendTime = 0L;
        Long lastSegmentEndTime = 0L;
        AtomicInteger segmentCount = new AtomicInteger(0);
        boolean paused = false;
        boolean stopped = false;
        final List<CompletableFuture<Void>> segmentPersistenceFutures = Collections.synchronizedList(new ArrayList<>());
        /** 本会话出现过的 segmentId（含当前段），用于校验打标请求 */
        final Set<String> validSegmentIds = ConcurrentHashMap.newKeySet();
        /** segmentId -> 打标 JSON，段落落库时写入；段已落库后打标则 UPDATE */
        final Map<String, String> segmentMarks = new ConcurrentHashMap<>();
        /** 已结束段落的时间轴区间，用于 offsetMs 映射到 segmentId */
        final List<SegmentRange> closedSegmentRanges = Collections.synchronizedList(new ArrayList<>());
        /** 最近一次成功打标时间（墙钟），用于 5s 间隔 */
        Long lastGlobalMarkSuccessAtMs;
    }

    private static final class SegmentRange {
        final String segmentId;
        final long startMs;
        final long endMs;

        SegmentRange(String segmentId, long startMs, long endMs) {
            this.segmentId = segmentId;
            this.startMs = startMs;
            this.endMs = endMs;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从 session attributes 中获取 traceId 和 spanId，设置到 MDC
        String traceId = (String) session.getAttributes().get(TRACE_ID_KEY);
        String spanId = (String) session.getAttributes().get(SPAN_ID_KEY);
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
        if (spanId != null) {
            MDC.put(SPAN_ID_KEY, spanId);
        }
        
        String deviceId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_DEVICE_ID);
        String userId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_USER_ID);
        String resumeMeetingId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_MEETING_ID);
        Long clientRecordElapsedMs = null;
        Object clientElapsedObj = session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CLIENT_RECORD_ELAPSED_MS);
        if (clientElapsedObj instanceof Number) {
            clientRecordElapsedMs = ((Number) clientElapsedObj).longValue();
        } else if (clientElapsedObj instanceof String s) {
            try {
                clientRecordElapsedMs = Long.parseLong(s);
            } catch (Exception ignore) {
            }
        }
        heartbeatManager.registerSession(session, "MeetingWebSocketHandlerV1");
        String sessionId = session.getId();

        if (StringUtils.isNotBlank(resumeMeetingId)) {
            if (!isMeetingResumable(resumeMeetingId)) {
                sendProtocolError(session, "MEETING_ID_INVALID", "会议ID不存在");
                try {
                    session.close(new CloseStatus(4403, "MEETING_ID_INVALID"));
                } catch (Exception e) {
                    logger.warn("[续接失败-关闭连接] {}", e.getMessage());
                }
                return;
            }
            SessionState resumeState = buildResumeSessionState(userId, resumeMeetingId, clientRecordElapsedMs);
            sessionStates.put(sessionId, resumeState);
            sendMeetingId(session, sessionId, deviceId, resumeState);
            setupAsrWebSocketConnection(session, resumeState);
            return;
        }

        SessionState state = new SessionState();
        state.meetingId = "M" + snowflake.nextId();
        state.userId = userId;
        state.meetingStartTime = System.currentTimeMillis();
        state.segmentId = String.valueOf(snowflake.nextId());
        state.validSegmentIds.add(state.segmentId);
        state.audioManager = new MeetingAudioManager(state.meetingId, userId, storageUtil, voiceprintManager, voiceprintThreshold, voiceBaseServiceClient, snowflake);
        sessionStates.put(sessionId, state);

        sendMeetingId(session, sessionId, deviceId, state);
        setupAsrWebSocketConnection(session, state);
    }

    private boolean isMeetingResumable(String meetingId) {
        if (meetingDAO.selectByMeetingId(meetingId) != null) {
            return true;
        }
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        return segments != null && !segments.isEmpty();
    }

    private SessionState buildResumeSessionState(String userId, String meetingId, Long clientRecordElapsedMs) {
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        if (segments == null) {
            segments = Collections.emptyList();
        }
        segments.sort(Comparator.comparing(s -> Optional.ofNullable(s.getStartTime()).orElse(0L)));

        Meeting meetingRow = meetingDAO.selectByMeetingId(meetingId);
        long maxEndFromSegments = segments.stream()
                .mapToLong(s -> Optional.ofNullable(s.getEndTime()).orElse(0L))
                .max()
                .orElse(0L);
        long maxEndFromMeeting = (meetingRow != null && meetingRow.getTotalDuration() != null)
                ? Math.max(0L, meetingRow.getTotalDuration())
                : 0L;
        long resumeElapsedMs = Math.max(maxEndFromSegments, maxEndFromMeeting);
        if (clientRecordElapsedMs != null) {
            long clientMs = clientRecordElapsedMs;
            if (clientMs < 0) {
                clientMs = 0;
            }
            if (clientMs > RESUME_CLIENT_ELAPSED_HARD_MAX_MS) {
                clientMs = RESUME_CLIENT_ELAPSED_HARD_MAX_MS;
            }
            // 只允许端侧在“略领先”时补齐，避免时间轴跳飞
            if (clientMs > resumeElapsedMs && clientMs - resumeElapsedMs <= RESUME_CLIENT_ELAPSED_LEAD_TOLERANCE_MS) {
                resumeElapsedMs = clientMs;
            }
        }

        long meetingStartTime;
        if (meetingRow != null && meetingRow.getStartTime() != null) {
            meetingStartTime = meetingRow.getStartTime();
        } else if (!segments.isEmpty()) {
            meetingStartTime = segments.stream()
                    .mapToLong(s -> Optional.ofNullable(s.getStartTime()).orElse(0L))
                    .min()
                    .orElse(System.currentTimeMillis());
        } else {
            meetingStartTime = System.currentTimeMillis();
        }

        SessionState state = new SessionState();
        state.meetingId = meetingId;
        state.userId = userId;
        state.meetingStartTime = meetingStartTime;
        state.segmentId = String.valueOf(snowflake.nextId());
        state.validSegmentIds.add(state.segmentId);
        long nextFrameIdx = resumeElapsedMs > 0 ? (resumeElapsedMs + AUDIO_FRAME_DURATION_MS - 1) / AUDIO_FRAME_DURATION_MS : 0;
        state.audioIndex.set(nextFrameIdx);
        // 续接时：previousSendTime 表示已推流进度（可能领先于已落库段落）；
        // lastSegmentEndTime 作为“当前段起点”，应尽量对齐最后一个已落库段的结束时间，
        // 这样 offsetMs 落在 (lastPersistedEnd, resumeElapsed] 的“空洞”会归属到当前段，避免打标失败。
        long lastPersistedEnd = Math.max(0L, maxEndFromSegments);
        if (lastPersistedEnd > resumeElapsedMs) {
            lastPersistedEnd = resumeElapsedMs;
        }
        state.previousSendTime = resumeElapsedMs;
        state.lastSegmentEndTime = lastPersistedEnd;
        state.segmentCount.set(segments.size());

        Long maxMarkedAtMs = null;
        for (MeetingSegment seg : segments) {
            if (seg.getSegmentId() != null) {
                state.validSegmentIds.add(seg.getSegmentId());
            }
            if (StringUtils.isNotBlank(seg.getMarkJson())) {
                state.segmentMarks.put(seg.getSegmentId(), seg.getMarkJson());
                Long ma = parseMarkedAtFromMarkJson(seg.getMarkJson());
                if (ma != null && (maxMarkedAtMs == null || ma > maxMarkedAtMs)) {
                    maxMarkedAtMs = ma;
                }
            }
            Long st = seg.getStartTime();
            Long et = seg.getEndTime();
            if (st != null && et != null && seg.getSegmentId() != null) {
                state.closedSegmentRanges.add(new SegmentRange(seg.getSegmentId(), st, et));
            }
        }
        if (maxMarkedAtMs != null) {
            state.lastGlobalMarkSuccessAtMs = maxMarkedAtMs;
        }

        Long resumeMs = resumeElapsedMs > 0 ? resumeElapsedMs : null;
        state.audioManager = new MeetingAudioManager(meetingId, userId, storageUtil, voiceprintManager, voiceprintThreshold, voiceBaseServiceClient, snowflake, resumeMs);
        return state;
    }

    private void setupAsrWebSocketConnection(WebSocketSession session, SessionState state) {
        // 从 Nacos 获取 ASR 服务地址
        String asrWebSocketUrl = getAsrWebSocketUrl();
        if (asrWebSocketUrl == null) {
            logger.error("[ASR服务发现失败] meetingId: {}, 无法获取服务地址", state.meetingId);
            sendError(session, "SERVICE_UNAVAILABLE", "ASR service not available", "asr");
            return;
        }
        
        logger.info("[ASR服务地址] meetingId: {}, url: {}", state.meetingId, asrWebSocketUrl);
        state.asrClient = new AsrWebSocketClient(state.meetingId, asrWebSocketUrl);
        
        state.asrClient.setResponseCallback(response -> {
            processAudioResponse(session, state, response);
        });
        
        state.asrClient.connect().thenAccept(connected -> {
            if (connected) {
                logger.info("[ASR连接建立成功] meetingId: {}", state.meetingId);
                
                // 连接成功后立即发送配置
                AsrConfigRequest config = new AsrConfigRequest();
                config.setPlatform(AsrPlatform.HOJO.getCode());
                
                boolean configSent = state.asrClient.sendConfig(config);
                if (configSent) {
                    logger.info("[ASR配置发送成功] meetingId: {}", state.meetingId);
                } else {
                    logger.error("[ASR配置发送失败] meetingId: {}", state.meetingId);
                }
            } else {
                logger.error("[ASR连接建立失败] meetingId: {}", state.meetingId);
            }
        });
    }

    /**
     * 从 Nacos 获取 ASR WebSocket 服务地址（支持负载均衡）
     */
    private String getAsrWebSocketUrl() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(ASR_SERVICE_NAME);
            if (instances == null || instances.isEmpty()) {
                logger.warn("[服务发现] 未找到服务: {}", ASR_SERVICE_NAME);
                return null;
            }
            
            // 随机选择一个服务实例（实现负载均衡）
            int instanceCount = instances.size();
            int selectedIndex = instanceCount > 1 ? RANDOM.nextInt(instanceCount) : 0;
            ServiceInstance instance = instances.get(selectedIndex);
            
            String host = instance.getHost();
            int port = instance.getPort();
            
            // 构建 WebSocket URL
            String wsUrl = String.format("ws://%s:%d%s", host, port, ASR_WEBSOCKET_PATH);
            logger.info("[服务发现] 服务: {}, 实例总数: {}, 选中实例: #{} (url: {})", 
                    ASR_SERVICE_NAME, instanceCount, selectedIndex + 1, wsUrl);
            
            return wsUrl;
        } catch (Exception e) {
            logger.error("[服务发现异常] 服务: {}", ASR_SERVICE_NAME, e);
            return null;
        }
    }

    private void sendMeetingId(WebSocketSession session, String sessionId, String deviceId, SessionState state) {
        try {
            MeetingIdResponse response = new MeetingIdResponse();
            response.setMeetingId(state.meetingId);
            response.setTimestamp(state.meetingStartTime);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_ID, response);
            logger.info("Meeting WebSocket V1 connected, session id: {}, deviceId: {}, meetingId: {}",
                    sessionId, deviceId, state.meetingId);
        } catch (Exception e) {
            logger.error("Failed to send meeting ID, session id: {}", sessionId, e);
        }
    }

    private void sendProtocolError(WebSocketSession session, String code, String message) {
        try {
            MeetingProtocolErrorResponse err = new MeetingProtocolErrorResponse();
            err.setCode(code);
            err.setMessage(message);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ERROR, err);
        } catch (Exception e) {
            logger.error("Failed to send protocol error", e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从 session attributes 中获取 traceId 和 spanId，设置到 MDC（WebSocket 异步场景需要每次设置）
        String traceId = (String) session.getAttributes().get(TRACE_ID_KEY);
        String spanId = (String) session.getAttributes().get(SPAN_ID_KEY);
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
        if (spanId != null) {
            MDC.put(SPAN_ID_KEY, spanId);
        }
        
        heartbeatManager.updateLastActiveTime(session);
        try {
            IMessage<?> iMessage = JacksonUtil.json2Bean(message.getPayload(), IMessage.class);
            String eventTypeStr = iMessage.getEventType();
            String sessionId = session.getId();
            SessionState state = sessionStates.get(sessionId);
            IMessageEvent eventType = IMessageEvent.getByEventType(eventTypeStr);
            switch (eventType) {
                case REQ_HEARTBEAT -> handleHeartbeat(session, message.getPayload());
                case REQ_MEETING_AUDIO_DATA -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Session not initialized", "config");
                        return;
                    }
                    handleAudioData(session, message.getPayload(), state);
                }
                case REQ_MEETING_PAUSE -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Session not initialized", "config");
                        return;
                    }
                    handleMeetingPause(session, message.getPayload(), state);
                }
                case REQ_MEETING_RESUME -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Session not initialized", "config");
                        return;
                    }
                    handleMeetingResume(session, message.getPayload(), state);
                }
                case REQ_MEETING_STOP -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Session not initialized", "config");
                        return;
                    }
                    handleMeetingStop(session, message.getPayload(), state);
                }
                case REQ_MEETING_MARK -> {
                    if (Objects.isNull(state)) {
                        sendError(session, "CONFIG_ERROR", "Session not initialized", "config");
                        return;
                    }
                    handleMeetingMark(session, message.getPayload(), state);
                }
                case null, default -> IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(IMessage.newInstance(IMessageEvent.UNSUPPORTED_OPERATION.getEventType())));
            }
        } catch (Exception e) {
            logger.error("Error handling text message", e);
            sendError(session, "GENERAL_ERROR", "Error processing message: " + e.getMessage(), "process");
        }
    }

    private void handleMeetingMark(WebSocketSession session, String message, SessionState state) {
        String resolvedSegmentId = null;
        try {
            MeetingMarkRequest request = IMessageCustom.parseBusinessRequest(message, MeetingMarkRequest.class);
            if (request == null || request.getOffsetMs() == null) {
                sendError(session, "ILLEGAL_ARGUMENT", "offsetMs required", "process");
                return;
            }
            long offsetMs = request.getOffsetMs();
            if (offsetMs < 0) {
                sendError(session, "ILLEGAL_ARGUMENT", "offsetMs must be non-negative", "process");
                return;
            }

            long now = System.currentTimeMillis();
            synchronized (state) {
                if (state.lastGlobalMarkSuccessAtMs != null
                        && now - state.lastGlobalMarkSuccessAtMs < GlobalConstants.MEETING_MARK_MIN_INTERVAL_MS) {
                    MeetingMarkAckResponse err = new MeetingMarkAckResponse();
                    err.setStatus("error");
                    err.setCode("TOO_CLOSE");
                    err.setMessage("打标距离过近，请至少间隔5秒");
                    IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_MARK_ACK, err);
                    return;
                }
            }

            resolvedSegmentId = resolveSegmentIdByOffsetMs(state, offsetMs);
            if (resolvedSegmentId == null || !state.validSegmentIds.contains(resolvedSegmentId)) {
                sendError(session, "ILLEGAL_ARGUMENT", "offsetMs does not match any segment on timeline", "process");
                return;
            }

            Map<String, Object> markPayload = new HashMap<>();
            markPayload.put("offsetMs", offsetMs);
            markPayload.put("markedAt", now);
            String markJson = JacksonUtil.bean2Json(markPayload);
            state.segmentMarks.put(resolvedSegmentId, markJson);

            int updated = meetingSegmentDAO.updateMarkJsonByMeetingIdAndSegmentId(state.meetingId, resolvedSegmentId, markJson);
            if (updated == 0) {
                logger.debug("[段落打标] 段尚未落库，仅会话缓存: meetingId={}, segmentId={}", state.meetingId, resolvedSegmentId);
            }

            synchronized (state) {
                state.lastGlobalMarkSuccessAtMs = now;
            }

            MeetingMarkAckResponse ack = new MeetingMarkAckResponse();
            ack.setStatus("success");
            ack.setMarkId("mark-" + snowflake.nextId());
            ack.setMessage("标记成功");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_MARK_ACK, ack);
        } catch (Exception e) {
            if (resolvedSegmentId != null) {
                state.segmentMarks.remove(resolvedSegmentId);
            }
            logger.error("[段落打标异常] meetingId: {}", state.meetingId, e);
            sendError(session, "PROCESS_ERROR", "Error marking: " + e.getMessage(), "process");
        }
    }

    /** 从段落 mark_json 解析 markedAt（毫秒），用于续接后恢复打标 5s 间隔限制 */
    private static Long parseMarkedAtFromMarkJson(String markJson) {
        if (StringUtils.isBlank(markJson)) {
            return null;
        }
        try {
            Map<String, Object> map = JacksonUtil.json2Bean(markJson, new TypeReference<Map<String, Object>>() {});
            if (map == null || !map.containsKey("markedAt")) {
                return null;
            }
            Object o = map.get("markedAt");
            if (o instanceof Number) {
                return ((Number) o).longValue();
            }
            if (o != null) {
                return Long.parseLong(o.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static long distanceToInterval(long offsetMs, long startMs, long endMs) {
        long a = Math.min(startMs, endMs);
        long b = Math.max(startMs, endMs);
        if (offsetMs >= a && offsetMs <= b) {
            return 0;
        }
        if (offsetMs < a) {
            return a - offsetMs;
        }
        return offsetMs - b;
    }

    /**
     * 将时间轴上的 offsetMs 映射到段落 segmentId：优先落入区间；否则取距离最近的段；距离过大返回 null。
     */
    private String resolveSegmentIdByOffsetMs(SessionState state, long offsetMs) {
        long curStart = state.lastSegmentEndTime != null ? state.lastSegmentEndTime : 0L;
        long curEnd = state.previousSendTime != null ? state.previousSendTime : 0L;
        if (curEnd < curStart) {
            curEnd = curStart;
        }
        String currentId = state.segmentId;

        List<SegmentRange> snapshot;
        synchronized (state.closedSegmentRanges) {
            snapshot = new ArrayList<>(state.closedSegmentRanges);
        }
        for (SegmentRange r : snapshot) {
            if (offsetMs >= r.startMs && offsetMs <= r.endMs) {
                return r.segmentId;
            }
        }
        if (offsetMs >= curStart && offsetMs <= curEnd) {
            return currentId;
        }
        if (offsetMs >= curStart && offsetMs <= curEnd + MEETING_MARK_OFFSET_TOLERANCE_MS) {
            return currentId;
        }

        String bestId = null;
        long bestDist = Long.MAX_VALUE;
        for (SegmentRange r : snapshot) {
            long d = distanceToInterval(offsetMs, r.startMs, r.endMs);
            if (d < bestDist) {
                bestDist = d;
                bestId = r.segmentId;
            }
        }
        long dCur = distanceToInterval(offsetMs, curStart, Math.max(curEnd, curStart));
        if (dCur < bestDist) {
            bestDist = dCur;
            bestId = currentId;
        }
        if (bestId == null) {
            return null;
        }
        if (bestDist > MEETING_MARK_MAX_OFFSET_DISTANCE_MS) {
            return null;
        }
        return bestId;
    }

    private static boolean segmentIsMarked(SessionState state, String segmentId) {
        return StringUtils.isNotBlank(state.segmentMarks.get(segmentId));
    }

    private void handleHeartbeat(WebSocketSession session, String message) {
        try {
            HeartbeatRequest request = IMessageCustom.parseBusinessRequest(message, HeartbeatRequest.class);
            HeartbeatResponse response = new HeartbeatResponse();
            response.setTimestamp(request != null ? request.getTimestamp() : System.currentTimeMillis());
            response.setServerTime(System.currentTimeMillis());
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_HEARTBEAT, response);
        } catch (Exception e) {
            logger.error("Error handling heartbeat", e);
        }
    }

    private void handleAudioData(WebSocketSession session, String message, SessionState state) {
        try {
            if (state.paused) {
                logger.warn("[会议已暂停] meetingId: {}, 拒绝音频数据", state.meetingId);
                return;
            }

            MeetingAudioDataRequest request = IMessageCustom.parseBusinessRequest(message, MeetingAudioDataRequest.class);
            if (Objects.isNull(request) || StringUtils.isBlank(request.getAudioData())) {
                sendError(session, "ILLEGAL_ARGUMENT", "Invalid audio data", "process");
                return;
            }

            byte[] audioBytes;
            try {
                audioBytes = Base64.getDecoder().decode(request.getAudioData());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid Base64 audio data, meetingId: {}", state.meetingId, e);
                sendError(session, "ILLEGAL_ARGUMENT", "Invalid Base64 audio data", "process");
                return;
            }

//            logger.info("[接收音频数据] meetingId: {}, audioSize: {} bytes", state.meetingId, audioBytes.length);
            
            // 收集音频包到 OSS 管理器
            long currentPackageIndex = state.audioIndex.get();
            state.audioManager.collectAudioPackage(currentPackageIndex, audioBytes);
            
            // 构建 ASR 请求并发送
            AsrWebSocketRequest.CustomData customData = buildAsrWebSocketRequestCustomData(state, AUDIO_FRAME_DURATION_MS);
            AsrWebSocketRequest asrWebSocketRequest = buildAsrWebSocketRequest(Base64.getEncoder().encodeToString(audioBytes), customData, false);
            logger.info("[发送音频帧] meetingId: {}, frameIndex: {}, audioSize: {} bytes, audioDuration: {} ms, previousSendTime: {} ms", 
                    state.meetingId, state.audioIndex.get(), audioBytes.length, AUDIO_FRAME_DURATION_MS, state.previousSendTime);

            boolean sent = state.asrClient != null && state.asrClient.sendAudioData(asrWebSocketRequest);
            if (!sent) {
                logger.error("[发送音频帧失败] meetingId: {}, asrClient: {}", state.meetingId, state.asrClient != null ? "exists" : "null");
                sendError(session, "ASR_ERROR", "Failed to send audio data to ASR service", "asr");
                return;
            }

            state.audioIndex.incrementAndGet();
            state.previousSendTime = (state.previousSendTime == null ? 0L : state.previousSendTime) + AUDIO_FRAME_DURATION_MS;

        } catch (Exception e) {
            logger.error("Error processing audio data, meetingId: {}", state.meetingId, e);
            sendError(session, "PROCESS_ERROR", "Error processing audio: " + e.getMessage(), "process");
        }
    }

    private void processAudioResponse(WebSocketSession session, SessionState state, AsrWebSocketResponse response) {
        try {
            if (response == null) {
                logger.warn("[响应为空] meetingId: {}", state.meetingId);
                return;
            }
            logger.info("[接收响应] meetingId: {}, response: {}", state.meetingId, response);
            // ASR 识别后，保存音频包到 OSS
            AsrWebSocketRequest.CustomData customData = JacksonUtil.json2Bean(response.getCustom(), AsrWebSocketRequest.CustomData.class);
            try {
                if (customData != null && customData.getAudioIndex() != null) {
                    long recognizedPackageIndex = customData.getAudioIndex();
                    state.audioManager.saveAudioPackage(recognizedPackageIndex, state.segmentId);
                    logger.debug("[保存音频包] meetingId: {}, packageIndex: {}", state.meetingId, recognizedPackageIndex);
                }
            } catch (Exception e) {
                logger.error("[保存音频包失败] meetingId: {}", state.meetingId, e);
            }

            // 发送响应给客户端
            sendAsrTextResponse(session, state, response);

            // 如果识别结束，重置段落状态
            if (response.getIsEnd() != null && response.getIsEnd() == 1) {
                // 发送段完成通知
                sendSegmentComplete(session, state, response);
                resetState(state, response);
            }

        } catch (Exception e) {
            logger.error("Error processing audio frame response, meetingId: {}", state.meetingId, e);
        }
    }

    private AsrWebSocketRequest.CustomData buildAsrWebSocketRequestCustomData(SessionState state, long audioDuration){
        AsrWebSocketRequest.CustomData customData = new AsrWebSocketRequest.CustomData();
        customData.setAudioIndex(state.audioIndex.get());
        customData.setPreviousAudioSendTime(state.previousSendTime);
        customData.setCurrentAudioTime(audioDuration);
        return customData;
    }

    private AsrWebSocketRequest buildAsrWebSocketRequest(String audioBase64, AsrWebSocketRequest.CustomData customData, boolean isEnd) {
        // 构建发送请求
        AsrWebSocketRequest frameRequest = new AsrWebSocketRequest();
        frameRequest.setAudioData(audioBase64);
        frameRequest.setCustom(JacksonUtil.bean2Json(customData));
        frameRequest.setIsEnd(isEnd);

        return frameRequest;
    }

    private void sendAsrTextResponse(WebSocketSession session, SessionState state, AsrWebSocketResponse asrWebSocketResponse) {
        try {
            MeetingAsrTextResponse response = new MeetingAsrTextResponse();
            response.setFixedText(asrWebSocketResponse.getFixedText());
            response.setTmpText(asrWebSocketResponse.getTmpText());
            response.setIsEnd(asrWebSocketResponse.getIsEnd());
            response.setSegmentId(state.segmentId);
            String segmentStartTime = calculateSegmentStartTime(asrWebSocketResponse, state);
            response.setSegmentStartTime(segmentStartTime);
            SpeakerInfo speakerInfo = state.audioManager.getSpeakerInfo(state.segmentId);
            if (speakerInfo != null){
                response.setSpeakerId(speakerInfo.getSpeakerId());
                response.setSpeakerName(speakerInfo.getSpeakerName());
                response.setSpeakerAvatar(speakerInfo.getSpeakerAvatar());
            }
            response.setIsMark(segmentIsMarked(state, state.segmentId));

            logger.info("[推送文本] meetingId: {}, isEnd: {}, 推送内容: fixedText=[{}], tmpText=[{}], segmentId=[{}], segmentStartTime=[{}]",
                    state.meetingId, asrWebSocketResponse.getIsEnd(), asrWebSocketResponse.getFixedText(),
                    asrWebSocketResponse.getTmpText(), state.segmentId, segmentStartTime);

            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_ASR_TEXT, response);
        } catch (Exception e) {
            logger.error("Failed to send ASR text response", e);
        }
    }

    private String calculateSegmentStartTime(AsrWebSocketResponse asrWebSocketResponse, SessionState state) {
        try {
            AsrWebSocketRequest.CustomData customData = JacksonUtil.json2Bean(
                    asrWebSocketResponse.getCustom(), 
                    AsrWebSocketRequest.CustomData.class
            );

            if (customData != null) {
                Long prev = customData.getPreviousAudioSendTime();
                Long cur = customData.getCurrentAudioTime();
                if (prev != null && cur != null) {
                    return formatTimeOffset(prev + cur);
                }
                if (prev != null) {
                    return formatTimeOffset(prev);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse custom data for time calculation, will fallback to session state: {}", e.getMessage());
        }
        long fallbackMs = Math.max(
                state != null && state.lastSegmentEndTime != null ? state.lastSegmentEndTime : 0L,
                state != null && state.previousSendTime != null ? state.previousSendTime : 0L
        );
        return formatTimeOffset(fallbackMs);
    }

    private void sendSegmentComplete(WebSocketSession session, SessionState state, AsrWebSocketResponse response) {
        try {
            MeetingSegmentCompleteResponse segmentResponse = buildMeetingSegmentCompleteResponse(state, response);
            String segmentId = state.segmentId;
            state.closedSegmentRanges.add(new SegmentRange(
                    segmentResponse.getSegmentId(),
                    segmentResponse.getStartTime() != null ? segmentResponse.getStartTime() : 0L,
                    segmentResponse.getEndTime() != null ? segmentResponse.getEndTime() : 0L));
            segmentResponse.setIsMark(segmentIsMarked(state, segmentResponse.getSegmentId()));

            boolean hasSpeaker = segmentResponse.getSpeakerId() != null;
            if (!hasSpeaker) {
                logger.info("[段完成-无发言人] meetingId: {}, segmentId: {}, 异步触发声纹识别", state.meetingId, segmentId);
                
                CompletableFuture.runAsync(() -> {
                    try {
                        SpeakerInfo recognizedSpeaker = state.audioManager.voiceprintRecognition(segmentId);
                        logger.info("[段完成-发言人识别完成] meetingId: {}, segmentId: {}, speakerId: {}, speakerName: {}", state.meetingId, segmentId, recognizedSpeaker.getSpeakerId(), recognizedSpeaker.getSpeakerName());
                        segmentResponse.setSpeakerId(recognizedSpeaker.getSpeakerId());
                        segmentResponse.setSpeakerName(recognizedSpeaker.getSpeakerName());
                        segmentResponse.setSpeakerAvatar(recognizedSpeaker.getSpeakerAvatar());
                        persistSegment(state, segmentResponse);
                        IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_SEGMENT_COMPLETE, segmentResponse);
                        state.audioManager.cleanupSegment(segmentId);
                    } catch (Exception e) {
                        logger.error("[段完成-异步处理异常] meetingId: {}, segmentId: {}", state.meetingId, segmentId, e);
                    }
                });
            } else {
                logger.info("[段完成-发言人已识别] meetingId: {}, segmentId: {}, speakerId: {}, speakerName: {}",
                        state.meetingId, segmentId, segmentResponse.getSpeakerId(), segmentResponse.getSpeakerName());
                
                persistSegment(state, segmentResponse);
                IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_SEGMENT_COMPLETE, segmentResponse);
                
                state.audioManager.cleanupSegment(segmentId);
            }
            
        } catch (Exception e) {
            logger.error("[发送段完成通知失败] meetingId: {}, segmentId: {}", state.meetingId, state.segmentId, e);
        }
    }

    private MeetingSegmentCompleteResponse buildMeetingSegmentCompleteResponse(SessionState state, AsrWebSocketResponse response) {
        MeetingSegmentCompleteResponse segmentResponse = new MeetingSegmentCompleteResponse();
        
        // 设置段ID和文本
        segmentResponse.setSegmentId(state.segmentId);
        segmentResponse.setText(response.getFixedText());
        
        // 获取说话人信息
        SpeakerInfo speakerInfo = state.audioManager.getSpeakerInfo(state.segmentId);
        if (speakerInfo != null) {
            segmentResponse.setSpeakerId(speakerInfo.getSpeakerId());
            segmentResponse.setSpeakerName(speakerInfo.getSpeakerName());
            segmentResponse.setSpeakerAvatar(speakerInfo.getSpeakerAvatar());
        }
        
        // 设置时间信息
        segmentResponse.setTimestamp(System.currentTimeMillis());
        segmentResponse.setStartTime(state.lastSegmentEndTime);
        
        long endTime = calculateLastSegmentEndTime(response, state.previousSendTime, state.meetingId);
        segmentResponse.setEndTime(endTime);
        
        String segmentStartTime = formatTimeOffset(state.lastSegmentEndTime);
        segmentResponse.setSegmentStartTime(segmentStartTime);
        
        return segmentResponse;
    }

    private void resetState(SessionState state, AsrWebSocketResponse response) {
        state.lastSegmentEndTime = calculateLastSegmentEndTime(response, state.previousSendTime, state.meetingId);
        state.segmentCount.incrementAndGet();
        
        state.segmentId = String.valueOf(snowflake.nextId());
        state.validSegmentIds.add(state.segmentId);
        logger.info("[段落结束并重置] meetingId: {}, newSegmentId: {}, lastSegmentEndTime: {}ms, totalSegments: {}", 
                state.meetingId, state.segmentId, state.lastSegmentEndTime, state.segmentCount.get());
    }

    private long calculateLastSegmentEndTime(AsrWebSocketResponse response, long fallbackTime, String meetingId) {
        AsrWebSocketRequest.CustomData customData = JacksonUtil.json2Bean(
                response.getCustom(), 
                AsrWebSocketRequest.CustomData.class
        );
        
        long previousTime = customData.getPreviousAudioSendTime() != null ? customData.getPreviousAudioSendTime() : 0L;
        long currentTime = customData.getCurrentAudioTime() != null ? customData.getCurrentAudioTime() : 0L;
        return previousTime + currentTime;
    }

    private String formatTimeOffset(long offsetMillis) {
        long totalSeconds = offsetMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void handleMeetingPause(WebSocketSession session, String message, SessionState state) {
        if (state.paused) {
            logger.warn("[会议已暂停] meetingId: {}, 忽略重复暂停请求", state.meetingId);
            return;
        }
        
        try {
            logger.info("[会议暂停] meetingId: {}, currentSegmentId: {}", state.meetingId, state.segmentId);
            
            AsrWebSocketRequest endRequest = buildEndRequest(state);
            boolean sent = state.asrClient != null && state.asrClient.sendAudioData(endRequest);
            
            if (sent) {
                logger.info("[发送结束信号] meetingId: {}, lastPackageIndex: {}, isEnd: true", 
                        state.meetingId, state.audioIndex.get() - 1);
                state.paused = true;
                sendPauseResponse(session, state);
            } else {
                logger.error("[发送结束信号失败] meetingId: {}", state.meetingId);
                sendError(session, "PAUSE_ERROR", "Failed to pause meeting", "pause");
            }
        } catch (Exception e) {
            logger.error("[会议暂停异常] meetingId: {}", state.meetingId, e);
            sendError(session, "PAUSE_ERROR", "Error pausing meeting: " + e.getMessage(), "pause");
        }
    }
    
    private void handleMeetingResume(WebSocketSession session, String message, SessionState state) {
        if (!state.paused) {
            logger.warn("[会议未暂停] meetingId: {}, 无需继续", state.meetingId);
            return;
        }
        try {
            logger.info("[会议继续] meetingId: {}, currentSegmentId: {}", state.meetingId, state.segmentId);
            state.paused = false;
            sendResumeResponse(session, state);
        } catch (Exception e) {
            logger.error("[会议继续异常] meetingId: {}", state.meetingId, e);
            sendError(session, "RESUME_ERROR", "Error resuming meeting: " + e.getMessage(), "resume");
        }
    }
    
    private void handleMeetingStop(WebSocketSession session, String message, SessionState state) {
        if (state.stopped) {
            logger.warn("[会议已停止] meetingId: {}, 忽略重复停止请求", state.meetingId);
            return;
        }
        
        try {
            MeetingStopRequest request = IMessageCustom.parseBusinessRequest(message, MeetingStopRequest.class);
            Integer needSave = request != null && request.getNeedSave() != null ? request.getNeedSave() : 1;
            state.stopped = true;
            
            logger.info("[会议停止-开始] meetingId: {}, needSave: {}", state.meetingId, needSave);
            
            if (!state.paused && state.asrClient != null) {
                AsrWebSocketRequest stopRequest = buildStopRequest(state);
                state.asrClient.sendAudioData(stopRequest);
                logger.info("[发送停止信号] meetingId: {}", state.meetingId);
            }
            logger.info("[会议停止]开始处理数据，meetingId: {}", state.meetingId);
            finishMeeting(session, state, needSave);
            sendStopResponse(session, state);
            
            logger.info("[会议停止-完成] meetingId: {}, totalDuration: {}ms, segmentCount: {}, needSave: {}", state.meetingId, state.lastSegmentEndTime, state.segmentCount.get(), needSave);
            
        } catch (Exception e) {
            logger.error("[会议停止异常] meetingId: {}", state.meetingId, e);
            sendError(session, "STOP_ERROR", "Error stopping meeting: " + e.getMessage(), "stop");
        }
    }

    /**
     * 构建停止请求
     */
    private AsrWebSocketRequest buildStopRequest(SessionState state) {
        // 获取上一个音频包的索引
        long lastPackageIndex = state.audioIndex.get() - 1;
        
        // 构建停止请求的 CustomData
        AsrWebSocketRequest.CustomData customData = new AsrWebSocketRequest.CustomData();
        customData.setAudioIndex(lastPackageIndex);
        customData.setPreviousAudioSendTime(state.previousSendTime);
        customData.setCurrentAudioTime(0L);
        customData.setIsStop(true);
        return buildAsrWebSocketRequest("", customData, true);
    }
    
    private AsrWebSocketRequest buildEndRequest(SessionState state) {
        long lastPackageIndex = state.audioIndex.get() - 1;
        AsrWebSocketRequest.CustomData customData = new AsrWebSocketRequest.CustomData();
        customData.setAudioIndex(lastPackageIndex);
        customData.setPreviousAudioSendTime(state.previousSendTime);
        customData.setCurrentAudioTime(0L);
        return buildAsrWebSocketRequest("", customData, true);
    }
    
    private void sendPauseResponse(WebSocketSession session, SessionState state) {
        try {
            MeetingPauseResponse response = new MeetingPauseResponse();
            response.setStatus("success");
            response.setMeetingId(state.meetingId);
            response.setTimestamp(System.currentTimeMillis());
            response.setMessage("Meeting paused successfully");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_PAUSE_ACK, response);
        } catch (Exception e) {
            logger.error("[发送暂停响应失败] meetingId: {}", state.meetingId, e);
        }
    }
    
    private void sendResumeResponse(WebSocketSession session, SessionState state) {
        try {
            MeetingResumeResponse response = new MeetingResumeResponse();
            response.setStatus("success");
            response.setMeetingId(state.meetingId);
            response.setTimestamp(System.currentTimeMillis());
            response.setMessage("Meeting resumed successfully");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_RESUME_ACK, response);
        } catch (Exception e) {
            logger.error("[发送继续响应失败] meetingId: {}", state.meetingId, e);
        }
    }
    
    private void sendStopResponse(WebSocketSession session, SessionState state) {
        try {
            MeetingStopResponse response = new MeetingStopResponse();
            response.setStatus("success");
            response.setMeetingId(state.meetingId);
            response.setTotalDuration(state.lastSegmentEndTime);
            response.setSegmentCount(state.segmentCount.get());
            response.setMessage("Meeting stopped successfully");
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_MEETING_STOP_ACK, response);
        } catch (Exception e) {
            logger.error("[发送停止响应失败] meetingId: {}", state.meetingId, e);
        }
    }
    
    private void finishMeeting(WebSocketSession session, SessionState state, Integer needSave) {
        this.persistMeeting(session, state, needSave);
        if (Objects.nonNull(state.audioManager)) {
            state.audioManager.waitForUploadComplete();
        }
        if (needSave != null && needSave == 1) {
            saveMeetingAudioToOss(state);
        } else {
            logger.info("[会议结束-跳过WAV处理] meetingId: {}, needSave: {}", state.meetingId, needSave);
        }
    }

    private void saveMeetingAudioToOss(SessionState state) {
        logger.info("[会议结果音频处理], sessionId: {}", state.meetingId);
        String wavObjectKey = null;
        String ossPcmObjectKey = Objects.nonNull(state.audioManager) ? state.audioManager.getOssPcmObjectKey() : null;
        if (Objects.isNull(ossPcmObjectKey)) {
            logger.warn("[会议结果-无音频数据] meetingId: {}", state.meetingId);
            return;
        }
        if (Objects.nonNull(state.audioManager) && state.audioManager.getOssCurrentPosition() == 0L) {
            logger.warn("[会议结果-OSS未写入PCM] meetingId: {}, 跳过下载与WAV转换（可能无ASR回调或未产生可上传帧）", state.meetingId);
            return;
        }
        logger.info("[会议结束-开始处理] meetingId: {}, totalDuration: {}ms, segmentCount: {}", state.meetingId, state.lastSegmentEndTime, state.segmentCount.get());
        File pcmFile = null;
        File wavFile = null;
        try {
            pcmFile = storageUtil.downloadObject(ossPcmObjectKey);
            if (Objects.isNull(pcmFile) || !pcmFile.exists()) {
                logger.error("[会议结果-下载PCM失败] meetingId: {}", state.meetingId);
                return;
            }
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            String wavFileName = state.meetingId + ".wav";
            File tempWavFile = tempDir.resolve(wavFileName).toFile();
            wavFile = audioConverter.convertPcmToWav(pcmFile, tempWavFile.getAbsolutePath());
            String wavKey = "meeting/" + state.meetingId + "/audio.wav";
            storageUtil.uploadFile(wavKey, wavFile);
            wavObjectKey = wavKey;
            logger.info("[会议结果-WAV上传成功] meetingId: {}, wavObjectKey: {}", state.meetingId, wavObjectKey);
            final String finalWavKey = wavObjectKey;
            transactionTemplate.execute(status -> {
                meetingPersistenceService.updateMeetingOssUrl(state.meetingId, finalWavKey);
                return null;
            });
        } catch (Exception e) {
            logger.error("[会议结果处理异常] meetingId: {}", state.meetingId, e);
        } finally {
            cleanupTempFiles(pcmFile, wavFile);
        }
    }
    
    private void cleanupTempFiles(File pcmFile, File wavFile) {
        if (pcmFile != null && pcmFile.exists()) {
            try {
                Files.delete(pcmFile.toPath());
            } catch (Exception e) {
                logger.warn("[删除临时PCM文件失败] {}", pcmFile.getAbsolutePath(), e);
            }
        }
        if (wavFile != null && wavFile.exists()) {
            try {
                Files.delete(wavFile.toPath());
            } catch (Exception e) {
                logger.warn("[删除临时WAV文件失败] {}", wavFile.getAbsolutePath(), e);
            }
        }
    }
    
    private void persistMeeting(WebSocketSession session, SessionState state, Integer needSave) {
        try {
            if (1 != needSave) {
                logger.info("[持久化会议结果]不进行保存 meetingId: {}, needSave: {}", state.meetingId, needSave);
                return;
            }
            String userId = state.userId;
            if (StringUtils.isBlank(userId)) {
                userId = (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_USER_ID);
            }
            String conversationId = Objects.nonNull(session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID)) ? (String) session.getAttributes().get(WebSocketConstants.ATTRIBUTE_CONVERSATION_ID) : StringUtils.EMPTY;
            MeetingResultMessage message = new MeetingResultMessage();
            message.setMeetingId(state.meetingId);
            message.setUserId(userId);
            long totalDuration = Math.max(
                    state.lastSegmentEndTime != null ? state.lastSegmentEndTime : 0L,
                    state.previousSendTime != null ? state.previousSendTime : 0L
            );
            message.setTotalDuration(totalDuration);
            message.setTimestamp(System.currentTimeMillis());
            message.setStartTime(state.meetingStartTime);
            message.setEndTime(System.currentTimeMillis());
            message.setNeedSave(needSave);
            logger.info("[持久化会议结果] meetingId: {}, userId: {}, needSave: {}, totalDuration: {}ms, segmentCount: {}", state.meetingId, userId, needSave, totalDuration, state.segmentCount.get());

            waitForSegmentFutures(state, 3000);
            meetingPersistenceService.initMeetingInfo(message);
            meetingPersistenceAsyncService.persistMeetingResultAsync(message, conversationId);
        } catch (Exception e) {
            logger.error("[持久化会议结果失败] meetingId: {}", state.meetingId, e);
        }
    }
    
    private void persistSegment(SessionState state, MeetingSegmentCompleteResponse segmentResponse) {
        try {
            String meetingId = state.meetingId;
            if (StringUtils.isBlank(segmentResponse.getText())) {
                voiceprintManager.correctSpeakerIndex(meetingId);
                logger.warn("[跳过空文本段] meetingId: {}, segmentId: {}", meetingId, segmentResponse.getSegmentId());
                return;
            }
            
            MeetingSegment segment = new MeetingSegment();
            segment.setMeetingId(meetingId);
            segment.setSegmentId(segmentResponse.getSegmentId());
            segment.setText(segmentResponse.getText());
            segment.setSpeakerId(segmentResponse.getSpeakerId());
            segment.setSpeakerName(segmentResponse.getSpeakerName());
            segment.setSpeakerAvatar(segmentResponse.getSpeakerAvatar());
            segment.setStartTime(segmentResponse.getStartTime());
            segment.setEndTime(segmentResponse.getEndTime());
            segment.setDuration(segmentResponse.getEndTime() - segmentResponse.getStartTime());
            segment.setTimestamp(System.currentTimeMillis());
            String markJson = state.segmentMarks.get(segmentResponse.getSegmentId());
            if (StringUtils.isNotBlank(markJson)) {
                segment.setMarkJson(markJson);
            }

            MeetingSegmentMessage message = new MeetingSegmentMessage();
            message.setMeetingId(meetingId);
            message.setSegment(segment);
            message.setTimestamp(segment.getTimestamp());
            
            logger.info("[持久化段数据] meetingId: {}, segmentId: {}, text: [{}], speaker: {}", 
                    meetingId, segment.getSegmentId(), segment.getText(), segment.getSpeakerName());
            
            CompletableFuture<Void> future = meetingPersistenceAsyncService.persistSegmentAsyncWithFuture(message);
            state.segmentPersistenceFutures.add(future);
        } catch (Exception e) {
            logger.error("[持久化段数据失败] meetingId: {}, segmentId: {}", 
                    state.meetingId, segmentResponse != null ? segmentResponse.getSegmentId() : null, e);
        }
    }

    private void waitForSegmentFutures(SessionState state, long maxWaitTimeMs) {
        if (state.segmentPersistenceFutures.isEmpty()) {
            return;
        }
        
        int futureCount = state.segmentPersistenceFutures.size();
        boolean allDone = state.segmentPersistenceFutures.stream().allMatch(CompletableFuture::isDone);
        
        if (allDone) {
            logger.info("[等待段数据Future] 所有任务已完成, meetingId: {}, futureCount: {}", state.meetingId, futureCount);
            return;
        }
        logger.info("[等待段数据Future] 开始等待, meetingId: {}, futureCount: {}, maxWaitTimeMs: {}", state.meetingId, futureCount, maxWaitTimeMs);
        try {
            CompletableFuture<?>[] futures = state.segmentPersistenceFutures.toArray(new CompletableFuture[0]);
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
            allFutures.get(maxWaitTimeMs, TimeUnit.MILLISECONDS);
            logger.info("[等待段数据Future] 完成, meetingId: {}, futureCount: {}", state.meetingId, futureCount);
        } catch (java.util.concurrent.TimeoutException e) {
            int completedCount = 0;
            for (CompletableFuture<Void> future : state.segmentPersistenceFutures) {
                if (future.isDone()) {
                    completedCount++;
                }
            }
            logger.warn("[等待段数据Future] 超时, meetingId: {}, completed: {}/{}, maxWaitTimeMs: {}", state.meetingId, completedCount, futureCount, maxWaitTimeMs);
        } catch (Exception e) {
            logger.error("[等待段数据Future] 异常, meetingId: {}, futureCount: {}", state.meetingId, futureCount, e);
        }
    }

    private void sendError(WebSocketSession session, String errorCode, String errorMessage, String stage) {
        try {
            ErrorResponse error = new ErrorResponse();
            error.setErrorCode(errorCode);
            error.setErrorMessage(errorMessage);
            error.setStage(stage);
            IMessageCustom.sendMessageCustom(session, IMessageEvent.BACK_ERROR, error);
        } catch (Exception e) {
            logger.error("Failed to send error", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("Meeting WebSocket V1 transport error, session id: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String sessionId = session.getId();
        heartbeatManager.unregisterSession(session);
        SessionState state = sessionStates.get(sessionId);
        
        if (state != null) {
            if (!state.stopped) {
                logger.warn("[连接异常断开-自动保存] meetingId: {}, closeStatus: {}", state.meetingId, closeStatus);
                try {
                    finishMeeting(session, state, 1);
                } catch (Exception e) {
                    logger.error("[连接断开-自动保存失败] meetingId: {}", state.meetingId, e);
                }
            }
            
            if (state.asrClient != null) {
                state.asrClient.disconnect();
                logger.info("[ASR连接已断开] meetingId: {}", state.meetingId);
            }
            if (state.audioManager != null) {
                state.audioManager.disconnect();
                logger.info("[OSS音频管理器已清理] meetingId: {}", state.meetingId);
            }
        }
        
        sessionStates.remove(sessionId);
        logger.info("Meeting WebSocket V1 closed, session id: {}, status: {}", sessionId, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
