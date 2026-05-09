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

import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参会人展示顺序：1) 用户本人（speakerId 与 owner userId 一致） 2) 非「发言人n」的具名说话人（含声纹昵称、用户改名） 3) 「发言人1」…「发言人n」按编号升序。
 */
public final class MeetingAttendeeOrderUtil {

    private static final Pattern ANONYMOUS_SPEAKER = Pattern.compile("^发言人(\\d+)$");

    private MeetingAttendeeOrderUtil() {
    }

    public static boolean isAnonymousSpeakerName(String speakerName) {
        if (StringUtils.isBlank(speakerName)) {
            return true;
        }
        return ANONYMOUS_SPEAKER.matcher(speakerName.trim()).matches();
    }

    static int anonymousIndex(String speakerName) {
        if (StringUtils.isBlank(speakerName)) {
            return Integer.MAX_VALUE;
        }
        Matcher m = ANONYMOUS_SPEAKER.matcher(speakerName.trim());
        if (!m.matches()) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(m.group(1));
    }

    /**
     * 每个 speakerId 仅出现一次，取该说话人在会议中首次出现段的展示名。
     */
    public static List<String> buildOrderedAttendees(List<MeetingSegment> segments, String ownerUserId) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        Map<String, String> speakerIdToName = new LinkedHashMap<>();
        Map<String, Long> speakerIdToFirstStart = new LinkedHashMap<>();

        for (MeetingSegment s : segments) {
            String sid = s.getSpeakerId();
            if (StringUtils.isBlank(sid)) {
                continue;
            }
            long orderKey = firstOrderKey(s);
            speakerIdToFirstStart.merge(sid, orderKey, Math::min);
            speakerIdToName.putIfAbsent(sid, StringUtils.defaultString(s.getSpeakerName()));
        }

        List<String> selfIds = new ArrayList<>();
        List<String> namedIds = new ArrayList<>();
        List<String> anonIds = new ArrayList<>();

        for (String sid : speakerIdToName.keySet()) {
            String name = speakerIdToName.get(sid);
            if (StringUtils.isNotBlank(ownerUserId) && ownerUserId.equals(sid)) {
                selfIds.add(sid);
            } else if (isAnonymousSpeakerName(name)) {
                anonIds.add(sid);
            } else {
                namedIds.add(sid);
            }
        }

        namedIds.sort(Comparator.comparingLong(id -> speakerIdToFirstStart.getOrDefault(id, 0L)));
        anonIds.sort(Comparator
                .comparingInt((String id) -> anonymousIndex(speakerIdToName.get(id)))
                .thenComparingLong(id -> speakerIdToFirstStart.getOrDefault(id, 0L)));

        List<String> out = new ArrayList<>();
        appendNames(out, selfIds, speakerIdToName);
        appendNames(out, namedIds, speakerIdToName);
        appendNames(out, anonIds, speakerIdToName);
        return out;
    }

    private static long firstOrderKey(MeetingSegment s) {
        if (s.getStartTime() != null) {
            return s.getStartTime();
        }
        if (s.getTimestamp() != null) {
            return s.getTimestamp();
        }
        return 0L;
    }

    private static void appendNames(List<String> out, List<String> ids, Map<String, String> speakerIdToName) {
        for (String id : ids) {
            String name = speakerIdToName.get(id);
            if (StringUtils.isNotBlank(name)) {
                out.add(name);
            }
        }
    }
}
