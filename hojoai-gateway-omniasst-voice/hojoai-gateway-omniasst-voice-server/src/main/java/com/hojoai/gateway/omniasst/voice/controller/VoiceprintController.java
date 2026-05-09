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
package com.hojoai.gateway.omniasst.voice.controller;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.VoicePrintService;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintPageResult;
import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintQueryRequest;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 声纹管理 HTTP 接口（注册、列表、删除）。
 * 依赖 {@link VoicePrintService}，异常时返回统一 {@link com.hojoai.common.response.Response} 结构。
 */
@RestController
@RequestMapping("/v1/voiceprint")
public class VoiceprintController {
    
    @Resource
    private VoicePrintService voicePrintService;

    /**
     * 为用户注册声纹。
     *
     * @param userId    用户唯一标识
     * @param userName  用户显示名
     * @param audioFile 声纹音频
     * @return 成功提示或错误信息
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<String> registerVoicePrint(@RequestParam("userId") String userId, @RequestParam("userName") String userName, @RequestParam("audioFile") MultipartFile audioFile) {
        try {
            voicePrintService.registerVoicePrint(userId, userName, audioFile);
            return Response.newInstance(ResponseStatusBasic.OK, "声纹录入成功");
        } catch (IllegalArgumentException e) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, e.getMessage());
        } catch (Exception e) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, "声纹录入失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询用户声纹列表。
     *
     * @param request 查询条件
     * @return 分页结果或错误信息
     */
    @PostMapping("/list")
    public Response<Object> queryUserVoicePrints(@RequestBody VoicePrintQueryRequest request) {
        try {
            VoicePrintPageResult result = voicePrintService.queryUserVoicePrints(request);
            return Response.newInstance(ResponseStatusBasic.OK, result);
        } catch (Exception e) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, "查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定用户的声纹数据。
     *
     * @param userId 用户 ID
     * @return 操作结果描述
     */
    @DeleteMapping("/{userId}")
    public Response<String> deleteUserVoicePrint(@PathVariable("userId") String userId) {
        try {
            voicePrintService.deleteUserVoicePrint(userId);
            return Response.newInstance(ResponseStatusBasic.OK, "删除成功");
        } catch (IllegalArgumentException e) {
            return Response.newInstance(ResponseStatusBasic.ILLEGAL_ARGUMENT, e.getMessage());
        } catch (Exception e) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR, "删除失败: " + e.getMessage());
        }
    }
}

