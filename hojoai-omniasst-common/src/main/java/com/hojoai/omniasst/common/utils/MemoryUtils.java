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

import com.hojoai.omniasst.common.api.memory.client.MemoryApiClient;
import com.hojoai.omniasst.common.api.memory.dto.Memory;
import com.hojoai.omniasst.common.api.memory.dto.StoreMessageDTO;
import com.hojoai.omniasst.common.api.memory.dto.request.*;
import com.hojoai.omniasst.common.api.memory.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Memory API 静态工具类，封装对 {@link MemoryApiClient} 的常用调用。
 * <p>
 * 依赖 Spring 容器中的 {@code MemoryApiClient} Bean（由 {@code MemoryAutoConfiguration} 注册），
 * 通过 {@link SpringContextHolder} 获取；适用于业务服务在任意层直接调用记忆存储、检索与元数据管理。
 * </p>
 */
@Slf4j
public class MemoryUtils {
    /**
     * 拉取记忆列表时默认按该字段排序，对应服务端 sort_by 参数。
     */
    public static final String DEFAULT_SORT_BY = "created_at";
    
    /**
     * 拉取记忆列表时默认排序方向（时间从新到旧）。
     */
    public static final String DEFAULT_SORT_ORDER = "desc";
    
    /**
     * 单次拉取记忆的默认条数上限（与服务端 limit 默认一致）。
     */
    public static final int DEFAULT_LIMIT = 40;
    
    /**
     * 分页拉取记忆时的默认起始偏移。
     */
    public static final int DEFAULT_OFFSET = 0;
    
    /**
     * 关键词/向量检索时默认返回的候选条数（与服务端 top_k 默认一致）。
     */
    public static final int DEFAULT_TOP_K = 40;

    /**
     * 私有构造函数，禁止实例化
     * 本类只提供静态方法
     */
    private MemoryUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 从 Spring 容器解析 {@link MemoryApiClient}。
     *
     * @return 已注册的 Feign 客户端实例
     * @throws IllegalStateException 当容器未就绪或未装配 Memory API 时
     */
    private static MemoryApiClient getMemoryApiClient() {
        try {
            return SpringContextHolder.getBean(MemoryApiClient.class);
        } catch (Exception e) {
            throw new IllegalStateException("无法获取 MemoryApiClient，请确保：\n" +
                    "1. Spring 容器已启动\n" +
                    "2. hojoai-omniasst-common 已正确引入\n" +
                    "3. Memory API 配置正确 (hojoai.memory.api.enabled=true)", e);
        }
    }

