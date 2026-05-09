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
package com.hojoai.gateway.omniasst.voice.service.alqw.impl;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.gateway.omniasst.voice.alqw.CreateTokenUtil;
import com.hojoai.gateway.omniasst.voice.alqw.HttpUtil;
import com.hojoai.gateway.omniasst.voice.alqw.dto.FlashRecognizerResponse;
import com.hojoai.gateway.omniasst.voice.service.alqw.AlqwSpeechFlashService;
import com.hojoai.gateway.omniasst.voice.service.asr.dto.AsrFileOssRequest;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

/**
 * 阿里云 FlashRecognizer 语音识别服务实现
 * 通过 CreateTokenUtil 获取 token，按 AsrFileOssRequest 入参、String 出参协议提供 convertToText
 */
@Service
public class AlqwSpeechFlashServiceImpl implements AlqwSpeechFlashService {

    private static final Logger logger = LoggerFactory.getLogger(AlqwSpeechFlashServiceImpl.class);

    private static final String FLASH_RECOGNIZER_URL = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/FlashRecognizer";
    private static final long SUCCESS_STATUS = 20000000L;
    private static final String DEFAULT_FORMAT = "wav";
    private static final int DEFAULT_SAMPLE_RATE = 16000;

    @Value("${aliyun.nls.appkey}")
    private String appkey;

    @Resource
    private CreateTokenUtil createTokenUtil;

    @Override
    public String convertToText(AsrFileOssRequest request) {
        Objects.requireNonNull(request, "AsrFileOssRequest must not be null");
        Objects.requireNonNull(request.getOssUrl(), "ossUrl must not be null");

        String token = createTokenUtil.getToken("录音文件识别");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Failed to obtain Aliyun NLS token");
        }

        if (appkey == null || appkey.isEmpty()) {
            throw new IllegalStateException("aliyun.nls.appkey is not configured");
        }

        String ossUrl = request.getOssUrl().trim();
        String requestUrl = buildRequestUrl(token, DEFAULT_FORMAT, DEFAULT_SAMPLE_RATE);
        logger.info("Request ossUrl: {}, URL: {}", ossUrl, requestUrl);
        HashMap<String, String> headers = new HashMap<>();
        String responseBody;
        if (new File(ossUrl).isFile()) {
            headers.put("Content-Type", "application/octet-stream");
            responseBody = HttpUtil.sendPostFile(requestUrl, headers, ossUrl);
        } else {
            headers.put("Content-Type", "application/text");
            String encodedUrl = urlEncode(ossUrl);
            responseBody = HttpUtil.sendPostLink(requestUrl, headers, encodedUrl);
        }

        if (responseBody == null || responseBody.isEmpty()) {
            logger.error("FlashRecognizer request failed, empty response, ossUrl: {}", ossUrl);
            throw new RuntimeException("阿里云语音识别返回为空");
        }

        FlashRecognizerResponse resp = JacksonUtil.json2Bean(responseBody, FlashRecognizerResponse.class);
        if (resp == null) {
            logger.error("FlashRecognizer response parse failed, raw: {}", responseBody);
            throw new RuntimeException("阿里云语音识别响应解析失败");
        }

        if (resp.getStatus() == null || resp.getStatus() != SUCCESS_STATUS) {
            logger.error("FlashRecognizer error, status: {}, message: {}", resp.getStatus(), resp.getMessage());
            throw new RuntimeException("阿里云语音识别失败: " + (resp.getMessage() != null ? resp.getMessage() : "unknown"));
        }

        return extractRecognizedText(resp);
    }

    /**
     * 从响应中提取识别文本：优先取 flash_result.sentences 拼接，否则取顶层 result
     */
    private String extractRecognizedText(FlashRecognizerResponse resp) {
        FlashRecognizerResponse.FlashResult flash = resp.getFlashResult();
        if (flash != null && flash.getSentences() != null && !flash.getSentences().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FlashRecognizerResponse.FlashSentence s : flash.getSentences()) {
                if (s.getText() != null) {
                    sb.append(s.getText());
                }
            }
            return sb.toString();
        }
        return resp.getResult() != null ? resp.getResult() : "";
    }

    private String buildRequestUrl(String token, String format, int sampleRate) {
        return FLASH_RECOGNIZER_URL
                + "?appkey=" + appkey
                + "&token=" + token
                + "&format=" + format
                + "&sample_rate=" + sampleRate;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL encode failed", e);
        }
    }
}
