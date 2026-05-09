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
package com.hojoai.template.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志配置属性
 * 支持从配置文件读取日志相关配置
 * 
 */
@Component
@ConfigurationProperties(prefix = "hojo.logger")
public class LoggerProperties {
    
    /**
     * 日志消息最大长度阈值，超过则截断
     */
    private int messageThreshold = 1000;
    
    /**
     * 慢接口告警阈值（毫秒），超过则记录WARN日志
     */
    private long slowApiThreshold = 3000;
    
    /**
     * 是否启用慢接口告警
     */
    private boolean slowApiAlertEnabled = true;
    
    /**
     * 是否打印方法签名
     */
    private boolean printMethodSignature = true;

    public int getMessageThreshold() {
        return messageThreshold;
    }

    public void setMessageThreshold(int messageThreshold) {
        this.messageThreshold = messageThreshold;
    }

    public long getSlowApiThreshold() {
        return slowApiThreshold;
    }

    public void setSlowApiThreshold(long slowApiThreshold) {
        this.slowApiThreshold = slowApiThreshold;
    }

    public boolean isSlowApiAlertEnabled() {
        return slowApiAlertEnabled;
    }

    public void setSlowApiAlertEnabled(boolean slowApiAlertEnabled) {
        this.slowApiAlertEnabled = slowApiAlertEnabled;
    }

    public boolean isPrintMethodSignature() {
        return printMethodSignature;
    }

    public void setPrintMethodSignature(boolean printMethodSignature) {
        this.printMethodSignature = printMethodSignature;
    }
}
