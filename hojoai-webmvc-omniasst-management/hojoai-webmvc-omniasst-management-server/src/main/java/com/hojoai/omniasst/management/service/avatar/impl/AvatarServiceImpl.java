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
package com.hojoai.omniasst.management.service.avatar.impl;

import com.hojoai.omniasst.common.api.avatar.AvatarItem;
import com.hojoai.omniasst.management.dao.avatar.AvatarDAO;
import com.hojoai.omniasst.management.dao.avatar.UserAvatarDAO;
import com.hojoai.omniasst.management.dao.avatar.po.Avatar;
import com.hojoai.omniasst.management.dao.avatar.po.UserAvatar;
import com.hojoai.omniasst.management.service.avatar.AvatarService;
import com.hojoai.omniasst.management.service.avatar.util.AvatarUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统头像与用户使用头像关联查询。
 */
@Service
public class AvatarServiceImpl implements AvatarService {

    @Resource
    private AvatarDAO avatarDAO;

    @Resource
    private UserAvatarDAO userAvatarDAO;

    @Override
    public List<AvatarItem> getAllAvatars() {
        List<Avatar> avatars = avatarDAO.selectAllAvatars();
        return avatars.stream()
                .map(AvatarUtils::convertToAvatarItem)
                .collect(Collectors.toList());
    }

    @Override
    public void assignAvatarToUser(String userId, String avatarId) {
        // Check if avatar exists
        Avatar avatar = avatarDAO.selectAvatarByAvatarId(avatarId);
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar not found with id: " + avatarId);
        }

        userAvatarDAO.insertOrUpdateUserAvatar(userId, avatarId);
    }

    @Override
    public AvatarItem getUserCurrentAvatar(String userId) {
        UserAvatar userAvatar = userAvatarDAO.selectUserAvatarByUserId(userId);
        Avatar avatar;
        if (userAvatar != null) {
            avatar = avatarDAO.selectAvatarByAvatarId(userAvatar.getAvatarId());
        }else {
            avatar = avatarDAO.selectDefaultAvatar();
            assignAvatarToUser(userId, avatar.getAvatarId());
        }
        return AvatarUtils.convertToAvatarItem(avatar);
    }

    @Override
    public AvatarItem getAvatarByAvatarId(String avatarId) {
        Avatar avatar = avatarDAO.selectAvatarByAvatarId(avatarId);
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar not found with id: " + avatarId);
        }
        return AvatarUtils.convertToAvatarItem(avatar);
    }
}