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

import com.hojoai.omniasst.management.service.voice.bean.UserMatchResult;
import com.hojoai.omniasst.management.service.voice.bean.UserVoicePrintProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintPageResult;

public interface VoicePrintVectorService {

    UserMatchResult matchUserVoicePrint(double[] voicePrint, Double threshold);

    void saveUserVoicePrint(String userId, String userName, double[] voicePrint);

    VoicePrintPageResult queryUserVoicePrints(String userName, int page, int pageSize);

    int deleteUserVoicePrint(String userId);

    /**
     * 仅更新 {@code user_voiceprints.user_name}，不改动向量。
     *
     * @return 影响行数，0 表示无该 userId 声纹记录
     */
    int updateUserName(String userId, String userName);

    String getUserNameByUserId(String userId);

    UserVoicePrintProfileResponse getUserVoicePrintProfile(String userId);
}
