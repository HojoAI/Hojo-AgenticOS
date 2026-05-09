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
package com.hojoai.omniasst.common.api.memory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Memory 等后端接口的统一响应包装（成功时 status 一般为 {@code ok}，业务数据在 result）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /** 状态字符串，成功时常为 {@code ok} */
    private String status;
    
    /** 人类可读说明或错误信息 */
    private String message;
    
    /** 成功时的业务载荷 */
    private T result;

    /** 错误码（失败响应时由服务端填充） */
    private String code;
    
    /** 服务端时间戳 */
    private String timestamp;
    
    /** 请求路径（错误诊断用） */
    private String path;

    /**
     * 是否表示调用成功（与业务约定的 status 取值一致）。
     *
     * @return {@code true} 当 status 等于 {@code ok}
     */
    public boolean isSuccess() {
        return "ok".equals(status);
    }
}
