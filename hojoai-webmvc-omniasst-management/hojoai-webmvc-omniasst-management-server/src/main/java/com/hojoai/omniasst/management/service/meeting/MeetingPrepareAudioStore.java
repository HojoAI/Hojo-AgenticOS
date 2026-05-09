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
package com.hojoai.omniasst.management.service.meeting;

/**
 * 会议 prepare 上传的原始音频暂存（Redis），供 {@code /v1/meeting/voiceprint/register/copy-voice} 消费。
 */
public interface MeetingPrepareAudioStore {

    class ConsumedAudio {
        private final byte[] bytes;
        private final String originalFilename;
        private final String contentType;

        public ConsumedAudio(byte[] bytes, String originalFilename, String contentType) {
            this.bytes = bytes;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getContentType() {
            return contentType;
        }
    }

    void put(String userId, String voiceId, byte[] audioBytes, String originalFilename, String contentType);

    /**
     * 若暂存存在且 voiceId 与 expectedVoiceId 一致则返回解码后的音频（不删 key，落库成功后请调用 {@link #delete}）。
     */
    ConsumedAudio loadIfVoiceIdMatches(String userId, String expectedVoiceId);

    void delete(String userId);
}
