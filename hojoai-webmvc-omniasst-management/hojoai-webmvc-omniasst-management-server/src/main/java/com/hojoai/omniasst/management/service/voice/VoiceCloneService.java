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
package com.hojoai.omniasst.management.service.voice;

import com.hojoai.omniasst.management.service.voice.bean.UserVoiceProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoiceCloneResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceCloneService {

    VoiceCloneResponse cloneVoice(String userId, String voiceName, String provider, Boolean needSave, MultipartFile audioFile);

    UserVoiceProfileResponse getUserVoiceProfile(String userId, String provider);

    /**
     * 仅按 userId + provider 精确查询，无 provider 回退（用于注销等必须厂商一致的场景）。
     */
    UserVoiceProfileResponse getUserVoiceProfileStrict(String userId, String provider);

    void deleteUserVoiceProfile(String userId, String provider);

    void saveUserVoiceProfile(VoiceCloneRequest request);

}
