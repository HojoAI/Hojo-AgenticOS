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
import com.hojoai.central.account.dao.auth.OverseaAuthIdentityDAO;
import com.hojoai.central.account.dao.auth.OverseaAuthIdentityMapper;
import com.hojoai.central.account.dao.auth.po.OverseaAuthIdentity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class OverseaAuthIdentityDAOImpl implements OverseaAuthIdentityDAO {

    @Resource
    private OverseaAuthIdentityMapper overseaAuthIdentityMapper;

    @Override
    public void insertIdentity(OverseaAuthIdentity identity) {
        overseaAuthIdentityMapper.insert(identity);
    }

    @Override
    public OverseaAuthIdentity selectByProviderAndProviderUserId(String provider, String providerUserId) {
        LambdaQueryWrapper<OverseaAuthIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OverseaAuthIdentity::getProvider, provider)
                .eq(OverseaAuthIdentity::getProviderUserId, providerUserId);
        return overseaAuthIdentityMapper.selectOne(wrapper);
    }

    @Override
    public void deleteByUserId(Long userId) {
        LambdaQueryWrapper<OverseaAuthIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OverseaAuthIdentity::getUserId, userId);
        overseaAuthIdentityMapper.delete(wrapper);
    }
}
