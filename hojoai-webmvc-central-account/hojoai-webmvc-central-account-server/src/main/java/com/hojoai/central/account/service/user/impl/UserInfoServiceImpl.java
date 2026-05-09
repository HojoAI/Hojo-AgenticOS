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
import com.hojoai.central.account.common.RegexUtil;
import com.hojoai.central.account.common.ResponseStatusExtra;
import com.hojoai.central.account.common.UserNicknameUtil;
import com.hojoai.central.account.dao.user.UserInfoDAO;
import com.hojoai.central.account.dao.user.po.UserInfo;
import com.hojoai.central.account.factory.captcha.CaptchaFactoryService;
import com.hojoai.central.account.service.user.UserInfoService;
import com.hojoai.central.account.service.user.bean.UserInfoRequest;
import com.hojoai.central.account.service.user.bean.UserInfoResponse;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.downgoon.snowflake.Snowflake;

import java.util.regex.Pattern;

/**
 * The service implements class for user info.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/29
 * @since hojoai-webmvc-central-account 1.0.0
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private Snowflake snowflake;

    @Resource
    private UserInfoDAO userInfoDAO;

    @Resource
    private CaptchaFactoryService captchaFactoryService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public Response<UserInfoResponse> addUserInfoByUserMobile(String mobileCaptcha, UserInfoRequest request) throws Exception {
        if (!Pattern.matches(RegexUtil.MOBILE, request.getUserMobile())) {
            return Response.newInstance(ResponseStatusExtra.ILLEGAL_USER_MOBILE);
        }

        Boolean isValid = captchaFactoryService.verifyMobileCaptcha(request.getUserMobile(), CaptchaFactoryService.MobileCaptchaType.CREATE_MOBILE_USER.getCode(), mobileCaptcha);
        if (!isValid) {
            return Response.newInstance(ResponseStatusExtra.MOBILE_CAPTCHA_IS_INVALID);
        }

        UserInfo userInfoBeforeAdd = userInfoDAO.selectUserInfoByUserMobile(request.getUserMobile());
        if (userInfoBeforeAdd != null && userInfoBeforeAdd.getIncrId() > 0) {
            return Response.newInstance(ResponseStatusExtra.USER_INFO_ALREADY_REGISTERED);
        }

        String userId = String.valueOf(snowflake.nextId());
        userInfoDAO.insertUserInfoByUserMobile(userId, request.getUserMobile(), DisabledStatus.NON.getCode());
        userInfoDAO.updateUserNicknameByUserId(userId, UserNicknameUtil.getRandomUserNickname());

        UserInfo userInfo = userInfoDAO.selectUserInfoByUserMobile(request.getUserMobile());
        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userInfo));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public Response<UserInfoResponse> updateUserInfoByUserId(String userId, String patchType, String newMobileCaptcha, String oldMobileCaptcha, UserInfoRequest request) throws Exception {
        UserInfo userInfoBeforeUpdate = userInfoDAO.selectUserInfoByUserId(userId);
        if (userInfoBeforeUpdate == null || userInfoBeforeUpdate.getIncrId() <= 0) {
            return Response.newInstance(ResponseStatusExtra.USER_INFO_NOT_REGISTERED);
        }

        if (UserInfoService.PatchType.UPDATE_BINDING_USER_MOBILE.getCode().equals(patchType)) {
            Boolean isValidForOldMoblie = captchaFactoryService.verifyMobileCaptcha(userInfoBeforeUpdate.getUserMobile(), CaptchaFactoryService.MobileCaptchaType.CREATE_MOBILE_USER.getCode(), oldMobileCaptcha);
            if (!isValidForOldMoblie) {
                return Response.newInstance(ResponseStatusExtra.OLD_MOBILE_CAPTCHA_IS_INVALID);
            }
            Boolean isValidForNewMoblie = captchaFactoryService.verifyMobileCaptcha(request.getUserMobile(), CaptchaFactoryService.MobileCaptchaType.CREATE_MOBILE_USER.getCode(), newMobileCaptcha);
            if (!isValidForNewMoblie) {
                return Response.newInstance(ResponseStatusExtra.NEW_MOBILE_CAPTCHA_IS_INVALID);
            }
            UserInfo userInfoByUserMobile = userInfoDAO.selectUserInfoByUserMobile(request.getUserMobile());
            if (userInfoByUserMobile != null && userInfoByUserMobile.getIncrId() > 0) {
                return Response.newInstance(ResponseStatusExtra.USER_MOBILE_ALREADY_REGISTERED);
            }
            userInfoDAO.updateUserMobileByUserId(userId, request.getUserMobile());
        }

        if (PatchType.UPDATE_USER_NICKNAME.getCode().equals(patchType)) {
            userInfoDAO.updateUserNicknameByUserId(userId, request.getUserNickname());
        }

        if (PatchType.UPDATE_USER_DESCRIPTION.getCode().equals(patchType)) {
            userInfoDAO.updateUserDescriptionByUserId(userId, request.getUserDescription());
        }

        if (PatchType.UPDATE_USER_HEAD_IMAGE.getCode().equals(patchType)) {
            userInfoDAO.updateUserHeadImageByUserId(userId, request.getUserHeadImage());
        }

        UserInfo userInfo = userInfoDAO.selectUserInfoByUserId(userId);
        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userInfo));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = true, rollbackFor = {Exception.class})
    public Response<UserInfoResponse> queryUserInfoByUserId(String userId) throws Exception {
        UserInfo userInfo = userInfoDAO.selectUserInfoByUserId(userId);
        if (userInfo == null || userInfo.getIncrId() <= 0) {
            return Response.newInstance(ResponseStatusExtra.USER_INFO_NOT_REGISTERED);
        }

        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userInfo));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = true, rollbackFor = {Exception.class})
    public Response<UserInfoResponse> queryUserInfoByUserMobile(String userMobile) throws Exception {
        UserInfo userInfo = userInfoDAO.selectUserInfoByUserMobile(userMobile);
        if (userInfo == null || userInfo.getIncrId() <= 0) {
            return Response.newInstance(ResponseStatusExtra.USER_INFO_NOT_REGISTERED);
        }

        return Response.newInstance(ResponseStatusBasic.OK, makeResponse(userInfo));
    }

    private UserInfoResponse makeResponse(UserInfo userInfo) {
        UserInfoResponse response = new UserInfoResponse();

        if (userInfo == null || userInfo.getIncrId() <= 0) {
            return response;
        }

        response.setUserId(userInfo.getUserId());
        response.setUserMobile(userInfo.getUserMobile());
        response.setUserNickname(userInfo.getUserNickname());
        response.setUserDescription(userInfo.getUserDescription());
        response.setUserHeadImage(userInfo.getUserHeadImage());
        response.setDisabledStatus(userInfo.getDisabledStatus());
        return response;
    }
}
