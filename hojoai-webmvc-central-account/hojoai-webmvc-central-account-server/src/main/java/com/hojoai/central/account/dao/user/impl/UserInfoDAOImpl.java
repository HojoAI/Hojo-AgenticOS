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
package com.hojoai.central.account.dao.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.central.account.dao.user.UserInfoDAO;
import com.hojoai.central.account.dao.user.UserInfoMapper;
import com.hojoai.central.account.dao.user.po.UserInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class UserInfoDAOImpl implements UserInfoDAO {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void insertUserInfoByUserMobile(String userId, String userMobile, Integer disabledStatus) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUserMobile(userMobile);
        userInfo.setDisabledStatus(disabledStatus);
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void updateUserMobileByUserId(String userId, String userMobile) {
        LambdaUpdateWrapper<UserInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserInfo::getUserId, userId).set(UserInfo::getUserMobile, userMobile);
        userInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateUserNicknameByUserId(String userId, String userNickname) {
        LambdaUpdateWrapper<UserInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserInfo::getUserId, userId).set(UserInfo::getUserNickname, userNickname);
        userInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateUserDescriptionByUserId(String userId, String userDescription) {
        LambdaUpdateWrapper<UserInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserInfo::getUserId, userId).set(UserInfo::getUserDescription, userDescription);
        userInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateUserHeadImageByUserId(String userId, String userHeadImage) {
        LambdaUpdateWrapper<UserInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserInfo::getUserId, userId).set(UserInfo::getUserHeadImage, userHeadImage);
        userInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public void updateDisabledStatusByUserId(String userId, Integer disabledStatus) {
        LambdaUpdateWrapper<UserInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserInfo::getUserId, userId).set(UserInfo::getDisabledStatus, disabledStatus);
        userInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public UserInfo selectUserInfoByUserId(String userId) {
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getUserId, userId);
        return userInfoMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public UserInfo selectUserInfoByUserMobile(String userMobile) {
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getUserMobile, userMobile);
        return userInfoMapper.selectOne(lambdaQueryWrapper);
    }
}
