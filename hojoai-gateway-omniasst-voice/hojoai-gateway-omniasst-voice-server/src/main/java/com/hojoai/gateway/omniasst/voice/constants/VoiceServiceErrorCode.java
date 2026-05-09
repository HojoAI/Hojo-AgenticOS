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
package com.hojoai.gateway.omniasst.voice.constants;

/**
 * 语音网关对前端/调用方暴露的业务错误码枚举。
 * 与 {@link com.hojoai.common.response.Response} 的 message 或 data 字段组合使用。
 */
public enum VoiceServiceErrorCode {
    /** 上传文件超过配置大小限制 */
    FILE_TOO_LARGE("FILE_TOO_LARGE", "文件大小超过限制"),
    /** 文件扩展名或 MIME 与允许列表不符 */
    INVALID_FILE_FORMAT("INVALID_FILE_FORMAT", "文件格式不正确"),
    /** 读取上传流或临时文件失败 */
    FILE_READ_ERROR("FILE_READ_ERROR", "文件读取失败"),
    /** 转码（如转为 PCM/WAV）失败 */
    FILE_CONVERSION_ERROR("FILE_CONVERSION_ERROR", "文件转换失败"),
    /** TTS 下游调用或合成流程失败 */
    TTS_SYNTHESIS_ERROR("TTS_SYNTHESIS_ERROR", "TTS合成失败"),
    /** ASR 识别或后处理失败 */
    ASR_PROCESSING_ERROR("ASR_PROCESSING_ERROR", "ASR处理失败");

    private final String code;
    private final String message;

    VoiceServiceErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return 机器可读错误码常量名对应的字符串
     */
    public String getCode() {
        return code;
    }

    /**
     * @return 默认中文描述，可展示给用户或写入日志
     */
    public String getMessage() {
        return message;
    }
}
