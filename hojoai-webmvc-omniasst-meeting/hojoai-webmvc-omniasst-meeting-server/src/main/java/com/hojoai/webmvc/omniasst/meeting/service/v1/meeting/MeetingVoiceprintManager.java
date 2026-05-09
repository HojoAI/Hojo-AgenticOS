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
package com.hojoai.webmvc.omniasst.meeting.service.v1.meeting;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.VoiceBaseServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.*;
import com.hojoai.webmvc.omniasst.meeting.service.voiceprint.bean.MeetingVoiceprintData;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.SpeakerInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xyz.downgoon.snowflake.Snowflake;

import java.util.ArrayList;
import java.util.Base64;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 会议实时会话内的声纹：匹配、向量缓存、与 management / 声纹服务交互。
 */
@Service
public class MeetingVoiceprintManager {

    private static final Logger logger = LoggerFactory.getLogger(MeetingVoiceprintManager.class);

    @Autowired
    private VoiceBaseServiceClient voiceBaseServiceClient;

    @Autowired
    private ManagementServiceClient managementServiceClient;

    @Autowired
    private Snowflake snowflake;

    // 会议说话人计数器管理
    private final ConcurrentHashMap<String, AtomicInteger> meetingSpeakerCounters = new ConcurrentHashMap<>();

    // 声纹内存存储
    private final Map<String, List<MeetingVoiceprintData>> voiceprintStore = new ConcurrentHashMap<>();

    /**
     * @param sessionUserId 当前 WebSocket 会话用户；仅当库表声纹命中该用户时返回实名，其余说话人统一为「发言人N」
     */
    public SpeakerInfo recognizeAndMatch(String meetingId, byte[] audioData,
                                          double voiceprintThreshold, boolean forceCreateFallback, int filteredAudioPackageCount,
                                          String sessionUserId) {
        AtomicInteger speakerCounter = meetingSpeakerCounters.computeIfAbsent(
                meetingId,
                k -> new AtomicInteger(0)
        );

        try {
            if (audioData == null || audioData.length == 0) {
                logger.warn("[音频数据为空] meetingId: {}, 创建说话人", meetingId);
                return saveVoicePrintToMemory(meetingId, null, null, null, null, speakerCounter, "音频数据为空");
            }

            String voiceprintVid = "v" + snowflake.nextId();
            logger.info("[开始声纹识别] meetingId: {}, vid: {}, audioSize: {} bytes, forceCreateFallback: {}, filteredPackageCount: {}, sessionUserId: {}", 
                    meetingId, voiceprintVid, audioData.length, forceCreateFallback, filteredAudioPackageCount, sessionUserId);

            double[] voiceprint = recognizeVoiceprint(meetingId, voiceprintVid, audioData);
            
            if (voiceprint == null || voiceprint.length == 0) {
                logger.warn("[声纹识别失败] meetingId: {}, vid: {}, 创建说话人", meetingId, voiceprintVid);
                return saveVoicePrintToMemory(meetingId, voiceprintVid, null, null, null, speakerCounter, "声纹识别失败");
            }

            logger.info("[声纹识别成功] meetingId: {}, vid: {}, voiceprintLength: {}",
                    meetingId, voiceprintVid, voiceprint.length);

            SpeakerInfo matchedSpeaker = matchVoiceprint(meetingId, voiceprintVid, voiceprint, voiceprintThreshold, speakerCounter, filteredAudioPackageCount, sessionUserId);
            
            if (matchedSpeaker != null) {
                return matchedSpeaker;
            }

            return saveVoicePrintToMemory(meetingId, voiceprintVid, null, null, voiceprint, speakerCounter, "数据库和内存都未匹配到");

        } catch (Exception e) {
            logger.error("[声纹识别流程异常] meetingId: {}", meetingId, e);
            return saveVoicePrintToMemory(meetingId, null, null, null, null, speakerCounter, "异常情况");
        }
    }

    private double[] recognizeVoiceprint(String meetingId, String voiceprintVid, byte[] audioData) {
        try {
            // 构建声纹识别请求
            VoiceprintRequest request = new VoiceprintRequest();
            request.setVid(voiceprintVid);
            String audioBase64 = Base64.getEncoder().encodeToString(audioData);
            request.setAudio(audioBase64);

            logger.info("[声纹请求发送] meetingId: {}, vid: {}, audioBase64Length: {}", 
                    meetingId, voiceprintVid, audioBase64.length());

            // 调用声纹识别服务
            VoiceprintResponse response = voiceBaseServiceClient.recognize(request).getResContent();

            logger.info("[声纹响应收到] meetingId: {}, vid: {}, rtn: {}, msg: {}", 
                    meetingId, voiceprintVid, 
                    response != null ? response.getRtn() : "null", 
                    response != null ? response.getMsg() : "null");

            // 检查响应
            if (response == null || response.getRtn() == null || response.getRtn() != 0) {
                return null;
            }

            // 获取声纹向量
            double[] voiceprint = response.getVoicePrint();
            if (voiceprint == null || voiceprint.length == 0) {
                logger.warn("[声纹向量为空] meetingId: {}, vid: {}", meetingId, voiceprintVid);
                return null;
            }

            return voiceprint;

        } catch (Exception e) {
            logger.error("[声纹识别异常] meetingId: {}, vid: {}", meetingId, voiceprintVid, e);
            return null;
        }
    }

