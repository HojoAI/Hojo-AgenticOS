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
package com.hojoai.gateway.omniasst.voice.service.voice.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.gateway.omniasst.voice.constants.VoiceProvider;
import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneProvider;
import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneProviderFactory;
import com.hojoai.gateway.omniasst.voice.service.voice.dto.VoiceCloneResponse;
import com.hojoai.gateway.omniasst.voice.util.AudioConverter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import xyz.downgoon.snowflake.Snowflake;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * MiniMax 声音克隆：先上传音频再调用 voice_clone 接口，需在配置中提供 API Key。
 */
@Service
public class MinimaxVoiceCloneServiceImpl implements VoiceCloneProvider {
    private static final Logger logger = LoggerFactory.getLogger(MinimaxVoiceCloneServiceImpl.class);
    
    /** MiniMax 文件上传 HTTP 地址 */
    private static final String UPLOAD_URL = "https://api.minimaxi.com/v1/files/upload";
    /** MiniMax 创建克隆音色接口 */
    private static final String CLONE_URL = "https://api.minimaxi.com/v1/voice_clone";
    private static final String DEFAULT_MODEL = "speech-2.8-turbo";
    private static final String DEFAULT_TEXT = "大兄弟，听您口音不是本地人吧，头回来天津卫，啊，待会您可甭跟着导航走，那玩意儿净给您往大马路上绕。";

    @Value("${minimax.ai.api-key:}")
    private String apiKey;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private VoiceCloneProviderFactory voiceCloneProviderFactory;

    @Resource
    private Snowflake snowflake;
    
    @Resource
    private AudioConverter audioConverter;

