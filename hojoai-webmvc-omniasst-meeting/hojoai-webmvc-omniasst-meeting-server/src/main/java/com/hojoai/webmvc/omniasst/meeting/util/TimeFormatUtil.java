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

/**
 * The TimeUtil class for formating date or time.
 *
 * @author lianghay
 * @version 1.0, 2026/01/05
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */
public class TimeFormatUtil {
    public static String formatMsToMMSS(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatMsToHHMMSS(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long mmssToMillis(String mmss) {
        if (mmss == null || !mmss.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Error time format,require mm:ss");
        }

        String[] parts = mmss.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        return (minutes * 60L + seconds) * 1000;
    }


    public static long hhmmssToMillis(String hhmmss){
        if (hhmmss == null || !hhmmss.matches("\\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Error time format,require hh:mm:ss");
        }

        String[] parts = hhmmss.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return (hours * 3600L + minutes * 60L + seconds) * 1000;
    }


}
