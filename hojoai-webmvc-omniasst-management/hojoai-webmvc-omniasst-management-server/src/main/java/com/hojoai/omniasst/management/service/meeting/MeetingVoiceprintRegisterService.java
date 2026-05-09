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

import com.hojoai.omniasst.management.service.meeting.bean.MeetingVoiceprintCommitRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MeetingVoiceprintRegisterService {

    /**
     * 音色克隆 + 声纹提取并暂存（Redis），返回 voiceId。
     */
    VoiceCloneResponse prepare(String userId, MultipartFile file, String voiceName, Integer type, boolean needSave, String provider);

    /**
     * 使用暂存数据：写入/更新音色档案并正式写入声纹表。
     */
    VoiceCloneResponse commit(MeetingVoiceprintCommitRequest request);
}
