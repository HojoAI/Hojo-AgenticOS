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
package com.hojoai.omniasst.management.common;

import java.time.Duration;


public class RedisConstants {

    public static final String REDIS_KEY_PLACEHOLDER = "%s";

    /**
     * The Redis channel for translate result.
     */
    public static final String CHANNEL_TRANSLATE_RESULT = "Omniasst:Voice:Translate:Result";

    /**
     * The Redis channel for meeting result (meeting end).
     */
    public static final String CHANNEL_MEETING_RESULT = "Omniasst:Voice:Meeting:Result";

    /**
     * The Redis channel for meeting segment (real-time transcription).
     */
    public static final String CHANNEL_MEETING_SEGMENT = "Omniasst:Voice:Meeting:Segment";

    /**
     * 会议录音 prepare 暂存声纹向量等（后缀接 userId）
     */
    public static final String KEY_MEETING_VOICEPRINT_PREPARE = "Omniasst:Mgmt:Meeting:Voiceprint:Prepare:";

    /**
     * 会议 prepare 暂存原始音频字节（Base64 JSON，后缀接 userId），供 {@code /v1/meeting/voiceprint/register/copy-voice} 仅传 userId 时提声纹
     */
    public static final String KEY_MEETING_VOICEPRINT_PREPARE_AUDIO = "Omniasst:Mgmt:Meeting:Voiceprint:PrepareAudio:";

    /**
     * /v0/voice/clone 成功后暂存声纹向量（后缀接 userId），供 /v0/voiceprint/register 无 file 时消费
     */
    public static final String KEY_VOICEPRINT_AFTER_CLONE = "Omniasst:Mgmt:Voiceprint:AfterClone:";

}
