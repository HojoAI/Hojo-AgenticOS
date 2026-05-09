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
 * 会议详情等场景下，发言人占位头像用的首字符（与 {@code speakerName} 一致，改名后立即反映）。
 */
public final class SpeakerDisplayUtil {

    private SpeakerDisplayUtil() {
    }

    /**
     * 取展示名的首个 Unicode 码位；拉丁字母大写，其余（含中文）原样。
     */
    public static String avatarInitialFromSpeakerName(String speakerName) {
        if (StringUtils.isBlank(speakerName)) {
            return "";
        }
        String trimmed = speakerName.trim();
        int cp = trimmed.codePointAt(0);
        if (!Character.isValidCodePoint(cp)) {
            return "";
        }
        String s = new String(Character.toChars(cp));
        if (s.length() == 1) {
            char c = s.charAt(0);
            if (c >= 'a' && c <= 'z') {
                return String.valueOf(Character.toUpperCase(c));
            }
        }
        return s;
    }
}
