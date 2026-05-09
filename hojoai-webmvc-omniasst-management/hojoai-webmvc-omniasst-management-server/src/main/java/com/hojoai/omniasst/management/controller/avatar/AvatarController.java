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
package com.hojoai.omniasst.management.controller.avatar;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.management.service.avatar.AvatarService;
import com.hojoai.omniasst.management.utils.ResultUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

/**
 * 头像列表与用户使用头像的查询接口。
 */
@RestController
@RequestMapping("v0/avatars")
public class AvatarController {

    @Resource
    private AvatarService avatarService;

    /**
     * Query avatar list - Get all available avatars
     */
    @GetMapping("/list")
    public Response getAvatarList() {
        try {
            return ResultUtils.Ok(avatarService.getAllAvatars());
        } catch (Exception e) {
            return ResultUtils.Error("Failed to get avatar list: " + e.getMessage());
        }
    }

    /**
     * Get current user's avatar
     */
    @GetMapping("/current")
    public Response getCurrentUserAvatar(@RequestHeader("X-User-Id") String userId) {
        try {
            return ResultUtils.Ok(avatarService.getUserCurrentAvatar(userId));
        } catch (Exception e) {
            return ResultUtils.Error("Failed to get user's current avatar: " + e.getMessage());
        }
    }

    /**
     * Switch user's avatar - Allows user to switch to a different avatar
     */
    @GetMapping("/switch/{avatarId}")
    public Response switchUserAvatar(@PathVariable String avatarId,
                                   @RequestHeader("X-User-Id") String userId) {
        try {
            avatarService.assignAvatarToUser(userId, avatarId);
            return ResultUtils.Ok();
        } catch (Exception e) {
            return ResultUtils.Error("Failed to switch avatar: " + e.getMessage());
        }
    }

    /**
     * Get avatar by avatarId
     */
    @GetMapping("/{avatarId}")
    public Response getAvatarByAvatarId(@PathVariable String avatarId) {
        try {
            return ResultUtils.Ok(avatarService.getAvatarByAvatarId(avatarId));
        } catch (Exception e) {
            return ResultUtils.Error("Failed to get avatar: " + e.getMessage());
        }
    }
}