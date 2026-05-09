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
package com.hojoai.omniasst.management.dao.avatar.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.omniasst.management.dao.avatar.UserAvatarDAO;
import com.hojoai.omniasst.management.dao.avatar.UserAvatarMapper;
import com.hojoai.omniasst.management.dao.avatar.po.UserAvatar;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;

/**
 * 用户选用头像数据访问实现。
 */
@Repository
public class UserAvatarDAOImpl implements UserAvatarDAO {

    @Resource
    private UserAvatarMapper userAvatarMapper;

    @Override
    public void insertOrUpdateUserAvatar(UserAvatar userAvatar) {
        userAvatarMapper.insertOrUpdate(userAvatar);
    }

    @Override
    public void insertOrUpdateUserAvatar(String userId, String avatarId) {
        UserAvatar userAvatar = selectUserAvatarByUserId(userId);
        if (userAvatar == null){
            userAvatar = new UserAvatar();
            userAvatar.setUserId(userId);
        }
        userAvatar.setAvatarId(avatarId);
        insertOrUpdateUserAvatar(userAvatar);
    }

    @Override
    public UserAvatar selectUserAvatarByUserId(String userId) {
        LambdaQueryWrapper<UserAvatar> wrapper =
            new LambdaQueryWrapper<>();
        wrapper.eq(UserAvatar::getUserId, userId);
        return userAvatarMapper.selectOne(wrapper);
    }
}