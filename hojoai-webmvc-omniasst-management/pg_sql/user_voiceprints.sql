-- 用户声纹表
CREATE TABLE user_voiceprints (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    user_name VARCHAR(200) NOT NULL,
    avatar VARCHAR(500),
    voiceprint VECTOR(192) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_user_voiceprints_user_id ON user_voiceprints(user_id);

-- 创建向量索引（提升相似度搜索性能）
CREATE INDEX idx_user_voiceprints_voiceprint_ivfflat 
ON user_voiceprints 
USING ivfflat (voiceprint vector_cosine_ops)
WITH (lists = 100);

-- 表注释
COMMENT ON TABLE user_voiceprints IS '用户声纹表，存储用户的声纹向量';
COMMENT ON COLUMN user_voiceprints.user_id IS '用户ID';
COMMENT ON COLUMN user_voiceprints.user_name IS '用户名';
COMMENT ON COLUMN user_voiceprints.avatar IS '用户头像URL';
COMMENT ON COLUMN user_voiceprints.voiceprint IS '声纹向量（192维）';

