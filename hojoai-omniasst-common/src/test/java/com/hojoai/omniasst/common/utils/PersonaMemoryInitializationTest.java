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
import com.hojoai.omniasst.common.api.memory.dto.response.ApiResponse;
import com.hojoai.omniasst.common.api.memory.dto.response.StoreMessageResponse;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 人设记忆初始化测试类
 * 根据"人设-初始化导入数据结构.pdf"中的三个角色初始化记忆数据
 * 
 * 三个角色：
 * 1. 王雷 - "进击的互联网人" (高级产品经理，上海)
 * 2. 李静 - "精致生活博主"
 * 3. 张伟 - "务实的家庭主义者"
 * 
 * 每个角色包含：
 * - Part 1: 关于您 - 核心档案 (L0 印象层)
 * - Part 2: 您的品味 - 偏好设定 (L1 偏好层)
 * - Part 3: 您的追求 - 目标设定 (L2 目标层)
 */
@SpringBootTest(classes = PersonaMemoryInitializationTest.TestApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonaMemoryInitializationTest {

    @SpringBootApplication
    @Import(MemoryAutoConfiguration.class)
    static class TestApplication {
    }

    @BeforeAll
    static void setup() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("人设记忆初始化测试 - 三个角色");
        System.out.println("=".repeat(100));
        System.out.println("1. 王雷 - \"进击的互联网人\"");
        System.out.println("2. 李静 - \"精致生活博主\"");
        System.out.println("3. 张伟 - \"务实的家庭主义者\"");
        System.out.println("=".repeat(100));
    }

    // ==================== 角色1: 王雷 - "进击的互联网人" ====================

    /**
     * 初始化王雷的记忆数据
     * 
     * Part 1 核心档案：
     * - 姓名：王雷(Lei)，上海，高级产品经理@恒聚愿景公司
     * - 健康：180cm/78kg/O型血，花生过敏
     * - 家庭：已婚，伴侣(教师，5月20日生日)，孩子(3岁，6月1日生日)
     * 
     * Part 2 偏好设定：
     * - 饮品：每天至少两杯美式咖啡(不加糖不加奶)
     * - 餐饮：工作日午餐注重营养，晚餐回家吃，喜欢精酿啤酒
     * - 运动：每周至少两次力量训练
     * - 购物：电子产品看重性能，服装偏好始祖鸟、Lululemon
     * - 旅行：每年一次长途家庭海岛度假
     * - 读书：商业、科技、历史类英文原版
     * 
     * Part 3 目标设定：
     * - 将血压控制在正常范围内(2025/12/31)
     * - 为妻子策划结婚20周年纪念旅行(2026/3/15)
     * - 带全家去北京看升旗(2026/8/1)
     */
    @Test
    @Order(1)
    void testInitializeWangLeiMemories() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("【角色1】初始化王雷 - \"进击的互联网人\" 的记忆数据");
        System.out.println("=".repeat(100));

        String groupId = "groupId_wanglei_001";
        String userId = "userId_wanglei_001";
        MemoryUtils.deleteMemories(userId, groupId);

        System.out.println("用户ID: " + userId);
        System.out.println("群组ID: " + groupId);
        System.out.println("城市: 上海");
        System.out.println("职业: 高级产品经理 @ 恒聚愿景公司");

        try {
            // 1. 保存对话元数据
            System.out.println("\n【步骤1】保存对话元数据...");
            saveWangLeiConversationMeta(groupId, userId);
            Thread.sleep(2000);

            // 2. 初始化记忆
            System.out.println("\n【步骤2】初始化记忆数据...");
            initializeWangLeiMemories(groupId, userId);

            System.out.println("\n" + "=".repeat(100));
            System.out.println("✅ 王雷的记忆初始化完成！");
            System.out.println("=".repeat(100));

        } catch (Exception e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveWangLeiConversationMeta(String groupId, String userId) {
        Map<String, ConversationMetaRequest.UserDetail> userDetails = new HashMap<>();
        
        // 王雷的信息
        Map<String, Object> userExtra = new HashMap<>();
        userExtra.put("city", "上海");
        userExtra.put("occupation", "高级产品经理");
        userExtra.put("company", "恒聚愿景公司");
        userExtra.put("height", "180cm");
        userExtra.put("weight", "78kg");
        userExtra.put("blood_type", "O型");
        userExtra.put("allergy", "花生");
        userExtra.put("marital_status", "已婚");
        userExtra.put("spouse_birthday", "5月20日");
        userExtra.put("spouse_occupation", "教师");
        userExtra.put("child_age", "3岁");
        userExtra.put("child_birthday", "6月1日");
        
        ConversationMetaRequest.UserDetail userDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("王雷(Lei)")
                .role("user")
                .extra(userExtra)
                .build();
        userDetails.put(userId, userDetail);

        // 助手信息
        Map<String, Object> assistantExtra = new HashMap<>();
        assistantExtra.put("type", "personal_assistant");
        assistantExtra.put("persona", "进击的互联网人");
        
        ConversationMetaRequest.UserDetail assistantDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("智能助手")
                .role("assistant")
                .extra(assistantExtra)
                .build();
        userDetails.put(userId, assistantDetail);

        ConversationMetaRequest request = ConversationMetaRequest.builder()
                .version("1.0.0")
                .scene("assistant")
                .sceneDesc(Collections.singletonMap("description", "王雷的个人助理对话"))
                .name("王雷 - 进击的互联网人")
                .description("高级产品经理王雷与智能助手关于工作、生活、健康、家庭等主题的对话记录")
                .groupId(groupId)
                .createdAt(Instant.now().toString())
                .defaultTimezone("Asia/Shanghai")
                .userDetails(userDetails)
                .tags(Arrays.asList("互联网", "产品经理", "健康管理", "家庭生活", "目标规划"))
                .build();

        MemoryUtils.saveConversationMeta(request);
        System.out.println("✓ 对话元数据已保存");
    }

    private void initializeWangLeiMemories(String groupId, String userId) throws InterruptedException {
        Instant baseTime = Instant.parse("2025-01-15T09:00:00Z");
        int messageCount = 0;

        // Part 1: 核心档案相关对话
        System.out.println("\n场景1: 基本信息确认");
        messageCount = storeWangLeiBasicInfoScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // Part 2: 偏好设定 - 咖啡习惯
        System.out.println("\n场景2: 咖啡偏好确认");
        baseTime = baseTime.plus(2, ChronoUnit.HOURS);
        messageCount = storeWangLeiCoffeePreferenceScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // Part 2: 偏好设定 - 运动习惯
        System.out.println("\n场景3: 运动习惯讨论");
        baseTime = baseTime.plus(1, ChronoUnit.DAYS);
        messageCount = storeWangLeiExerciseScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // Part 2: 偏好设定 - 家庭晚餐
        System.out.println("\n场景4: 家庭与餐饮偏好");
        baseTime = baseTime.plus(2, ChronoUnit.DAYS);
        messageCount = storeWangLeiFamilyDiningScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // Part 3: 目标设定 - 健康目标
        System.out.println("\n场景5: 血压控制目标");
        baseTime = baseTime.plus(3, ChronoUnit.DAYS);
        messageCount = storeWangLeiHealthGoalScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // Part 3: 目标设定 - 旅行计划
        System.out.println("\n场景6: 结婚纪念日旅行规划");
        baseTime = baseTime.plus(7, ChronoUnit.DAYS);
        messageCount = storeWangLeiAnniversaryTripScenario(groupId, userId, baseTime, messageCount);

        System.out.println("\n总计发送 " + messageCount + " 条消息");
    }

    private int storeWangLeiBasicInfoScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "你好，我是王雷，在上海工作，目前是恒聚愿景公司的高级产品经理。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "您好王雷！很高兴认识您。我已经记录了您的基本信息：上海，高级产品经理，恒聚愿景公司。" +
                "我会作为您的智能助手，帮助您管理工作和生活的各个方面。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "对了，我有花生过敏，这点需要特别注意。另外我已婚，有一个3岁的孩子。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "非常重要的信息！我已经记录：您对花生过敏（这是最高优先级的安全信息），已婚，有一个3岁的孩子。" +
                "在为您推荐餐厅或食物时，我会严格避免任何含有花生成分的选项。");

        return count;
    }

    private int storeWangLeiCoffeePreferenceScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "我每天工作都需要喝咖啡提神，一般至少要喝两杯美式。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "了解！咖啡是您的工作必需品。您偏好美式咖啡吗？对糖和奶有什么要求吗？");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(30, ChronoUnit.SECONDS), Memory.Role.HUMAN, count++,
                "我喜欢纯黑咖啡，不加糖也不加奶。偶尔也会尝试一些精品手冲咖啡。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(40, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "明白了！我已记录您的咖啡偏好：每天至少两杯美式，不加糖不加奶，对精品手冲咖啡有兴趣。" +
                "我可以在上午和午后适时提醒您补充咖啡，也可以为您推荐附近的精品咖啡店。");

        return count;
    }

    private int storeWangLeiExerciseScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "最近想保持规律的运动习惯，主要是为了维持精力。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "保持运动是个很好的习惯！您平时偏好什么类型的运动呢？");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "我主要做力量训练，目标是每周至少两次。不是为了专业增肌，就是想保持状态。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "很好的运动规划！我已记录：每周至少两次力量训练，目的是维持精力而非专业增肌。" +
                "我可以帮您制定每周的训练提醒，并推荐一些高效的力量训练动作。");

        return count;
    }

    private int storeWangLeiFamilyDiningScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "工作日午餐我一般在公司附近解决，比较注重营养均衡。晚餐都是回家吃的。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "理解！工作日午餐注重营养，晚餐是家庭时间。我会在推荐午餐时优先考虑营养均衡的选项，" +
                "晚餐时段我会避免安排非必要的提醒，给您留出家庭时间。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "对，晚餐是家庭时间。不过周末偶尔会和朋友出去喝精酿啤酒。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "已记录！周末您会和朋友聚会，喜欢精酿啤酒。当您需要安排朋友聚会时，" +
                "我可以为您推荐有格调的精酿啤酒吧或适合深度交流的餐厅。");

        return count;
    }

    private int storeWangLeiHealthGoalScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "最近体检发现血压有点偏高，我想在今年年底前把血压控制到正常范围。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "这是一个很重要的健康目标！将血压控制在正常范围内需要从多方面着手：" +
                "规律运动、控制饮食（低盐低油）、保证睡眠、减轻压力。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "对，我打算从饮食和运动两方面来调整。目标是2025年12月31日前达标。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "已记录您的健康目标：2025年12月31日前将血压控制在正常范围内。" +
                "我会：1) 在午餐推荐时优先选择低盐低油的健康选项；2) 提醒您保持每周两次的力量训练；" +
                "3) 定期提醒您测量血压并记录数据；4) 在目标日期前提醒您进行体检复查。");

        return count;
    }

    private int storeWangLeiAnniversaryTripScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "2026年是我和妻子结婚20周年，我想策划一次特别的纪念旅行。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "结婚20周年是个重要的里程碑！您有心仪的目的地吗？");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "还没定，但我们每年都会去海岛度假放松，这次想要更特别一些。目标是2026年3月15日前敲定方案。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "太好了！我已记录这个重要目标：2026年3月15日前为妻子策划结婚20周年纪念旅行。" +
                "基于您的偏好（海岛度假、放松为主），我会为您：" +
                "1) 推荐几个适合家庭的高端海岛度假目的地；" +
                "2) 帮您规划行程安排；3) 在伴侣生日（5月20日）前提醒您提前告知这个惊喜。");

        return count;
    }

    // ==================== 角色2: 李静 - "精致生活博主" ====================

    /**
     * 初始化李静的记忆数据
     * 
     * Part 1 核心档案：
     * - 姓名：李静，精致生活博主
     * - 特点：注重生活品质，热爱分享
     * 
     * Part 2 偏好设定：
     * - 内容创作、时尚美妆、美食探店
     * - 社交媒体运营
     * 
     * Part 3 目标设定：
     * - 粉丝增长目标
     * - 内容质量提升
     */
    @Test
    @Order(2)
    void testInitializeLiJingMemories() throws InterruptedException {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("【角色2】初始化李静 - \"精致生活博主\" 的记忆数据");
        System.out.println("=".repeat(100));

        String groupId = "groupId_lijing_001";
        String userId = "userId_lijing_001";
        MemoryUtils.deleteMemories(userId, groupId);

        System.out.println("用户ID: " + userId);
        System.out.println("群组ID: " + groupId);
        System.out.println("职业: 精致生活博主");

        try {
            // 1. 保存对话元数据
            System.out.println("\n【步骤1】保存对话元数据...");
            saveLiJingConversationMeta(groupId, userId);
            Thread.sleep(2000);

            // 2. 初始化记忆
            System.out.println("\n【步骤2】初始化记忆数据...");
            initializeLiJingMemories(groupId, userId);

            System.out.println("\n" + "=".repeat(100));
            System.out.println("✅ 李静的记忆初始化完成！");
            System.out.println("=".repeat(100));

        } catch (Exception e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveLiJingConversationMeta(String groupId, String userId) {
        Map<String, ConversationMetaRequest.UserDetail> userDetails = new HashMap<>();
        
        // 李静的信息
        Map<String, Object> userExtra = new HashMap<>();
        userExtra.put("occupation", "精致生活博主");
        userExtra.put("content_focus", "时尚、美妆、美食、生活方式");
        userExtra.put("platform", "小红书、微博、抖音");
        
        ConversationMetaRequest.UserDetail userDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("李静")
                .role("user")
                .extra(userExtra)
                .build();
        userDetails.put(userId, userDetail);

        // 助手信息
        Map<String, Object> assistantExtra = new HashMap<>();
        assistantExtra.put("type", "content_assistant");
        assistantExtra.put("persona", "精致生活博主");
        
        ConversationMetaRequest.UserDetail assistantDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("内容助手")
                .role("assistant")
                .extra(assistantExtra)
                .build();
        userDetails.put(userId, assistantDetail);

        ConversationMetaRequest request = ConversationMetaRequest.builder()
                .version("1.0.0")
                .scene("assistant")
                .sceneDesc(Collections.singletonMap("description", "李静的内容创作助手对话"))
                .name("李静 - 精致生活博主")
                .description("生活博主李静与内容助手关于内容创作、时尚美妆、探店等主题的对话记录")
                .groupId(groupId)
                .createdAt(Instant.now().toString())
                .defaultTimezone("Asia/Shanghai")
                .userDetails(userDetails)
                .tags(Arrays.asList("生活博主", "内容创作", "时尚美妆", "美食探店", "社交媒体"))
                .build();

        MemoryUtils.saveConversationMeta(request);
        System.out.println("✓ 对话元数据已保存");
    }

    private void initializeLiJingMemories(String groupId, String userId) throws InterruptedException {
        Instant baseTime = Instant.parse("2025-01-20T10:00:00Z");
        int messageCount = 0;

        // 场景1: 博主身份介绍
        System.out.println("\n场景1: 博主身份介绍");
        messageCount = storeLiJingIntroScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景2: 内容创作讨论
        System.out.println("\n场景2: 内容创作计划");
        baseTime = baseTime.plus(3, ChronoUnit.HOURS);
        messageCount = storeLiJingContentCreationScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景3: 探店计划
        System.out.println("\n场景3: 探店计划");
        baseTime = baseTime.plus(2, ChronoUnit.DAYS);
        messageCount = storeLiJingStoreVisitScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景4: 粉丝互动
        System.out.println("\n场景4: 粉丝增长目标");
        baseTime = baseTime.plus(5, ChronoUnit.DAYS);
        messageCount = storeLiJingFansGrowthScenario(groupId, userId, baseTime, messageCount);

        System.out.println("\n总计发送 " + messageCount + " 条消息");
    }

    private int storeLiJingIntroScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "你好！我是李静，是一名精致生活博主，主要在小红书、微博和抖音分享内容。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "您好李静！很高兴认识您。作为精致生活博主，您在多个平台都有运营。" +
                "我可以帮助您管理内容创作、规划探店行程、分析数据等。您主要分享哪些类型的内容呢？");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "我主要分享时尚穿搭、美妆护肤、美食探店和生活方式相关的内容。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "明白了！您的内容领域包括：时尚穿搭、美妆护肤、美食探店和生活方式。" +
                "我会根据这些领域为您提供内容创意、热点追踪和数据分析支持。");

        return count;
    }

    private int storeLiJingContentCreationScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "这周想做一期春季穿搭合集，需要策划一下拍摄内容。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "春季穿搭是个很应季的话题！我可以帮您：1) 分析当前春季穿搭的热门趋势；" +
                "2) 规划不同场景的穿搭组合（通勤、约会、周末）；3) 建议拍摄地点和时间段。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "太好了！我想主打轻熟风格，色彩以温柔的莫兰迪色系为主。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "很好的定位！轻熟风+莫兰迪色系非常适合春季。我会为您准备：" +
                "1) 5-7套穿搭方案；2) 推荐拍摄地点（适合莫兰迪色调的场景）；" +
                "3) 文案撰写建议；4) 最佳发布时间分析。");

        return count;
    }

    private int storeLiJingStoreVisitScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "最近市中心新开了几家网红餐厅，我想去探店做内容。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "探店是很好的内容选题！我可以帮您筛选热度高、出片率好的餐厅，" +
                "并提供：1) 餐厅背景介绍；2) 招牌菜品推荐；3) 最佳拍摄角度建议；4) 预约和优惠信息。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "好的，我偏好那种装修有特色、菜品颜值高的店。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "已记录您的探店偏好：装修有特色、菜品颜值高。我会优先推荐符合这些标准的餐厅。" +
                "同时我也会关注这些店在社交媒体上的热度，帮您选择流量潜力大的探店对象。");

        return count;
    }

    private int storeLiJingFansGrowthScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "目前小红书粉丝5万，希望今年能突破10万。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "从5万到10万是一个重要的增长阶段！我会帮您：1) 分析当前内容的数据表现；" +
                "2) 优化发布策略（时间、频率、标签）；3) 追踪热点话题；4) 提升互动率。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "好的，我希望在保证内容质量的前提下实现增长。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "完全理解！质量永远是第一位的。我已记录您的目标：今年小红书粉丝从5万增长到10万。" +
                "我会每周为您生成数据报告，包括粉丝增长曲线、爆款内容分析、最佳发布时段等，" +
                "帮助您在保持内容品质的同时实现稳步增长。");

        return count;
    }

    // ==================== 角色3: 张伟 - "务实的家庭主义者" ====================

    /**
     * 初始化张伟的记忆数据
     * 
     * Part 1 核心档案：
     * - 姓名：张伟，务实的家庭主义者
     * - 特点：重视家庭、务实稳健
     * 
     * Part 2 偏好设定：
     * - 家庭生活、子女教育
     * - 性价比消费
     * 
     * Part 3 目标设定：
     * - 子女教育目标
     * - 家庭财务规划
     */
    @Test
    @Order(3)
    void testInitializeZhangWeiMemories() throws InterruptedException {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("【角色3】初始化张伟 - \"务实的家庭主义者\" 的记忆数据");
        System.out.println("=".repeat(100));

        String groupId = "groupId_zhangwei_001";
        String userId = "userId_zhangwei_001";
        MemoryUtils.deleteMemories(userId, groupId);

        System.out.println("用户ID: " + userId);
        System.out.println("群组ID: " + groupId);
        System.out.println("性格: 务实的家庭主义者");

        try {
            // 1. 保存对话元数据
            System.out.println("\n【步骤1】保存对话元数据...");
            saveZhangWeiConversationMeta(groupId, userId);
            Thread.sleep(2000);

            // 2. 初始化记忆
            System.out.println("\n【步骤2】初始化记忆数据...");
            initializeZhangWeiMemories(groupId, userId);

            System.out.println("\n" + "=".repeat(100));
            System.out.println("✅ 张伟的记忆初始化完成！");
            System.out.println("=".repeat(100));

        } catch (Exception e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveZhangWeiConversationMeta(String groupId, String userId) {
        Map<String, ConversationMetaRequest.UserDetail> userDetails = new HashMap<>();
        
        // 张伟的信息
        Map<String, Object> userExtra = new HashMap<>();
        userExtra.put("personality", "务实的家庭主义者");
        userExtra.put("values", "家庭第一、稳健务实、性价比优先");
        userExtra.put("family_focus", "子女教育、家庭生活");
        
        ConversationMetaRequest.UserDetail userDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("张伟")
                .role("user")
                .extra(userExtra)
                .build();
        userDetails.put(userId, userDetail);

        // 助手信息
        Map<String, Object> assistantExtra = new HashMap<>();
        assistantExtra.put("type", "family_assistant");
        assistantExtra.put("persona", "务实的家庭主义者");
        
        ConversationMetaRequest.UserDetail assistantDetail = ConversationMetaRequest.UserDetail.builder()
                .fullName("家庭助手")
                .role("assistant")
                .extra(assistantExtra)
                .build();
        userDetails.put(userId, assistantDetail);

        ConversationMetaRequest request = ConversationMetaRequest.builder()
                .version("1.0.0")
                .scene("assistant")
                .sceneDesc(Collections.singletonMap("description", "张伟的家庭助手对话"))
                .name("张伟 - 务实的家庭主义者")
                .description("张伟与家庭助手关于家庭生活、子女教育、财务规划等主题的对话记录")
                .groupId(groupId)
                .createdAt(Instant.now().toString())
                .defaultTimezone("Asia/Shanghai")
                .userDetails(userDetails)
                .tags(Arrays.asList("家庭生活", "子女教育", "务实稳健", "性价比", "财务规划"))
                .build();

        MemoryUtils.saveConversationMeta(request);
        System.out.println("✓ 对话元数据已保存");
    }

    private void initializeZhangWeiMemories(String groupId, String userId) throws InterruptedException {
        Instant baseTime = Instant.parse("2025-01-22T19:00:00Z");
        int messageCount = 0;

        // 场景1: 家庭主义者介绍
        System.out.println("\n场景1: 家庭价值观介绍");
        messageCount = storeZhangWeiIntroScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景2: 子女教育关注
        System.out.println("\n场景2: 子女教育规划");
        baseTime = baseTime.plus(1, ChronoUnit.DAYS);
        messageCount = storeZhangWeiEducationScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景3: 性价比购物
        System.out.println("\n场景3: 务实消费观念");
        baseTime = baseTime.plus(3, ChronoUnit.DAYS);
        messageCount = storeZhangWeiShoppingScenario(groupId, userId, baseTime, messageCount);
        Thread.sleep(1000);

        // 场景4: 家庭活动安排
        System.out.println("\n场景4: 家庭活动计划");
        baseTime = baseTime.plus(5, ChronoUnit.DAYS);
        messageCount = storeZhangWeiFamilyActivityScenario(groupId, userId, baseTime, messageCount);

        System.out.println("\n总计发送 " + messageCount + " 条消息");
    }

    private int storeZhangWeiIntroScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "你好，我是张伟。对我来说，家庭是最重要的，我做任何决定都会优先考虑家人。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "您好张伟！很高兴认识您。我理解家庭对您来说是第一位的。" +
                "作为您的家庭助手，我会从家庭的角度出发，帮助您做好生活规划和决策支持。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "我的性格比较务实，不喜欢冒险，做事喜欢稳妥一点。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "明白了！您是务实稳健的性格。我会在为您提供建议时，优先考虑稳妥的方案，" +
                "并为您详细分析各种选择的利弊，帮助您做出最符合家庭利益的决策。");

        return count;
    }

    private int storeZhangWeiEducationScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "孩子的教育是我最关心的事情，我想给孩子最好的教育资源。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "子女教育确实是家庭的重中之重。您的孩子现在多大了？对教育方面有什么具体的规划吗？");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "孩子今年要上小学了，我在考察学区和培训班。希望能给孩子一个好的起点。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "幼升小是个重要阶段！我可以帮您：1) 对比周边学区的教育质量和口碑；" +
                "2) 分析各类培训班的性价比和效果；3) 规划孩子的学习时间表；" +
                "4) 提供科学的教育理念和方法建议。最重要的是找到适合您孩子的教育方式。");

        return count;
    }

    private int storeZhangWeiShoppingScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "买东西我都会仔细比较，看重性价比。不追求品牌，但质量一定要好。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "非常务实的消费观！我理解您注重性价比，追求实用和质量。" +
                "我会在为您推荐商品时，重点关注：1) 用户评价和使用反馈；2) 性能参数和质量保证；" +
                "3) 价格对比和优惠信息；4) 售后服务政策。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "对，我经常在618和双十一这些大促的时候囤货，能省不少钱。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "这是个很好的省钱策略！我会在618、双十一等大促期间：" +
                "1) 提前帮您整理购物清单；2) 追踪商品的价格历史，确保确实优惠；" +
                "3) 研究满减和优惠券的最优组合；4) 提醒您在最佳时机下单。");

        return count;
    }

    private int storeZhangWeiFamilyActivityScenario(String groupId, String userId, Instant baseTime, int count) throws InterruptedException {
        storeMessage(groupId, userId, baseTime, Memory.Role.HUMAN, count++,
                "周末我一般都陪家人，带孩子去公园玩，或者全家一起看电影。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "周末的家庭时光很宝贵！我可以帮您：1) 推荐适合全家的户外活动地点；" +
                "2) 筛选适合孩子观看的优质电影；3) 规划周末家庭活动行程；4) 提供亲子互动游戏建议。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES), Memory.Role.HUMAN, count++,
                "好的，我希望能多花时间陪伴家人，这比什么都重要。");
        Thread.sleep(500);

        storeMessage(groupId, userId, baseTime.plus(1, ChronoUnit.MINUTES).plus(10, ChronoUnit.SECONDS), Memory.Role.AI, count++,
                "您的家庭观念非常好！陪伴确实是给家人最好的礼物。" +
                "我会帮您守护好周末的家庭时光，合理安排工作和生活，确保您有充足的时间陪伴家人。" +
                "同时我也会为您推荐一些能增进家庭感情的活动和话题。");

        return count;
    }

    // ==================== 工具方法 ====================

    /**
     * 存储消息的通用方法
     */
    private void storeMessage(String groupId, String senderId, Instant timestamp, 
                            Memory.Role role, int index, String content) {
        String messageId = "msg_" + groupId + "_" + index + "_" + System.currentTimeMillis();
        String senderName = role == Memory.Role.HUMAN ? "User" : "Assistant";
        
        ApiResponse<StoreMessageResponse> response = MemoryUtils.storeMessage(
                messageId,
                timestamp.toString(),
                senderId,
                content,
                groupId,
                "Persona Memory Init",
                senderName,
                role,
                Collections.emptyList(),
                true  // 使用同步模式
        );
        
        if (response != null && MemoryUtils.isSuccess(response)) {
            StoreMessageResponse result = MemoryUtils.getResult(response);
            System.out.println(String.format("  [%d] %s: %s... (状态: %s, 记忆数: %d)",
                    index + 1,
                    role == Memory.Role.HUMAN ? "用户" : "助手",
                    content.length() > 40 ? content.substring(0, 40) : content,
                    result.getStatusInfo(),
                    result.getCount()));
        } else {
            System.err.println(String.format("  [%d] ✗ 消息存储失败", index + 1));
        }
    }
}
