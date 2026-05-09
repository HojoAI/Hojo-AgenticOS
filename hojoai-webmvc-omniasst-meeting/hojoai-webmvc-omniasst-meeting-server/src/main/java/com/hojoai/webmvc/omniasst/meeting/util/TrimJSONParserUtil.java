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
package com.hojoai.webmvc.omniasst.meeting.util;

import java.util.Map;

/**
 * The util class for model parse. *
 *
 * @author JM.Guo
 * @version 1.0, 2025/10/21
 * @since ecarx-webmvc-cockpit-aigateway 1.0.0
 */
public class TrimJSONParserUtil {

    public static String parser(String content, Map<String, String> agentContext) {
        // 去除开头的 '```json'
        int startIndex = content.indexOf("```json");
        if (startIndex != -1) {
            content = content.substring(startIndex + "```json".length());
        }

        // 去除尾部的 '```'
        int endIndex = content.lastIndexOf("```");
        if (endIndex != -1) {
            content = content.substring(0, endIndex);
        }

        // 去除前后空白
        return content.trim();
    }
}

