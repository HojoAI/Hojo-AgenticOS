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
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceCloneResContent;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceProfileResponse;
import com.hojoai.webmvc.omniasst.meeting.configure.LoadBalancedRestTemplateConfig;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingVoiceSaveRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingVoiceprintDisplayNameRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 通过 LoadBalanced RestTemplate 调用 management 音色克隆/保存（multipart 与 JSON）。
 * 与 {@link ManagementServiceClient} 并存：此处为历史 multipart/DELETE 路径，后续可逐步迁入 Feign。
 */
@Component
public class ManagementVoiceHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(ManagementVoiceHttpClient.class);

    private static final String CLONE_PATH = "/omniasst-management/api/v0/voice/clone";
    private static final String SAVE_PATH = "/omniasst-management/api/v0/voice/save";
    private static final String PROFILE_PATH = "/omniasst-management/api/v0/voice/profile";
    private static final String VOICEPRINT_DISPLAY_NAME_PATH = "/omniasst-management/api/v0/voiceprint/display-name";

    private final RestTemplate loadBalancedRestTemplate;

    @Value("${meeting.voiceprint.register.management-service-name:hojoai-webmvc-omniasst-management}")
    private String managementServiceName;

    public ManagementVoiceHttpClient(
            @Qualifier(LoadBalancedRestTemplateConfig.BEAN_NAME) RestTemplate loadBalancedRestTemplate) {
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
    }

    /**
     * POST multipart：userId、needSave、provider、file；voiceName 可选。
     */
    public Response<VoiceCloneResContent> cloneVoice(
            String userId,
            String voiceName,
            boolean needSave,
            String provider,
            byte[] fileBytes,
            String uploadFilename) {
        String url = "http://" + managementServiceName + CLONE_PATH;
        String filename = StringUtils.isNotBlank(uploadFilename) ? uploadFilename : "voice.wav";

        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userId", userId);
        if (StringUtils.isNotBlank(voiceName)) {
            body.add("voiceName", voiceName);
        }
        body.add("needSave", needSave);
        body.add("provider", provider);
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> responseEntity =
                    loadBalancedRestTemplate.postForEntity(url, requestEntity, String.class);
            return parseCloneResponse(responseEntity);
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[management voice clone] HTTP 异常 status={}, body={}", e.getStatusCode(), errBody, e);
            throw new IllegalStateException("音色克隆失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    /**
     * GET：与 management 文档 1.4 {@code /v0/voice/profile} 一致（Query：{@code userId}、{@code provider}）。
     * 第三参为 **management 侧** 约定：值为 {@code 1} 时附加 {@code type=1}（查声音/音色）；{@code 0} 不传 {@code type}（查声纹）。
     */
    public Response<VoiceProfileResponse> getVoiceProfile(String userId, String provider, Integer type) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl("http://" + managementServiceName + PROFILE_PATH)
                .queryParam("userId", userId)
                .queryParam("provider", StringUtils.isNotBlank(provider) ? provider : "hojo");
        if (type != null && type == 1) {
            b.queryParam("type", 1);
        }
        String url = b.build(true).toUriString();
        try {
            ResponseEntity<String> responseEntity = loadBalancedRestTemplate.getForEntity(url, String.class);
            String responseBody = responseEntity.getBody();
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.warn("[management voice profile] HTTP 非 2xx status={}, body={}", responseEntity.getStatusCode(), responseBody);
                throw new IllegalStateException("获取用户音色失败 HTTP " + responseEntity.getStatusCode());
            }
            if (StringUtils.isBlank(responseBody)) {
                throw new IllegalStateException("management 返回空 body");
            }
            Response<VoiceProfileResponse> parsed =
                    JacksonUtil.json2Bean(responseBody, new TypeReference<Response<VoiceProfileResponse>>() {});
            if (parsed == null) {
                throw new IllegalStateException("management 响应无法解析为 Response");
            }
            return parsed;
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[management voice profile] HTTP 异常 status={}, body={}", e.getStatusCode(), errBody, e);
            throw new IllegalStateException("获取用户音色失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    /**
     * 兼容调用：按 management 约定查询声音/音色资料（等同对 {@code /profile} 传 {@code type=1}）。
     */
    public Response<VoiceProfileResponse> getVoiceProfile(String userId, String provider) {
        return getVoiceProfile(userId, provider, 1);
    }

    /**
     * DELETE：management {@code DELETE /v0/voice/profile}，Query 仅 {@code userId}、{@code provider}（与 VoiceProviderEnum 等一致）。
     */
    public Response<VoiceProfileResponse> deleteVoiceProfile(String userId, String provider) {
        String url = UriComponentsBuilder.fromHttpUrl("http://" + managementServiceName + PROFILE_PATH)
                .queryParam("userId", userId)
                .queryParam("provider", StringUtils.isNotBlank(provider) ? provider : "hojo")
                .build(true)
                .toUriString();
        try {
            ResponseEntity<String> responseEntity =
                    loadBalancedRestTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            String responseBody = responseEntity.getBody();
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.warn("[management voice profile delete] HTTP 非 2xx status={}, body={}",
                        responseEntity.getStatusCode(), responseBody);
                throw new IllegalStateException("删除用户音色/声纹失败 HTTP " + responseEntity.getStatusCode());
            }
            if (StringUtils.isBlank(responseBody)) {
                throw new IllegalStateException("management 返回空 body");
            }
            Response<VoiceProfileResponse> parsed =
                    ManagementVoiceProfileResponseParseSupport.parseFlexibleProfileBody(responseBody);
            if (parsed == null) {
                throw new IllegalStateException("management 响应无法解析为 Response");
            }
            return parsed;
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[management voice profile delete] HTTP 异常 status={}, body={}", e.getStatusCode(), errBody, e);
            throw new IllegalStateException("删除用户音色/声纹失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    /**
     * POST application/json：与 management /v0/voice/save 一致。
     */
    public Response<String> saveVoice(MeetingVoiceSaveRequest request) {
        String url = "http://" + managementServiceName + SAVE_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MeetingVoiceSaveRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<String> responseEntity =
                    loadBalancedRestTemplate.postForEntity(url, entity, String.class);
            String responseBody = responseEntity.getBody();
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.warn("[management voice save] HTTP 非 2xx status={}, body={}", responseEntity.getStatusCode(), responseBody);
                throw new IllegalStateException("音色保存失败 HTTP " + responseEntity.getStatusCode());
            }
            if (StringUtils.isBlank(responseBody)) {
                throw new IllegalStateException("management 返回空 body");
            }
            Response<String> parsed = JacksonUtil.json2Bean(responseBody, new TypeReference<Response<String>>() {});
            if (parsed == null) {
                throw new IllegalStateException("management 响应无法解析为 Response");
            }
            return parsed;
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[management voice save] HTTP 异常 status={}, body={}", e.getStatusCode(), errBody, e);
            throw new IllegalStateException("音色保存失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    /**
     * POST application/json：management {@code /v0/voiceprint/display-name}，更新声纹表展示名。
     */
    public Response<String> updateVoiceprintDisplayName(String userId, String userName) {
        MeetingVoiceprintDisplayNameRequest body = new MeetingVoiceprintDisplayNameRequest();
        body.setUserId(userId);
        body.setUserName(userName);
        return updateVoiceprintDisplayName(body);
    }

    public Response<String> updateVoiceprintDisplayName(MeetingVoiceprintDisplayNameRequest body) {
        String url = "http://" + managementServiceName + VOICEPRINT_DISPLAY_NAME_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MeetingVoiceprintDisplayNameRequest> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> responseEntity =
                    loadBalancedRestTemplate.postForEntity(url, entity, String.class);
            String responseBody = responseEntity.getBody();
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.warn("[management voiceprint display-name] HTTP 非 2xx status={}, body={}",
                        responseEntity.getStatusCode(), responseBody);
                throw new IllegalStateException("更新声纹展示名失败 HTTP " + responseEntity.getStatusCode());
            }
            if (StringUtils.isBlank(responseBody)) {
                throw new IllegalStateException("management 返回空 body");
            }
            Response<String> parsed = JacksonUtil.json2Bean(responseBody, new TypeReference<Response<String>>() {});
            if (parsed == null) {
                throw new IllegalStateException("management 响应无法解析为 Response");
            }
            return parsed;
        } catch (HttpStatusCodeException e) {
            String errBody = e.getResponseBodyAsString();
            logger.error("[management voiceprint display-name] HTTP 异常 status={}, body={}",
                    e.getStatusCode(), errBody, e);
            throw new IllegalStateException("更新声纹展示名失败(HTTP " + e.getStatusCode().value() + ")"
                    + (StringUtils.isNotBlank(errBody) ? "：" + errBody : ""));
        }
    }

    private Response<VoiceCloneResContent> parseCloneResponse(ResponseEntity<String> responseEntity) {
        String responseBody = responseEntity.getBody();
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.warn("[management voice clone] HTTP 非 2xx status={}, body={}", responseEntity.getStatusCode(), responseBody);
            throw new IllegalStateException("音色克隆失败 HTTP " + responseEntity.getStatusCode());
        }
        if (StringUtils.isBlank(responseBody)) {
            throw new IllegalStateException("management 返回空 body");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[management voice clone] raw body: {}", responseBody);
        }
        Response<VoiceCloneResContent> parsed =
                JacksonUtil.json2Bean(responseBody, new TypeReference<Response<VoiceCloneResContent>>() {});
        if (parsed == null) {
            throw new IllegalStateException("management 响应无法解析为 Response，body 前 500 字："
                    + responseBody.substring(0, Math.min(500, responseBody.length())));
        }
        return parsed;
    }

    /**
     * 判断 management 响应是否为成功状态。
     */
    public static boolean isManagementOk(Response<?> res) {
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
