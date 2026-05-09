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
package com.hojoai.webmvc.omniasst.meeting.service.impl;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.meeting.service.ModelService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.HojoMeetingSummaryRequest;
import com.hojoai.webmvc.omniasst.meeting.service.bean.HojoMeetingSummaryWrapper;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSummaryResponse;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * The ModelServiceImpl class for nlp model
 *
 * @author lianghay
 * @version 1.0, 2026/01/04
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */

@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

    @Resource(name = "meetingTitleModel")
    private ChatClient meetingTitleChatClient;

    @Value("${model.meeting-title.sys-prompt}")
    private String meetingTitleSysPrompt;

    @Value("${model.meeting-hojo.summary.url}")
    private String hojoSummaryUrl;

    @Value("${model.meeting-hojo.summary.auth-token}")
    private String hojoSummaryAuthToken;

    private static final String HEADER_TRACE_ID = "trace_id";

    /** Hojo 会议总结接口：读超时 5 分钟，仅在此处使用 */
    private static final int HOJO_READ_TIMEOUT_MS = 5 * 60 * 1000;
    private static final int HOJO_CONNECT_TIMEOUT_MS = 10_000;
    /** 返回空时重试次数（不含首次），共最多 6 次请求 */
    private static final int HOJO_RETRY_TIMES = 5;

    private volatile RestTemplate hojoRestTemplate;

    private RestTemplate getHojoRestTemplate() {
        if (hojoRestTemplate == null) {
            synchronized (this) {
                if (hojoRestTemplate == null) {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(HOJO_CONNECT_TIMEOUT_MS);
                    factory.setReadTimeout(HOJO_READ_TIMEOUT_MS);
                    hojoRestTemplate = new RestTemplate(factory);
                }
            }
        }
        return hojoRestTemplate;
    }

    @Override
    public MeetingSummaryResponse getMeetingSummaryResult(String transcriptContent, List<String> attendeeIdentity, String myIdentity) {
        HojoMeetingSummaryRequest request = new HojoMeetingSummaryRequest();
        request.setTranscriptContent(transcriptContent != null ? transcriptContent : "");
        request.setAttendeeIdentity(attendeeIdentity != null ? attendeeIdentity : Collections.emptyList());
        request.setMyIdentity(myIdentity != null ? myIdentity : "");
        request.setStream(false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hojoSummaryAuthToken);
        HttpEntity<HojoMeetingSummaryRequest> entity = new HttpEntity<>(request, headers);

        for (int attempt = 1; attempt <= 1 + HOJO_RETRY_TIMES; attempt++) {
            try {
                MeetingSummaryResponse result = doHojoSummaryRequest(entity);
                if (result != null) {
                    return result;
                }
            } catch (HttpStatusCodeException e) {
                String bodySnippet = e.getResponseBodyAsString();
                if (bodySnippet != null && bodySnippet.length() > 800) {
                    bodySnippet = bodySnippet.substring(0, 800) + "...";
                }
                logger.error("[会议结果持久化] Hojo会议总结HTTP错误(不重试), status: {}, body: {}", e.getStatusCode(), bodySnippet);
                return null;
            }
            if (attempt <= HOJO_RETRY_TIMES) {
                logger.warn("[会议结果持久化] Hojo会议总结返回空, 第{}次重试, 共{}次", attempt, 1 + HOJO_RETRY_TIMES);
            }
        }
        logger.error("[会议结果持久化] Hojo会议总结重试{}次后仍失败, url: {}", 1 + HOJO_RETRY_TIMES, hojoSummaryUrl);
        return null;
    }

    private MeetingSummaryResponse doHojoSummaryRequest(HttpEntity<HojoMeetingSummaryRequest> entity) {
        try {
            ResponseEntity<String> response = getHojoRestTemplate().postForEntity(hojoSummaryUrl, entity, String.class);
            String traceId = response.getHeaders().getFirst(HEADER_TRACE_ID);
            if (response.getBody() == null) {
                logger.warn("[会议结果持久化] Hojo会议总结响应 body 为空, trace_id: {}", traceId);
                return null;
            }
            logger.info("[会议结果持久化] 生成会议总结原始返回, trace_id: {}, body: {}", traceId, response.getBody());
            HojoMeetingSummaryWrapper wrapper = JacksonUtil.json2Bean(response.getBody(), HojoMeetingSummaryWrapper.class);
            MeetingSummaryResponse result = wrapper != null ? wrapper.getAbstractData() : null;
            if (result == null) {
                logger.warn("[会议结果持久化] Hojo会议总结 abstract 为空, trace_id: {}", traceId);
            }
            return result;
        } catch (HttpStatusCodeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[会议结果持久化] Hojo会议总结请求失败, url: {}", hojoSummaryUrl, e);
            return null;
        }
    }

    @Override
    public String getMeetingTitleResult(String query) {
        Prompt prompt = new Prompt(new SystemMessage(meetingTitleSysPrompt), new UserMessage(query));
        String result = meetingTitleChatClient.prompt(prompt).call().content();
        return result;
    }
}
