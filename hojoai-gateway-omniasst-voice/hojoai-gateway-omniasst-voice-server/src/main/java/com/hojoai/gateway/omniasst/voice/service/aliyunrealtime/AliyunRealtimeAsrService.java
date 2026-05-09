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
package com.hojoai.gateway.omniasst.voice.service.aliyunrealtime;

import com.hojoai.gateway.omniasst.voice.api.dto.ASRRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.ASRResponse;

/**
 * 阿里云实时语音识别服务接口
 * 基于阿里云智能语音交互 Java SDK（SpeechTranscriber）实现流式 ASR，
 * 入参与回参与 /realtime 接口保持一致，支持实时接收音频包并处理。
 *
 */
public interface AliyunRealtimeAsrService {

    /**
     * 实时 ASR 识别：接收单包或流式音频数据，返回当前/最终识别结果
     *
     * @param request ASR 请求（vid、package_data base64、package_end、vad_end 等）
     * @return ASR 响应（rtn、is_end、vid、text 等），与 realtime 接口一致
     */
    ASRResponse asr(ASRRequest request);

    /**
     * 从本地 WAV 文件读取音频，按包模拟实时发送并调用 asr，用于本地测试。
     * WAV 需为 16kHz、16bit、单声道 PCM。
     *
     * @param wavFilePath 本地 WAV 文件路径
     * @return 最后一包 asr 的响应（isEnd=1 时包含完整识别文本）
     */
    ASRResponse asrFromLocalWav(String wavFilePath);

    /**
     * 从本地 WAV 文件读取音频，按包模拟实时发送并调用 asr。
     *
     * @param wavFilePath 本地 WAV 文件路径
     * @param vid         会话 id，与 asr 接口一致
     * @return 最后一包 asr 的响应
     */
    ASRResponse asrFromLocalWav(String wavFilePath, String vid);
}