    private SpeakerInfo matchVoiceprint(String meetingId, String voiceprintVid, double[] voiceprint, double threshold, AtomicInteger speakerCounter, int filteredAudioPackageCount, String sessionUserId) {
        try {
            boolean forceMatchBest = filteredAudioPackageCount < 9;
            logger.info("[开始匹配声纹-并发执行] meetingId: {}, vid: {}, threshold: {}, filteredPackageCount: {}, forceMatchBest: {}", 
                    meetingId, voiceprintVid, threshold, filteredAudioPackageCount, forceMatchBest);

            VoiceprintMatchRequest matchRequest = new VoiceprintMatchRequest();
            matchRequest.setVoiceprint(voiceprint);
            matchRequest.setThreshold(0.35);

            CompletableFuture<UserMatchResult> databaseFuture = CompletableFuture.supplyAsync(() -> 
                matchFromDatabase(meetingId, voiceprintVid, matchRequest)
            );

            CompletableFuture<UserMatchResult> memoryFuture = CompletableFuture.supplyAsync(() -> 
                matchFromMemory(meetingId, voiceprintVid, voiceprint, threshold, forceMatchBest)
            );

            try {
                UserMatchResult databaseMatch = databaseFuture.get();
                UserMatchResult memoryMatch = memoryFuture.get();

                if (databaseMatch != null && isSessionUser(databaseMatch.getUserId(), sessionUserId)) {
                    logger.info("[匹配成功-数据库-当前会话用户] meetingId: {}, vid: {}, userId: {}, userName: {}", meetingId, voiceprintVid, databaseMatch.getUserId(), databaseMatch.getUserName());
                    return convertToSpeakerInfo(databaseMatch);
                }
                if (databaseMatch != null) {
                    logger.info("[忽略数据库匹配-非当前会话用户] meetingId: {}, vid: {}, matchedUserId: {}, sessionUserId: {}",
                            meetingId, voiceprintVid, databaseMatch.getUserId(), sessionUserId);
                }

                if (memoryMatch != null) {
                    logger.info("[匹配成功-内存] meetingId: {}, vid: {}, speakerId: {}, speakerName: {}", meetingId, voiceprintVid, memoryMatch.getUserId(), memoryMatch.getUserName());
                    return convertToSpeakerInfo(memoryMatch);
                }

                logger.info("[匹配失败-未找到] meetingId: {}, vid: {}", meetingId, voiceprintVid);
                return null;

            } catch (Exception e) {
                logger.error("[等待匹配结果异常] meetingId: {}, vid: {}", meetingId, voiceprintVid, e);
                return null;
            }

        } catch (Exception e) {
            logger.error("[匹配声纹异常] meetingId: {}, vid: {}", meetingId, voiceprintVid, e);
            return null;
        }
    }

    private static boolean isSessionUser(String matchedUserId, String sessionUserId) {
        return StringUtils.isNotBlank(sessionUserId) && Objects.equals(matchedUserId, sessionUserId);
    }

    private UserMatchResult matchFromDatabase(String meetingId, String voiceprintVid, 
                                               VoiceprintMatchRequest matchRequest) {
        try {
            Response<UserMatchResult> response = managementServiceClient.matchVoiceprint(matchRequest);
            logger.info("[声纹匹配][数据库匹配] api调用结果: {}", JacksonUtil.bean2Json(response));
            if (response != null && response.getResContent() != null) {
                return response.getResContent();
            }
        } catch (Exception e) {
            logger.error("[数据库匹配异常] meetingId: {}, vid: {}", meetingId, voiceprintVid, e);
        }
        return null;
    }

