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
package com.hojoai.omniasst.common.api.memory.client;

import com.hojoai.omniasst.common.api.memory.config.MemoryFeignConfig;
import com.hojoai.omniasst.common.api.memory.dto.request.*;
import com.hojoai.omniasst.common.api.memory.dto.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Memory 服务的 OpenFeign 客户端，声明 {@code /api/v1/memories} 下 REST 接口。
 * <p>通过 {@link MemoryFeignConfig} 独立配置超时、日志与租户头，与宿主应用其他 Feign 客户端隔离。</p>
 */
@FeignClient(
        name = "memory-api",
        url = "${hojoai.memory.api.url:http://localhost:8001}",
        configuration = MemoryFeignConfig.class,
        path = "/api/v1/memories"
)
public interface MemoryApiClient {

    /**
     * POST /memories - 存储消息
     * @param request  消息体
     * @param syncMode   是否同步处理（true=同步处理，false=异步处理，默认false）
     * @return 存储结果封装
     */
    @PostMapping
    ApiResponse<StoreMessageResponse> storeMessage(
            @RequestBody StoreMessageRequest request,
            @RequestParam(value = "sync_mode", required = false, defaultValue = "false") Boolean syncMode
    );

    /**
     * GET /memories - 获取记忆（按类型检索记忆，并可选择使用筛选条件。）
     * @param userId        用户 ID
     * @param groupId       群组 ID
     * @param memoryType    记忆类型，默认 episodic_memory
     * @param limit         条数上限
     * @param offset        分页偏移
     * @param sortBy        排序字段
     * @param sortOrder     排序方向
     * @param startTime     起始时间
     * @param endTime       结束时间
     * @param versionRange  版本范围
     * @return 记忆列表与分页信息
     */
    @GetMapping
    ApiResponse<FetchMemoriesResponse> fetchMemories(
            @RequestParam(value = "user_id", required = false) String userId,
            @RequestParam(value = "group_id", required = false) String groupId,
            @RequestParam(value = "memory_type", required = false, defaultValue = "episodic_memory") String memoryType,
            @RequestParam(value = "limit", required = false, defaultValue = "40") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(value = "sort_order", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "start_time", required = false) String startTime,
            @RequestParam(value = "end_time", required = false) String endTime,
            @RequestParam(value = "version_range", required = false) String versionRange
    );

    /**
     * GET /memories/search - 搜索记忆（使用关键词、向量或混合检索方法搜索记忆。）
     * 注意：使用 Query String 参数，不是 Request Body
     * @param query             检索关键词或条件
     * @param userId            用户 ID
     * @param groupId           群组 ID
     * @param retrieveMethod    检索方式：keyword/vector 等
     * @param memoryTypes       记忆类型列表（逗号分隔）
     * @param topK              返回条数上限
     * @param startTime         起始时间
     * @param endTime           结束时间
     * @param radius            向量检索半径等扩展参数
     * @param includeMetadata   是否包含元数据
     * @param currentTime       当前时间（部分检索策略使用）
     * @return 检索结果、分数与待处理消息等
     */
    @GetMapping("/search")
    ApiResponse<SearchMemoriesResponse> searchMemories(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "user_id", required = false) String userId,
            @RequestParam(value = "group_id", required = false) String groupId,
            @RequestParam(value = "retrieve_method", required = false, defaultValue = "keyword") String retrieveMethod,
            @RequestParam(value = "memory_types", required = false) String memoryTypes,
            @RequestParam(value = "top_k", required = false, defaultValue = "40") Integer topK,
            @RequestParam(value = "start_time", required = false) String startTime,
            @RequestParam(value = "end_time", required = false) String endTime,
            @RequestParam(value = "radius", required = false) Double radius,
            @RequestParam(value = "include_metadata", required = false, defaultValue = "true") Boolean includeMetadata,
            @RequestParam(value = "current_time", required = false) String currentTime
    );

    /**
     * GET /memories/conversation-meta - 获取对话元数据
     * @param groupId 群组 ID
     * @return 对话元数据
     */
    @GetMapping("/conversation-meta")
    ApiResponse<ConversationMetaResponse> getConversationMeta(
            @RequestParam(value = "group_id", required = false) String groupId
    );

    /**
     * POST /memories/conversation-meta - 保存对话元数据
     * @param request 元数据写入请求
     * @return 保存后的元数据
     */
    @PostMapping("/conversation-meta")
    ApiResponse<ConversationMetaResponse> saveConversationMeta(@RequestBody ConversationMetaRequest request);

    /**
     * PATCH /memories/conversation-meta - 更新对话元数据
     * @param request 元数据增量更新请求
     * @return 更新后的元数据
     */
    @PatchMapping("/conversation-meta")
    ApiResponse<ConversationMetaResponse> updateConversationMeta(@RequestBody UpdateConversationMetaRequest request);

    /**
     * DELETE /memories - 删除记忆
     * @param request 删除条件（用户、群组、事件等）
     * @return 删除条数与过滤条件说明
     */
    @DeleteMapping
    ApiResponse<DeleteMemoriesResponse> deleteMemories(@RequestBody DeleteMemoriesRequest request);

    /**
     * POST /internal/tenant/init-db - 初始化租户数据库
     * 内部接口，用于多租户场景下的数据库初始化
     * @return 初始化是否成功及租户信息
     */
    @PostMapping(value = "/internal/tenant/init-db", consumes = "application/json")
    ApiResponse<InitTenantDbResponse> initTenantDatabase();
}
