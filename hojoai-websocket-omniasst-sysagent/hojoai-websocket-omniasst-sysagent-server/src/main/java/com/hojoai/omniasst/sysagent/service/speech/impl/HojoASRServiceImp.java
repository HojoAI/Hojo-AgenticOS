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

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.service.api.VoiceAPI;
import com.hojoai.omniasst.sysagent.service.speech.ASRService;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRFileToTextRequest;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRRequest;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 基于 omniasst-voice 微服务的 ASR 实现，通过 {@link VoiceAPI} 完成文件转文本；
 * 流式 {@link #asr(ASRRequest)} 当前未对接远程能力，由其他实现类负责。
 */
@Service
public class HojoASRServiceImp implements ASRService {
    @Resource
    private VoiceAPI voiceAPI;

    /**
     * 流式语音识别占位实现，当前始终返回 {@code null}，实际流式能力请使用 {@link com.hojoai.omniasst.sysagent.service.speech.impl.ASRServiceImpl}。
     *
     * @param request 流式 ASR 请求参数
     * @return 当前固定为 {@code null}
     */
    @Override
    public ASRResponse asr(ASRRequest request) {
        return null;
    }

    /**
     * 根据音频在 OSS 上的地址调用语音服务完成整段转写。
     *
     * @param url 音频文件的 OSS URL，由上游保证可访问
     * @return 识别成功返回文本；业务失败或非 OK 状态码时返回空字符串
     */
    @Override
    public String asrFileToText(String url) {
        ASRFileToTextRequest request = new ASRFileToTextRequest();
        request.setOssUrl(url);
        Response<String> response = voiceAPI.asr(request);
        // 仅当网关/统一响应为成功时透传文本，避免将错误信息当作文本返回
        if (response.getResCode().equals(ResponseStatusBasic.OK.getResCode())){
            return response.getResContent();
        }
        return "";
    }
}