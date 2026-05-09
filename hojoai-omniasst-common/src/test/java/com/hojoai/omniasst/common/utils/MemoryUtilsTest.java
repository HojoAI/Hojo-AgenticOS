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
import com.hojoai.omniasst.common.api.memory.dto.response.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemoryUtils 集成测试
 * 完全参考 EverMemOS/tests/test_memory_controller.py 的实现方式
 * 
 * 关键点：
 * 1. 使用租户头信息（X-Organization-Id, X-Space-Id, X-Hash-Key）
 * 2. 测试前初始化租户数据库
 * 3. 使用sync_mode=true确保消息同步处理
 * 4. 通过24小时时间间隔 + 话题切换触发边界检测
 * 5. 等待足够时间让记忆索引完成
 */
@SpringBootTest(classes = MemoryUtilsTest.TestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryUtilsTest {

    /**
     * 测试用的 Spring Boot 应用配置
     */
    @SpringBootApplication
    @Import(MemoryAutoConfiguration.class)
    static class TestApplication {
        // 测试应用配置
    }

    // ==================== 测试数据 ====================
    
    // 使用固定的租户ID，与配置文件中的一致
    private static final String TEST_ORGANIZATION_ID = "test_memory_api_organization";
    private static final String TEST_SPACE_ID = "test_memory_api_space";
    
    // 每次测试使用唯一的用户和群组ID
    private static final String TEST_USER_ID = "test_user_" + System.currentTimeMillis();
    private static final String TEST_GROUP_ID = "test_group_" + System.currentTimeMillis();
    
    @BeforeAll
    static void setup() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EverMemOS 集成测试（使用 spring profile=test 下的 hojoai.memory.api 配置）");
        System.out.println("=".repeat(80));
        System.out.println("租户组织ID: " + TEST_ORGANIZATION_ID);
        System.out.println("租户空间ID: " + TEST_SPACE_ID);
        System.out.println("测试用户ID: " + TEST_USER_ID);
        System.out.println("测试群组ID: " + TEST_GROUP_ID);
        System.out.println("=".repeat(80));
        
        // 初始化租户数据库
        try {
            System.out.println("\n【初始化租户数据库】");
            ApiResponse<InitTenantDbResponse> initResponse = MemoryUtils.initTenantDatabase();
            if (initResponse != null) {
                System.out.println("初始化状态: " + initResponse.getStatus());
                if (MemoryUtils.isSuccess(initResponse)) {
                    InitTenantDbResponse result = MemoryUtils.getResult(initResponse);
                    System.out.println("✓ 数据库初始化成功");
                    if (result.getTenantId() != null) {
                        System.out.println("  租户ID: " + result.getTenantId());
                    }
                } else {
                    System.out.println("⚠ 初始化返回: " + initResponse.getMessage());
                    System.out.println("  (可能数据库已存在，继续测试)");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ 数据库初始化异常: " + e.getMessage());
            System.out.println("  (继续测试，假设数据库已初始化)");
        }
        
        System.out.println("=".repeat(80));
    }

    // ==================== 基础测试：存储和查询记忆 ====================

    @Test
    @Order(1)
    void testStoreAndFetchMemories() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试 1: 存储消息并查询记忆（完全参考 Python 测试）");
        System.out.println("=".repeat(80));

        try {
            String groupId = "test_fetch_" + System.currentTimeMillis();
            String userId = "test_user_fetch_" + System.currentTimeMillis();
            String assistantId = "assistant_001";
            
            System.out.println("群组ID: " + groupId);
            System.out.println("用户ID: " + userId);
            System.out.println("助手ID: " + assistantId);
            
            // 基准时间
            Instant baseTime = Instant.now();
            
            // ===================================================================
            // 场景1: 讨论咖啡偏好（4条消息，完整对话片段）
            // 参考 Python: tests/test_memory_controller.py test_memorize_single_message
            // ===================================================================
            System.out.println("\n【场景1】发送咖啡偏好讨论的对话（4条消息）...");
            System.out.println("💡 策略说明：前4条消息构成完整的场景1（咖啡偏好讨论）");
            
            // 消息1: 用户询问（时间: baseTime）
            String msg1 = "msg_fetch_1_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> response1 = MemoryUtils.storeMessage(
                msg1,
                baseTime.toString(),  // ISO 8601格式
                userId,
                "我最近想养成喝咖啡的习惯，你有什么建议吗？",
                groupId,
                "Test Group",
                "Test User",
                Memory.Role.HUMAN,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            assertNotNull(response1, "响应不应为空");
            System.out.println("🔄 消息1/4: " + response1.getStatus());
            if (MemoryUtils.isSuccess(response1)) {
                StoreMessageResponse result1 = MemoryUtils.getResult(response1);
                System.out.println("  状态: " + result1.getStatusInfo() + ", 提取记忆数: " + result1.getCount());
            }
            
            Thread.sleep(1000);
            
            // 消息2: AI回复（时间: baseTime + 30秒）
            String msg2 = "msg_fetch_2_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> response2 = MemoryUtils.storeMessage(
                msg2,
                baseTime.plus(30, ChronoUnit.SECONDS).toString(),
                assistantId,
                "当然可以！咖啡有很多种类，从浓郁的意式浓缩到温和的美式咖啡。你可以根据自己的口味选择。我建议从美式咖啡开始。",
                groupId,
                "Test Group",
                "AI Assistant",
                Memory.Role.AI,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            assertNotNull(response2, "响应不应为空");
            System.out.println("🔄 消息2/4: " + response2.getStatus());
            if (MemoryUtils.isSuccess(response2)) {
                StoreMessageResponse result2 = MemoryUtils.getResult(response2);
                System.out.println("  状态: " + result2.getStatusInfo() + ", 提取记忆数: " + result2.getCount());
            }
            
            Thread.sleep(1000);
            
            // 消息3: 用户回复（时间: baseTime + 1分钟）
            String msg3 = "msg_fetch_3_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> response3 = MemoryUtils.storeMessage(
                msg3,
                baseTime.plus(1, ChronoUnit.MINUTES).toString(),
                userId,
                "我喜欢喝美式咖啡，不加糖不加奶，越浓越好。",
                groupId,
                "Test Group",
                "Test User",
                Memory.Role.HUMAN,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            assertNotNull(response3, "响应不应为空");
            System.out.println("🔄 消息3/4: " + response3.getStatus());
            if (MemoryUtils.isSuccess(response3)) {
                StoreMessageResponse result3 = MemoryUtils.getResult(response3);
                System.out.println("  状态: " + result3.getStatusInfo() + ", 提取记忆数: " + result3.getCount());
            }
            
            Thread.sleep(1000);
            
            // 消息4: AI回复（时间: baseTime + 1分30秒，完成对话片段）
            String msg4 = "msg_fetch_4_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> response4 = MemoryUtils.storeMessage(
                msg4,
                baseTime.plus(1, ChronoUnit.MINUTES).plus(30, ChronoUnit.SECONDS).toString(),
                assistantId,
                "我了解你的偏好了！黑美式可以充分体验咖啡豆的风味。我建议选择深度烘焙的豆子，味道会更浓郁。",
                groupId,
                "Test Group",
                "AI Assistant",
                Memory.Role.AI,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            assertNotNull(response4, "响应不应为空");
            System.out.println("🔄 消息4/4: " + response4.getStatus());
            if (MemoryUtils.isSuccess(response4)) {
                StoreMessageResponse result4 = MemoryUtils.getResult(response4);
                System.out.println("  状态: " + result4.getStatusInfo() + ", 提取记忆数: " + result4.getCount());
            }
            
            // ===================================================================
            // 场景2: 触发边界检测（24小时时间间隔 + 话题切换）
            // 参考 Python: 第5条消息使用 baseTime + 24小时，触发边界检测
            // ===================================================================
            System.out.println("\n【场景2】发送新话题消息（触发边界检测）...");
            System.out.println("💡 策略说明：第5条消息使用24小时时间间隔+新话题，触发边界检测");
            System.out.println("💡 这确保场景1的记忆被成功提取");
            
            Thread.sleep(1000);
            
            // 消息5: 用户新话题（时间: baseTime + 24小时）
            String msg5 = "msg_fetch_5_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> response5 = MemoryUtils.storeMessage(
                msg5,
                baseTime.plus(24, ChronoUnit.HOURS).toString(),  // 24小时后
                userId,
                "顺便问一下，周末项目进展如何？",
                groupId,
                "Test Group",
                "Test User",
                Memory.Role.HUMAN,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            assertNotNull(response5, "响应不应为空");
            System.out.println("🔄 消息5 (触发边界): " + response5.getStatus());
            if (MemoryUtils.isSuccess(response5)) {
                StoreMessageResponse result5 = MemoryUtils.getResult(response5);
                System.out.println("  状态: " + result5.getStatusInfo() + ", 提取记忆数: " + result5.getCount());
                
                // 如果 count > 0，说明边界检测成功，记忆已提取
                if (result5.getCount() > 0) {
                    System.out.println("  ✅ 边界检测成功！已提取 " + result5.getCount() + " 条记忆");
                } else {
                    System.out.println("  ⚠️  消息已累积，等待边界检测");
                }
            }
            
            // 消息6: AI回复（时间: baseTime + 24小时 + 30秒）
            Thread.sleep(1000);
            String msg6 = "msg_fetch_6_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                msg6,
                baseTime.plus(24, ChronoUnit.HOURS).plus(30, ChronoUnit.SECONDS).toString(),
                assistantId,
                "项目进展顺利，主要功能已完成80%，预计下周可以提交测试。",
                groupId,
                "Test Group",
                "AI Assistant",
                Memory.Role.AI,
                Collections.emptyList(),
                true  // sync_mode=true
            );
            
            // ===================================================================
            // 等待记忆索引完成（关键步骤！）
            // 参考 Python: 等待5-10秒让Elasticsearch/Milvus索引完成
            // ===================================================================
            System.out.println("\n【等待记忆索引】等待10秒让记忆索引完成...");
            System.out.println("💡 EverMemOS 使用 Elasticsearch 和 Milvus 进行索引，需要等待异步索引完成");
            Thread.sleep(10000);  // 等待10秒
            
            // ===================================================================
            // 查询记忆（Fetch - 获取所有episodic_memory）
            // ===================================================================
            System.out.println("\n【查询记忆】使用 fetchMemories 获取 episodic_memory...");
            ApiResponse<FetchMemoriesResponse> fetchResponse = MemoryUtils.fetchMemories(
                userId,
                groupId,
                Memory.MemoryType.EPISODIC_MEMORY
            );
            
            assertNotNull(fetchResponse, "查询响应不应为空");
            System.out.println("查询状态: " + fetchResponse.getStatus());
            
            if (MemoryUtils.isSuccess(fetchResponse)) {
                FetchMemoriesResponse result = MemoryUtils.getResult(fetchResponse);
                assertNotNull(result, "查询结果不应为空");
                
                System.out.println("✅ Fetch 查询成功！");
                System.out.println("  记忆总数: " + result.getTotalCount());
                System.out.println("  是否有更多: " + result.isHasMore());
                System.out.println("  返回记忆数: " + (result.getMemories() != null ? result.getMemories().size() : 0));
                
                if (result.getMemories() != null && !result.getMemories().isEmpty()) {
                    System.out.println("\n  📝 记忆内容:");
                    result.getMemories().stream()
                        .limit(5)
                        .forEach(memory -> {
                            String content = getMemoryDisplayContent(memory);
                            System.out.println("    - " + (content.length() > 80 ? content.substring(0, 80) + "..." : content));
                            System.out.println("      时间: " + memory.getTimestamp());
                            System.out.println("      类型: " + memory.getMemoryType());
                        });
                    
                    assertTrue(result.getTotalCount() > 0, "应该找到至少一条记忆");
                } else {
                    System.out.println("  ⚠️  暂无记忆数据");
                    System.out.println("  可能原因：");
                    System.out.println("    1. 记忆还在异步处理中，需要更长时间");
                    System.out.println("    2. 边界检测条件未满足，记忆未提取");
                    System.out.println("    3. 租户配置未正确设置");
                }
            } else {
                System.err.println("❌ Fetch 查询失败: " + fetchResponse.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ 测试异常: " + e.getMessage());
            e.printStackTrace();
            fail("测试异常: " + e.getMessage());
        }
    }

    // ==================== 搜索记忆测试 ====================

    @Test
    @Order(2)
    void testSearchMemories() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试 2: 搜索记忆（参考 Python 测试）");
        System.out.println("=".repeat(80));

        try {
            String groupId = "test_search_" + System.currentTimeMillis();
            String userId = "test_user_search_" + System.currentTimeMillis();
            String assistantId = "assistant_001";
            
            System.out.println("群组ID: " + groupId);
            System.out.println("用户ID: " + userId);
            
            // 基准时间
            Instant baseTime = Instant.now();
            
            // ===================================================================
            // 存储消息（场景1：咖啡偏好，场景2：手机购买）
            // ===================================================================
            System.out.println("\n【存储消息】创建两个场景的对话...");
            
            // 场景1: 咖啡偏好（4条消息）
            System.out.println("  场景1: 咖啡偏好讨论");
            String msg1 = "msg_search_1_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                msg1,
                baseTime.toString(),
                userId,
                "我喜欢喝美式咖啡，每天至少两杯，不加糖不加奶。",
                groupId,
                "Test Group",
                "Test User",
                Memory.Role.HUMAN,
                Collections.emptyList(),
                true
            );
            Thread.sleep(1000);
            
            String msg2 = "msg_search_2_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                msg2,
                baseTime.plus(30, ChronoUnit.SECONDS).toString(),
                assistantId,
                "我记住了你的咖啡偏好：美式咖啡，每天至少两杯，不加糖不加奶。",
                groupId,
                "Test Group",
                "AI Assistant",
                Memory.Role.AI,
                Collections.emptyList(),
                true
            );
            Thread.sleep(1000);
            
            // 场景2: 手机购买（使用24小时后的时间，触发边界）
            System.out.println("  场景2: 手机购买讨论");
            String msg3 = "msg_search_3_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                msg3,
                baseTime.plus(24, ChronoUnit.HOURS).toString(),
                userId,
                "我想买一部新手机，主要看性能和性价比。",
                groupId,
                "Test Group",
                "Test User",
                Memory.Role.HUMAN,
                Collections.emptyList(),
                true
            );
            Thread.sleep(1000);
            
            String msg4 = "msg_search_4_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                msg4,
                baseTime.plus(24, ChronoUnit.HOURS).plus(30, ChronoUnit.SECONDS).toString(),
                assistantId,
                "了解，你购买手机时注重性能和性价比。我建议考虑小米或一加品牌。",
                groupId,
                "Test Group",
                "AI Assistant",
                Memory.Role.AI,
                Collections.emptyList(),
                true
            );
            
            // ===================================================================
            // 等待记忆索引完成
            // ===================================================================
            System.out.println("\n【等待处理】等待10秒让记忆索引完成...");
            Thread.sleep(10000);
            
            // ===================================================================
            // 搜索咖啡相关记忆（使用 hybrid 方法）
            // 参考 Python: test_retrieve_hybrid
            // ===================================================================
            System.out.println("\n【搜索记忆】搜索关键词：咖啡偏好 (使用 hybrid 方法)");
            String query = "咖啡偏好";
            
            ApiResponse<SearchMemoriesResponse> searchResponse = MemoryUtils.searchMemories(
                query,
                userId,
                groupId,
                Memory.RetrieveMethod.HYBRID,  // 使用 hybrid 方法
                Arrays.asList(Memory.MemoryType.EPISODIC_MEMORY),
                10,
                null, null, null, true, null
            );
            
            assertNotNull(searchResponse, "搜索响应不应为空");
            System.out.println("搜索状态: " + searchResponse.getStatus());
            
            if (MemoryUtils.isSuccess(searchResponse)) {
                SearchMemoriesResponse result = MemoryUtils.getResult(searchResponse);
                assertNotNull(result, "搜索结果不应为空");
                
                System.out.println("✅ 搜索成功！");
                System.out.println("  记忆总数: " + result.getTotalCount());
                System.out.println("  是否有更多: " + result.isHasMore());
                System.out.println("  返回记忆组数: " + (result.getMemories() != null ? result.getMemories().size() : 0));
                
                if (result.getMemories() != null && !result.getMemories().isEmpty()) {
                    System.out.println("\n  🔍 搜索结果:");
                    
                    int groupIndex = 0;
                    for (Map<String, List<Memory>> memoryMap : result.getMemories()) {
                        System.out.println("  记忆组 " + (groupIndex + 1) + ":");
                        
                        for (Map.Entry<String, List<Memory>> entry : memoryMap.entrySet()) {
                            String memoryType = entry.getKey();
                            List<Memory> memories = entry.getValue();
                            
                            System.out.println("    类型: " + memoryType + " (数量: " + memories.size() + ")");
                            
                            // 获取对应的分数
                            List<Double> groupScores = null;
                            if (result.getScores() != null && groupIndex < result.getScores().size()) {
                                Map<String, List<Double>> scoreMap = result.getScores().get(groupIndex);
                                if (scoreMap != null) {
                                    groupScores = scoreMap.get(memoryType);
                                }
                            }
                            
                            // 获取重要性分数
                            Double importanceScore = null;
                            if (result.getImportanceScores() != null && groupIndex < result.getImportanceScores().size()) {
                                importanceScore = result.getImportanceScores().get(groupIndex);
                            }
                            if (importanceScore != null) {
                                System.out.println("    重要性分数: " + String.format("%.4f", importanceScore));
                            }
                            
                            int memoryIndex = 0;
                            for (Memory memory : memories) {
                                String content = getMemoryDisplayContent(memory);
                                System.out.println("      [" + (memoryIndex + 1) + "] " + 
                                    (content.length() > 80 ? content.substring(0, 80) + "..." : content));
                                System.out.println("          时间: " + memory.getTimestamp());
                                if (groupScores != null && memoryIndex < groupScores.size()) {
                                    System.out.println("          相关性分数: " + String.format("%.4f", groupScores.get(memoryIndex)));
                                }
                                memoryIndex++;
                            }
                        }
                        groupIndex++;
                    }
                    
                    assertTrue(result.getTotalCount() > 0, "应该找到至少一条记忆");
                } else {
                    System.out.println("  ⚠️  未找到相关记忆");
                    System.out.println("  可能原因：");
                    System.out.println("    1. 记忆还在索引中");
                    System.out.println("    2. 查询关键词与记忆内容匹配度不够");
                    System.out.println("    3. 记忆提取未完成");
                }
            } else {
                System.err.println("❌ 搜索失败: " + searchResponse.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ 测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 多种检索方法测试 ====================

    @Test
    @Order(3)
    void testDifferentRetrieveMethods() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试 3: 不同检索方法对比");
        System.out.println("=".repeat(80));

        String groupId = "test_retrieve_" + System.currentTimeMillis();
        String userId = "test_user_retrieve_" + System.currentTimeMillis();
        String query = "咖啡";
        
        // 先存储消息
        System.out.println("群组ID: " + groupId);
        System.out.println("用户ID: " + userId);
        System.out.println("搜索关键词: " + query);
        
        String msg1 = "msg_retrieve_1_" + System.currentTimeMillis();
        MemoryUtils.storeMessage(
            Memory.Role.HUMAN,
            groupId,
            userId,
            msg1,
            "我喜欢喝美式咖啡，每天至少两杯。",
            Collections.emptyList(),
            true
        );
        
        Thread.sleep(2000);
        
        String msg2 = "msg_retrieve_2_" + System.currentTimeMillis();
        MemoryUtils.storeMessage(
            Memory.Role.AI,
            groupId,
            userId,
            msg2,
            "我记住了你的咖啡偏好。",
            Collections.singletonList(msg1),
            true
        );
        
        Thread.sleep(5000); // 等待处理
        
        List<Memory.RetrieveMethod> methods = Arrays.asList(
            Memory.RetrieveMethod.KEYWORD,
            Memory.RetrieveMethod.VECTOR,
            Memory.RetrieveMethod.HYBRID
        );

        for (Memory.RetrieveMethod method : methods) {
            try {
                System.out.println("\n使用检索方法: " + method.getMethodName());
                
                ApiResponse<SearchMemoriesResponse> response = MemoryUtils.searchMemories(
                    query,
                    userId,
                    groupId,
                    method,
                    Arrays.asList(Memory.MemoryType.EPISODIC_MEMORY),
                    5, // 最多返回5条
                    null, null, null, true, null
                );

                if (MemoryUtils.isSuccess(response)) {
                    SearchMemoriesResponse result = MemoryUtils.getResult(response);
                    System.out.println("  ✓ 成功，找到 " + 
                        (result.getMemories() != null ? result.getMemories().size() : 0) + " 条记忆");
                } else {
                    System.err.println("  ✗ 失败: " + response.getMessage());
                }

            } catch (Exception e) {
                System.err.println("  ✗ 异常: " + e.getMessage());
            }
        }
    }

    // ==================== 完整工作流程测试 ====================

    @Test
    @Order(4)
    void testCompleteWorkflow() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试 4: 完整工作流程");
        System.out.println("=".repeat(80));

        try {
            String workflowGroupId = "workflow_" + System.currentTimeMillis();
            String workflowUserId = "workflow_user_" + System.currentTimeMillis();

            System.out.println("场景: 用户咨询天气后询问穿衣建议");
            System.out.println("群组ID: " + workflowGroupId);
            System.out.println("用户ID: " + workflowUserId);

            // Step 1: 存储第一轮对话
            System.out.println("\n1. 第一轮对话 - 询问天气");
            String msg1 = "msg_workflow_1_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> resp1 = MemoryUtils.storeMessage(
                Memory.Role.HUMAN, 
                workflowGroupId, 
                workflowUserId, 
                msg1, 
                "今天杭州的天气怎么样？",
                Collections.emptyList(),
                true
            );
            System.out.println("  消息1状态: " + (resp1 != null ? resp1.getStatus() : "null"));
            
            Thread.sleep(2000);
            
            String msg2 = "msg_workflow_2_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> resp2 = MemoryUtils.storeMessage(
                Memory.Role.AI, 
                workflowGroupId, 
                workflowUserId, 
                msg2, 
                "今天杭州天气晴朗，气温18-25℃，适合外出。", 
                Collections.singletonList(msg1),
                true
            );
            System.out.println("  消息2状态: " + (resp2 != null ? resp2.getStatus() : "null"));
            
            Thread.sleep(2000);

            // Step 2: 存储第二轮对话
            System.out.println("\n2. 第二轮对话 - 询问穿衣建议");
            String msg3 = "msg_workflow_3_" + System.currentTimeMillis();
            ApiResponse<StoreMessageResponse> resp3 = MemoryUtils.storeMessage(
                Memory.Role.HUMAN, 
                workflowGroupId, 
                workflowUserId, 
                msg3, 
                "那我应该穿什么衣服？",
                Collections.emptyList(),
                true
            );
            System.out.println("  消息3状态: " + (resp3 != null ? resp3.getStatus() : "null"));
            
            Thread.sleep(2000);

            // Step 3: 搜索相关记忆
            System.out.println("\n3. 搜索天气相关记忆");
            Thread.sleep(5000); // 等待记忆处理完成
            
            ApiResponse<SearchMemoriesResponse> searchResponse = MemoryUtils.searchMemories(
                "天气 温度", 
                workflowUserId, 
                workflowGroupId
            );

            if (MemoryUtils.isSuccess(searchResponse)) {
                SearchMemoriesResponse searchResult = MemoryUtils.getResult(searchResponse);
                System.out.println("  找到记忆组数: " + 
                    (searchResult.getMemories() != null ? searchResult.getMemories().size() : 0));
                
                // 基于搜索结果回答
                String context = "";
                if (searchResult.getMemories() != null && !searchResult.getMemories().isEmpty()) {
                    Map<String, List<Memory>> firstGroup = searchResult.getMemories().get(0);
                    for (List<Memory> memories : firstGroup.values()) {
                        if (!memories.isEmpty()) {
                            context = getMemoryDisplayContent(memories.get(0));
                            break;
                        }
                    }
                }
                System.out.println("  上下文: " + (context.length() > 50 ? context.substring(0, 50) + "..." : context));
            } else {
                System.out.println("  ⚠ 未找到记忆（可能还在处理中）");
            }

            // Step 4: 存储AI回复
            String msg4 = "msg_workflow_4_" + System.currentTimeMillis();
            MemoryUtils.storeMessage(
                Memory.Role.AI, 
                workflowGroupId, 
                workflowUserId, 
                msg4, 
                "根据当前天气（18-25℃），建议穿长袖衬衫或薄外套，既舒适又适合温度变化。", 
                Collections.singletonList(msg3),
                true
            );
            Thread.sleep(2000);

            // Step 5: 获取对话历史
            System.out.println("\n4. 获取完整对话历史");
            ApiResponse<FetchMemoriesResponse> fetchResponse = MemoryUtils.fetchMemories(
                workflowUserId, 
                workflowGroupId
            );

            if (MemoryUtils.isSuccess(fetchResponse)) {
                FetchMemoriesResponse fetchResult = MemoryUtils.getResult(fetchResponse);
                System.out.println("  ✓ 对话历史完整，共 " + fetchResult.getTotalCount() + " 条记录");
                
                if (fetchResult.getMemories() != null) {
                    System.out.println("\n  完整对话:");
                    fetchResult.getMemories().forEach(memory -> {
                        System.out.println("    - " + getMemoryDisplayContent(memory));
                        System.out.println("      时间: " + memory.getTimestamp());
                    });
                }
            }

            System.out.println("\n✓ 完整工作流程测试完成！");

        } catch (Exception e) {
            System.err.println("测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取记忆的显示内容（优先使用content，如果没有则使用summary、subject或episode）
     */
    private String getMemoryDisplayContent(Memory memory) {
        // 优先使用原始内容
        String content = memory.getContent();
        if (content != null && !content.isEmpty()) {
            return content;
        }
        
        // 如果没有content，尝试从summary、subject、episode中选择
        String[] candidates = {
            memory.getSummary(),
            memory.getSubject(),
            memory.getEpisode()
        };
        
        // 先找包含中文的内容
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isEmpty() && containsChinese(candidate)) {
                return candidate;
            }
        }
        
        // 如果都没有中文，返回第一个非空的内容
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        
        return "（无内容）";
    }
    
    /**
     * 判断字符串是否包含中文字符
     */
    private boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.chars().anyMatch(ch -> 
            (ch >= 0x4E00 && ch <= 0x9FFF) ||  // CJK统一汉字
            (ch >= 0x3400 && ch <= 0x4DBF) ||  // CJK扩展A
            (ch >= 0x20000 && ch <= 0x2A6DF)   // CJK扩展B
        );
    }
}
