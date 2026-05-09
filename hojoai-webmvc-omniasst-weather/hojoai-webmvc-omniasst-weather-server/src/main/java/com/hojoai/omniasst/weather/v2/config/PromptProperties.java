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
package com.hojoai.omniasst.weather.v2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 提示词公用配置（properties 格式，支持 Nacos 动态刷新）
 * 前缀：prompt，各场景提示词按 key 区分（如 slot-extraction-system、tts 等）
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "prompt")
public class PromptProperties {

    /**
     * 提槽场景：系统提示词（调用大模型时的 system 消息内容）
     * 配置键：prompt.slot-extraction-system
     */
    private String slotExtractionSystem;

    public String getSlotExtractionSystem() {
        return slotExtractionSystem;
    }

    public void setSlotExtractionSystem(String slotExtractionSystem) {
        this.slotExtractionSystem = slotExtractionSystem;
    }
}
