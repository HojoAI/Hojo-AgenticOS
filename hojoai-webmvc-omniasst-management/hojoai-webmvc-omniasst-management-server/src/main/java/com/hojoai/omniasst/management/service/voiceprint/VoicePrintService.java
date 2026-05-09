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

import com.hojoai.omniasst.management.service.voice.bean.VoicePrintQueryRequest;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintPageResult;
import org.springframework.web.multipart.MultipartFile;

public interface VoicePrintService {

    void registerVoicePrint(String userId, String userName, MultipartFile audioFile);

    /**
     * 使用 /v0/voice/clone 成功后写入 Redis 的声纹暂存入库（无 file）。
     */
    void registerVoicePrintFromPending(String userId, String userName);

    VoicePrintPageResult queryUserVoicePrints(VoicePrintQueryRequest request);

    void deleteUserVoicePrint(String userId);

    /**
     * 若该 userId 有声纹则删除，否则不报错（用于注销音色时一并清理配套声纹）。
     */
    void deleteUserVoicePrintIfPresent(String userId);

    /**
     * 仅 userId：先尝试会议 prepare 暂存的原始音频提声纹；否则尝试与档案 voiceId 一致的克隆暂存向量。
     */
    void copyVoiceProfileToVoiceprint(String userId);
}
