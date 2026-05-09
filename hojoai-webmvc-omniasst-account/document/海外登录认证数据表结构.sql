-- ============================================================
-- 海外 App 登录认证 - 建表语句
-- 服务：hojoai-webmvc-omniasst-account
-- ============================================================

-- 海外用户表
CREATE TABLE oversea_user_info (
    id BIGINT PRIMARY KEY COMMENT '雪花算法 ID',
    email VARCHAR(255) COMMENT '主邮箱',
    nickname VARCHAR(100) COMMENT '用户昵称',
    avatar VARCHAR(500) COMMENT '用户头像 URL',
    status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT 'active / disabled / deleted',
    token_version INT NOT NULL DEFAULT 0 COMMENT 'Token 版本号，递增后使该用户所有已签发 Token 失效',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL COMMENT '软删除时间',
    UNIQUE KEY uk_email (email)
) COMMENT '海外用户表';

-- 海外用户认证身份表
CREATE TABLE oversea_auth_identity (
    id BIGINT PRIMARY KEY COMMENT '雪花算法 ID',
    user_id BIGINT NOT NULL COMMENT '关联 oversea_user_info.id',
    provider VARCHAR(20) NOT NULL COMMENT 'google / apple / email',
    provider_user_id VARCHAR(255) NOT NULL COMMENT '第三方用户标识（Google sub / Apple sub / email 地址）',
    email VARCHAR(255) COMMENT '该身份关联的邮箱',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_uid (provider, provider_user_id),
    KEY idx_user_id (user_id),
    KEY idx_email (email)
) COMMENT '海外用户认证身份表';
