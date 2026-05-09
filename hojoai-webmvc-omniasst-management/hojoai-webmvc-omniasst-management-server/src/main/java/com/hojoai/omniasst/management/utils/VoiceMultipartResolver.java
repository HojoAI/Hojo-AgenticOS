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
package com.hojoai.omniasst.management.utils;

import jakarta.servlet.http.Part;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Locale;

/**
 * 从 multipart 请求中解析音频文件，避免 {@code @RequestParam MultipartFile} 缺字段时框架抛错。
 */
public final class VoiceMultipartResolver {

    private VoiceMultipartResolver() {
    }

    public static MultipartFile resolveAudioMultipart(MultipartHttpServletRequest mreq) {
        String[] preferred = {"file", "audioFile", "audio", "voiceFile", "media", "voice", "upload",
                "recording", "sound", "blob", "data"};
        for (String name : preferred) {
            MultipartFile f = mreq.getFile(name);
            if (f != null && !f.isEmpty()) {
                return f;
            }
            List<MultipartFile> list = mreq.getFiles(name);
            if (list != null) {
                for (MultipartFile mf : list) {
                    if (mf != null && !mf.isEmpty()) {
                        return mf;
                    }
                }
            }
        }
        for (String key : mreq.getFileMap().keySet()) {
            if (key == null) {
                continue;
            }
            MultipartFile f = mreq.getFile(key);
            if (f == null || f.isEmpty()) {
                continue;
            }
            String k = key.toLowerCase(Locale.ROOT);
            if ("file".equals(k) || k.contains("audio") || k.contains("voice") || k.contains("media")
                    || k.contains("wav") || k.contains("sound") || k.contains("recording")) {
                return f;
            }
        }
        for (List<MultipartFile> list : mreq.getMultiFileMap().values()) {
            for (MultipartFile f : list) {
                if (f != null && !f.isEmpty()) {
                    return f;
                }
            }
        }
        for (MultipartFile f : mreq.getFileMap().values()) {
            if (f != null && !f.isEmpty()) {
                return f;
            }
        }
        try {
            for (Part p : mreq.getParts()) {
                if (p.getSize() <= 0) {
                    continue;
                }
                String n = p.getName();
                if (isNonFilePartName(n)) {
                    continue;
                }
                String fn = p.getSubmittedFileName();
                if (fn != null && !fn.isBlank()) {
                    return new ServletPartMultipartFile(p);
                }
            }
            for (Part p : mreq.getParts()) {
                if (p.getSize() <= 0) {
                    continue;
                }
                String n = p.getName();
                if (isNonFilePartName(n)) {
                    continue;
                }
                String ct = p.getContentType();
                if (ct != null && ct.toLowerCase(Locale.ROOT).startsWith("audio/")) {
                    return new ServletPartMultipartFile(p);
                }
                if (n != null) {
                    String nl = n.toLowerCase(Locale.ROOT);
                    if (nl.equals("file") || nl.contains("audio") || nl.contains("voice") || nl.contains("media")) {
                        return new ServletPartMultipartFile(p);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isNonFilePartName(String n) {
        if (n == null) {
            return false;
        }
        String x = n.toLowerCase(Locale.ROOT);
        return x.equals("userid") || x.equals("username") || x.equals("provider") || x.equals("token")
                || x.equals("voicename") || x.equals("type") || x.equals("needsave");
    }
}
