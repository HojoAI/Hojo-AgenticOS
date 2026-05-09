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
package com.hojoai.gateway.omniasst.voice.service.asrtranslate;

import java.util.HashMap;
import java.util.Map;

public class LanguageMapper {
    private static final Map<String, String> HOJO_TO_AZURE_RECOGNITION = new HashMap<>();
    private static final Map<String, String> HOJO_TO_AZURE_TRANSLATION = new HashMap<>();
    private static final Map<String, String> AZURE_RECOGNITION_TO_HOJO = new HashMap<>();
    private static final Map<String, String> AZURE_TRANSLATION_TO_HOJO = new HashMap<>();

    static {
        HOJO_TO_AZURE_RECOGNITION.put("chinese", "zh-CN");
        HOJO_TO_AZURE_RECOGNITION.put("english", "en-US");
        HOJO_TO_AZURE_RECOGNITION.put("japanese", "ja-JP");
        HOJO_TO_AZURE_RECOGNITION.put("french", "fr-FR");

        HOJO_TO_AZURE_TRANSLATION.put("chinese", "zh-Hans");
        HOJO_TO_AZURE_TRANSLATION.put("english", "en");
        HOJO_TO_AZURE_TRANSLATION.put("japanese", "ja");
        HOJO_TO_AZURE_TRANSLATION.put("french", "fr");

        AZURE_RECOGNITION_TO_HOJO.put("zh-CN", "chinese");
        AZURE_RECOGNITION_TO_HOJO.put("en-US", "english");
        AZURE_RECOGNITION_TO_HOJO.put("ja-JP", "japanese");
        AZURE_RECOGNITION_TO_HOJO.put("fr-FR", "french");

        AZURE_TRANSLATION_TO_HOJO.put("zh-Hans", "chinese");
        AZURE_TRANSLATION_TO_HOJO.put("en", "english");
        AZURE_TRANSLATION_TO_HOJO.put("ja", "japanese");
        AZURE_TRANSLATION_TO_HOJO.put("fr", "french");
    }

    public static String toAzureRecognition(String hojoLang) {
        if (hojoLang == null) return null;
        return HOJO_TO_AZURE_RECOGNITION.getOrDefault(hojoLang.toLowerCase(), hojoLang);
    }

    public static String toAzureTranslation(String hojoLang) {
        if (hojoLang == null) return null;
        return HOJO_TO_AZURE_TRANSLATION.getOrDefault(hojoLang.toLowerCase(), hojoLang);
    }

    public static String fromAzureRecognition(String azureLang) {
        if (azureLang == null) return null;
        return AZURE_RECOGNITION_TO_HOJO.getOrDefault(azureLang, azureLang);
    }

    public static String fromAzureTranslation(String azureLang) {
        if (azureLang == null) return null;
        return AZURE_TRANSLATION_TO_HOJO.getOrDefault(azureLang, azureLang);
    }
}
