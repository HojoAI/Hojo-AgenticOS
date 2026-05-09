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
package com.hojoai.omniasst.weather.v2.service.caiyun;

/**
 * 彩云天气 HTTP 或业务状态异常时抛出（非 200、{@code status != ok}、解析失败等）。
 * 调用方应记录日志并转换为对用户的友好提示或上游错误码，必要时附带 {@link #getResponseBody()} 便于排障。
 *
 * @author wangjian
 * @since 2026-02-02
 */
public class CaiyunWeatherApiException extends RuntimeException {

    /** 接口原始响应体，可能为 null（如仅网络异常时） */
    private final String responseBody;

    /**
     * @param message       简要原因说明
     * @param responseBody  服务端返回 body，可为 null
     */
    public CaiyunWeatherApiException(String message, String responseBody) {
        super(message);
        this.responseBody = responseBody;
    }

    /**
     * @return 原始响应体，便于日志与排障；可能为 null
     */
    public String getResponseBody() {
        return responseBody;
    }
}
