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
package com.hojoai.omniasst.sysagent.service.speech.impl;

import com.hojoai.omniasst.sysagent.service.speech.ASRService;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRRequest;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * The service implements class for asr.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class ASRServiceImpl implements ASRService {

    private static final Logger logger = LoggerFactory.getLogger(ASRServiceImpl.class);

    /** 流式 ASR HTTP 接口地址（与 {@link RestTemplate} 对接）。 */
    private static final String ASR_URL = "https://daily-www.hojoai.com/asr/asr_stream";

    @Resource
    private RestTemplate restTemplate;

    /**
     * 通过 HTTP POST 将流式 ASR 请求提交至 {@link #ASR_URL}。
     *
     * @param request 流式分片与音频数据
     * @return 远端返回的 {@link ASRResponse}；异常时 {@code rtn = -1}
     */
    @Override
    public ASRResponse asr(ASRRequest request) {
        try {
            return restTemplate.postForObject(ASR_URL, request, ASRResponse.class);
        } catch (Exception e) {
            // 网络或反序列化失败：返回可识别的错误码，避免向调用方抛未检查异常
            logger.error("asr response error", e);
            ASRResponse errorResponse = new ASRResponse();
            errorResponse.setRtn(-1);
            return errorResponse;
        }
    }

    /**
     * 本实现专注于流式 HTTP，文件转写请使用 {@link com.hojoai.omniasst.sysagent.service.speech.impl.HojoASRServiceImp}。
     *
     * @param url 音频 OSS 地址（本方法不使用）
     * @return 固定为空字符串
     */
    @Override
    public String asrFileToText(String url) {
        return "";
    }
}