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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Hojo 声音克隆：将 PCM/WAV multipart 提交至配置的 TTS 网关完成克隆注册。
 */
@Service
public class HojoVoiceCloneServiceImpl implements VoiceCloneProvider {
    private static final Logger logger = LoggerFactory.getLogger(HojoVoiceCloneServiceImpl.class);

    @Value("${ai.tts.host}")
    private String ttsHost;

    @Value("${ai.tts.api-key}")
    private String apiKey;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private AudioConverter audioConverter;

    @Resource
    private VoiceCloneProviderFactory voiceCloneProviderFactory;

    /**
     * 向 {@link VoiceCloneProviderFactory} 注册 {@link VoiceProvider#HOJO}。
     */
    @PostConstruct
    public void init() {
        voiceCloneProviderFactory.registerProvider(VoiceProvider.HOJO.getCode(), this);
        logger.info("HojoVoiceCloneServiceImpl initialized and registered");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 空文件
     */
    @Override
    public VoiceCloneResponse cloneVoice(MultipartFile audioFile, String fileType) {
        if (Objects.isNull(audioFile) || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        String originalFilename = audioFile.getOriginalFilename();
        logger.info("Voice clone request, filename: {}, size: {}, fileType: {}", originalFilename, audioFile.getSize(), fileType);
        File wavFile = null;
        try {
            byte[] fileBytes = audioFile.getBytes();
            boolean isPcm = "pcm".equalsIgnoreCase(fileType) || 
                           (originalFilename != null && originalFilename.toLowerCase().endsWith(".pcm"));
            byte[] wavBytes;
            String finalFilename;
            if (isPcm) {
                logger.info("PCM audio detected, converting to WAV format");
                Path tempWavPath = Files.createTempFile("voice_clone_", ".wav");
                wavFile = audioConverter.convertPcmToWav(fileBytes, tempWavPath.toString());
                wavBytes = Files.readAllBytes(wavFile.toPath());
                finalFilename = originalFilename.replace(".pcm", ".wav");
                logger.info("PCM to WAV conversion completed, original size: {} bytes, WAV size: {} bytes", fileBytes.length, wavBytes.length);
            } else {
                wavBytes = fileBytes;
                finalFilename = originalFilename;
            }
            ByteArrayResource fileResource = new ByteArrayResource(wavBytes) {
                @Override
                public String getFilename() {
                    return finalFilename;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("text", "");
            body.add("lang", "unknown");
            body.add("persistence", "true");
            body.add("desc", "");
            logger.info("Calling algorithm service, url: {}/voice/register, filename: {}, size: {} bytes", ttsHost, finalFilename, wavBytes.length);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("API-Key", apiKey);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String url = ttsHost + "/voice/register";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Algorithm service response, status: {}, body: {}", response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                VoiceCloneResponse cloneResponse = objectMapper.readValue(response.getBody(), VoiceCloneResponse.class);
                if (cloneResponse != null && Boolean.TRUE.equals(cloneResponse.getSuccess()) && cloneResponse.getVoiceId() != null) {
                    logger.info("Voice clone success, voiceId: {}", cloneResponse.getVoiceId());
                    return cloneResponse;
                } else {
                    String errorMsg = cloneResponse != null && cloneResponse.getError() != null ? cloneResponse.getError() : "算法服务返回错误";
                    logger.error("Voice clone failed, error: {}, response: {}", errorMsg, response.getBody());
                    throw new RuntimeException("音色复刻失败：" + errorMsg);
                }
            } else {
                logger.error("Voice clone failed, status: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("音色复刻失败：算法服务返回异常状态");
            }
        } catch (RestClientException e) {
            logger.error("Error calling algorithm service, error: {}", e.getMessage(), e);
            throw new RuntimeException("音色复刻服务暂时不可用，请稍后重试", e);
        } catch (IOException e) {
            logger.error("Error processing audio or parsing response, error: {}", e.getMessage(), e);
            throw new RuntimeException("音频处理或响应解析失败", e);
        } catch (Exception e) {
            logger.error("Unexpected error in voice clone, error: {}", e.getMessage(), e);
            throw new RuntimeException("音色复刻失败：" + e.getMessage(), e);
        } finally {
            if (wavFile != null && wavFile.exists()) {
                try {
                    Files.deleteIfExists(wavFile.toPath());
                    logger.debug("Temporary WAV file deleted: {}", wavFile.getPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary WAV file: {}, error: {}", wavFile.getPath(), e.getMessage());
                }
            }
        }
    }
}
