# PostgreSQL声纹数据库设计文档

## 一、数据库扩展

### 1.1 安装pgvector扩展

**注意**：需要数据库管理员权限

```sql
-- 连接到目标数据库
\c omniasst

-- 安装pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 验证安装
SELECT * FROM pg_extension WHERE extname = 'vector';
```

**安装结果**：如果安装成功，会看到类似输出：
```
 extname | extversion | nspname | extconfig | extcondition 
---------+------------+---------+-----------+--------------
 vector  | 0.5.1      | public  |           | 
```

---

## 二、表结构设计

### 2.1 声纹表（meeting_voiceprints）

```sql
-- 创建说话人声纹表
CREATE TABLE meeting_voiceprints (
    id BIGSERIAL PRIMARY KEY,
    meeting_id VARCHAR(100) NOT NULL,
    speaker_id VARCHAR(100) NOT NULL,
    voiceprint VECTOR(192) NOT NULL,  -- 192维向量，pgvector类型
    segment_count INTEGER DEFAULT 1,   -- 该说话人的音频段数量
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 唯一约束：同一会议同一说话人只能有一个向量
    CONSTRAINT uk_meeting_speaker UNIQUE(meeting_id, speaker_id)
);

-- 添加表注释
COMMENT ON TABLE meeting_voiceprints IS '会议声纹表，存储每个说话人的声纹向量';
COMMENT ON COLUMN meeting_voiceprints.meeting_id IS '会议ID';
COMMENT ON COLUMN meeting_voiceprints.speaker_id IS '说话人ID';
COMMENT ON COLUMN meeting_voiceprints.voiceprint IS '声纹向量（192维）';
COMMENT ON COLUMN meeting_voiceprints.segment_count IS '该说话人的音频段数量';
```

### 2.2 索引设计

#### 2.2.1 基础索引

```sql
-- 创建meeting_id索引（提升查询性能）
CREATE INDEX idx_meeting_voiceprints_meeting_id ON meeting_voiceprints(meeting_id);

-- 创建speaker_id索引（可选，如果经常按speaker_id查询）
CREATE INDEX idx_meeting_voiceprints_speaker_id ON meeting_voiceprints(speaker_id);
```

#### 2.2.2 向量索引（可选，提升相似度查询性能）

**IVFFlat索引**（推荐，平衡性能和空间）：
```sql
-- 创建IVFFlat索引，用于余弦相似度搜索
CREATE INDEX idx_meeting_voiceprints_voiceprint_ivfflat 
ON meeting_voiceprints 
USING ivfflat (voiceprint vector_cosine_ops)
WITH (lists = 100);  -- lists参数根据数据量调整（建议：向量数量/1000）
```

**HNSW索引**（性能更好但占用空间更大）：
```sql
-- 创建HNSW索引（可选，性能更好但占用空间大）
CREATE INDEX idx_meeting_voiceprints_voiceprint_hnsw 
ON meeting_voiceprints 
USING hnsw (voiceprint vector_cosine_ops)
WITH (m = 16, ef_construction = 64);
```

**索引选择建议**：
- **数据量 < 1000**：可以不创建向量索引，全表扫描足够快
- **数据量 1k - 10k**：使用IVFFlat索引
- **数据量 > 10k**：使用HNSW索引

---

## 三、SQL操作

### 3.1 插入声纹向量

**场景**：保存新的说话人声纹向量

