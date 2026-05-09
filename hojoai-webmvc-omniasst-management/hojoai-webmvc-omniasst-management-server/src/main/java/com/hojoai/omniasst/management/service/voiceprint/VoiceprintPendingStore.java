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
package com.hojoai.omniasst.management.service.voiceprint;

/**
 * 音色克隆成功后暂存声纹向量，供仅调用一次上传的客户端在后续无 file 的 register 中消费。
 */
public interface VoiceprintPendingStore {

    void putAfterClone(String userId, String voiceId, String provider, double[] voiceprint);

    /**
     * 取出并删除；无数据返回 null。
     */
    VoiceprintPendingSnapshot take(String userId);

    /**
     * 若存在克隆暂存且 voiceId 与 expectedVoiceId 一致则返回快照（不删 key，落库成功后请调用 {@link #removePending}）。
     */
    VoiceprintPendingSnapshot peekIfVoiceIdMatches(String userId, String expectedVoiceId);

    void removePending(String userId);
}
