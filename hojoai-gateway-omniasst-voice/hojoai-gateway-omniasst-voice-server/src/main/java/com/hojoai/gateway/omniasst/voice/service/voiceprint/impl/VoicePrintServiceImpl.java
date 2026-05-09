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
package com.hojoai.gateway.omniasst.voice.service.voiceprint.impl;

import com.hojoai.gateway.omniasst.voice.api.TtsAPI;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintResponse;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.VoicePrintService;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.VoicePrintVectorService;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintPageResult;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintQueryRequest;
import com.hojoai.gateway.omniasst.voice.util.AudioConverter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;

/**
 * 声纹业务实现：调 {@link TtsAPI#recognize} 提取向量，再委托 {@link VoicePrintVectorService} 落库与查询。
 */
@Service
public class VoicePrintServiceImpl implements VoicePrintService {
    private static final Logger logger = LoggerFactory.getLogger(VoicePrintServiceImpl.class);

    /** PCM 采样率 16kHz */
    private static final int PCM_SAMPLE_RATE = 16000;
    /** 单声道 16bit 下每秒字节数 */
    private static final int PCM_BYTES_PER_SECOND = PCM_SAMPLE_RATE * 2;
    /** 参与声纹的音频最大时长（秒） */
    private static final int MAX_VOICEPRINT_DURATION_SECONDS = 7;
    /** 与最大时长对应的 PCM 字节上限 */
    private static final int MAX_PCM_BYTES_FOR_VOICEPRINT = PCM_BYTES_PER_SECOND * MAX_VOICEPRINT_DURATION_SECONDS;
    
    @Resource
    private AudioConverter audioConverter;

    @Resource
    private TtsAPI ttsAPI;

    @Resource
    private VoicePrintVectorService voicePrintVectorService;

