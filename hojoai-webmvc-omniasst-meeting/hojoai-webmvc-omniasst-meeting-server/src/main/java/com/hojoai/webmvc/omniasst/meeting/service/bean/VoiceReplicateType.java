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

/**
 * 声音复刻（音色克隆）与声纹复刻（声纹注册）区分；用于各接口可选参数 {@code replicateType}。
 */
public enum VoiceReplicateType {

    /** 声音复刻（音色克隆），对应 {@code /v1/meeting/voice/clone}、{@code /save} */
    VOICE,

    /** 声纹复刻（声纹注册），对应 {@code /v1/meeting/voiceprint/register/prepare}、{@code commit} */
    VOICEPRINT
}
