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
package com.hojoai.webmvc.omniasst.meeting.service.voiceprint.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingVoiceprintData {
    private String speakerId;
    private String speakerName;
    private double[] voiceprint;

    public MeetingVoiceprintData() {}

    public MeetingVoiceprintData(String speakerId, String speakerName, double[] voiceprint) {
        this.speakerId = speakerId;
        this.speakerName = speakerName;
        this.voiceprint = voiceprint;
    }

    public String getSpeakerId() { return speakerId; }
    public void setSpeakerId(String speakerId) { this.speakerId = speakerId; }
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    public double[] getVoiceprint() { return voiceprint; }
    public void setVoiceprint(double[] voiceprint) { this.voiceprint = voiceprint; }
}
