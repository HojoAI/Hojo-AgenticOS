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

import org.apache.commons.lang3.StringUtils;

/**
 * 会议标题后处理：模型提示词约束「不超过 N 字」，此处做硬截断兜底。
 */
public final class MeetingTitleUtil {

    private MeetingTitleUtil() {
    }

    /**
     * 按 Unicode 码点截断，与中文「字」计数一致。
     */
    public static String truncateToMaxChars(String title, int maxChars) {
        if (StringUtils.isBlank(title) || maxChars <= 0) {
            return StringUtils.trimToEmpty(title);
        }
        int[] cps = title.codePoints().toArray();
        if (cps.length <= maxChars) {
            return title.trim();
        }
        return new String(cps, 0, maxChars).trim();
    }
}
