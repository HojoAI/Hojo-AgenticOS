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

public class VoiceprintPendingSnapshot {

    private final String userId;
    private final String voiceId;
    private final String provider;
    private final double[] voiceprint;

    public VoiceprintPendingSnapshot(String userId, String voiceId, String provider, double[] voiceprint) {
        this.userId = userId;
        this.voiceId = voiceId;
        this.provider = provider;
        this.voiceprint = voiceprint;
    }

    public String getUserId() {
        return userId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public String getProvider() {
        return provider;
    }

    public double[] getVoiceprint() {
        return voiceprint;
    }
}
