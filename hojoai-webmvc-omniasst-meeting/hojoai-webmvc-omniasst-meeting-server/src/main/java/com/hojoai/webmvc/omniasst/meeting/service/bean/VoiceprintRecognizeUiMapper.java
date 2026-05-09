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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * 声纹抽取服务返回的 {@code rtn}/{@code msg} 与前端 {@link VoiceprintRegisterUiCodes} 的映射（启发式，可按引擎文档扩展 rtn 白名单）。
 */
public final class VoiceprintRecognizeUiMapper {

    private VoiceprintRecognizeUiMapper() {}

    /**
     * @param rtn 引擎返回码，可为 null
     * @param msg 引擎提示文案
     * @return {@link VoiceprintRegisterUiCodes#NOISY_ENVIRONMENT} 或 {@link VoiceprintRegisterUiCodes#GENERIC_ERROR}
     */
    public static String mapExtractFailure(Integer rtn, String msg) {
        if (looksLikeNoisyEnvironment(msg)) {
            return VoiceprintRegisterUiCodes.NOISY_ENVIRONMENT;
        }
        return VoiceprintRegisterUiCodes.GENERIC_ERROR;
    }

    static boolean looksLikeNoisyEnvironment(String msg) {
        if (StringUtils.isBlank(msg)) {
            return false;
        }
        String s = msg.toLowerCase();
        return msg.contains("噪") || msg.contains("嘈杂") || msg.contains("信噪") || msg.contains("环境声")
                || s.contains("noise") || s.contains("snr") || s.contains("ambient") || s.contains("too noisy");
    }
}
