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
package com.hojoai.gateway.omniasst.voice.service.voiceprint;

import com.hojoai.gateway.omniasst.voice.service.voiceprint.dto.VoicePrintPageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基于 PostgreSQL {@code pgvector} 的声纹向量存储与检索；依赖可选注入的 {@code postgresqlJdbcTemplate}。
 */
@Service
public class VoicePrintVectorService {
    private static final Logger logger = LoggerFactory.getLogger(VoicePrintVectorService.class);
    
    @Autowired(required = false)
    private JdbcTemplate postgresqlJdbcTemplate;
    
    @Value("${voiceprint.similarity.threshold:0.85}")
    private double defaultThreshold;
    
    /**
     * 全局匹配用户声纹
     * 
     * @param voiceprint 声纹向量（192维）
     * @param threshold 相似度阈值（0-1之间，推荐0.85）
     * @return 匹配结果，包含userId和userName，如果未匹配则返回null
     */
    public UserMatchResult matchUserVoicePrint(double[] voiceprint, Double threshold) {
        if (voiceprint == null || voiceprint.length == 0) {
            logger.warn("Voiceprint is empty");
            return null;
        }
        
        double similarityThreshold = threshold != null ? threshold : defaultThreshold;
        String vectorStr = arrayToVectorString(voiceprint);
        
        String sql = """
            SELECT user_id, user_name, avatar, 1 - (voiceprint <=> ?::vector) as similarity
            FROM user_voiceprints
            ORDER BY voiceprint <=> ?::vector
            LIMIT 1
            """;
        
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available, cannot match user voiceprint");
            return null;
        }
        try {
            Map<String, Object> result = postgresqlJdbcTemplate.queryForMap(sql, vectorStr, vectorStr);
            Double similarity = ((Number) result.get("similarity")).doubleValue();
            String userId = (String) result.get("user_id");
            String userName = (String) result.get("user_name");
            String avatar = result.get("avatar") != null ? (String) result.get("avatar") : "";
            
            logger.debug("User voiceprint match result, userId: {}, userName: {}, similarity: {}", userId, userName, similarity);
            
            if (similarity != null && similarity >= similarityThreshold) {
                return new UserMatchResult(userId, userName, avatar);
            } else {
                logger.debug("Similarity {} below threshold {}, no match", similarity, similarityThreshold);
                return null;
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            logger.debug("No users found in voiceprint database");
            return null;
        } catch (Exception e) {
            logger.error("Error matching user voiceprint", e);
            return null;
        }
    }
    