    /**
     * {@inheritDoc}
     */
    @Override
    public VoicePrintResponse recognizeFromRequest(VoicePrintRequest request) {
        String originalAudio = request.getAudio();
        if (originalAudio == null || originalAudio.isEmpty()) {
            logger.error("Audio data is empty, vid: {}", request.getVid());
            VoicePrintResponse errorResponse = new VoicePrintResponse();
            errorResponse.setRtn(1);
            errorResponse.setVid(request.getVid());
            errorResponse.setMsg("音频数据为空");
            return errorResponse;
        }
        byte[] pcmData = Base64.getDecoder().decode(originalAudio);
        if (pcmData.length > MAX_PCM_BYTES_FOR_VOICEPRINT) {
            byte[] truncatedPcmData = new byte[MAX_PCM_BYTES_FOR_VOICEPRINT];
            System.arraycopy(pcmData, 0, truncatedPcmData, 0, MAX_PCM_BYTES_FOR_VOICEPRINT);
            pcmData = truncatedPcmData;
        }
        String processedAudio = Base64.getEncoder().encodeToString(pcmData);
        request.setAudio(processedAudio);
        request.setAudioLength(processedAudio.length());
        VoicePrintResponse response = null;
        try {
            response = ttsAPI.recognize(request);
        } catch (Exception e) {
            throw e;
        }
        return response;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 空文件、超 10MB 或非 WAV
     * @throws RuntimeException         下游识别失败
     */
    @Override
    public VoicePrintResponse recognizeFromFile(MultipartFile audioFile) {
        if (Objects.isNull(audioFile) || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        if (audioFile.getSize() > 10 * 1024 * 1024L) {
            throw new IllegalArgumentException("文件大小超过限制，最大10MB");
        }
        if (!isWavFile(audioFile)) {
            throw new IllegalArgumentException("文件格式不正确，仅支持WAV格式");
        }
        File tempWavFile = null;
        File pcmFile = null;
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            tempWavFile = tempDir.resolve("voiceprint_" + System.currentTimeMillis() + ".wav").toFile();
            audioFile.transferTo(tempWavFile);
            pcmFile = tempDir.resolve("voiceprint_" + System.currentTimeMillis() + ".pcm").toFile();
            audioConverter.convertWavToPcm(tempWavFile, pcmFile.getAbsolutePath());
            byte[] pcmData = Files.readAllBytes(pcmFile.toPath());
            String audioBase64 = Base64.getEncoder().encodeToString(pcmData);
            VoicePrintRequest request = new VoicePrintRequest();
            request.setVid("recognize-" + System.currentTimeMillis());
            request.setAudioLength(audioBase64.length());
            request.setAudio(audioBase64);
            logger.info("Calling voiceprint API from file, vid: {}, audioLength: {}", request.getVid(), request.getAudioLength());
            VoicePrintResponse response = ttsAPI.recognize(request);
            logger.info("Voiceprint API response, rtn: {}", response != null ? response.getRtn() : "null");
            if (Objects.isNull(response) || Objects.isNull(response.getRtn()) || response.getRtn() != 0) {
                logger.error("Voiceprint recognition failed, rtn: {}", response != null ? response.getRtn() : "null");
                throw new RuntimeException("声纹识别失败");
            }
            return response;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing voiceprint recognition from file", e);
            throw new RuntimeException("声纹识别失败: " + e.getMessage(), e);
        } finally {
            if (Objects.nonNull(tempWavFile) && tempWavFile.exists()) {
                try {
                    Files.deleteIfExists(tempWavFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temp WAV file: {}", tempWavFile.getAbsolutePath(), e);
                }
            }
            if (Objects.nonNull(pcmFile) && pcmFile.exists()) {
                try {
                    Files.deleteIfExists(pcmFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 参数或文件校验失败
     * @throws RuntimeException         识别失败或向量为空
     */
    @Override
    public void registerVoicePrint(String userId, String userName, MultipartFile audioFile) throws Exception {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (Objects.isNull(audioFile) || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        if (audioFile.getSize() > 10 * 1024 * 1024L) {
            throw new IllegalArgumentException("文件大小超过限制，最大10MB");
        }
        if (!isWavFile(audioFile)) {
            throw new IllegalArgumentException("文件格式不正确，仅支持WAV格式");
        }
        
        File tempWavFile = null;
        File pcmFile = null;
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            tempWavFile = tempDir.resolve("voiceprint_" + System.currentTimeMillis() + ".wav").toFile();
            audioFile.transferTo(tempWavFile);
            pcmFile = tempDir.resolve("voiceprint_" + System.currentTimeMillis() + ".pcm").toFile();
            audioConverter.convertWavToPcm(tempWavFile, pcmFile.getAbsolutePath());
            
            byte[] pcmData = Files.readAllBytes(pcmFile.toPath());
            String audioBase64 = Base64.getEncoder().encodeToString(pcmData);
            
            VoicePrintRequest request = new VoicePrintRequest();
            request.setVid("register-" + System.currentTimeMillis());
            // audioLength应该是Base64编码后的字符串长度
            request.setAudioLength(audioBase64.length());
            request.setAudio(audioBase64);
            
            VoicePrintResponse response = ttsAPI.recognize(request);
            if (Objects.isNull(response) || Objects.isNull(response.getRtn()) || response.getRtn() != 0) {
                logger.error("Voiceprint recognition failed, userId: {}, userName: {}, rtn: {}", userId, userName, response != null ? response.getRtn() : "null");
                throw new RuntimeException("声纹识别失败");
            }
            
            double[] voiceprint = response.getVoiceprint();
            if (Objects.isNull(voiceprint) || voiceprint.length == 0) {
                logger.error("Voiceprint is empty, userId: {}, userName: {}", userId, userName);
                throw new RuntimeException("声纹向量为空");
            }
            
            voicePrintVectorService.saveUserVoiceprint(userId, userName, voiceprint);
            logger.info("Voiceprint registered successfully, userId: {}, userName: {}", userId, userName);
        } finally {
            if (Objects.nonNull(tempWavFile) && tempWavFile.exists()) {
                try {
                    Files.deleteIfExists(tempWavFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temp WAV file: {}", tempWavFile.getAbsolutePath(), e);
                }
            }
            if (Objects.nonNull(pcmFile) && pcmFile.exists()) {
                try {
                    Files.deleteIfExists(pcmFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete PCM file: {}", pcmFile.getAbsolutePath(), e);
                }
            }
        }
    }

    private boolean isWavFile(MultipartFile file) {
        try (var inputStream = file.getInputStream()) {
            byte[] header = new byte[4];
            if (inputStream.read(header) != 4) return false;
            byte[] wavHeader = new byte[]{0x52, 0x49, 0x46, 0x46};
            for (int i = 0; i < 4; i++) {
                if (header[i] != wavHeader[i]) return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error reading file header", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VoicePrintPageResult queryUserVoicePrints(VoicePrintQueryRequest request) {
        if (request == null) {
            request = new VoicePrintQueryRequest();
        }
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 20;
        return voicePrintVectorService.queryUserVoiceprints(request.getUserName(), page, pageSize);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 用户 ID 为空或库中无记录
     */
    @Override
    public void deleteUserVoicePrint(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        int rows = voicePrintVectorService.deleteUserVoiceprint(userId);
        if (rows == 0) {
            throw new IllegalArgumentException("用户声纹不存在");
        }
    }
}