    /**
     * 组装存储消息请求体，并校验必填字段。
     *
     * @param messageId   消息唯一标识
     * @param createTime  创建时间（ISO-8601 字符串）
     * @param sender      发送方 ID
     * @param content     正文
     * @param groupId     会话/群组作用域 ID（已与 user 维度拼接后的值）
     * @param groupName   群组展示名
     * @param senderName  发送方展示名
     * @param role        人类或助手角色
     * @param referList   引用消息 ID 列表，可为 null（将转为空列表）
     * @return 构建完成的请求对象
     * @throws RuntimeException 任一必填参数为空时
     */
    private static StoreMessageRequest buildStoreMessageRequest(String messageId, String createTime,
                                                                  String sender, String content,
                                                                  String groupId, String groupName,
                                                                  String senderName, Memory.Role role,
                                                                  List<String> referList) {
        if (StringUtils.isBlank(messageId)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest messageId is blank.");
        }
        if (StringUtils.isBlank(createTime)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest createTime is blank.");
        }
        if (StringUtils.isBlank(sender)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest sender is blank.");
        }
        if (StringUtils.isBlank(content)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest content is blank.");
        }
        if (StringUtils.isBlank(groupId)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest groupId is blank.");
        }
        if (StringUtils.isBlank(groupName)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest groupName is blank.");
        }
        if (StringUtils.isBlank(senderName)) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest senderName is blank.");
        }
        if (role == null) {
            throw new RuntimeException("MemoryUtils buildStoreMessageRequest role is null.");
        }
        return StoreMessageRequest.builder()
                .messageId(messageId)
                .createTime(createTime)
                .sender(sender)
                .content(content)
                .groupId(groupId)
                .groupName(groupName)
                .senderName(senderName)
                .role(role.getRoleName())
                .referList(referList != null ? referList : Collections.emptyList())
                .build();
    }

    /**
     * 将记忆类型枚举列表序列化为接口所需的逗号分隔字符串。
     *
     * @param memoryTypes 记忆类型集合，空则使用默认 episodic
     * @return 逗号分隔的类型名，供 search 接口 memory_types 使用
     */
    private static String convertMemoryTypesToString(List<Memory.MemoryType> memoryTypes) {
        if (memoryTypes == null || memoryTypes.isEmpty()) {
            return Memory.MemoryType.getDefaultMemoryType().getMemoryTypeName();
        }
        return memoryTypes.stream().map(Memory.MemoryType::getMemoryTypeName).collect(Collectors.joining(","));
    }

    /**
     * 批量异步存储消息（每条独立调用底层存储接口）。
     *
     * @param groupId               业务作用域/群组 ID
     * @param userId                发送者 ID
     * @param storeMessageDTOList   待写入的消息列表；为空则直接返回
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static void storeMessageList(String groupId, String userId, List<StoreMessageDTO> storeMessageDTOList) {
        log.info("Storing message list - groupId: {}, userId: {}, messageCount: {}", 
                groupId, userId, storeMessageDTOList != null ? storeMessageDTOList.size() : 0);
        if (CollectionUtils.isEmpty(storeMessageDTOList)) {
            log.info("Message list is empty, skipping storage");
            return;
        }
        storeMessageDTOList.forEach(messageDTO -> {
            log.info("Storing message - messageId: {}, role: {}, contentLength: {}", 
                    messageDTO.getMessageId(), messageDTO.getRole(), 
                    messageDTO.getContent() != null ? messageDTO.getContent().length() : 0);
            storeMessage(messageDTO.getMessageId(), Instant.now().toString(), userId, messageDTO.getContent(), groupId, groupId, userId, messageDTO.getRole(), Collections.emptyList(), false);
        });
        log.info("Message list storage initiated - groupId: {}, userId: {}, messageCount: {}", 
                groupId, userId, storeMessageDTOList.size());
    }

    /**
     * 存储单条消息（简化版本，异步）
     *
     * @param role 角色：HUMAN (用户Query) or AI (模型回复)
     * @param groupId：用来区分不同的作用域
     * @param userId    发送者ID
     * @param messageId 消息ID
     * @param content   消息内容
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static void storeMessage(Memory.Role role, String groupId, String userId, String messageId, String content) {
        log.info("Storing message (simplified, async) - messageId: {}, role: {}, groupId: {}, userId: {}, contentLength: {}", 
                messageId, role, groupId, userId, content != null ? content.length() : 0);
        storeMessage(messageId, Instant.now().toString(), userId, content, groupId, groupId, userId, role, Collections.emptyList(), false);
    }

    /**
     * 存储单条消息（简化版本，异步）
     *
     * @param role 角色：HUMAN (用户Query) or AI (模型回复)
     * @param groupId：用来区分不同的作用域
     * @param userId    发送者ID
     * @param messageId 消息ID
     * @param content   消息内容
     * @param referList  引用消息ID列表
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static void storeMessage(Memory.Role role, String groupId, String userId, String messageId, String content, List<String> referList) {
        log.info("Storing message (simplified, async, with referList) - messageId: {}, role: {}, groupId: {}, userId: {}, contentLength: {}, referListSize: {}", 
                messageId, role, groupId, userId, content != null ? content.length() : 0, 
                referList != null ? referList.size() : 0);
        storeMessage(messageId, Instant.now().toString(), userId, content, groupId, groupId, userId, role, referList, false);
    }

    /**
     * 存储单条消息（简化版本，支持同步模式）
     *
     * @param role 角色：HUMAN (用户Query) or AI (模型回复)
     * @param groupId：用来区分不同的作用域
     * @param userId    发送者ID
     * @param messageId 消息ID
     * @param content   消息内容
     * @param syncMode  是否同步处理（true=同步处理并返回响应，false=异步处理）
     * @return API响应（仅在syncMode=true时返回，否则返回null）
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<StoreMessageResponse> storeMessage(Memory.Role role, String groupId, String userId, String messageId, String content, boolean syncMode) {
        log.info("Storing message (simplified, syncMode: {}) - messageId: {}, role: {}, groupId: {}, userId: {}, contentLength: {}", 
                syncMode, messageId, role, groupId, userId, content != null ? content.length() : 0);
        ApiResponse<StoreMessageResponse> response = storeMessage(messageId, Instant.now().toString(), userId, content, groupId, groupId, userId, role, Collections.emptyList(), syncMode);
        if (syncMode && response != null) {
            log.info("Message stored (sync) - messageId: {}, success: {}, count: {}", 
                    messageId, isSuccess(response), 
                    response.getResult() != null ? response.getResult().getCount() : null);
        }
        return response;
    }

    /**
     * 存储单条消息（简化版本，支持同步模式）
     *
     * @param role 角色：HUMAN (用户Query) or AI (模型回复)
     * @param groupId：用来区分不同的作用域
     * @param userId    发送者ID
     * @param messageId 消息ID
     * @param content   消息内容
     * @param referList  引用消息ID列表
     * @param syncMode  是否同步处理（true=同步处理并返回响应，false=异步处理）
     * @return API响应（仅在syncMode=true时返回，否则返回null）
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<StoreMessageResponse> storeMessage(Memory.Role role, String groupId, String userId, String messageId, String content, List<String> referList, boolean syncMode) {
        log.info("Storing message (simplified, syncMode: {}, with referList) - messageId: {}, role: {}, groupId: {}, userId: {}, contentLength: {}, referListSize: {}", 
                syncMode, messageId, role, groupId, userId, content != null ? content.length() : 0, 
                referList != null ? referList.size() : 0);
        ApiResponse<StoreMessageResponse> response = storeMessage(messageId, Instant.now().toString(), userId, content, groupId, groupId, userId, role, referList, syncMode);
        if (syncMode && response != null) {
            log.info("Message stored (sync) - messageId: {}, success: {}, count: {}", 
                    messageId, isSuccess(response), 
                    response.getResult() != null ? response.getResult().getCount() : null);
        }
        return response;
    }

    /**
     * 存储单条消息（完整参数）
     *
     * @param messageId  消息ID
     * @param createTime 创建时间 (ISO 8601格式)
     * @param sender     发送者ID
     * @param content    消息内容
     * @param groupId    群组ID
     * @param groupName  群组名称
     * @param senderName 发送者名称
     * @param role       角色：HUMAN (用户Query) or AI (模型回复)
     * @param referList  引用消息ID列表
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static void storeMessage(String messageId, String createTime,
                                                                 String sender, String content,
                                                                 String groupId, String groupName,
                                                                 String senderName, Memory.Role role,
                                                                 List<String> referList) {
        log.info("Storing message (full params, async) - messageId: {}, createTime: {}, sender: {}, groupId: {}, groupName: {}, senderName: {}, role: {}, contentLength: {}, referListSize: {}", 
                messageId, createTime, sender, groupId, groupName, senderName, role, 
                content != null ? content.length() : 0, referList != null ? referList.size() : 0);
        storeMessage(messageId, createTime, sender, content, groupId, groupName, senderName, role, referList, false);
    }

    /**
     * 存储单条消息（完整参数，支持同步模式）
     *
     * @param messageId  消息ID
     * @param createTime 创建时间 (ISO 8601格式)
     * @param sender     发送者ID
     * @param content    消息内容
     * @param groupId    群组ID
     * @param groupName  群组名称
     * @param senderName 发送者名称
     * @param role       角色：HUMAN (用户Query) or AI (模型回复)
     * @param referList  引用消息ID列表
     * @param syncMode   是否同步处理（true=同步处理并返回响应，false=异步处理）
     * @return API响应（仅在syncMode=true时返回，否则返回null）
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<StoreMessageResponse> storeMessage(String messageId, String createTime,
                                                                 String sender, String content,
                                                                 String groupId, String groupName,
                                                                 String senderName, Memory.Role role,
                                                                 List<String> referList, boolean syncMode) {
        // 与服务端约定：group_id 需携带发送方维度，避免不同发送方在同一业务 group 下冲突
        groupId = groupId + sender;
        log.info("Storing message (full params, syncMode: {}) - messageId: {}, createTime: {}, sender: {}, groupId: {}, groupName: {}, senderName: {}, role: {}, contentLength: {}, referListSize: {}", 
                syncMode, messageId, createTime, sender, groupId, groupName, senderName, role, 
                content != null ? content.length() : 0, referList != null ? referList.size() : 0);
        
        StoreMessageRequest request = buildStoreMessageRequest(
                messageId, createTime, sender, content,
                groupId, groupName, senderName, role, referList
        );

        if (syncMode) {
            // 同步模式：直接调用并返回响应
            ApiResponse<StoreMessageResponse> response = getMemoryApiClient().storeMessage(request, true);
            log.info("Message stored (sync) - messageId: {}, success: {}, count: {}, statusInfo: {}", 
                    messageId, isSuccess(response), 
                    response != null && response.getResult() != null ? response.getResult().getCount() : null,
                    response != null && response.getResult() != null ? response.getResult().getStatusInfo() : null);
            return response;
        } else {
            log.info("Initiating async message storage - messageId: {}", messageId);
            Thread.startVirtualThread(()-> getMemoryApiClient().storeMessage(request, false));
            return null;
        }
    }

    /**
     * 获取记忆-按类型检索记忆，并可选择使用筛选条件。（简化版本）
     *
     * @param userId  用户ID
     * @param groupId 群组ID
     * @return API响应
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<FetchMemoriesResponse> fetchMemories(String userId, String groupId) {
        log.info("Fetching memories (simplified) - userId: {}, groupId: {}", userId, groupId);
        ApiResponse<FetchMemoriesResponse> response = fetchMemories(userId, groupId, Memory.MemoryType.getDefaultMemoryType(), DEFAULT_LIMIT, DEFAULT_OFFSET, null, null, null);
        log.info("Memories fetched (simplified) - userId: {}, groupId: {}, success: {}, totalCount: {}", 
                userId, groupId, isSuccess(response), 
                response != null && response.getResult() != null ? response.getResult().getTotalCount() : null);
        return response;
    }

    /**
     * 获取记忆-按类型检索记忆，并可选择使用筛选条件。（简化版本）
     *
     * @param userId  用户ID
     * @param groupId 群组ID
     * @return API响应
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<FetchMemoriesResponse> fetchMemories(String userId, String groupId, Memory.MemoryType memoryType) {
        log.info("Fetching memories (simplified, with memoryType) - userId: {}, groupId: {}, memoryType: {}", 
                userId, groupId, memoryType);
        ApiResponse<FetchMemoriesResponse> response = fetchMemories(userId, groupId, memoryType, DEFAULT_LIMIT, DEFAULT_OFFSET, null, null, null);
        log.info("Memories fetched (simplified, with memoryType) - userId: {}, groupId: {}, memoryType: {}, success: {}, totalCount: {}", 
                userId, groupId, memoryType, isSuccess(response), 
                response != null && response.getResult() != null ? response.getResult().getTotalCount() : null);
        return response;
    }

    /**
     * 获取记忆-按类型检索记忆，并可选择使用筛选条件。（完整参数）
     *
     * @param userId     用户ID
     * @param groupId    群组ID
     * @param memoryType 记忆类型 (默认: episodic_memory)
     * @param limit      限制数量 (默认: 40, 最大: 500)
     * @param offset     偏移量 (默认: 0)
     * @param startTime  起始时间筛选，可为 null
     * @param endTime    结束时间筛选，可为 null
     * @param versionRange 版本范围筛选，可为 null
     * @return API响应
     * @throws RuntimeException userId 或 groupId 为空时
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<FetchMemoriesResponse> fetchMemories(String userId, String groupId, Memory.MemoryType memoryType, Integer limit, Integer offset, String startTime, String endTime, String versionRange) {
        // 拉取记忆时 group_id 需拼接 user，与存储侧约定一致
        groupId = groupId + userId;
        log.info("Fetching memories (full params) - userId: {}, groupId: {}, memoryType: {}, limit: {}, offset: {}, startTime: {}, endTime: {}, versionRange: {}", 
                userId, groupId, memoryType, limit, offset, startTime, endTime, versionRange);
        
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("MemoryUtils fetchMemories userId is blank.");
        }
        if (StringUtils.isBlank(groupId)) {
            throw new RuntimeException("MemoryUtils fetchMemories groupId is blank.");
        }
        if (memoryType == null) {
            memoryType = Memory.MemoryType.getDefaultMemoryType();
        }
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (offset == null) {
            offset = DEFAULT_OFFSET;
        }
        
        ApiResponse<FetchMemoriesResponse> response = getMemoryApiClient().fetchMemories(userId, groupId, memoryType.getMemoryTypeName(), limit, offset, DEFAULT_SORT_BY, DEFAULT_SORT_ORDER, startTime, endTime, versionRange);
        
        log.info("Memories fetched (full params) - userId: {}, groupId: {}, memoryType: {}, success: {}, totalCount: {}", 
                userId, groupId, memoryType, isSuccess(response), 
                response != null && response.getResult() != null ? response.getResult().getTotalCount() : null);
        
        return response;
    }

    /**
     * 搜索记忆-使用关键词、向量或混合检索方法搜索记忆。（简化版本）
     *
     * @param query   搜索查询
     * @param userId  用户ID
     * @param groupId 群组ID
     * @return API响应
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<SearchMemoriesResponse> searchMemories(String query, String userId, String groupId) {
        log.info("Searching memories (simplified) - query: {}, userId: {}, groupId: {}", query, userId, groupId);
        ApiResponse<SearchMemoriesResponse> response = searchMemories(query, userId, groupId, Memory.RetrieveMethod.getDefaultRetrieveMethod(), Arrays.asList(Memory.MemoryType.getDefaultMemoryType()), DEFAULT_TOP_K, null, null, null, true, null);
        log.info("Memories searched (simplified) - query: {}, userId: {}, groupId: {}, success: {}, resultCount: {}", 
                query, userId, groupId, isSuccess(response), 
                response != null && response.getResult() != null && response.getResult().getMemories() != null ? response.getResult().getMemories().size() : null);
        return response;
    }

    /**
     * 搜索记忆-使用关键词、向量或混合检索方法搜索记忆。（完整参数）
     *
     * @param query          搜索查询
     * @param userId         用户ID
     * @param groupId        群组ID
     * @param retrieveMethod 检索方法 (keyword/vector/hybrid/rrf/agentic)
     * @param memoryTypes    记忆类型列表
     * @param topK           返回数量 (最大: 100)
     * @return API响应
     * @throws RuntimeException userId 或 groupId 为空时
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<SearchMemoriesResponse> searchMemories(String query, String userId, String groupId, Memory.RetrieveMethod retrieveMethod, List<Memory.MemoryType> memoryTypes, Integer topK,
                                                                     String startTime, String endTime, Double radius, Boolean includeMetadata, String currentTime) {
        // 检索时 group_id 与 fetch 一致，拼接 user 维度
        groupId = groupId + userId;
        log.info("Searching memories (full params) - query: {}, userId: {}, groupId: {}, retrieveMethod: {}, memoryTypes: {}, topK: {}, startTime: {}, endTime: {}, radius: {}, includeMetadata: {}, currentTime: {}", 
                query, userId, groupId, retrieveMethod, memoryTypes, topK, startTime, endTime, radius, includeMetadata, currentTime);
        
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("MemoryUtils fetchMemories userId is blank.");
        }
        if (StringUtils.isBlank(groupId)) {
            throw new RuntimeException("MemoryUtils fetchMemories groupId is blank.");
        }
        if (retrieveMethod == null) {
            retrieveMethod = Memory.RetrieveMethod.getDefaultRetrieveMethod();
        }
        
        ApiResponse<SearchMemoriesResponse> response = getMemoryApiClient().searchMemories(query, userId, groupId, retrieveMethod.getMethodName(),
                convertMemoryTypesToString(memoryTypes), topK != null ? topK : DEFAULT_TOP_K, startTime, endTime, radius, Boolean.TRUE.equals(includeMetadata), currentTime);
        
        log.info("Memories searched (full params) - query: {}, userId: {}, groupId: {}, retrieveMethod: {}, success: {}, resultCount: {}", 
                query, userId, groupId, retrieveMethod, isSuccess(response), 
                response != null && response.getResult() != null && response.getResult().getMemories() != null ? response.getResult().getMemories().size() : null);
        
        return response;
    }

    /**
     * 获取对话元数据
     *
     * @param groupId 群组ID (可选，不传则获取默认配置)
     * @return API响应
     * @throws RuntimeException groupId 为空时
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<ConversationMetaResponse> getConversationMeta(String groupId) {
        log.info("Getting conversation metadata - groupId: {}", groupId);
        if (StringUtils.isBlank(groupId)) {
            throw new RuntimeException("MemoryUtils fetchMemories groupId is blank.");
        }
        ApiResponse<ConversationMetaResponse> response = getMemoryApiClient().getConversationMeta(groupId);
        log.info("Conversation metadata retrieved - groupId: {}, success: {}", groupId, isSuccess(response));
        return response;
    }

    /**
     * 异步保存对话元数据（虚拟线程发起调用，不阻塞当前线程）。
     *
     * @param request 保存请求
     * @throws IllegalStateException 异步任务执行时若无法获取 {@link MemoryApiClient}
     */
    public static void saveConversationMeta(ConversationMetaRequest request) {
        log.info("Saving conversation metadata (async) - groupId: {}, request: {}", 
                request != null ? request.getGroupId() : null, request);
        Thread.startVirtualThread(()-> {
            log.info("Executing async save conversation metadata - groupId: {}", 
                    request != null ? request.getGroupId() : null);
            getMemoryApiClient().saveConversationMeta(request);
            log.info("Conversation metadata save completed - groupId: {}", 
                    request != null ? request.getGroupId() : null);
        });
    }

    /**
     * 调用 Memory 服务内部接口，初始化当前租户对应的数据库结构（多租户首次接入时使用）。
     *
     * @return 初始化结果，含 success、tenantId、message 等字段
     * @throws IllegalStateException 当无法从 Spring 容器获取 {@link MemoryApiClient} 时
     */
    public static ApiResponse<InitTenantDbResponse> initTenantDatabase() {
        return getMemoryApiClient().initTenantDatabase();
    }

    /**
     * 异步更新对话元数据（虚拟线程发起调用）。
     *
     * @param request 更新请求
     * @throws IllegalStateException 异步任务执行时若无法获取 {@link MemoryApiClient}
     */
    public static void updateConversationMeta(UpdateConversationMetaRequest request) {
        log.info("Updating conversation metadata (async) - groupId: {}, request: {}", 
                request != null ? request.getGroupId() : null, request);
        Thread.startVirtualThread(()-> {
            log.info("Executing async update conversation metadata - groupId: {}", 
                    request != null ? request.getGroupId() : null);
            getMemoryApiClient().updateConversationMeta(request);
            log.info("Conversation metadata update completed - groupId: {}", 
                    request != null ? request.getGroupId() : null);
        });
    }

    /**
     * 构建删除记忆请求；eventId 为空时表示删除该用户在该组下的全部事件（使用占位符 {@code __all__}）。
     *
     * @param userId  用户 ID
     * @param groupId 群组 ID
     * @param eventId 事件 ID，可为 null 表示全部
     * @return 删除请求体
     */
    private static DeleteMemoriesRequest buildDeleteMemoriesRequest(String userId, String groupId, String eventId) {
        return DeleteMemoriesRequest.builder()
                .userId(userId)
                .groupId(groupId)
                .eventId(StringUtils.isBlank(eventId) ? "__all__" : eventId)
                .build();
    }

    /**
     * 异步删除指定用户在群组下的全部记忆事件。
     *
     * @param userId  用户ID
     * @param groupId 群组ID
     * @throws IllegalStateException 异步任务执行时若无法获取 {@link MemoryApiClient}
     */
    public static void deleteMemories(String userId, String groupId) {
        log.info("Deleting memories (async, all events) - userId: {}, groupId: {}", userId, groupId);
        deleteMemories(userId, groupId, null);
    }

    /**
     * 异步删除记忆；可指定 eventId，否则删除该用户在该组下全部事件。
     *
     * @param userId  用户ID
     * @param groupId 群组ID
     * @param eventId 事件 ID，null 表示全部
     * @throws RuntimeException userId 或 groupId 为空时（在异步线程中抛出）
     * @throws IllegalStateException 异步任务执行时若无法获取 {@link MemoryApiClient}
     */
    public static void deleteMemories(String userId, String groupId, String eventId) {
        log.info("Deleting memories (async) - userId: {}, groupId: {}, eventId: {}", userId, groupId, eventId);
        Thread.startVirtualThread(() -> {
            log.info("Executing async delete memories - userId: {}, groupId: {}, eventId: {}", userId, groupId, eventId);
            if (StringUtils.isBlank(userId)) {
                throw new RuntimeException("MemoryUtils deleteMemories userId is blank.");
            }
            if (StringUtils.isBlank(groupId)) {
                throw new RuntimeException("MemoryUtils deleteMemories groupId is blank.");
            }
            DeleteMemoriesRequest request = buildDeleteMemoriesRequest(userId, groupId, eventId);
            log.info("Delete memories request built - userId: {}, groupId: {}, eventId: {}", 
                    userId, groupId, request.getEventId());
            getMemoryApiClient().deleteMemories(request);
            log.info("Memories deletion completed - userId: {}, groupId: {}, eventId: {}", userId, groupId, eventId);
        });
    }

    /**
     * 判断 Memory API 返回体是否表示成功（status 为 {@code ok}）。
     *
     * @param response API响应，可为 null
     * @return {@code true} 表示调用成功
     */
    public static boolean isSuccess(ApiResponse<?> response) {
        return response != null && response.isSuccess();
    }

    /**
     * 在响应成功时取出 {@code result} 载荷，失败或 null 时返回 null。
     *
     * @param response API响应
     * @param <T>      数据类型
     * @return 业务数据对象；非成功响应时为 null
     */
    public static <T> T getResult(ApiResponse<T> response) {
        return isSuccess(response) ? response.getResult() : null;
    }

    /**
     * 检查消息是否已处理成记忆
     * 
     * @param storeResponse 存储消息的响应
     * @return true 如果消息已处理（count > 0），false 如果还在队列中（accumulated）
     */
    public static boolean isMessageProcessed(ApiResponse<StoreMessageResponse> storeResponse) {
        if (!isSuccess(storeResponse)) {
            return false;
        }
        StoreMessageResponse result = getResult(storeResponse);
        if (result == null) {
            return false;
        }
        // 如果 count > 0，说明已有记忆生成
        // 如果 status_info 不是 "accumulated"，也可能表示已处理
        return result.getCount() > 0 || 
               (result.getStatusInfo() != null && !"accumulated".equals(result.getStatusInfo()));
    }

    /**
     * 等待记忆处理完成（轮询检查）
     * 
     * @param userId 用户ID
     * @param groupId 群组ID
     * @param maxWaitSeconds 最大等待时间（秒）
     * @param checkIntervalSeconds 检查间隔（秒）
     * @return true 如果记忆已生成，false 如果超时
     * @throws InterruptedException 如果等待被中断
     */
    public static boolean waitForMemoryProcessed(String userId, String groupId, 
                                                int maxWaitSeconds, int checkIntervalSeconds) 
            throws InterruptedException {
        int waited = 0;
        
        while (waited < maxWaitSeconds) {
            Thread.sleep(checkIntervalSeconds * 1000L);
            waited += checkIntervalSeconds;
            
            ApiResponse<FetchMemoriesResponse> response = fetchMemories(userId, groupId);
            
            if (isSuccess(response)) {
                FetchMemoriesResponse result = getResult(response);
                if (result != null && result.getTotalCount() > 0) {
                    return true; // 记忆已生成
                }
            }
        }
        
        return false; // 超时
    }

    /**
     * 等待记忆处理完成（使用默认参数：最大60秒，每5秒检查一次）
     * 
     * @param userId 用户ID
     * @param groupId 群组ID
     * @return true 如果记忆已生成，false 如果超时
     * @throws InterruptedException 如果等待被中断
     */
    public static boolean waitForMemoryProcessed(String userId, String groupId) 
            throws InterruptedException {
        return waitForMemoryProcessed(userId, groupId, 60, 5);
    }
}
