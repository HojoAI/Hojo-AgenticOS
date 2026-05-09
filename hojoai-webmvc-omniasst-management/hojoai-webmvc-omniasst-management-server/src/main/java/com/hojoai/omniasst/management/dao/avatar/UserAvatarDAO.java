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
package com.hojoai.omniasst.management.dao.avatar;

import com.hojoai.omniasst.management.dao.avatar.po.UserAvatar;


public interface UserAvatarDAO {

    /**
     * Insert or update user avatar association
     */
    void insertOrUpdateUserAvatar(UserAvatar userAvatar);

    void insertOrUpdateUserAvatar(String userId, String avatarId);

    /**
     * Select user avatar association by userId
     */
    UserAvatar selectUserAvatarByUserId(String userId);
}