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
package com.hojoai.omniasst.sysagent.service.speech;

import com.hojoai.omniasst.sysagent.service.speech.bean.ASRRequest;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRResponse;

/**
 * The service interface for asr.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/27
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public interface ASRService {

    /**
     * 流式语音识别：按分包上传音频数据并获取实时/增量识别结果。
     *
     * @param request 流式任务、分片索引、音频包及语言等参数
     * @return 识别结果；具体实现可能在失败时返回 {@code rtn != 0} 的 {@link ASRResponse}
     */
    ASRResponse asr(ASRRequest request);

    /**
     * 整文件（OSS URL）语音识别，适用于非流式、一次性转写场景。
     *
     * @param url 音频资源地址（通常为 OSS URL）
     * @return 识别文本；失败或无可用时可能为空字符串
     */
    String asrFileToText(String url);
}