    /**
     * 保存或更新用户声纹（每个用户只保留一个最新声纹）
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param voiceprint 声纹向量（192维）
     */
    public void saveUserVoiceprint(String userId, String userName, double[] voiceprint) {
        if (voiceprint == null || voiceprint.length == 0) {
            logger.warn("Voiceprint is empty, cannot save, userId: {}, userName: {}", userId, userName);
            return;
        }
        
        if (voiceprint.length != 192) {
            logger.warn("Voiceprint dimension mismatch: expected 192, got {}, userId: {}, userName: {}", 
                voiceprint.length, userId, userName);
        }
        
        String vectorStr = arrayToVectorString(voiceprint);
        
        String sql = """
            INSERT INTO user_voiceprints (user_id, user_name, voiceprint, created_at, updated_at)
            VALUES (?, ?, ?::vector(192), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id) 
            DO UPDATE SET 
                user_name = ?,
                voiceprint = ?::vector(192),
                updated_at = CURRENT_TIMESTAMP
            """;
        
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available, cannot save user voiceprint");
            return;
        }
        try {
            postgresqlJdbcTemplate.update(sql, userId, userName, vectorStr, userName, vectorStr);
            logger.info("User voiceprint saved, userId: {}, userName: {}, vectorDim: {}", userId, userName, voiceprint.length);
        } catch (Exception e) {
            logger.error("Error saving user voiceprint, userId: {}, userName: {}", userId, userName, e);
            throw new RuntimeException("Failed to save user voiceprint", e);
        }
    }
    
    /**
     * 分页查询用户声纹列表（支持用户名模糊查询）
     * 
     * @param userName 用户名（模糊查询，可为null）
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 查询结果
     */
    public VoicePrintPageResult queryUserVoiceprints(String userName, int page, int pageSize) {
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available");
            return new VoicePrintPageResult(List.of(), 0, page, pageSize);
        }
        
        try {
            // 构建查询条件
            String whereClause = "";
            Object[] countParams;
            Object[] queryParams;
            
            if (userName != null && !userName.trim().isEmpty()) {
                whereClause = "WHERE user_name LIKE ?";
                String likePattern = "%" + userName.trim() + "%";
                countParams = new Object[]{likePattern};
                queryParams = new Object[]{likePattern, (page - 1) * pageSize, pageSize};
            } else {
                countParams = new Object[]{};
                queryParams = new Object[]{(page - 1) * pageSize, pageSize};
            }
            
            // 查询总数
            String countSql = "SELECT COUNT(*) FROM user_voiceprints " + whereClause;
            Long total = postgresqlJdbcTemplate.queryForObject(countSql, Long.class, countParams);
            if (total == null) total = 0L;
            
            // 查询列表（PostgreSQL中OFFSET在前，LIMIT在后）
            String querySql = "SELECT user_id, user_name, created_at, updated_at FROM user_voiceprints " + 
                whereClause + " ORDER BY updated_at DESC OFFSET ? LIMIT ?";
            
            List<Map<String, Object>> rows = postgresqlJdbcTemplate.queryForList(querySql, queryParams);
            
            List<VoicePrintPageResult.VoicePrintUserInfo> list = rows.stream()
                .map(row -> {
                    String userId = (String) row.get("user_id");
                    String name = (String) row.get("user_name");
                    Object createdAt = row.get("created_at");
                    Object updatedAt = row.get("updated_at");
                    return new VoicePrintPageResult.VoicePrintUserInfo(userId, name, createdAt != null ? createdAt.toString() : null, updatedAt != null ? updatedAt.toString() : null);
                })
                .toList();
            return new VoicePrintPageResult(list, total, page, pageSize);
        } catch (Exception e) {
            logger.error("Error querying user voiceprints", e);
            return new VoicePrintPageResult(List.of(), 0, page, pageSize);
        }
    }
    
    /**
     * 根据用户ID删除用户声纹
     * 
     * @param userId 用户ID
     * @return 删除的行数
     */
    public int deleteUserVoiceprint(String userId) {
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available, cannot delete user voiceprint");
            return 0;
        }
        
        String sql = "DELETE FROM user_voiceprints WHERE user_id = ?";
        try {
            int rows = postgresqlJdbcTemplate.update(sql, userId);
            logger.info("User voiceprint deleted, userId: {}, rows: {}", userId, rows);
            return rows;
        } catch (Exception e) {
            logger.error("Error deleting user voiceprint, userId: {}", userId, e);
            throw new RuntimeException("Failed to delete user voiceprint", e);
        }
    }

    /** 全局声纹 1:N 匹配命中结果 */
    public static class UserMatchResult {
        private final String userId;
        private final String userName;
        private final String avatar;

        /**
         * @param userId   匹配用户 ID
         * @param userName 用户名
         * @param avatar   头像 URL，可为空串
         */
        public UserMatchResult(String userId, String userName, String avatar) {
            this.userId = userId;
            this.userName = userName;
            this.avatar = avatar;
        }

        /**
         * @return 用户 ID
         */
        public String getUserId() {
            return userId;
        }

        /**
         * @return 用户名
         */
        public String getUserName() {
            return userName;
        }

        /**
         * @return 头像地址
         */
        public String getAvatar() {
            return avatar;
        }
    }
    
    /**
     * 将double数组转换为PostgreSQL vector格式字符串
     * 例如: [0.277, 1.037, 1.280, -0.049] -> "[0.277,1.037,1.280,-0.049]"
     */
    private String arrayToVectorString(double[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}

