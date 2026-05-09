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

import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.meeting.api.VoiceBaseServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VADRequest;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VADResponse;
import com.hojoai.webmvc.omniasst.meeting.util.StorageUtil;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.SpeakerInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.downgoon.snowflake.Snowflake;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MeetingAudioManager {

    private static final Logger logger = LoggerFactory.getLogger(MeetingAudioManager.class);
    /** 等待上传队列与正在执行的 OSS 写入结束，超时后打日志并返回，避免 stop 永久阻塞 */
    private static final long UPLOAD_DRAIN_TIMEOUT_MS = 120_000L;
    private static final long FRAME_MS = 320L;
    /** PCM 16kHz 16bit 单声道，每毫秒字节数 */
    private static final long PCM_BYTES_PER_MS = 32L;

    private final String meetingId;
    /** 当前推流会话用户，用于声纹库命中时仅对该用户展示实名 */
    private final String sessionUserId;
    private final StorageUtil storageUtil;
    private final MeetingVoiceprintManager voiceprintManager;   // 声纹管理器
    private final double voiceprintThreshold;                   // 声纹匹配阈值

    private final ConcurrentSkipListMap<Long, byte[]> audioCache = new ConcurrentSkipListMap<>();
    private final BlockingQueue<AudioPackage> audioQueue = new LinkedBlockingQueue<>();
    /** 已从队列取出、正在 processUploadPackage / processSegmentAudio 的任务数 */
    private final AtomicInteger uploadInFlight = new AtomicInteger(0);
    private ExecutorService uploadExecutor;
    private VoiceBaseServiceClient voiceClient;
    private Snowflake snowflake;
    // OSS状态
    private final String ossPcmObjectKey;
    private long ossCurrentPosition = 0;
    private long lastUploadedIndex = -1;                            // 最后已上传的帧索引

    // 声纹检测相关
    private static final int VOICEPRINT_DETECTION_THRESHOLD = 25;   // 声纹检测阈值：25个音频包
    private final ConcurrentHashMap<String, SegmentAudioInfo> segmentAudioMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SpeakerInfo> segmentSpeakerMap = new ConcurrentHashMap<>();  // segmentId -> SpeakerInfo

    private static class SegmentAudioInfo {
        String segmentId;
        List<byte[]> audioDataList = new ArrayList<>();     // 该段的音频包数据列表
        int savedCount = 0;                                 // 已保存的音频包数量

        SegmentAudioInfo(String segmentId) {
            this.segmentId = segmentId;
        }
    }

    private static class AudioPackage {
        long packageIndex;
        String segmentId;
        byte[] audioData;

        public AudioPackage(long packageIndex, byte[] audioData) {
            this.audioData = audioData;
            this.packageIndex = packageIndex;
        }

        AudioPackage(long packageIndex, String segmentId, byte[] audioData) {
            this.packageIndex = packageIndex;
            this.segmentId = segmentId;
            this.audioData = audioData;
        }
    }

    public MeetingAudioManager(String meetingId, String sessionUserId, StorageUtil storageUtil, MeetingVoiceprintManager voiceprintManager, double voiceprintThreshold, VoiceBaseServiceClient voiceClient, Snowflake snowflake) {
        this(meetingId, sessionUserId, storageUtil, voiceprintManager, voiceprintThreshold, voiceClient, snowflake, null);
    }

    /**
     * @param resumeElapsedMs 续接时时间轴已推进的毫秒数；null 或 ≤0 表示新会议
     */
    public MeetingAudioManager(String meetingId, String sessionUserId, StorageUtil storageUtil, MeetingVoiceprintManager voiceprintManager, double voiceprintThreshold, VoiceBaseServiceClient voiceClient, Snowflake snowflake, Long resumeElapsedMs) {
        this.meetingId = meetingId;
        this.sessionUserId = sessionUserId;
        this.storageUtil = storageUtil;
        this.voiceprintManager = voiceprintManager;
        this.voiceprintThreshold = voiceprintThreshold;
        this.ossPcmObjectKey = "meeting/" + meetingId + "/audio.pcm";
        this.voiceClient = voiceClient;
        this.snowflake = snowflake;

        if (resumeElapsedMs != null && resumeElapsedMs > 0) {
            this.ossCurrentPosition = resumeElapsedMs * PCM_BYTES_PER_MS;
            long nextFrameIdx = (resumeElapsedMs + FRAME_MS - 1) / FRAME_MS;
            this.lastUploadedIndex = nextFrameIdx - 1;
            logger.info("[初始化音频管理器-续接] meetingId: {}, resumeElapsedMs: {}, ossNextPosition: {}, lastUploadedIndex: {}",
                    meetingId, resumeElapsedMs, this.ossCurrentPosition, this.lastUploadedIndex);
        }

        logger.info("[初始化音频管理器] meetingId: {}, voiceprintThreshold: {}, sessionUserId: {}", meetingId, voiceprintThreshold, sessionUserId);

        startUploadExecutor();
    }

    private void startUploadExecutor() {
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "audio-upload-" + meetingId);
            thread.setDaemon(false);
            return thread;
        };
        
        uploadExecutor = Executors.newSingleThreadExecutor(threadFactory);

        uploadExecutor.submit(this::processUploadQueue);
        
        logger.info("[创建音频上传执行器] meetingId: {}", meetingId);
    }

    public void collectAudioPackage(long packageIndex, byte[] audioData) {
        audioCache.put(packageIndex, audioData);
        logger.debug("[收集音频包] meetingId: {}, packageIndex: {}, audioSize: {} bytes, 缓存总数: {}",
                meetingId, packageIndex, audioData.length, audioCache.size());
    }

    public void saveAudioPackage(long packageIndex, String segmentId) {
        byte[] audioData = audioCache.get(packageIndex);
        if (audioData == null) {
            logger.error("[保存音频包-音频数据不存在] meetingId: {}, packageIndex: {}", meetingId, packageIndex);
            return;
        }

        try {
            AudioPackage pkg = new AudioPackage(packageIndex, segmentId, audioData);
            audioQueue.put(pkg);
            logger.debug("[加入上传队列] meetingId: {}, packageIndex: {}, audioSize: {} bytes, 队列大小: {}",
                    meetingId, packageIndex, audioData.length, audioQueue.size());
        } catch (InterruptedException e) {
            logger.error("[保存音频包-加入队列失败] meetingId: {}, packageIndex: {}", meetingId, packageIndex, e);
            Thread.currentThread().interrupt();
        }
    }

    private void processUploadQueue() {
        logger.info("[启动上传队列处理] meetingId: {}", meetingId);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 阻塞等待下一个待上传的包
                AudioPackage pkg = audioQueue.poll(100, TimeUnit.MILLISECONDS);
                if (pkg == null) {
                    continue;
                }

                uploadInFlight.incrementAndGet();
                try {
                    processUploadPackage(pkg);
                    if (pkg.segmentId != null && pkg.audioData != null) {
                        processSegmentAudio(pkg.segmentId, pkg.packageIndex, pkg.audioData);
                    }
                } finally {
                    uploadInFlight.decrementAndGet();
                }

            } catch (InterruptedException e) {
                logger.info("[上传队列处理-中断] meetingId: {}", meetingId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("[上传队列处理-异常] meetingId: {}", meetingId, e);
            }
        }

        logger.info("[停止上传队列处理] meetingId: {}", meetingId);
    }

    private void processUploadPackage(AudioPackage pkg){
        try {
            List<AudioPackage> packagesToUpload = new ArrayList<>();

            if (pkg.packageIndex > lastUploadedIndex + 1) {
                logger.info("[检测到包跳跃] meetingId: {}, 上次上传: {}, 当前包: {}, 需补充: {} 到 {}",
                        meetingId, lastUploadedIndex, pkg.packageIndex,
                        lastUploadedIndex + 1, pkg.packageIndex - 1);

                // 收集中间所有包
                for (long i = lastUploadedIndex + 1; i <= pkg.packageIndex; i++) {
                    byte[] audioData = audioCache.get(i);
                    if (audioData != null) {
                        AudioPackage missingPkg = new AudioPackage(i, audioData);
                        packagesToUpload.add(missingPkg);
                    } else {
                        logger.error("[缺失包不存在] meetingId: {}, packageIndex: {}", meetingId, i);
                    }
                }
            } else if (pkg.packageIndex == lastUploadedIndex + 1) {
                byte[] audioData = audioCache.get(pkg.packageIndex);
                if (audioData != null) {
                    packagesToUpload.add(new AudioPackage(pkg.packageIndex, audioData));
                } else {
                    logger.error("[缺失包不存在] meetingId: {}, packageIndex: {}", meetingId, pkg.packageIndex);
                }
            }

            if (!packagesToUpload.isEmpty()) {
                uploadToOss(packagesToUpload);

                lastUploadedIndex = packagesToUpload.get(packagesToUpload.size() - 1).packageIndex;

                packagesToUpload.stream()
                        .map(uploadedPkg -> uploadedPkg.packageIndex)
                        .forEach(audioCache::remove);
            }

        } catch (Exception e) {
            logger.error("[上传队列处理-异常] meetingId: {}", meetingId, e);
        }
    }

    private void processSegmentAudio(String segmentId, long packageIndex, byte[] audioData) {
        try {
            if (segmentSpeakerMap.containsKey(segmentId)) {
                logger.debug("[跳过音频收集-已存在说话人信息] meetingId: {}, segmentId: {}, packageIndex: {}",
                        meetingId, segmentId, packageIndex);
                return;
            }

            SegmentAudioInfo segmentInfo = segmentAudioMap.computeIfAbsent(
                    segmentId, 
                    k -> new SegmentAudioInfo(segmentId)
            );

            synchronized (segmentInfo) {
                if (segmentSpeakerMap.containsKey(segmentId)) {
                    logger.debug("[双重检测-跳过音频收集] meetingId: {}, segmentId: {}, packageIndex: {}",
                            meetingId, segmentId, packageIndex);
                    return;
                }

                segmentInfo.audioDataList.add(audioData);
                segmentInfo.savedCount++;

                logger.debug("[段音频记录] meetingId: {}, segmentId: {}, packageIndex: {}, 音频大小: {} bytes, 当前计数: {}/{}", meetingId, segmentId, packageIndex, audioData.length, segmentInfo.savedCount, VOICEPRINT_DETECTION_THRESHOLD);

                // 检查是否达到声纹检测阈值
                if (segmentInfo.savedCount == VOICEPRINT_DETECTION_THRESHOLD) {
                    int totalAudioSize = segmentInfo.audioDataList.stream().mapToInt(data -> data.length).sum();
                    logger.info("[触发声纹检测] meetingId: {}, segmentId: {}, 音频包数: {}, 总音频大小: {} bytes", meetingId, segmentId, segmentInfo.savedCount, totalAudioSize);
                    // 调用声纹检测逻辑
                    performVoiceprintDetection(segmentId, segmentInfo, false);
                }
            }

        } catch (Exception e) {
            logger.error("[处理段音频失败] meetingId: {}, segmentId: {}, packageIndex: {}", 
                    meetingId, segmentId, packageIndex, e);
        }
    }

    private SpeakerInfo performVoiceprintDetection(String segmentId, SegmentAudioInfo segmentInfo, boolean forceCreateFallback) {
        try {
            logger.info("[开始声纹检测] meetingId: {}, segmentId: {}, 音频包数: {}",
                    meetingId, segmentId, segmentInfo.savedCount);

            // 先筛选出符合条件的音频包
            List<byte[]> filteredAudioList = new ArrayList<>();
            String vadVid = String.valueOf(snowflake.nextId());
            int audioNum = 0;
            logger.info("[声纹检测][声纹音频包筛选开始], 收集包数量: {}", segmentInfo.audioDataList.size());
            for (byte[] audioData : segmentInfo.audioDataList) {
                Response<VADResponse> response = voiceClient.vad(this.makeVadRequest(audioData, vadVid));
                if (Objects.isNull(response) || !"OK".equals(response.getResCode()) || response.getResContent().getRtn() != 0) {
                    // 重置vad vid
                    vadVid = String.valueOf(snowflake.nextId());
                }
                if (evaluate(response.getResContent()) >= 10) {
                    filteredAudioList.add(audioData);
                    audioNum++;
                }
            }
            logger.info("[声纹检测][声纹音频包筛选结束], 通过筛选的包个数: {}", audioNum);
            
            if (filteredAudioList.isEmpty()) {
                logger.warn("[声纹检测-无有效音频包] meetingId: {}, segmentId: {}, 所有音频包均未通过VAD筛选", meetingId, segmentId);
                return voiceprintManager.recognizeAndMatch(meetingId, new byte[0], voiceprintThreshold, forceCreateFallback, 0, sessionUserId);
            }
            
            int totalSize = filteredAudioList.stream().mapToInt(data -> data.length).sum();
            byte[] mergedAudio = new byte[totalSize];
            int offset = 0;
            for (byte[] audioData : filteredAudioList) {
                System.arraycopy(audioData, 0, mergedAudio, offset, audioData.length);
                offset += audioData.length;
            }
            
            logger.info("[声纹检测-音频数据准备完成] meetingId: {}, segmentId: {}, 筛选前包数: {}, 筛选后包数: {}, 总大小: {} bytes", 
                    meetingId, segmentId, segmentInfo.audioDataList.size(), audioNum, totalSize);
            
            SpeakerInfo recognizedSpeaker = voiceprintManager.recognizeAndMatch(meetingId, mergedAudio, voiceprintThreshold, forceCreateFallback, audioNum, sessionUserId);
            
            if (recognizedSpeaker != null) {
                segmentSpeakerMap.put(segmentId, recognizedSpeaker);
                logger.info("[声纹检测完成-已保存] meetingId: {}, segmentId: {}, speakerId: {}, speakerName: {}",
                        meetingId, segmentId, recognizedSpeaker.getSpeakerId(), recognizedSpeaker.getSpeakerName());
                return recognizedSpeaker;
            } else {
                logger.warn("[声纹检测失败-无识别结果] meetingId: {}, segmentId: {}", meetingId, segmentId);
            }

        } catch (Exception e) {
            logger.error("[声纹检测异常] meetingId: {}, segmentId: {}", meetingId, segmentId, e);
        }
        return null;
    }

    public SpeakerInfo voiceprintRecognition(String segmentId) {
        if (StringUtils.isEmpty(segmentId)) {
            logger.error("[触发声纹识别-参数错误] meetingId: {}, segmentId为空", meetingId);
            return voiceprintManager.recognizeAndMatch(meetingId, new byte[0], voiceprintThreshold, true, 0, sessionUserId);
        }

        try {
            if (segmentSpeakerMap.containsKey(segmentId)) {
                SpeakerInfo existingSpeaker = segmentSpeakerMap.get(segmentId);
                logger.info("[返回已有说话人信息] meetingId: {}, segmentId: {}, speakerId: {}, speakerName: {}",
                        meetingId, segmentId, existingSpeaker.getSpeakerId(), existingSpeaker.getSpeakerName());
                return existingSpeaker;
            }

            SegmentAudioInfo segmentInfo = segmentAudioMap.get(segmentId);
            
            if (segmentInfo == null || segmentInfo.audioDataList.isEmpty()) {
                logger.warn("[触发声纹识别-音频数据不足] meetingId: {}, segmentId: {}", meetingId, segmentId);
                return voiceprintManager.recognizeAndMatch(meetingId, new byte[0], voiceprintThreshold, true, 0, sessionUserId);
            }

            synchronized (segmentInfo) {
                if (segmentSpeakerMap.containsKey(segmentId)) {
                    SpeakerInfo existingSpeaker = segmentSpeakerMap.get(segmentId);
                    logger.info("[双重检测-返回已有说话人信息] meetingId: {}, segmentId: {}, speakerId: {}, speakerName: {}", meetingId, segmentId, existingSpeaker.getSpeakerId(), existingSpeaker.getSpeakerName());
                    return existingSpeaker;
                }

                int totalAudioSize = segmentInfo.audioDataList.stream().mapToInt(data -> data.length).sum();

                logger.info("[手动触发声纹识别] meetingId: {}, segmentId: {}, 音频包数: {}, 总音频大小: {} bytes", meetingId, segmentId, segmentInfo.savedCount, totalAudioSize);

                SpeakerInfo result = performVoiceprintDetection(segmentId, segmentInfo, true);
                if (result == null) {
                    logger.warn("[声纹检测返回null-强制创建兜底] meetingId: {}, segmentId: {}", meetingId, segmentId);
                    result = voiceprintManager.recognizeAndMatch(meetingId, new byte[0], voiceprintThreshold, true, 0, sessionUserId);
                    if (result != null) {
                        segmentSpeakerMap.put(segmentId, result);
                    }
                }
                return result;
            }

        } catch (Exception e) {
            logger.error("[触发声纹识别-异常] meetingId: {}, segmentId: {}", meetingId, segmentId, e);
            return voiceprintManager.recognizeAndMatch(meetingId, new byte[0], voiceprintThreshold, true, 0, sessionUserId);
        }
    }

    public SpeakerInfo getSpeakerInfo(String segmentId) {
        if (segmentId == null || segmentId.isEmpty()) {
            logger.error("[获取说话人信息-参数错误] meetingId: {}, segmentId为空", meetingId);
            return null;
        }

        return segmentSpeakerMap.get(segmentId);
    }

    public void cleanupSegment(String segmentId) {
        if (segmentId == null || segmentId.isEmpty()) {
            logger.warn("[清理段数据-参数无效] meetingId: {}, segmentId为空", meetingId);
            return;
        }

        try {
            SegmentAudioInfo audioInfo = segmentAudioMap.remove(segmentId);
            int audioPackageCount = audioInfo != null ? audioInfo.savedCount : 0;
            
            logger.info("[清理段数据完成] meetingId: {}, segmentId: {}, 音频包数: {}", meetingId, segmentId, audioPackageCount);
            
        } catch (Exception e) {
            logger.error("[清理段数据异常] meetingId: {}, segmentId: {}", meetingId, segmentId, e);
        }
    }

    /**
     * 批量上传音频包到OSS（一次性拼接所有包）
     */
    private void uploadToOss(List<AudioPackage> packages) {
        try {
            if (packages == null || packages.isEmpty()) {
                logger.warn("[上传音频包-包列表为空] meetingId: {}", meetingId);
                return;
            }

            // 计算总大小
            int totalSize = 0;
            for (AudioPackage pkg : packages) {
                totalSize += pkg.audioData.length;
            }

            // 拼接所有音频数据
            byte[] mergedAudioData = new byte[totalSize];
            int offset = 0;
            for (AudioPackage pkg : packages) {
                System.arraycopy(pkg.audioData, 0, mergedAudioData, offset, pkg.audioData.length);
                offset += pkg.audioData.length;
            }

            // 一次性上传
            long uploadPosition = ossCurrentPosition;
            long actualNextPosition = storageUtil.appendObject(
                    ossPcmObjectKey,
                    mergedAudioData,
                    uploadPosition
            );

            long firstIndex = packages.get(0).packageIndex;
            long lastIndex = packages.get(packages.size() - 1).packageIndex;
            
            logger.info("[批量上传音频包-成功] meetingId: {}, packageIndex: {} -> {}, 包数: {}, 总大小: {} bytes, position: {} -> {}",
                    meetingId, firstIndex, lastIndex, packages.size(), totalSize, uploadPosition, actualNextPosition);

            // 更新OSS位置
            ossCurrentPosition = actualNextPosition;

            // 校验位置是否一致
            long expectedPosition = uploadPosition + totalSize;
            if (actualNextPosition != expectedPosition) {
                logger.error("[OSS位置不一致] meetingId: {}, packageIndex: {} -> {}, expected: {}, actual: {}",
                        meetingId, firstIndex, lastIndex, expectedPosition, actualNextPosition);
            }

        } catch (Exception e) {
            logger.error("[批量上传音频包-失败] meetingId: {}, 包数: {}",
                    meetingId, packages != null ? packages.size() : 0, e);
            throw new RuntimeException("批量上传音频包失败", e);
        }
    }

    public String getOssPcmObjectKey() {
        return ossPcmObjectKey;
    }

    /** OSS 上已追加的 PCM 字节数；为 0 表示本会话尚未成功写入任何分片 */
    public long getOssCurrentPosition() {
        return ossCurrentPosition;
    }

    /**
     * 等待所有上传任务完成
     */
    public void waitForUploadComplete() {
        logger.info("[等待上传完成] meetingId: {}, 队列剩余: {}, inFlight: {}", meetingId, audioQueue.size(), uploadInFlight.get());

        long deadline = System.currentTimeMillis() + UPLOAD_DRAIN_TIMEOUT_MS;
        while ((!audioQueue.isEmpty() || uploadInFlight.get() > 0) && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("[等待上传完成-中断] meetingId: {}", meetingId);
                break;
            }
        }
        if (!audioQueue.isEmpty() || uploadInFlight.get() > 0) {
            logger.warn("[等待上传完成-超时] meetingId: {}, 队列: {}, inFlight: {}", meetingId, audioQueue.size(), uploadInFlight.get());
        } else {
            logger.info("[上传完成] meetingId: {}", meetingId);
        }
    }

    public void disconnect() {
        logger.info("[清理会议资源] meetingId: {}", meetingId);

        // 等待上传完成
        waitForUploadComplete();

        // 关闭上传执行器
        if (uploadExecutor != null) {
            uploadExecutor.shutdown();
            try {
                if (!uploadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("[强制关闭执行器] meetingId: {}", meetingId);
                    uploadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("[关闭执行器-中断] meetingId: {}", meetingId, e);
                uploadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清理缓存和队列
        logger.info("[清理音频缓存] meetingId: {}, 剩余帧数: {}", meetingId, audioCache.size());
        audioCache.clear();

        logger.info("[清理上传队列] meetingId: {}, 剩余帧数: {}", meetingId, audioQueue.size());
        audioQueue.clear();

        logger.info("[清理完成] meetingId: {}", meetingId);
    }

    private VADRequest makeVadRequest(byte[] audioData, String vid) {
        VADRequest request = new VADRequest();
        String s = Base64.getEncoder().encodeToString(audioData);
        request.setPackageData(s);
        request.setPackageSize(s.length());
        request.setVadEnd(60);
        request.setVid(vid);
        request.setVadDrop(0);
        return request;
    }

    private int evaluate(VADResponse vadResponse) {
        if (Objects.isNull(vadResponse) || Objects.isNull(vadResponse.getVadData()) || vadResponse.getVadData().length == 0) {
            return 0;
        }
        return Arrays.stream(vadResponse.getVadData()).sum();
    }

}
