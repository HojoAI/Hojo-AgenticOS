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
package com.hojoai.omniasst.management.dao.voice.impl;

import com.hojoai.omniasst.management.dao.voice.UserVoiceProfileDAO;
import com.hojoai.omniasst.management.dao.voice.po.UserVoiceProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户音色档案表访问实现（PostgreSQL）。
 */
@Repository
public class UserVoiceProfileDAOImpl implements UserVoiceProfileDAO {

    @Autowired(required = false)
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    @Override
    public UserVoiceProfile selectByUserId(String userId) {
        if (postgresqlJdbcTemplate == null) {
            throw new RuntimeException("PostgreSQL JdbcTemplate not available");
        }
        String sql = "SELECT id, user_id, voice_id, voice_name, nickname, provider, created_at, updated_at " +
                     "FROM user_voice_profiles WHERE user_id = ? LIMIT 1";
        try {
            List<UserVoiceProfile> results = postgresqlJdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(UserVoiceProfile.class),
                userId
            );
            return results.isEmpty() ? null : results.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public UserVoiceProfile selectByUserIdAndProvider(String userId, String provider) {
        if (postgresqlJdbcTemplate == null) {
            throw new RuntimeException("PostgreSQL JdbcTemplate not available");
        }
        String sql = "SELECT id, user_id, voice_id, voice_name, nickname, provider, created_at, updated_at " +
                     "FROM user_voice_profiles WHERE user_id = ? AND provider = ? LIMIT 1";
        try {
            List<UserVoiceProfile> results = postgresqlJdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(UserVoiceProfile.class),
                userId,
                provider
            );
            return results.isEmpty() ? null : results.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int insert(UserVoiceProfile profile) {
        if (postgresqlJdbcTemplate == null) {
            throw new RuntimeException("PostgreSQL JdbcTemplate not available");
        }
        String sql = "INSERT INTO user_voice_profiles (user_id, voice_id, voice_name, nickname, provider, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return postgresqlJdbcTemplate.update(
            sql,
            profile.getUserId(),
            profile.getVoiceId(),
            profile.getVoiceName(),
            profile.getNickname(),
            profile.getProvider(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }

    @Override
    public int deleteByUserId(String userId, String provider) {
        if (postgresqlJdbcTemplate == null) {
            throw new RuntimeException("PostgreSQL JdbcTemplate not available");
        }
        String sql = "DELETE FROM user_voice_profiles WHERE user_id = ? AND provider = ?";
        return postgresqlJdbcTemplate.update(sql, userId, provider);
    }
}
