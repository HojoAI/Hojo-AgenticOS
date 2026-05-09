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
import com.hojoai.central.account.dao.user.UserTokenInfoDAO;
import com.hojoai.central.account.dao.user.UserTokenInfoMapper;
import com.hojoai.central.account.dao.user.po.UserTokenInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class UserTokenInfoDAOImpl implements UserTokenInfoDAO {

    @Resource
    private UserTokenInfoMapper userTokenInfoMapper;

    @Override
    public void insertUserTokenInfo(String userId, Integer businessType, Integer platformType, String userToken) {
        UserTokenInfo userTokenInfo = new UserTokenInfo();
        userTokenInfo.setUserId(userId);
        userTokenInfo.setBusinessType(businessType);
        userTokenInfo.setPlatformType(platformType);
        userTokenInfo.setUserToken(userToken);
        userTokenInfoMapper.insert(userTokenInfo);
    }

    @Override
    public void deleteUserTokenInfoByUserId(String userId) {
        LambdaQueryWrapper<UserTokenInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTokenInfo::getUserId, userId);
        userTokenInfoMapper.delete(lambdaQueryWrapper);
    }

    @Override
    public void deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(String userId, Integer businessType, Integer platformType) {
        LambdaQueryWrapper<UserTokenInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTokenInfo::getUserId, userId).eq(UserTokenInfo::getBusinessType, businessType).eq(UserTokenInfo::getPlatformType, platformType);
        userTokenInfoMapper.delete(lambdaQueryWrapper);
    }

    @Override
    public void updateUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(String userId, Integer businessType, Integer platformType, String userToken) {
        LambdaUpdateWrapper<UserTokenInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserTokenInfo::getUserId, userId).eq(UserTokenInfo::getBusinessType, businessType).eq(UserTokenInfo::getPlatformType, platformType).set(UserTokenInfo::getUserToken, userToken);
        userTokenInfoMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public UserTokenInfo selectUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(String userId, Integer businessType, Integer platformType) {
        LambdaQueryWrapper<UserTokenInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTokenInfo::getUserId, userId).eq(UserTokenInfo::getBusinessType, businessType).eq(UserTokenInfo::getPlatformType, platformType);
        return userTokenInfoMapper.selectOne(lambdaQueryWrapper);
    }
}
