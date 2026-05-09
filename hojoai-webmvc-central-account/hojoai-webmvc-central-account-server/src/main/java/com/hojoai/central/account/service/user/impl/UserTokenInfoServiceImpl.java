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
package com.hojoai.central.account.service.user.impl;

import com.hojoai.central.account.common.DisabledStatus;
import com.hojoai.central.account.common.ResponseStatusExtra;
import com.hojoai.central.account.common.UserNicknameUtil;
import com.hojoai.central.account.dao.user.UserInfoDAO;
import com.hojoai.central.account.dao.user.UserTokenInfoDAO;
import com.hojoai.central.account.dao.user.po.UserInfo;
import com.hojoai.central.account.dao.user.po.UserTokenInfo;
import com.hojoai.central.account.factory.captcha.CaptchaFactoryService;
import com.hojoai.central.account.service.user.UserTokenInfoService;
import com.hojoai.central.account.service.user.bean.UserTokenInfoRequest;
import com.hojoai.central.account.service.user.bean.UserTokenInfoResponse;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.downgoon.snowflake.Snowflake;

import java.util.UUID;

/**
 * The service implements class for user token info.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/30
 * @since hojoai-webmvc-central-account 1.0.0
 */
@Service
public class UserTokenInfoServiceImpl implements UserTokenInfoService {

    @Resource
    private UserInfoDAO userInfoDAO;

    @Resource
    private UserTokenInfoDAO userTokenInfoDAO;

    @Resource
    private CaptchaFactoryService captchaFactoryService;

    @Resource
    private Snowflake snowflake;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public Response<UserTokenInfoResponse> addUserTokenInfoByUserMobileAndMobileCaptcha(String userMobile, String mobileCaptcha, UserTokenInfoRequest request) throws Exception {
        Boolean isValid = captchaFactoryService.verifyMobileCaptcha(userMobile, CaptchaFactoryService.MobileCaptchaType.CREATE_USER_TOKEN.getCode(), mobileCaptcha);
        if (!isValid) {
            return Response.newInstance(ResponseStatusExtra.MOBILE_CAPTCHA_IS_INVALID);
        }

        UserInfo userInfoBeforeAdd = userInfoDAO.selectUserInfoByUserMobile(userMobile);
        if (userInfoBeforeAdd == null || userInfoBeforeAdd.getIncrId() <= 0) {
            String userId = String.valueOf(snowflake.nextId());
            userInfoDAO.insertUserInfoByUserMobile(userId, userMobile, DisabledStatus.NON.getCode());
            userInfoDAO.updateUserNicknameByUserId(userId, UserNicknameUtil.getRandomUserNickname());
        }

        UserInfo userInfo = userInfoDAO.selectUserInfoByUserMobile(userMobile);

        String userToken = makeUserToken(userInfo.getUserId(), request.getBusinessType(), request.getPlatformType());

        UserTokenInfo userTokenInfoBeforeAdd = userTokenInfoDAO.selectUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userInfo.getUserId(), request.getBusinessType(), request.getPlatformType());
        if (userTokenInfoBeforeAdd == null || userTokenInfoBeforeAdd.getIncrId() <= 0) {
            userTokenInfoDAO.insertUserTokenInfo(userInfo.getUserId(), request.getBusinessType(), request.getPlatformType(), userToken);
        } else {
            userTokenInfoDAO.updateUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userInfo.getUserId(), request.getBusinessType(), request.getPlatformType(), userToken);
        }

        UserTokenInfo userTokenInfo = userTokenInfoDAO.selectUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userInfo.getUserId(), request.getBusinessType(), request.getPlatformType());
        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userTokenInfo));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserId(String userId) throws Exception {
        userTokenInfoDAO.deleteUserTokenInfoByUserId(userId);
        return Response.newInstance(ResponseStatusBasic.OK);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public Response<UserTokenInfoResponse> deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(String userId, Integer businessType, Integer platformType) throws Exception {
        userTokenInfoDAO.deleteUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userId, businessType, platformType);
        return Response.newInstance(ResponseStatusBasic.OK);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = true, rollbackFor = {Exception.class})
    public Response<UserTokenInfoResponse> queryUserTokenInfoByUserIdAndBusinessTypeAndPlatformTypeAndUserToken(String userId, Integer businessType, Integer platformType, String userToken) throws Exception {
        UserTokenInfo userTokenInfo = userTokenInfoDAO.selectUserTokenInfoByUserIdAndBusinessTypeAndPlatformType(userId, businessType, platformType);
        if (userTokenInfo == null || userTokenInfo.getIncrId() == null || userTokenInfo.getIncrId() <= 0L) {
            return Response.newInstance(ResponseStatusExtra.ILLEGAL_USER_TOKEN);
        }
        if (!userToken.equals(userTokenInfo.getUserToken())) {
            return Response.newInstance(ResponseStatusExtra.ILLEGAL_USER_TOKEN);
        }
        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userTokenInfo));
    }

    private String makeUserToken(String userId, int businessType, int platformType) {
        return DigestUtils.sha256Hex(userId + businessType + platformType + UUID.randomUUID()).toUpperCase();
    }

    private UserTokenInfoResponse makeResponse(UserTokenInfo userTokenInfo) {
        UserTokenInfoResponse response = new UserTokenInfoResponse();

        if (userTokenInfo == null || userTokenInfo.getIncrId() <= 0) {
            return response;
        }

        response.setUserId(userTokenInfo.getUserId());
        response.setBusinessType(userTokenInfo.getBusinessType());
        response.setPlatformType(userTokenInfo.getPlatformType());
        response.setUserToken(userTokenInfo.getUserToken());
        return response;
    }
}
