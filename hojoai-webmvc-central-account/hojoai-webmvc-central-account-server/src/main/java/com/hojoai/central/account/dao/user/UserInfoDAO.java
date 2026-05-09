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
package com.hojoai.central.account.dao.user;

import com.hojoai.central.account.dao.user.po.UserInfo;

public interface UserInfoDAO {

    void insertUserInfoByUserMobile(String userId, String userMobile, Integer disabledStatus);

    void updateUserMobileByUserId(String userId, String userMobile);

    void updateUserNicknameByUserId(String userId, String userNickname);

    void updateUserDescriptionByUserId(String userId, String userDescription);

    void updateUserHeadImageByUserId(String userId, String userHeadImage);

    void updateDisabledStatusByUserId(String userId, Integer disabledStatus);

    UserInfo selectUserInfoByUserId(String userId);

    UserInfo selectUserInfoByUserMobile(String userMobile);
}