```sql
-- 插入新记录
INSERT INTO meeting_voiceprints (meeting_id, speaker_id, voiceprint, segment_count, created_at, updated_at)
VALUES (
    'meeting-1234567890123456789',
    'speaker-001',
    '[0.277,1.037,1.280,-0.049,...]'::vector(192),  -- 192个数值
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

**注意**：
- `::vector(192)` 是PostgreSQL的类型转换语法
- 向量格式：`[值1,值2,值3,...]`，数值之间用逗号分隔，无空格
- 向量维度必须与表定义一致（192维）

### 3.2 更新声纹向量（如果已存在）

**场景**：同一说话人再次识别，更新向量并增加段计数

```sql
-- 使用ON CONFLICT进行UPSERT操作
INSERT INTO meeting_voiceprints (meeting_id, speaker_id, voiceprint, segment_count, created_at, updated_at)
VALUES (
    'meeting-1234567890123456789',
    'speaker-001',
    '[0.278,1.038,1.281,-0.048,...]'::vector(192),
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (meeting_id, speaker_id) 
DO UPDATE SET 
    voiceprint = EXCLUDED.voiceprint::vector(192),
    updated_at = CURRENT_TIMESTAMP,
    segment_count = meeting_voiceprints.segment_count + 1;
```

### 3.3 查询说话人（向量相似度匹配）

**场景**：根据新的声纹向量，查找最相似的说话人

```sql
-- 余弦相似度查询（最常用）
SELECT 
    speaker_id,
    1 - (voiceprint <=> $1::vector) as similarity  -- <=> 是余弦距离操作符
FROM meeting_voiceprints
WHERE meeting_id = $2
ORDER BY voiceprint <=> $1::vector  -- 按相似度排序
LIMIT 1;

-- 示例（使用具体值）
SELECT 
    speaker_id,
    1 - (voiceprint <=> '[0.277,1.037,1.280,...]'::vector) as similarity
FROM meeting_voiceprints
WHERE meeting_id = 'meeting-1234567890123456789'
ORDER BY voiceprint <=> '[0.277,1.037,1.280,...]'::vector
LIMIT 1;
```

**说明**：
- `<=>` 是pgvector提供的余弦距离操作符
- `1 - (voiceprint <=> ?)` 计算余弦相似度（范围：0-1，1表示完全相同）
- `ORDER BY voiceprint <=> ?` 按距离升序排列（最相似的在最前面）

### 3.4 带阈值过滤的相似度查询

**场景**：只返回相似度超过阈值（如0.85）的说话人

```sql
-- 方式1：在WHERE子句中过滤（推荐，可以利用索引）
SELECT 
    speaker_id,
    1 - (voiceprint <=> $1::vector) as similarity
FROM meeting_voiceprints
WHERE meeting_id = $2
  AND (1 - (voiceprint <=> $1::vector)) >= 0.85  -- 阈值0.85
ORDER BY voiceprint <=> $1::vector
LIMIT 1;

-- 方式2：在应用层过滤（如果阈值可能变化）
SELECT 
    speaker_id,
    1 - (voiceprint <=> $1::vector) as similarity
FROM meeting_voiceprints
WHERE meeting_id = $2
ORDER BY voiceprint <=> $1::vector
LIMIT 1;
-- 然后在Java代码中判断 similarity >= 0.85
```

### 3.5 查询会议的所有说话人

**场景**：获取某个会议中所有的说话人列表

```sql
SELECT 
    speaker_id, 
    segment_count, 
    created_at, 
    updated_at
FROM meeting_voiceprints
WHERE meeting_id = $1
ORDER BY created_at;
```

### 3.6 其他查询操作

```sql
-- 查询某个说话人的声纹向量（一般不直接返回向量，只是验证存在）
SELECT speaker_id, segment_count, created_at, updated_at
FROM meeting_voiceprints
WHERE meeting_id = $1 AND speaker_id = $2;

-- 删除某个会议的声纹数据
DELETE FROM meeting_voiceprints
WHERE meeting_id = $1;

-- 删除某个说话人的声纹数据
DELETE FROM meeting_voiceprints
WHERE meeting_id = $1 AND speaker_id = $2;

-- 统计某个会议的说话人数量
SELECT COUNT(*) as speaker_count
FROM meeting_voiceprints
WHERE meeting_id = $1;
```

---

## 四、Java代码中的操作

### 4.1 保存声纹向量

**方法**：`VoiceprintVectorService.saveVoiceprint()`

```java
public void saveVoiceprint(String meetingId, String speakerId, double[] voiceprint) {
    // 1. 将double[]转换为PostgreSQL vector格式字符串
    String vectorStr = arrayToVectorString(voiceprint);
    // 结果示例: "[0.277,1.037,1.280,-0.049,...]"
    
    // 2. 执行UPSERT操作
    String sql = """
        INSERT INTO meeting_voiceprints (meeting_id, speaker_id, voiceprint, segment_count, created_at, updated_at)
        VALUES (?, ?, ?::vector(192), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (meeting_id, speaker_id) 
        DO UPDATE SET 
            voiceprint = ?::vector(192),
            updated_at = CURRENT_TIMESTAMP,
            segment_count = meeting_voiceprints.segment_count + 1
        """;
    
    jdbcTemplate.update(sql, meetingId, speakerId, vectorStr, vectorStr);
}
```

**向量格式转换**：
```java
private String arrayToVectorString(double[] vector) {
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
```

### 4.2 匹配说话人

**方法**：`VoiceprintVectorService.matchSpeaker()`

```java
public String matchSpeaker(String meetingId, double[] voiceprint, Double threshold) {
    // 1. 转换向量格式
    String vectorStr = arrayToVectorString(voiceprint);
    
    // 2. 查询最相似的说话人
    String sql = """
        SELECT 
            speaker_id,
            1 - (voiceprint <=> ?::vector) as similarity
        FROM meeting_voiceprints
        WHERE meeting_id = ?
        ORDER BY voiceprint <=> ?::vector
        LIMIT 1
        """;
    
    Map<String, Object> result = jdbcTemplate.queryForMap(sql, 
        vectorStr, meetingId, vectorStr);
    
    // 3. 判断相似度是否超过阈值
    Double similarity = ((Number) result.get("similarity")).doubleValue();
    if (similarity >= threshold) {
        return (String) result.get("speaker_id");
    }
    return null;
}
```

### 4.3 获取所有说话人

**方法**：`VoiceprintVectorService.getAllSpeakers()`

```java
public List<Map<String, Object>> getAllSpeakers(String meetingId) {
    String sql = """
        SELECT speaker_id, segment_count, created_at, updated_at
        FROM meeting_voiceprints
        WHERE meeting_id = ?
        ORDER BY created_at
        """;
    
    return jdbcTemplate.queryForList(sql, meetingId);
}
```

---

## 五、向量格式说明

### 5.1 向量维度

- **维度**：192维
- **类型**：`VECTOR(192)`
- **来源**：声纹识别API返回的向量数组

### 5.2 向量格式

**输入格式**（Java代码）：
```java
double[] voiceprint = {
    0.27790358662605286, 
    1.0373380184173584, 
    1.2803142070770264, 
    -0.04967552423477173,
    // ... 共192个double值
};
```

**字符串格式**（PostgreSQL）：
```
[0.27790358662605286,1.0373380184173584,1.2803142070770264,-0.04967552423477173,...]
```

**规则**：
- 以方括号`[`开头，`]`结尾
- 数值之间用逗号`,`分隔
- 没有空格（紧凑格式）
- 共192个数值

### 5.3 类型转换

**SQL中的类型转换**：
```sql
'[0.277,1.037,...]'::vector(192)
```

**说明**：
- `::vector(192)` 是PostgreSQL的类型转换语法
- 将字符串转换为VECTOR类型
- 维度必须与表定义一致

---

## 六、相似度计算

### 6.1 余弦相似度

**操作符**：`<=>` （余弦距离）

**计算方式**：
```sql
-- 余弦相似度 = 1 - 余弦距离
1 - (voiceprint <=> $1::vector) as similarity
```

**结果范围**：
- `similarity = 1.0`：完全相同的向量
- `similarity = 0.0`：完全正交的向量
- `similarity < 0`：方向相反（很少见）

**阈值**：
- 默认阈值：0.85
- 可配置：`voiceprint.similarity.threshold=0.85`

### 6.2 其他距离计算（pgvector支持）

```sql
-- L2距离（欧氏距离）
voiceprint <-> $1::vector

-- 内积
voiceprint <#> $1::vector
```

**注意**：当前实现使用余弦相似度，这是最常用的向量相似度计算方法。

---

## 七、数据示例

### 7.1 插入示例

```sql
-- 插入说话人1的声纹
INSERT INTO meeting_voiceprints (meeting_id, speaker_id, voiceprint, segment_count)
VALUES (
    'meeting-1234567890123456789',
    'speaker-001',
    '[0.277,1.037,1.280,-0.049,0.123,0.456,...]'::vector(192),
    1
);

-- 插入说话人2的声纹
INSERT INTO meeting_voiceprints (meeting_id, speaker_id, voiceprint, segment_count)
VALUES (
    'meeting-1234567890123456789',
    'speaker-002',
    '[0.189,0.923,1.156,0.234,0.678,0.345,...]'::vector(192),
    1
);
```

### 7.2 查询示例

```sql
-- 查找最相似的说话人
SELECT 
    speaker_id,
    1 - (voiceprint <=> '[0.275,1.035,1.278,-0.051,...]'::vector) as similarity
FROM meeting_voiceprints
WHERE meeting_id = 'meeting-1234567890123456789'
ORDER BY voiceprint <=> '[0.275,1.035,1.278,-0.051,...]'::vector
LIMIT 1;

-- 结果示例：
-- speaker_id | similarity
-- -----------+------------
-- speaker-001| 0.9234567  (如果超过0.85阈值，返回speaker-001)
```

---

## 八、配置说明

### 8.1 数据源配置

连接地址与账号密码须通过环境变量注入，勿在仓库中写入真实值；变量名见本模块 `deploy.md`（`OMNIASST_POSTGRESQL_*`）。

**application-dev.properties**（与仓库内配置一致，占位引用环境变量）：
```properties
########## PostgreSQL config #####################################################
spring.datasource.postgresql.url=${OMNIASST_POSTGRESQL_DATASOURCE_URL}
spring.datasource.postgresql.username=${OMNIASST_POSTGRESQL_DATASOURCE_USERNAME}
spring.datasource.postgresql.password=${OMNIASST_POSTGRESQL_DATASOURCE_PASSWORD}
```

### 8.2 声纹识别配置

声纹服务基地址须通过环境变量注入（例如部署平台设置 `OMNIASST_AI_VOICEPRINT_HOST`），勿在仓库中写死生产域名。

```properties
########## Voiceprint config #####################################################
voiceprint.similarity.threshold=0.85
ai.voiceprint.host=${OMNIASST_AI_VOICEPRINT_HOST}
```

### 8.3 PostgreSQLConfig配置类

```java
@Configuration
public class PostgreSQLConfig {
    @Value("${spring.datasource.postgresql.url:}")
    private String url;
    
    @Value("${spring.datasource.postgresql.username:}")
    private String username;
    
    @Value("${spring.datasource.postgresql.password:}")
    private String password;
    
    @Bean
    public DataSource postgresqlDataSource() {
        // 创建数据源
    }
    
    @Bean
    public JdbcTemplate postgresqlJdbcTemplate(DataSource postgresqlDataSource) {
        // 创建JdbcTemplate
    }
}
```

---

## 九、性能优化建议

### 9.1 索引策略

1. **必须索引**：`meeting_id`（查询时总是按meeting_id过滤）
2. **可选索引**：`voiceprint`向量索引（如果数据量大）
3. **唯一约束**：`(meeting_id, speaker_id)`（保证数据完整性）

### 9.2 查询优化

1. **总是使用WHERE meeting_id = ?**：缩小查询范围
2. **使用LIMIT 1**：只返回最相似的结果
3. **阈值过滤**：在SQL中使用阈值，减少数据传输

### 9.3 数据量估算

- **单会议说话人数**：通常10-50人
- **向量存储大小**：192维 × 8字节 ≈ 1.5KB/向量
- **单会议存储**：10-50条记录 ≈ 15-75KB

---

## 十、常见问题

### 10.1 向量维度不匹配

**错误**：`dimensions mismatch: expected 192, got 128`

**原因**：声纹识别API返回的维度与表定义不一致

**解决**：
1. 确认API返回的向量维度
2. 修改表结构：`ALTER TABLE meeting_voiceprints ALTER COLUMN voiceprint TYPE vector(128);`
3. 或确认API配置

### 10.2 pgvector扩展未安装

**错误**：`type "vector" does not exist`

**解决**：
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 10.3 相似度计算很慢

**原因**：没有创建向量索引

**解决**：
```sql
CREATE INDEX idx_meeting_voiceprints_voiceprint_ivfflat 
ON meeting_voiceprints 
USING ivfflat (voiceprint vector_cosine_ops)
WITH (lists = 100);
```

---

## 十一、维护操作

### 11.1 查看表结构

```sql
-- 查看表定义
\d meeting_voiceprints

-- 查看索引
\di meeting_voiceprints*
```

### 11.2 数据清理

```sql
-- 删除某个会议的所有声纹数据
DELETE FROM meeting_voiceprints WHERE meeting_id = 'meeting-xxx';

-- 删除某个时间之前的数据（可选，用于数据清理）
DELETE FROM meeting_voiceprints 
WHERE created_at < '2024-01-01 00:00:00';
```

### 11.3 统计信息

```sql
-- 统计总记录数
SELECT COUNT(*) FROM meeting_voiceprints;

-- 统计每个会议的说话人数量
SELECT meeting_id, COUNT(*) as speaker_count
FROM meeting_voiceprints
GROUP BY meeting_id
ORDER BY speaker_count DESC;

-- 查看存储空间使用
SELECT 
    pg_size_pretty(pg_total_relation_size('meeting_voiceprints')) as total_size,
    pg_size_pretty(pg_relation_size('meeting_voiceprints')) as table_size,
    pg_size_pretty(pg_indexes_size('meeting_voiceprints')) as indexes_size;
```

---

## 十二、参考

- **pgvector文档**：https://github.com/pgvector/pgvector
- **向量相似度**：https://en.wikipedia.org/wiki/Cosine_similarity
- **PostgreSQL类型转换**：https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-TYPE-CASTS

