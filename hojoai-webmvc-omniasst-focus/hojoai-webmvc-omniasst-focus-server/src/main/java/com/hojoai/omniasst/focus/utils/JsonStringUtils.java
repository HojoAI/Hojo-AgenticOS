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
package com.hojoai.omniasst.focus.utils;

/**
 *
 * @author My
 * @version 1.0, 2026/01/17
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class JsonStringUtils {

    public static String extractJsonString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        int firstBrace = input.indexOf('{');
        if (firstBrace == -1) {
            return ""; // 没有找到第一个 {
        }

        int lastBrace = input.lastIndexOf('}');
        if (lastBrace == -1 || lastBrace < firstBrace) {
            return ""; // 没有找到最后一个 } 或者 } 在 { 之前
        }

        return input.substring(firstBrace, lastBrace + 1);
    }

}
