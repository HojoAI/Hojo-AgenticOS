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
package com.hojoai.gateway.omniasst.voice.service.voiceprint;

import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.VoicePrintResponse;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintPageResult;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintQueryRequest;
import org.springframework.web.multipart.MultipartFile;

public interface VoicePrintService {
    /**
     * 从请求中识别声纹
     * @param request 声纹识别请求（包含Base64编码的音频数据）
     * @return 声纹识别响应（包含声纹向量）
     */
    VoicePrintResponse recognizeFromRequest(VoicePrintRequest request);
    
    /**
     * 从文件中识别声纹
     * @param audioFile WAV音频文件
     * @return 声纹识别响应（包含声纹向量）
     */
    VoicePrintResponse recognizeFromFile(MultipartFile audioFile);
    
    /**
     * 录入用户声纹
     * @param userId 用户ID
     * @param userName 用户名
     * @param audioFile WAV音频文件
     */
    void registerVoicePrint(String userId, String userName, MultipartFile audioFile) throws Exception;
    
    /**
     * 分页查询用户声纹列表
     * @param request 查询请求
     * @return 查询结果
     */
    VoicePrintPageResult queryUserVoicePrints(VoicePrintQueryRequest request);
    
    /**
     * 根据用户ID删除用户声纹
     * @param userId 用户ID
     */
    void deleteUserVoicePrint(String userId);
}
