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

import java.util.Locale;

/**
 * 解析并校验 {@code replicateType}；音色类接口默认 {@link VoiceReplicateType#VOICE}，声纹类接口默认 {@link VoiceReplicateType#VOICEPRINT}。
 */
public final class VoiceReplicateTypeSupport {

    private static final String VOICEPRINT_APIS =
            "POST /omniasst-meeting/v1/meeting/voiceprint/register/prepare + .../commit（声纹注册两阶段；路由由 prepare 暂存决定）";
    private static final String VOICE_APIS =
            "POST /omniasst-meeting/v1/meeting/voice/clone 与 .../voice/save（声音复刻专用）";

    private VoiceReplicateTypeSupport() {}

    /**
     * 音色 clone/save：未传或空 → 视为声音复刻；若显式传 {@link VoiceReplicateType#VOICEPRINT} 则拒绝并提示走声纹接口。
     */
    public static void validateForVoiceEndpoints(String replicateTypeRaw) {
        if (StringUtils.isBlank(replicateTypeRaw)) {
            return;
        }
        VoiceReplicateType t = parse(replicateTypeRaw.trim());
        if (t == VoiceReplicateType.VOICEPRINT) {
            throw new IllegalArgumentException(
                    "当前接口用于声音复刻（音色克隆）。声纹复刻请改用 " + VOICEPRINT_APIS + "；勿对本接口传 replicateType=VOICEPRINT。");
        }
    }

    /**
     * 声纹 prepare/commit：未传或空 → 视为声纹复刻；若显式传 {@link VoiceReplicateType#VOICE} 则拒绝并提示走音色接口。
     */
    public static void validateForVoiceprintEndpoints(String replicateTypeRaw) {
        if (StringUtils.isBlank(replicateTypeRaw)) {
            return;
        }
        VoiceReplicateType t = parse(replicateTypeRaw.trim());
        if (t == VoiceReplicateType.VOICE) {
            throw new IllegalArgumentException(
                    "当前接口用于声纹复刻（声纹注册）。JSON 路径勿传 replicateType=VOICE；"
                            + "声音场景请使用 multipart prepare 的 type=0（默认），或改用 " + VOICE_APIS + "。");
        }
    }

    /**
     * 兼容参数：优先 replicateType，其次 type（0=VOICE，1=VOICEPRINT）。
     */
    public static String resolveReplicateType(String replicateTypeRaw, Integer type) {
        if (StringUtils.isNotBlank(replicateTypeRaw)) {
            return replicateTypeRaw.trim();
        }
        if (type == null) {
            return null;
        }
        if (type == 0) {
            return VoiceReplicateType.VOICE.name();
        }
        if (type == 1) {
            return VoiceReplicateType.VOICEPRINT.name();
        }
        throw new IllegalArgumentException("type 不合法，请使用 0（声音复刻）或 1（声纹复刻）");
    }

    public static void validateForVoiceprintEndpoints(String replicateTypeRaw, Integer type) {
        validateForVoiceprintEndpoints(resolveReplicateType(replicateTypeRaw, type));
    }

    private static VoiceReplicateType parse(String s) {
        String u = s.toUpperCase(Locale.ROOT);
        if ("VOICE".equals(u)) {
            return VoiceReplicateType.VOICE;
        }
        if ("VOICEPRINT".equals(u)) {
            return VoiceReplicateType.VOICEPRINT;
        }
        throw new IllegalArgumentException("replicateType 不合法，请使用 VOICE（声音复刻）或 VOICEPRINT（声纹复刻）");
    }
}
