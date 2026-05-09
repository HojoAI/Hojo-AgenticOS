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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 个人声纹注册单次提交结果：在声纹落库同时完成音色克隆与保存时返回 management 侧音色 ID。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintRegisterOneShotResponse {

    /** 经 clone + save 后的音色 ID；联调 bypass 等未执行音色链路时为 null */
    private String voiceId;

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}