    /**
     * 向 {@link VoiceCloneProviderFactory} 注册 {@link VoiceProvider#MINIMAX}。
     */
    @PostConstruct
    public void init() {
        voiceCloneProviderFactory.registerProvider(VoiceProvider.MINIMAX.getCode(), this);
        logger.info("MinimaxVoiceCloneServiceImpl initialized and registered");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 空文件
     * @throws IllegalStateException    未配置 API Key
     */
    @Override
    public VoiceCloneResponse cloneVoice(MultipartFile audioFile, String fileType) {
        if (Objects.isNull(audioFile) || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Minimax API key is not configured");
        }
        
        String originalFilename = audioFile.getOriginalFilename();
        logger.info("Minimax voice clone request, filename: {}, size: {}, fileType: {}", 
                   originalFilename, audioFile.getSize(), fileType);
        
        File tempFile = null;
        try {
            ProcessedAudioResult result = processAudioFile(audioFile, fileType, originalFilename);
            tempFile = result.getTempFile();
            byte[] audioBytes = result.getAudioBytes();
            String finalFilename = result.getFinalFilename();
            
            // Step 1: Upload file
            Long fileId = uploadFile(audioBytes, finalFilename);
            
            // Step 2: Generate voice_id
            String voiceId = "VMiniMax" + snowflake.nextId();
            logger.info("Generated voiceId: {}", voiceId);
            
            // Step 3: Clone voice
            VoiceCloneResponse response = cloneVoice(fileId, voiceId);
            logger.info("Voice clone completed, voiceId: {}", response.getVoiceId());
            
            return response;
        } catch (RestClientException e) {
            logger.error("Error calling Minimax API, error: {}", e.getMessage(), e);
            throw new RuntimeException("音色复刻服务暂时不可用，请稍后重试", e);
        } catch (Exception e) {
            logger.error("Unexpected error in Minimax voice clone, error: {}", e.getMessage(), e);
            throw new RuntimeException("音色复刻失败：" + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                    logger.debug("Deleted temporary file: {}", tempFile.getAbsolutePath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }

    private Long uploadFile(byte[] fileBytes, String filename) throws Exception {
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "audio.wav";
            }
        };
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("purpose", "voice_clone");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        logger.info("Uploading file to Minimax, url: {}", UPLOAD_URL);
        ResponseEntity<String> response = restTemplate.exchange(
            UPLOAD_URL, HttpMethod.POST, requestEntity, String.class);
        
        logger.info("Minimax upload response, status: {}, body: {}", 
                   response.getStatusCode(), response.getBody());
        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("文件上传失败，状态码: " + response.getStatusCode());
        }
        
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode baseResp = rootNode.get("base_resp");
        
        if (baseResp == null || baseResp.get("status_code") == null || 
            baseResp.get("status_code").asInt() != 0) {
            String errorMsg = baseResp != null && baseResp.get("status_msg") != null 
                ? baseResp.get("status_msg").asText() : "未知错误";
            throw new RuntimeException("文件上传失败: " + errorMsg);
        }
        
        JsonNode fileNode = rootNode.get("file");
        if (fileNode == null || fileNode.get("file_id") == null) {
            throw new RuntimeException("文件上传响应中缺少 file_id: " + response.getBody());
        }
        
        // file_id 在响应中是数字类型，返回 Long
        Long fileId = fileNode.get("file_id").asLong();
        logger.info("File uploaded successfully, fileId: {}", fileId);
        return fileId;
    }

    private VoiceCloneResponse cloneVoice(Long fileId, String voiceId) throws Exception {
        Map<String, Object> cloneRequest = new HashMap<>();
        cloneRequest.put("file_id", fileId);
        cloneRequest.put("voice_id", voiceId);
        cloneRequest.put("text", DEFAULT_TEXT);
        cloneRequest.put("model", DEFAULT_MODEL);
        cloneRequest.put("need_noise_reduction", true);
        cloneRequest.put("continuous_sound", true);
        
        // 手动序列化为 JSON 字符串，确保格式正确（对应 Python 的 json=clone_payload）
        String jsonBody = objectMapper.writeValueAsString(cloneRequest);
        logger.info("Minimax clone request body: {}", jsonBody);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        
        logger.info("Cloning voice with Minimax, url: {}, voiceId: {}", CLONE_URL, voiceId);
        ResponseEntity<String> response = restTemplate.exchange(
            CLONE_URL, HttpMethod.POST, requestEntity, String.class);
        
        logger.info("Minimax clone response, status: {}, body: {}", 
                   response.getStatusCode(), response.getBody());
        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("音色克隆失败，状态码: " + response.getStatusCode());
        }
        
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode baseResp = rootNode.get("base_resp");
        
        if (baseResp == null || baseResp.get("status_code") == null || 
            baseResp.get("status_code").asInt() != 0) {
            String errorMsg = baseResp != null && baseResp.get("status_msg") != null 
                ? baseResp.get("status_msg").asText() : "未知错误";
            throw new RuntimeException("音色克隆失败: " + errorMsg);
        }
        
        VoiceCloneResponse cloneResponse = new VoiceCloneResponse();
        cloneResponse.setVoiceId(voiceId);
        cloneResponse.setSuccess(true);
        
        return cloneResponse;
    }

