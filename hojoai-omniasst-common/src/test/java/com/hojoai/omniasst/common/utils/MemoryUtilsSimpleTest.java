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
package com.hojoai.omniasst.common.utils;

import com.hojoai.omniasst.common.api.memory.config.MemoryAutoConfiguration;
import com.hojoai.omniasst.common.api.memory.dto.Memory;
import com.hojoai.omniasst.common.api.memory.dto.request.ConversationMetaRequest;
import com.hojoai.omniasst.common.api.memory.dto.response.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;

/**
 * Memory Utils 简单测试
 * 完全参考 Python 的 simple_demo.py 实现
 * 
 * 关键要点：
 * 1. 不使用 sync_mode 参数
 * 2. 先保存 conversation-meta
 * 3. 消息包含 scene 字段
 * 4. 使用固定的 group_id
 * 5. 等待足够时间让记忆提取和索引完成
 */
@SpringBootTest(classes = MemoryUtilsSimpleTest.TestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryUtilsSimpleTest {

    /**
     * 测试用的 Spring Boot 应用配置
     */
    @SpringBootApplication
    @Import(MemoryAutoConfiguration.class)
    static class TestApplication {
    }

    // 使用固定的 group_id（与 Python simple_demo 一致）
    private static final String GROUP_ID = "simple_demo_group_" + System.currentTimeMillis();
    private static final String GROUP_NAME = "Simple Demo Group";
    private static final String SCENE = "assistant";
    
    private static int messageCounter = 0;

    @BeforeAll
    static void setup() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🧠 EverMemOS 简单测试（参考 simple_demo.py）");
        System.out.println("=".repeat(80));
        System.out.println("Group ID: " + GROUP_ID);
        System.out.println("Scene: " + SCENE);
        System.out.println("=".repeat(80));
        
        // 初始化租户数据库
        try {
            System.out.println("\n【初始化租户数据库】");
            ApiResponse<InitTenantDbResponse> initResponse = MemoryUtils.initTenantDatabase();
            if (initResponse != null && MemoryUtils.isSuccess(initResponse)) {
                System.out.println("✅ 数据库初始化成功");
            }
        } catch (Exception e) {
            System.out.println("⚠️  继续测试（假设数据库已初始化）");
        }
        
        // 保存 conversation-meta（关键步骤！）
        saveConversationMeta();
    }
    
    /**
     * 保存对话元数据（参考 Python simple_demo.py）
     */
    private static void saveConversationMeta() {
        try {
            System.out.println("\n【保存对话元数据】");
            
            // 构建 scene_desc
            Map<String, Object> sceneDescMap = new HashMap<>();
            sceneDescMap.put("description", "Simple Demo - " + SCENE + " scene");
            sceneDescMap.put("extra", new HashMap<>());
            
            // 构建 user_details
            Map<String, ConversationMetaRequest.UserDetail> userDetailsMap = new HashMap<>();
            userDetailsMap.put("User", ConversationMetaRequest.UserDetail.builder()
                .fullName("Demo User")
                .role("user")
                .build());
            userDetailsMap.put("Assistant", ConversationMetaRequest.UserDetail.builder()
                .fullName("AI Assistant")
                .role("assistant")
                .build());
            
            ConversationMetaRequest request = ConversationMetaRequest.builder()
                .version("1.0.0")
                .scene(SCENE)
                .sceneDesc(sceneDescMap)
                .name(GROUP_NAME)
                .description("Simple Demo Test")
                .groupId(GROUP_ID)
                .createdAt(Instant.now().toString())
                .defaultTimezone("Asia/Shanghai")
                .userDetails(userDetailsMap)
                .tags(Arrays.asList("demo", SCENE))
                .build();
            
            MemoryUtils.saveConversationMeta(request);
            System.out.println("ℹ️  对话元数据已保存 (Scene: " + SCENE + ")");
            
            // 等待一下确保元数据保存完成
            Thread.sleep(2000);
            
        } catch (Exception e) {
            System.err.println("⚠️  保存对话元数据失败: " + e.getMessage());
        }
    }

    /**
     * 存储消息（参考 Python SimpleMemoryManager.store()）
     * 关键：不使用 sync_mode，让服务端自动处理
     */
    private void storeMessage(String content, String sender) throws InterruptedException {
        messageCounter++;
        Instant now = Instant.now();
        String messageId = String.format("msg_%d_%d", messageCounter, now.toEpochMilli());
        
        // 关键：不传 sync_mode，使用异步模式（调用不带 syncMode 参数的方法）
        Memory.Role role = sender.equals("Assistant") ? Memory.Role.AI : Memory.Role.HUMAN;
        MemoryUtils.storeMessage(
            role,
            GROUP_ID,
            sender,
            messageId,
            content,
            Collections.emptyList()
            // 注意：这里调用的是异步版本，没有 syncMode 参数！
        );
        
        System.out.println("  📝 已记录: " + (content.length() > 40 ? content.substring(0, 40) + "..." : content));
        
        // 每条消息间隔2秒（与 Python 一致）
        Thread.sleep(2000);
    }

    @Test
    @Order(1)
    void testSimpleDemo() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📝 步骤 1: 存储对话消息");
        System.out.println("=".repeat(80));
        
        // 存储消息序列（与 Python simple_demo.py 完全一致）
        storeMessage("我喜欢踢足球，周末经常去球场", "User");
        storeMessage("足球是个很棒的运动！你喜欢哪个球队？", "Assistant");
        storeMessage("我最喜欢巴塞罗那，梅西是我的偶像", "User");
        storeMessage("我还喜欢看篮球，NBA是我的最爱", "User");
        storeMessage("我现在要去睡觉了", "User");
        storeMessage("今天天气真不错", "User");
        storeMessage("宇宙在膨胀", "User");
        
        // ===================================================================
        // 步骤 2: 等待索引构建（关键！）
        // ===================================================================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⏳ 步骤 2: 等待索引构建");
        System.out.println("=".repeat(80));
        System.out.println("💡 提示: 记忆提取需要足够的上下文");
        System.out.println("   - 短对话可能只记录消息，不会立即生成记忆");
        System.out.println("   - 包含具体信息的多轮对话更容易提取记忆");
        System.out.println("   - 系统在对话边界（话题变化、时间间隔）提取记忆");
        System.out.println("⏳ 等待 15 秒确保数据写入...");
        Thread.sleep(15000);  // 等待15秒（比Python多5秒，更保险）
        System.out.println("✅ 索引构建完成");
        
        // ===================================================================
        // 步骤 3: 搜索记忆
        // ===================================================================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 步骤 3: 搜索记忆");
        System.out.println("=".repeat(80));
        
        // 查询 1
        searchMemories("用户喜欢什么运动？");
        
        // 查询 2
        searchMemories("用户最喜欢的球队是什么？");
        
        // 查询 3
        searchMemories("用户的运动爱好有哪些？");
        
        // 打印总结
        printSummary();
    }
    
    /**
     * 搜索记忆（参考 Python SimpleMemoryManager.search()）
     */
    private void searchMemories(String query) {
        System.out.println("\n💬 查询: " + query);
        
        try {
            ApiResponse<SearchMemoriesResponse> response = MemoryUtils.searchMemories(
                query,
                "__all__",  // 查询所有用户（因为我们关心的是 group_id）
                GROUP_ID,
                Memory.RetrieveMethod.RRF,  // 使用 rrf 方法
                Arrays.asList(Memory.MemoryType.EPISODIC_MEMORY),
                3,  // top_k = 3
                null, null, null, true, null
            );
            
            if (MemoryUtils.isSuccess(response)) {
                SearchMemoriesResponse result = MemoryUtils.getResult(response);
                
                // 展平分组的记忆
                List<Memory> memories = new ArrayList<>();
                if (result.getMemories() != null) {
                    for (Map<String, List<Memory>> memoryMap : result.getMemories()) {
                        for (List<Memory> memList : memoryMap.values()) {
                            memories.addAll(memList);
                        }
                    }
                }
                
                Object metadata = result.getMetadata();
                Double latency = 0.0;
                if (metadata instanceof Map) {
                    Object latencyObj = ((Map<?, ?>) metadata).get("total_latency_ms");
                    if (latencyObj instanceof Number) {
                        latency = ((Number) latencyObj).doubleValue();
                    }
                }
                
                System.out.println(String.format("  🔍 找到 %d 条记忆 (耗时 %.2fms)", memories.size(), latency));
                
                if (memories.isEmpty()) {
                    System.out.println("     💡 提示: 未找到相关记忆");
                    System.out.println("         可能原因:");
                    System.out.println("         - 对话输入太少，系统还没有生成记忆");
                    System.out.println("         - 记忆提取需要足够的上下文和时间");
                } else {
                    printMemories(memories, result.getScores());
                }
                
            } else {
                System.err.println("  ❌ 搜索失败: " + response.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("  ❌ 搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 打印记忆详情
     */
    private void printMemories(List<Memory> memories, List<Map<String, List<Double>>> scores) {
        for (int i = 0; i < memories.size(); i++) {
            Memory mem = memories.get(i);
            
            // 获取分数
            double score = 0.0;
            if (scores != null && !scores.isEmpty() && i < scores.size()) {
                Map<String, List<Double>> scoreMap = scores.get(i);
                if (scoreMap != null) {
                    for (List<Double> scoreList : scoreMap.values()) {
                        if (scoreList != null && !scoreList.isEmpty()) {
                            score = scoreList.get(0);
                            break;
                        }
                    }
                }
            }
            
            // 提取事件时间
            String eventTime = extractEventTime(mem);
            
            System.out.println(String.format("\n     [%d] 相关度: %.4f | 时间: %s", i + 1, score, eventTime));
            
            if (mem.getSubject() != null && !mem.getSubject().isEmpty()) {
                System.out.println("         主题: " + mem.getSubject());
            }
            if (mem.getSummary() != null && !mem.getSummary().isEmpty()) {
                String summary = mem.getSummary();
                System.out.println("         摘要: " + (summary.length() > 60 ? summary.substring(0, 60) + "..." : summary));
            }
            if (mem.getEpisode() != null && !mem.getEpisode().isEmpty()) {
                String episode = mem.getEpisode();
                System.out.println("         详情: " + (episode.length() > 80 ? episode.substring(0, 80) + "..." : episode));
            }
        }
    }
    
    /**
     * 提取事件时间（参考 Python extract_event_time_from_memory）
     */
    private String extractEventTime(Memory memory) {
        // 简化版本，直接返回 timestamp 或 N/A
        if (memory.getTimestamp() != null && !memory.getTimestamp().isEmpty()) {
            return memory.getTimestamp();
        }
        return "N/A";
    }
    
    /**
     * 打印总结
     */
    private void printSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 测试完成！");
        System.out.println("=".repeat(80));
        System.out.println("\n📚 关于记忆提取:");
        System.out.println("   记忆系统使用智能提取策略，不会记录所有对话：");
        System.out.println("   - ✅ 会提取: 包含具体信息、观点、偏好、事件的对话");
        System.out.println("   - ❌ 不会提取: 过于简短、低信息量的寒暄");
        System.out.println("   - 🎯 最佳实践: 多轮对话、丰富上下文、具体细节");
        System.out.println("=".repeat(80));
    }
}
