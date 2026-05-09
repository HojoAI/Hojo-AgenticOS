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
package com.hojoai.webmvc.omniasst.meeting.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceprintResponse;
import com.hojoai.webmvc.omniasst.meeting.configure.LoadBalancedRestTemplateConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 通过 LoadBalanced RestTemplate 调用 voice 的 multipart 声纹向量抽取接口：
 * {@code POST /omniasst-voice/v1/voiceprintrecognize/file}。
 * <p>
 * 该接口外层响应为 {@link Response}，向量位于 {@link VoiceprintResponse#getVoicePrint()}。
 */
@Component
public class VoiceprintRecognizeFileHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(VoiceprintRecognizeFileHttpClient.class);

    private static final String RECOGNIZE_FILE_PATH = "/omniasst-voice/v1/voiceprint/recognize/file";

    private final RestTemplate loadBalancedRestTemplate;

    @Value("${meeting.voiceprint.register.voice-service-name:hojoai-webmvc-omniasst-voice}")
    private String voiceServiceName;

    public VoiceprintRecognizeFileHttpClient(
            @Qualifier(LoadBalancedRestTemplateConfig.BEAN_NAME) RestTemplate loadBalancedRestTemplate) {
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
    }

    /**
     * multipart/form-data：file
     */
    public Response<VoiceprintResponse> recognizeByFile(byte[] audioBytes, String originalFilename) {
        String url = "http://" + voiceServiceName + RECOGNIZE_FILE_PATH;
        String filename = StringUtils.isNotBlank(originalFilename) ? originalFilename.trim() : "voiceprint.wav";

        ByteArrayResource fileResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> responseEntity =
                    loadBalancedRestTemplate.postForEntity(url, requestEntity, String.class);
            return parseResponse(responseEntity);
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[voice recognize file] HTTP 异常 status={}, body={}", e.getStatusCode(), errBody, e);
            throw new IllegalStateException("声纹服务调用失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    private Response<VoiceprintResponse> parseResponse(ResponseEntity<String> responseEntity) {
        String responseBody = responseEntity.getBody();
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.warn("[voice recognize file] HTTP 非 2xx status={}, body={}", responseEntity.getStatusCode(), responseBody);
            throw new IllegalStateException("声纹服务调用失败 HTTP " + responseEntity.getStatusCode());
        }
        if (StringUtils.isBlank(responseBody)) {
            throw new IllegalStateException("声纹服务返回空 body");
        }
        Response<VoiceprintResponse> parsed =
                JacksonUtil.json2Bean(responseBody, new TypeReference<Response<VoiceprintResponse>>() {});
        if (parsed == null) {
            throw new IllegalStateException("声纹服务响应无法解析为 Response，body 前 500 字："
                    + responseBody.substring(0, Math.min(500, responseBody.length())));
        }
        return parsed;
    }

    /**
     * voice 外层 Response 的 resCode 可能为 200/success 等（与 meeting 侧 OK 不完全一致），此处做兼容。
     */
    public static boolean isVoiceOk(Response<?> res) {
        if (res == null) {
            return false;
        }
        String code = StringUtils.trimToEmpty(res.getResCode());
        if (code.isEmpty()) {
            return false;
        }
        String expected = ResponseStatusBasic.OK.getResCode();
        if (expected != null && expected.equals(code)) {
            return true;
        }
        if ("OK".equalsIgnoreCase(code)) {
            return true;
        }
        if ("SUCCESS".equalsIgnoreCase(code) || "200".equals(code)) {
            return true;
        }
        return false;
    }
}