    /**
     * 处理音频文件：检查时长、拼接（如需要）、转换为WAV格式
     * @param audioFile 音频文件
     * @param fileType 文件类型
     * @param originalFilename 原始文件名
     * @return 处理结果，包含WAV字节数组、文件名和临时文件（用于清理）
     * @throws Exception 处理过程中的异常
     */
    private ProcessedAudioResult processAudioFile(MultipartFile audioFile, String fileType, String originalFilename) throws Exception {
        byte[] fileBytes = audioFile.getBytes();
        boolean isPcm = "pcm".equalsIgnoreCase(fileType) || 
                       (originalFilename != null && originalFilename.toLowerCase().endsWith(".pcm"));
        
        // 检查音频时长，不能小于3秒
        double audioDuration;
        if (isPcm) {
            audioDuration = audioConverter.getPcmDuration(fileBytes);
        } else {
            audioDuration = audioConverter.getWavDuration(fileBytes);
        }
        
        logger.info("Audio duration: {} seconds", audioDuration);
        
        if (audioDuration < 3.0) {
            throw new IllegalArgumentException("音频时长不能小于3秒，当前时长: " + String.format("%.2f", audioDuration) + "秒");
        }
        
        byte[] wavBytes;
        String finalFilename;
        File tempFile = null;
        
        if (isPcm) {
            logger.info("PCM audio detected, converting to WAV format");
            
            // 如果音频时长小于10秒，需要拼接成12秒
            byte[] processedPcmData = fileBytes;
            if (audioDuration < 10.0) {
                logger.info("Audio duration {} seconds is less than 10 seconds, concatenating to 12 seconds", audioDuration);
                processedPcmData = audioConverter.concatenatePcmAudio(fileBytes, 12.0);
                double newDuration = audioConverter.getPcmDuration(processedPcmData);
                logger.info("Audio concatenated, new duration: {} seconds", newDuration);
            }
            
            // 将处理后的PCM数据写入临时文件（项目目录下的temp文件夹）
            Path tempDir = Paths.get("temp");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            String tempPcmFileName = "minimax_voice_clone_pcm_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pcm";
            Path tempPcmPath = tempDir.resolve(tempPcmFileName);
            Files.write(tempPcmPath, processedPcmData);
            
            // PCM转WAV
            String tempWavFileName = "minimax_voice_clone_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".wav";
            Path tempWavPath = tempDir.resolve(tempWavFileName);
            tempFile = audioConverter.convertPcmToWav(tempPcmPath.toFile(), tempWavPath.toString());
            wavBytes = Files.readAllBytes(tempFile.toPath());
            
            // 清理临时PCM文件
            try {
                Files.deleteIfExists(tempPcmPath);
            } catch (Exception e) {
                logger.warn("Failed to delete temporary PCM file: {}", tempPcmPath, e);
            }
            
            finalFilename = originalFilename != null && originalFilename.toLowerCase().endsWith(".pcm")
                ? originalFilename.replace(".pcm", ".wav")
                : "audio.wav";
            logger.info("PCM to WAV conversion completed, PCM size: {} bytes, WAV size: {} bytes, temp file: {}", 
                       processedPcmData.length, wavBytes.length, tempFile.getAbsolutePath());
        } else {
            logger.info("WAV audio detected, processing WAV format");
            
            // 如果音频时长小于10秒，需要拼接成12秒
            byte[] processedWavData = fileBytes;
            if (audioDuration < 10.0) {
                logger.info("Audio duration {} seconds is less than 10 seconds, concatenating to 12 seconds", audioDuration);
                processedWavData = audioConverter.concatenateWavAudio(fileBytes, 12.0);
                double newDuration = audioConverter.getWavDuration(processedWavData);
                logger.info("Audio concatenated, new duration: {} seconds", newDuration);
            }
            
            // 直接使用处理后的WAV数据，无需写入临时文件
            wavBytes = processedWavData;
            
            finalFilename = originalFilename != null ? originalFilename : "audio.wav";
            logger.info("WAV processing completed, size: {} bytes", wavBytes.length);
        }
        
        return new ProcessedAudioResult(wavBytes, finalFilename, tempFile);
    }

    /**
     * 音频处理结果封装类
     */
    private static class ProcessedAudioResult {
        private final byte[] audioBytes;
        private final String finalFilename;
        private final File tempFile;

        public ProcessedAudioResult(byte[] audioBytes, String finalFilename, File tempFile) {
            this.audioBytes = audioBytes;
            this.finalFilename = finalFilename;
            this.tempFile = tempFile;
        }

        public byte[] getAudioBytes() {
            return audioBytes;
        }

        public String getFinalFilename() {
            return finalFilename;
        }

        public File getTempFile() {
            return tempFile;
        }
    }
}
