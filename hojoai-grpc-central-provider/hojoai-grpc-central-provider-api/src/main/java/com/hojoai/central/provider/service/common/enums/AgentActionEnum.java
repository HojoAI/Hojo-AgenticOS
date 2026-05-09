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
package com.hojoai.central.provider.service.common.enums;

/**
 * Agent 动作类型枚举
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
public enum AgentActionEnum implements CommonEnum {

    WEB_MESSAGE("message", "action.web.message"),
    WEB_NAVIGATION("navigation", "action.web.navigation"),
    WEB_AUDIO_RECORD("audioRecord", "action.web.audioRecord"),
    WEB_MUSIC("music", "action.web.music"),
    PA_MESSAGE("paMessage", "action.pa.message"),
    WEB_REFRESH_PAGE("webRefreshPage", "action.web.refreshPage");

    private final String code;

    private final String value;

    AgentActionEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * 根据 code 获取对应的枚举值
     *
     * @param code 代码
     * @return AgentActionEnum
     */
    public static AgentActionEnum getByCode(String code) {
        for (AgentActionEnum actionEnum : values()) {
            if (actionEnum.getCode().equals(code)) {
                return actionEnum;
            }
        }
        return null;
    }

}

