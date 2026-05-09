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
package com.hojoai.central.account.service.auth.impl;

import com.hojoai.central.account.common.DisabledStatus;
import com.hojoai.central.account.common.ResponseStatusExtra;
import com.hojoai.central.account.dao.auth.OverseaAuthIdentityDAO;
import com.hojoai.central.account.dao.auth.OverseaUserInfoDAO;
import com.hojoai.central.account.dao.auth.po.OverseaAuthIdentity;
import com.hojoai.central.account.dao.auth.po.OverseaUserInfo;
import com.hojoai.central.account.dao.user.UserInfoDAO;
import com.hojoai.central.account.dao.user.UserTokenInfoDAO;
import com.hojoai.central.account.dao.user.po.UserInfo;
import com.hojoai.central.account.dao.user.po.UserTokenInfo;
import com.hojoai.central.account.service.auth.OverseaAuthService;
import com.hojoai.central.account.service.auth.bean.OverseaTokenRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.downgoon.snowflake.Snowflake;

import java.util.UUID;

/**
 * 海外/第三方登录身份与令牌服务：查找或创建用户、同步 user_info、签发与持久化 user_token。
 */
@Service
public class OverseaAuthServiceImpl implements OverseaAuthService {

    private static final Logger logger = LoggerFactory.getLogger(OverseaAuthServiceImpl.class);

    private static final String STATUS_ACTIVE = "active";
    private static final int BUSINESS_TYPE = 1;

    @Resource
    private OverseaUserInfoDAO overseaUserInfoDAO;

    @Resource
    private OverseaAuthIdentityDAO overseaAuthIdentityDAO;

    @Resource
    private UserInfoDAO userInfoDAO;

    @Resource
    private UserTokenInfoDAO userTokenInfoDAO;

    @Resource
    private Snowflake snowflake;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Response<UserTokenInfoResponse> createOverseaToken(OverseaTokenRequest request) throws Exception {
        OverseaUserInfo user = findOrCreateUser(
                request.getProvider(), request.getProviderUserId(),
                request.getEmail(), request.getNickname(), request.getAvatar());

        if (!STATUS_ACTIVE.equals(user.getStatus())) {
            if ("disabled".equals(user.getStatus())) {
                return Response.newInstance(ResponseStatusExtra.ACCOUNT_DISABLED);
            }
            return Response.newInstance(ResponseStatusExtra.ACCOUNT_DELETED);
        }

        String userId = String.valueOf(user.getId());
        int platformType = (request.getPlatformType() != null) ? request.getPlatformType() : 1;

        syncToUserInfo(userId, user.getNickname(), user.getAvatar());
        String userToken = generateAndSaveUserToken(userId, BUSINESS_TYPE, platformType);

        UserTokenInfoResponse data = new UserTokenInfoResponse();
        data.setUserId(userId);
        data.setUserToken(userToken);
        data.setBusinessType(BUSINESS_TYPE);
        data.setPlatformType(platformType);
        return Response.newInstance(ResponseStatusBasic.OK, data);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Response<?> deleteOverseaAccount(String userId) throws Exception {
        Long userIdLong = Long.valueOf(userId);

        OverseaUserInfo user = overseaUserInfoDAO.selectById(userIdLong);
        if (user == null) {
            return Response.newInstance(ResponseStatusExtra.ACCOUNT_DELETED);
        }

        overseaAuthIdentityDAO.deleteByUserId(userIdLong);
        userTokenInfoDAO.deleteUserTokenInfoByUserId(userId);
        overseaUserInfoDAO.softDelete(userIdLong);
        userInfoDAO.updateDisabledStatusByUserId(userId, DisabledStatus.ALREADY.getCode());

        logger.info("oversea user {} account deleted (identities removed, email released, user_info disabled)", userId);
        return Response.newInstance(ResponseStatusBasic.OK);
    }

    // ======================== 内部方法 ========================

    private OverseaUserInfo findOrCreateUser(String provider, String providerUserId, String email, String nickname, String avatar) {
        OverseaAuthIdentity identity = overseaAuthIdentityDAO.selectByProviderAndProviderUserId(provider, providerUserId);
        if (identity != null) {
            OverseaUserInfo existingUser = overseaUserInfoDAO.selectById(identity.getUserId());
            if (existingUser != null && nickname != null && existingUser.getNickname() == null) {
                overseaUserInfoDAO.updateNicknameAndAvatar(existingUser.getId(), nickname, avatar);
                existingUser.setNickname(nickname);
                existingUser.setAvatar(avatar);
            }
            return existingUser;
        }

        OverseaUserInfo userByEmail = (email != null) ? overseaUserInfoDAO.selectByEmail(email) : null;
        if (userByEmail != null) {
            bindIdentity(userByEmail.getId(), provider, providerUserId, email);
            return userByEmail;
        }

        OverseaUserInfo newUser = new OverseaUserInfo();
        newUser.setId(snowflake.nextId());
        newUser.setEmail(email);
        newUser.setNickname(nickname);
        newUser.setAvatar(avatar);
        newUser.setStatus(STATUS_ACTIVE);
        newUser.setTokenVersion(0);
        overseaUserInfoDAO.insertUser(newUser);

        bindIdentity(newUser.getId(), provider, providerUserId, email);

        logger.info("new oversea user created, userId={}, provider={}", newUser.getId(), provider);
        return newUser;
    }

    private void bindIdentity(Long userId, String provider, String providerUserId, String email) {
        OverseaAuthIdentity identity = new OverseaAuthIdentity();
        identity.setId(snowflake.nextId());
        identity.setUserId(userId);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setEmail(email);
        overseaAuthIdentityDAO.insertIdentity(identity);
    }

    private void syncToUserInfo(String userId, String nickname, String avatar) {
        UserInfo existing = userInfoDAO.selectUserInfoByUserId(userId);
        if (existing != null && existing.getIncrId() > 0) {
            return;
        }

        userInfoDAO.insertUserInfoByUserMobile(userId, "oversea_" + userId, DisabledStatus.NON.getCode());
        if (nickname != null) {
            userInfoDAO.updateUserNicknameByUserId(userId, nickname);
        }
        if (avatar != null) {
            userInfoDAO.updateUserHeadImageByUserId(userId, avatar);
        }
    }

    private String generateAndSaveUserToken(String userId, int businessType, int platformType) {
        String userToken = DigestUtils.sha256Hex(userId + businessType + platformType + UUID.randomUUID()).toUpperCase();

        UserTokenInfo existing = userTokenInfoDAO.selectUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userId, businessType, platformType);
        if (existing != null && existing.getIncrId() > 0) {
            userTokenInfoDAO.updateUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userId, businessType, platformType, userToken);
        } else {
            userTokenInfoDAO.insertUserTokenInfo(userId, businessType, platformType, userToken);
        }

        return userToken;
    }
}
