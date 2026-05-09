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
package com.hojoai.webmvc.omniasst.meeting.service.importtask;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingPersistenceService;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingSegmentPersistenceService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSegmentMessage;
import com.hojoai.webmvc.omniasst.meeting.service.v1.meeting.AsrWebSocketClient;
import com.hojoai.webmvc.omniasst.meeting.util.AudioConverter;
import com.hojoai.webmvc.omniasst.meeting.util.StorageUtil;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.MeetingResultMessage;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.AsrPlatform;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrConfigRequest;
import com.hojoai.webmvc.omniasst.meeting.websocket.v1.dto.AsrWebSocketRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import xyz.downgoon.snowflake.Snowflake;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 外部 OSS 导入录音的“直处理”实现：
 * 下载音频 →（WAV：Java Sound；mp3/m4a：ffmpeg）→ 16k/16bit/mono PCM → ASR WebSocket → 落段 → 纪要落库。
 *
 * <p>mp3/m4a 依赖运行环境安装 ffmpeg，见 {@code meeting.import.ffmpeg.*} 配置。
 */
@Service
public class MeetingImportDirectProcessService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingImportDirectProcessService.class);
    private static final String ASR_SERVICE_NAME = "hojoai-webmvc-omniasst-voice";
    private static final String ASR_WEBSOCKET_PATH = "/omniasst-voice/asr/v1";
    private static final Random RANDOM = new Random();

    /**
     * 与前端/ASR 约定的标准帧大小：10240 bytes ≈ 320ms（16k*16bit*mono）
     */
    private static final int PCM_FRAME_BYTES = 10240;
    private static final long BYTES_PER_MS = 32; // 16kHz * 16bit * 1ch = 32000 bytes/s

    private final MeetingImportTaskDAO meetingImportTaskDAO;
    private final StorageUtil storageUtil;
    private final AudioConverter audioConverter;
    private final DiscoveryClient discoveryClient;
    private final Snowflake snowflake;
    private final MeetingSegmentPersistenceService meetingSegmentPersistenceService;
    private final MeetingPersistenceService meetingPersistenceService;

    @Value("${meeting.import.direct.asr.final-wait-seconds:10}")
    private int finalWaitSeconds;

    /**
     * 每帧推送后休眠 = 该帧对应音频时长(ms) × 本系数。1.0=与线上一致（最慢最稳）；0.25≈4 倍速；0=不间隔（易 ASR 空结果）。
     */
    @Value("${meeting.import.direct.asr.pace-realtime-factor:0.25}")
    private double paceRealtimeFactor;

    public MeetingImportDirectProcessService(
            MeetingImportTaskDAO meetingImportTaskDAO,
            StorageUtil storageUtil,
            AudioConverter audioConverter,
            DiscoveryClient discoveryClient,
            Snowflake snowflake,
            MeetingSegmentPersistenceService meetingSegmentPersistenceService,
            MeetingPersistenceService meetingPersistenceService) {
        this.meetingImportTaskDAO = meetingImportTaskDAO;
        this.storageUtil = storageUtil;
        this.audioConverter = audioConverter;
        this.discoveryClient = discoveryClient;
        this.snowflake = snowflake;
        this.meetingSegmentPersistenceService = meetingSegmentPersistenceService;
        this.meetingPersistenceService = meetingPersistenceService;
    }

    public void process(String meetingId) {
        if (StringUtils.isBlank(meetingId)) {
            return;
        }
        MeetingImportTask task = meetingImportTaskDAO.selectByMeetingId(meetingId);
        if (task == null) {
            logger.warn("[导入直处理] 任务不存在 meetingId={}", meetingId);
            return;
        }

        File downloaded = null;
        File pcmFile = null;
        AsrWebSocketClient asr = null;
        try {
            meetingImportTaskDAO.updatePipelineStatus(meetingId, MeetingImportConstants.PIPELINE_TRANSCRIBING);

            String ext = StringUtils.trimToEmpty(task.getFileExt()).toLowerCase(Locale.ROOT);
            if (!isDirectProcessAudioExt(ext)) {
                logger.warn("[导入直处理] 不支持的格式，已标记失败 meetingId={}, fileExt={}, objectKey={}",
                        meetingId, task.getFileExt(), task.getOssObjectKey());
                meetingImportTaskDAO.updatePipelineFailed(meetingId,
                        MeetingImportConstants.ERROR_UNSUPPORTED_FORMAT,
                        "直处理仅支持 wav、mp3、m4a");
                return;
            }

            downloaded = storageUtil.downloadObject(task.getOssObjectKey());
            if (downloaded == null || !downloaded.exists() || downloaded.length() == 0) {
                meetingImportTaskDAO.updatePipelineFailed(meetingId,
                        MeetingImportConstants.ERROR_NOT_FOUND,
                        "下载录音失败或文件为空");
                return;
            }

            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
            String pcmName = "import_" + meetingId + "_" + System.currentTimeMillis() + ".pcm";
            String pcmPath = tempDir.resolve(pcmName).toString();
            if ("wav".equals(ext)) {
                pcmFile = audioConverter.convertWavToPcm(downloaded, pcmPath);
            } else {
                pcmFile = audioConverter.convertCompressedAudioToPcm(downloaded, pcmPath);
            }

            String asrUrl = resolveAsrWebSocketUrl();
            if (asrUrl == null) {
                meetingImportTaskDAO.updatePipelineFailed(meetingId,
                        "ASR_UNAVAILABLE",
                        "ASR 服务不可用（服务发现失败）");
                return;
            }

            StringBuilder transcript = new StringBuilder();
            CountDownLatch endLatch = new CountDownLatch(1);
            AtomicLong currentAudioMs = new AtomicLong(0);
            AtomicLong audioIndex = new AtomicLong(0);
            AtomicReference<String> lastAsrText = new AtomicReference<>("");

            asr = new AsrWebSocketClient(meetingId, asrUrl);
            AsrWebSocketClient finalAsr = asr;
            asr.setResponseCallback(resp -> {
                try {
                    if (resp == null) {
                        return;
                    }
                    // 兼容不同 ASR 返回：有的只填 text/tmpText，有的用 fixedText
                    String fixed = StringUtils.trimToNull(resp.getFixedText());
                    String text = StringUtils.trimToNull(resp.getText());
                    String tmp = StringUtils.trimToNull(resp.getTmpText());
                    if (text != null) {
                        lastAsrText.set(text);
                    } else if (fixed != null) {
                        lastAsrText.set(fixed);
                    } else if (tmp != null) {
                        lastAsrText.set(tmp);
                    }

                    // 以 isEnd=1 作为“一个句段结束”，把 fixed/text 落到 transcript
                    Integer isEnd = resp.getIsEnd();
                    if (isEnd != null && isEnd == 1) {
                        String line = fixed != null ? fixed : (text != null ? text : tmp);
                        if (line != null) {
                            if (!transcript.isEmpty()) {
                                transcript.append('\n');
                            }
                            transcript.append(line);
                        }
                    }

                    // 若服务端回传 custom 中带 isStop=true，则可提前结束等待
                    String custom = StringUtils.trimToNull(resp.getCustom());
                    if (custom != null && custom.contains("\"isStop\":true")) {
                        endLatch.countDown();
                    }
                } finally {
                    // 尝试捕捉结束信号：如果服务端在最后一次返回带 isEnd=1 且 custom.isStop=true，这里无法可靠判断，
                    // 因此我们由发送端在发送 isEnd=true 后做超时等待。
                }
            });

            boolean connected = asr.connect().get(15, TimeUnit.SECONDS);
            if (!connected || !asr.isConnected()) {
                meetingImportTaskDAO.updatePipelineFailed(meetingId,
                        "ASR_CONNECT_FAILED",
                        "ASR 连接失败");
                return;
            }

            AsrConfigRequest cfg = new AsrConfigRequest();
            cfg.setPlatform(AsrPlatform.HOJO.getCode());
            cfg.setType("pcm");
            boolean cfgSent = asr.sendConfig(cfg);
            if (!cfgSent) {
                meetingImportTaskDAO.updatePipelineFailed(meetingId,
                        "ASR_CONFIG_FAILED",
                        "ASR 配置发送失败");
                return;
            }

            try (FileInputStream fis = new FileInputStream(pcmFile)) {
                byte[] buf = new byte[PCM_FRAME_BYTES];
                int n;
                long previousAudioSendTime = 0L;
                while ((n = fis.read(buf)) != -1) {
                    if (n <= 0) {
                        continue;
                    }
                    byte[] frame = (n == buf.length) ? buf : copyOf(buf, n);
                    long durMs = Math.max(1L, frame.length / BYTES_PER_MS);
                    long cur = currentAudioMs.addAndGet(durMs);

                    AsrWebSocketRequest.CustomData customData = new AsrWebSocketRequest.CustomData();
                    customData.setAudioIndex(audioIndex.getAndIncrement());
                    customData.setPreviousAudioSendTime(previousAudioSendTime);
                    customData.setCurrentAudioTime(cur);
                    customData.setIsStop(false);
                    previousAudioSendTime = cur;

                    AsrWebSocketRequest req = new AsrWebSocketRequest();
                    req.setAudioData(Base64.getEncoder().encodeToString(frame));
                    req.setCustom(JacksonUtil.bean2Json(customData));
                    req.setIsEnd(false);

                    boolean sent = finalAsr.sendAudioData(req);
                    if (!sent) {
                        meetingImportTaskDAO.updatePipelineFailed(meetingId,
                                "ASR_SEND_FAILED",
                                "发送音频到 ASR 失败");
                        return;
                    }
                    double pace = Math.min(2.0, Math.max(0.0, paceRealtimeFactor));
                    if (pace > 0) {
                        long sleepMs = Math.max(1L, Math.round(durMs * pace));
                        try {
                            Thread.sleep(sleepMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("导入直处理推流被中断", ie);
                        }
                    }
                }
            }

            // 发送结束帧
            AsrWebSocketRequest.CustomData endCustom = new AsrWebSocketRequest.CustomData();
            endCustom.setAudioIndex(audioIndex.get());
            endCustom.setPreviousAudioSendTime(currentAudioMs.get());
            endCustom.setCurrentAudioTime(currentAudioMs.get());
            endCustom.setIsStop(true);
            AsrWebSocketRequest endReq = new AsrWebSocketRequest();
            endReq.setAudioData("");
            endReq.setCustom(JacksonUtil.bean2Json(endCustom));
            endReq.setIsEnd(true);
            asr.sendAudioData(endReq);

            // 等待一段时间收尾
            endLatch.await(Math.max(1, finalWaitSeconds), TimeUnit.SECONDS);

            String fullText = StringUtils.trimToEmpty(transcript.toString());
            if (fullText.isEmpty()) {
                // 兜底：有的 ASR 可能只回传最后一段 text，但未触发 isEnd=1
                fullText = StringUtils.trimToEmpty(lastAsrText.get());
            }

            long audioDurationMs = currentAudioMs.get();
            if (audioDurationMs <= 0 && pcmFile != null && pcmFile.exists()) {
                audioDurationMs = Math.max(0L, pcmFile.length() / BYTES_PER_MS);
            }

            if (fullText.isEmpty()) {
                if (audioDurationMs <= 0) {
                    meetingImportTaskDAO.updatePipelineFailed(meetingId,
                            "ASR_EMPTY",
                            "转写结果为空且无有效音频时长");
                    return;
                }
                fullText = "（未识别到语音内容）";
                logger.warn("[导入直处理] ASR 未返回可展示文本，已写入占位转写并保留音频时长 meetingId={}, durationMs={}",
                        meetingId, audioDurationMs);
            }

            // 落 1 条段落（离线导入先按单段处理，后续可优化为多段）
            MeetingSegment seg = new MeetingSegment();
            seg.setSegmentId(String.valueOf(snowflake.nextId()));
            seg.setText(fullText);
            seg.setSpeakerId("import");
            seg.setSpeakerName("录音");
            seg.setStartTime(0L);
            seg.setEndTime(audioDurationMs);
            seg.setDuration(Math.max(0L, audioDurationMs));

            MeetingSegmentMessage segMsg = new MeetingSegmentMessage();
            segMsg.setMeetingId(meetingId);
            segMsg.setSegment(seg);
            segMsg.setTimestamp(System.currentTimeMillis());
            meetingSegmentPersistenceService.persistSegment(segMsg);

            meetingImportTaskDAO.updatePipelineStatus(meetingId, MeetingImportConstants.PIPELINE_ANALYZING);

            // 触发纪要/解读落库（使用同一套 meeting 结果持久化）
            long start = System.currentTimeMillis();
            MeetingResultMessage result = new MeetingResultMessage();
            result.setMeetingId(meetingId);
            result.setUserId(task.getUserId());
            result.setNeedSave(1);
            result.setTotalDuration(audioDurationMs);
            result.setStartTime(start);
            result.setEndTime(start + audioDurationMs);
            result.setTimestamp(System.currentTimeMillis());
            result.setOssUrl(task.getOssObjectKey());
            // 摘要生成较慢，先同步转写与时长到 meetings 表，便于详情轮询展示（与 WebSocket 会中 initMeetingInfo 一致）
            meetingPersistenceService.initMeetingInfo(result);
            meetingPersistenceService.persistMeetingResult(result, null);

            meetingImportTaskDAO.updatePipelineStatus(meetingId, MeetingImportConstants.PIPELINE_COMPLETED);
            logger.info("[导入直处理] 已完成 meetingId={}, durationMs={}", meetingId, audioDurationMs);
        } catch (Exception e) {
            logger.error("[导入直处理] 异常 meetingId={}", meetingId, e);
            meetingImportTaskDAO.updatePipelineFailed(meetingId, "PIPELINE_EXCEPTION",
                    "处理异常: " + StringUtils.defaultString(e.getMessage(), e.getClass().getSimpleName()));
        } finally {
            if (asr != null) {
                try {
                    asr.disconnect();
                } catch (Exception ignore) {
                }
            }
            cleanup(downloaded);
            cleanup(pcmFile);
        }
    }

    private static boolean isDirectProcessAudioExt(String extLower) {
        return "wav".equals(extLower) || "mp3".equals(extLower) || "m4a".equals(extLower);
    }

    private String resolveAsrWebSocketUrl() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(ASR_SERVICE_NAME);
            if (instances == null || instances.isEmpty()) {
                return null;
            }
            int instanceCount = instances.size();
            int selectedIndex = instanceCount > 1 ? RANDOM.nextInt(instanceCount) : 0;
            ServiceInstance instance = instances.get(selectedIndex);
            return String.format("ws://%s:%d%s", instance.getHost(), instance.getPort(), ASR_WEBSOCKET_PATH);
        } catch (Exception e) {
            logger.error("[导入直处理] 服务发现异常 service={}", ASR_SERVICE_NAME, e);
            return null;
        }
    }

    private static byte[] copyOf(byte[] src, int len) {
        byte[] out = new byte[len];
        System.arraycopy(src, 0, out, 0, len);
        return out;
    }

    private static void cleanup(File f) {
        if (f == null) {
            return;
        }
        try {
            Files.deleteIfExists(f.toPath());
        } catch (Exception ignore) {
        }
    }
}

