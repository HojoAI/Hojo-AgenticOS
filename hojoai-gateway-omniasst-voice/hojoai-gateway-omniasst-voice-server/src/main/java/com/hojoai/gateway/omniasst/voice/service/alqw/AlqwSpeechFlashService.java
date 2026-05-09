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
package com.hojoai.gateway.omniasst.voice.service.alqw;

import com.hojoai.gateway.omniasst.voice.service.asr.dto.AsrFileOssRequest;

/**
 * 阿里云语音识别 FlashRecognizer 服务接口
 * 封装录音文件/URL 的语音转文字能力，兼容 AsrFileOssRequest 入参与 String 出参
 */
public interface AlqwSpeechFlashService {

    /**
     * 将语音文件或语音 URL 转为文字
     *
     * @param request 含 ossUrl（本地路径或可访问 URL）、language 等
     * @return 识别得到的文本，失败时抛出异常
     */
    String convertToText(AsrFileOssRequest request);
}
