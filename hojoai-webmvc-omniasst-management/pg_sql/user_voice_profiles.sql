CREATE TABLE IF NOT EXISTS user_voice_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    voice_id VARCHAR(255) NOT NULL,
    voice_name VARCHAR(255),
    nickname VARCHAR(255),
    provider VARCHAR(50) NOT NULL DEFAULT 'hojo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, voice_id)
);

ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS voice_name VARCHAR(255);
ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);
ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS provider VARCHAR(50);
ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE user_voice_profiles ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);

ALTER TABLE user_voice_profiles ALTER COLUMN provider SET DEFAULT 'hojo';
ALTER TABLE user_voice_profiles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_voice_profiles ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_user_voice_profiles_user_id ON user_voice_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_voice_profiles_voice_id ON user_voice_profiles(voice_id);
CREATE INDEX IF NOT EXISTS idx_user_voice_profiles_provider ON user_voice_profiles(provider);
