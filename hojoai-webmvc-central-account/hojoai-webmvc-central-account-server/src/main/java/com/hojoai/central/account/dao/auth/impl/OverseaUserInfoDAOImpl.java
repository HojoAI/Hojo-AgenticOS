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
package com.hojoai.central.account.dao.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.central.account.dao.auth.OverseaUserInfoDAO;
import com.hojoai.central.account.dao.auth.OverseaUserInfoMapper;
import com.hojoai.central.account.dao.auth.po.OverseaUserInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class OverseaUserInfoDAOImpl implements OverseaUserInfoDAO {

    private static final String STATUS_ACTIVE = "active";

    @Resource
    private OverseaUserInfoMapper overseaUserInfoMapper;

    @Override
    public void insertUser(OverseaUserInfo user) {
        overseaUserInfoMapper.insert(user);
    }

    @Override
    public OverseaUserInfo selectById(Long id) {
        return overseaUserInfoMapper.selectById(id);
    }

    @Override
    public OverseaUserInfo selectByEmail(String email) {
        LambdaQueryWrapper<OverseaUserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OverseaUserInfo::getEmail, email)
                .eq(OverseaUserInfo::getStatus, STATUS_ACTIVE);
        return overseaUserInfoMapper.selectOne(wrapper);
    }

    @Override
    public void updateNicknameAndAvatar(Long id, String nickname, String avatar) {
        LambdaUpdateWrapper<OverseaUserInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OverseaUserInfo::getId, id);
        if (nickname != null) {
            wrapper.set(OverseaUserInfo::getNickname, nickname);
        }
        if (avatar != null) {
            wrapper.set(OverseaUserInfo::getAvatar, avatar);
        }
        overseaUserInfoMapper.update(wrapper);
    }

    @Override
    public void softDelete(Long id) {
        String tombstoneEmail = "deleted+" + id + "@void.hojoai";
        LambdaUpdateWrapper<OverseaUserInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OverseaUserInfo::getId, id)
                .set(OverseaUserInfo::getStatus, "deleted")
                .set(OverseaUserInfo::getEmail, tombstoneEmail)
                .set(OverseaUserInfo::getDeletedAt, new Date());
        overseaUserInfoMapper.update(wrapper);
    }
}
