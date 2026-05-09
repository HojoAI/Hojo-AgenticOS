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
package com.hojoai.omniasst.management.aop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Controller 请求日志切面相关配置项（前缀 {@code controller.log}）。
 */
@Configuration
@ConfigurationProperties(prefix = "controller.log")
public class ControllerLogConfig {

    /**
     * 是否启用 Controller 日志监控（默认：true）
     */
    private boolean enabled = true;

    /**
     * 日志截断阈值（字符数，默认：2000）
     */
    private int logThreshold = 2000;

    /**
     * 慢接口阈值（毫秒，默认：1000ms）
     */
    private long slowApiThreshold = 1000;

    /**
     * 是否打印请求参数（默认：true）
     */
    private boolean logRequestParams = true;

    /**
     * 是否打印响应结果（默认：true）
     */
    private boolean logResponse = true;

    /**
     * 是否打印客户端 IP（默认：true）
     */
    private boolean logClientIp = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLogThreshold() {
        return logThreshold;
    }

    public void setLogThreshold(int logThreshold) {
        this.logThreshold = logThreshold;
    }

    public long getSlowApiThreshold() {
        return slowApiThreshold;
    }

    public void setSlowApiThreshold(long slowApiThreshold) {
        this.slowApiThreshold = slowApiThreshold;
    }

    public boolean isLogRequestParams() {
        return logRequestParams;
    }

    public void setLogRequestParams(boolean logRequestParams) {
        this.logRequestParams = logRequestParams;
    }

    public boolean isLogResponse() {
        return logResponse;
    }

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    public boolean isLogClientIp() {
        return logClientIp;
    }

    public void setLogClientIp(boolean logClientIp) {
        this.logClientIp = logClientIp;
    }
}