    private UserMatchResult matchFromMemory(String meetingId, String voicePrintVid, double[] voicePrint, double threshold, boolean forceMatchBest) {
        try {
            if (voicePrint == null || voicePrint.length == 0 || StringUtils.isEmpty(meetingId)) {
                return null;
            }
            
            List<MeetingVoiceprintData> meetingDataList = voiceprintStore.get(meetingId);
            if (CollectionUtils.isEmpty(meetingDataList)) {
                return null;
            }
            
            AtomicReference<Double> maxSimilarity = new AtomicReference<>(0.0);
            AtomicReference<MeetingVoiceprintData> matched = new AtomicReference<>();
            meetingDataList.forEach(data -> {
                double similarity = calculateCosineSimilarity(voicePrint, data.getVoiceprint());
                if (similarity > maxSimilarity.get()) {
                    maxSimilarity.set(similarity);
                    matched.set(data);
                }
            });
            
            if (matched.get() == null) {
                logger.info("[声纹匹配失败-内存-无匹配] meetingId: {}, vid: {}, maxSimilarity: {}, threshold: {}", meetingId, voicePrintVid, maxSimilarity, threshold);
                return null;
            }
            
            if (forceMatchBest) {
                logger.info("[声纹匹配成功-内存-强制匹配] meetingId: {}, vid: {}, speakerId: {}, speakerName: {}, similarity: {}", meetingId, voicePrintVid, matched.get().getSpeakerId(), matched.get().getSpeakerName(), maxSimilarity.get());
                return new UserMatchResult(matched.get().getSpeakerId(), matched.get().getSpeakerName(), "");
            }
            
            if (maxSimilarity.get() >= threshold) {
                logger.info("[声纹匹配成功-内存] meetingId: {}, vid: {}, speakerId: {}, speakerName: {}, similarity: {}, threshold: {}", meetingId, voicePrintVid, matched.get().getSpeakerId(), matched.get().getSpeakerName(), maxSimilarity.get(), threshold);
                return new UserMatchResult(matched.get().getSpeakerId(), matched.get().getSpeakerName(), "");
            }
            
            logger.info("[声纹匹配失败-内存-未达阈值] meetingId: {}, vid: {}, speakerId: {}, speakerName: {}, similarity: {}, threshold: {}", meetingId, voicePrintVid, matched.get().getSpeakerId(), matched.get().getSpeakerName(), maxSimilarity.get(), threshold);
            return null;
        } catch (Exception e) {
            logger.error("[内存匹配异常] meetingId: {}, vid: {}", meetingId, voicePrintVid, e);
        }
        return null;
    }

    private double calculateCosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have same length");
        }
        RealVector v1 = new ArrayRealVector(vector1);
        RealVector v2 = new ArrayRealVector(vector2);
        return v1.cosine(v2);
    }

    private SpeakerInfo saveVoicePrintToMemory(String meetingId, String voicePrintVid, String speakerName, String speakerId,
                                                 double[] voicePrint, AtomicInteger speakerCounter, String reason) {
        if (StringUtils.isEmpty(speakerId) || StringUtils.isEmpty(speakerName)) {
            int speakerIndex = speakerCounter.incrementAndGet();
            speakerId = "UserId-" + speakerIndex;
            speakerName = "发言人" + speakerIndex;
        }
        SpeakerInfo speakerInfo = new SpeakerInfo();
        speakerInfo.setSpeakerId(speakerId);
        speakerInfo.setSpeakerName(speakerName);
        speakerInfo.setSpeakerAvatar("");
        try {
            List<MeetingVoiceprintData> meetingDataList = voiceprintStore.computeIfAbsent(meetingId, k -> new ArrayList<>());
            if (voicePrint != null && voicePrint.length > 0) {
                meetingDataList.add(new MeetingVoiceprintData(speakerId, speakerName, voicePrint));
                logger.info("[创建说话人] meetingId: {}, speakerId: {}, speakerName: {}, reason: {}, 声纹已保存", meetingId, speakerId, speakerName, reason);
            } else {
                logger.info("[创建说话人] meetingId: {}, speakerId: {}, speakerName: {}, reason: {}, 声纹为空未保存", meetingId, speakerId, speakerName, reason);
            }
        } catch (Exception e) {
            logger.error("[保存说话人到内存失败] meetingId: {}, vid: {}, speakerId: {}", meetingId, voicePrintVid, speakerId, e);
        }
        return speakerInfo;
    }

    public int deleteVoiceprintsByMeeting(String meetingId) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            return 0;
        }
        List<MeetingVoiceprintData> meetingVoicePrintData = voiceprintStore.remove(meetingId);
        int size = meetingVoicePrintData != null ? meetingVoicePrintData.size() : 0;
        logger.info("[声纹数据已删除] meetingId: {}, 删除数量: {}", meetingId, size);
        return size;
    }

    private SpeakerInfo convertToSpeakerInfo(UserMatchResult matchResult) {
        SpeakerInfo speakerInfo = new SpeakerInfo();
        speakerInfo.setSpeakerId(matchResult.getUserId());
        speakerInfo.setSpeakerName(matchResult.getUserName());
        speakerInfo.setSpeakerAvatar(matchResult.getAvatar() != null ? matchResult.getAvatar() : "");
        return speakerInfo;
    }

    public void cleanupMeeting(String meetingId) {
        if (meetingId == null || meetingId.isEmpty()) {
            return;
        }
        
        // 清理说话人计数器
        AtomicInteger counter = meetingSpeakerCounters.remove(meetingId);
        
        // 清理声纹数据
        int voiceprintCount = deleteVoiceprintsByMeeting(meetingId);
        
        logger.info("[清理会议资源完成] meetingId: {}, 说话人数: {}, 声纹数量: {}", 
                meetingId, 
                counter != null ? counter.get() : 0, 
                voiceprintCount);
    }

    public void correctSpeakerIndex(String meetingId) {
        AtomicInteger speakerCounter = meetingSpeakerCounters.computeIfAbsent(meetingId, k -> new AtomicInteger(0));
        speakerCounter.decrementAndGet();
    }

}
