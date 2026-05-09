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
package com.hojoai.omniasst.management.service.voiceprint.impl;

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.service.voice.bean.UserMatchResult;
import com.hojoai.omniasst.management.service.voice.bean.UserVoicePrintProfileResponse;
import com.hojoai.omniasst.management.service.voice.bean.VoicePrintPageResult;
import com.hojoai.omniasst.management.service.voiceprint.VoicePrintVectorService;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Stream;

/**
 * 声纹向量相似度检索与档案持久化（PostgreSQL）。
 */
@Service
public class VoicePrintVectorServiceImpl implements VoicePrintVectorService {
    private static final Logger logger = LoggerFactory.getLogger(VoicePrintVectorServiceImpl.class);

    @Autowired(required = false)
    @Qualifier("postgresqlJdbcTemplate")
    private JdbcTemplate postgresqlJdbcTemplate;

    @Value("${voiceprint.similarity.threshold:0.50}")
    private double defaultThreshold;

    @Override
    public UserMatchResult matchUserVoicePrint(double[] voiceprint, Double threshold) {
        if (voiceprint == null || voiceprint.length == 0) {
            logger.info("[数据库声纹匹配], Voiceprint is empty");
            return null;
        }
        logger.info("[数据库声纹匹配], 声纹向量: {}, 匹配阈值: {}", voiceprint, threshold);
        double similarityThreshold = Objects.nonNull(threshold) ? threshold : defaultThreshold;
        if (Objects.isNull(postgresqlJdbcTemplate)) {
            logger.info("[数据库声纹匹配], PostgreSQL JdbcTemplate not available, cannot match user voiceprint");
            return null;
        }
        try {
            String sql = "SELECT user_id, user_name, avatar, voiceprint::text FROM user_voiceprints";
            List<Map<String, Object>> rows = postgresqlJdbcTemplate.queryForList(sql);
            if (CollectionUtils.isEmpty(rows)) {
                logger.info("[数据库声纹匹配], 数据库中没有声纹数据");
                return null;
            }
            String bestMatchUserId = null;
            String bestMatchUserName = null;
            String bestMatchAvatar = null;
            double bestSimilarity = -1.0;
            for (Map<String, Object> row : rows) {
                String userId = (String) row.get("user_id");
                String userName = (String) row.get("user_name");
                String avatar = row.get("avatar") != null ? (String) row.get("avatar") : "";
                String vectorText = (String) row.get("voiceprint");
                if (Objects.isNull(vectorText) || vectorText.isEmpty()) {
                    continue;
                }
                try {
                    double[] dbVector = parseVectorString(vectorText);
                    double similarity = calculateCosineSimilarity(voiceprint, dbVector);
                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        bestMatchUserId = userId;
                        bestMatchUserName = userName;
                        bestMatchAvatar = avatar;
                    }
                } catch (Exception e) {
                    logger.warn("[数据库声纹匹配], 解析用户 {} 的声纹向量失败: {}", userId, e.getMessage());
                }
            }
            if (Objects.nonNull(bestMatchUserId) && bestSimilarity >= similarityThreshold) {
                logger.info("[数据库声纹匹配], 匹配到用户: {}, 相似度: {}", bestMatchUserName, bestSimilarity);
                return new UserMatchResult(bestMatchUserId, bestMatchUserName, bestMatchAvatar);
            } else {
                logger.info("[数据库声纹匹配], Similarity {} below threshold {}, no match", bestSimilarity, similarityThreshold);
                return null;
            }
        } catch (Exception e) {
            logger.info("[数据库声纹匹配]报错, 报错信息: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void saveUserVoicePrint(String userId, String userName, double[] voiceprint) {
        if (voiceprint == null || voiceprint.length == 0) {
            logger.warn("Voiceprint is empty, cannot save, userId: {}, userName: {}", userId, userName);
            return;
        }
        if (voiceprint.length != 192) {
            logger.warn("Voiceprint dimension mismatch: expected 192, got {}, userId: {}, userName: {}", voiceprint.length, userId, userName);
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
            runVoiceprintUpsert(sql, userId, userName, vectorStr, false);
            logger.info("User voiceprint saved, userId: {}, userName: {}, vectorDim: {}", userId, userName, voiceprint.length);
        } catch (Exception e) {
            logger.error("Error saving user voiceprint, userId: {}, userName: {}", userId, userName, e);
            throw new RuntimeException("Failed to save user voiceprint", e);
        }
    }

    /**
     * 插入新用户时 id 由序列生成；若序列落后于表中已有 MAX(id)，会报 user_voiceprints_pkey 冲突。
     * 此时将序列拨到 MAX(id) 后重试一次（仍建议 DBA 在库侧执行规范修复）。
     */
    private void runVoiceprintUpsert(String sql, String userId, String userName, String vectorStr, boolean alreadyRetried) {
        try {
            postgresqlJdbcTemplate.update(sql, userId, userName, vectorStr, userName, vectorStr);
        } catch (DataIntegrityViolationException e) {
            if (!alreadyRetried && isUserVoiceprintsPkeyDuplicate(e)) {
                logger.warn("user_voiceprints id sequence likely out of sync, attempting repair and retry once, userId: {}", userId);
                repairUserVoiceprintsIdSequence();
                runVoiceprintUpsert(sql, userId, userName, vectorStr, true);
                return;
            }
            throw e;
        }
    }

    private static boolean isUserVoiceprintsPkeyDuplicate(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && msg.contains("user_voiceprints_pkey")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private void repairUserVoiceprintsIdSequence() {
        String plpgsql = """
                DO $$
                DECLARE
                    seq_text text;
                    mx bigint;
                BEGIN
                    SELECT COALESCE(MAX(id), 0) INTO mx FROM public.user_voiceprints;
                    seq_text := pg_get_serial_sequence('public.user_voiceprints', 'id');
                    IF seq_text IS NULL THEN
                        SELECT quote_ident(schemaname) || '.' || quote_ident(sequencename)
                        INTO seq_text
                        FROM pg_sequences
                        WHERE sequencename = 'user_voiceprints_id_seq'
                        LIMIT 1;
                    END IF;
                    IF seq_text IS NOT NULL THEN
                        PERFORM setval(seq_text::regclass, mx);
                    END IF;
                END $$;
                """;
        postgresqlJdbcTemplate.execute(plpgsql);
    }

    @Override
    public VoicePrintPageResult queryUserVoicePrints(String userName, int page, int pageSize) {
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available");
            return new VoicePrintPageResult(List.of(), 0L, page, pageSize);
        }
        try {
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
            String countSql = "SELECT COUNT(*) FROM user_voiceprints " + whereClause;
            Long total = postgresqlJdbcTemplate.queryForObject(countSql, Long.class, countParams);
            if (total == null) total = 0L;
            String querySql = "SELECT user_id, user_name, created_at, updated_at FROM user_voiceprints " + whereClause + " ORDER BY updated_at DESC OFFSET ? LIMIT ?";
            List<Map<String, Object>> rows = postgresqlJdbcTemplate.queryForList(querySql, queryParams);
            List<VoicePrintPageResult.VoicePrintUserInfo> list = rows.stream().map(row -> {
                String userId = (String) row.get("user_id");
                String name = (String) row.get("user_name");
                Object createdAt = row.get("created_at");
                Object updatedAt = row.get("updated_at");
                return new VoicePrintPageResult.VoicePrintUserInfo(userId, name, createdAt != null ? createdAt.toString() : null, updatedAt != null ? updatedAt.toString() : null);
            }).toList();
            return new VoicePrintPageResult(list, total, page, pageSize);
        } catch (Exception e) {
            logger.error("Error querying user voiceprints", e);
            return new VoicePrintPageResult(List.of(), 0L, page, pageSize);
        }
    }

    @Override
    public int deleteUserVoicePrint(String userId) {
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

    @Override
    public int updateUserName(String userId, String userName) {
        if (postgresqlJdbcTemplate == null) {
            logger.warn("PostgreSQL JdbcTemplate not available, cannot update voiceprint display name");
            return 0;
        }
        String sql = "UPDATE user_voiceprints SET user_name = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try {
            int rows = postgresqlJdbcTemplate.update(sql, userName, userId);
            logger.info("User voiceprint display name updated, userId: {}, rows: {}", userId, rows);
            return rows;
        } catch (Exception e) {
            logger.error("Error updating voiceprint user_name, userId: {}", userId, e);
            throw new RuntimeException("Failed to update voiceprint display name", e);
        }
    }

    @Override
    public String getUserNameByUserId(String userId) {
        if (postgresqlJdbcTemplate == null) {
            return null;
        }
        String sql = "SELECT user_name FROM user_voiceprints WHERE user_id = ?";
        try {
            return postgresqlJdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UserVoicePrintProfileResponse getUserVoicePrintProfile(String userId) {
        if (postgresqlJdbcTemplate == null) {
            return null;
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        String sql = "SELECT id, user_id, user_name, created_at, updated_at FROM user_voiceprints WHERE user_id = ? LIMIT 1";
        try {
            List<Map<String, Object>> rows = postgresqlJdbcTemplate.queryForList(sql, userId);
            if (CollectionUtils.isEmpty(rows)) {
                return null;
            }
            Map<String, Object> row = rows.get(0);
            UserVoicePrintProfileResponse res = new UserVoicePrintProfileResponse();
            Object idObj = row.get("id");
            if (idObj instanceof Number) {
                res.setId(((Number) idObj).longValue());
            } else if (idObj != null) {
                try {
                    res.setId(Long.parseLong(idObj.toString()));
                } catch (Exception ignored) {
                }
            }
            res.setUserId(row.get("user_id") != null ? row.get("user_id").toString() : null);
            res.setUserName(row.get("user_name") != null ? row.get("user_name").toString() : null);
            Object createdAt = row.get("created_at");
            Object updatedAt = row.get("updated_at");
            if (createdAt instanceof Date) {
                res.setCreatedAt((Date) createdAt);
            }
            if (updatedAt instanceof Date) {
                res.setUpdatedAt((Date) updatedAt);
            }
            return res;
        } catch (Exception e) {
            logger.error("Error querying user voiceprint profile, userId: {}", userId, e);
            throw new RuntimeException("查询失败，请稍后重试");
        }
    }

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

    private double[] parseVectorString(String vectorText) {
        if (vectorText == null || vectorText.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector text cannot be null or empty");
        }
        String trimmed = vectorText.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Vector text must be in format [1.0,2.0,3.0]");
        }
        String content = trimmed.substring(1, trimmed.length() - 1);
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Vector text cannot be empty");
        }
        String[] parts = content.split(",");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i].trim());
        }
        return vector;
    }

    private double calculateCosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have same length");
        }
        RealVector v1 = new ArrayRealVector(vector1);
        RealVector v2 = new ArrayRealVector(vector2);
        return v1.cosine(v2);
    }
}
