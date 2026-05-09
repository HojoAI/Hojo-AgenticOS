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
package com.hojoai.gateway.omniasst.voice.service.azure;

import java.util.HashMap;
import java.util.Map;

/**
 * Azure语言代码映射器
 * 负责将业务系统的语言代码(english/chinese等)转换为Azure要求的格式
 */
public class AzureLanguageMapper {

    /**
     * 业务语言代码到Azure语音识别语言代码的映射
     * 格式：BCP-47标准，如zh-CN、en-US
     */
    private static final Map<String, String> RECOGNITION_LANGUAGE_MAP = new HashMap<>();

    /**
     * 业务语言代码到Azure翻译语言代码的映射
     * 格式：ISO 639-1标准，如zh-Hans、en
     */
    private static final Map<String, String> TRANSLATION_LANGUAGE_MAP = new HashMap<>();

    static {
        // 中文映射
        RECOGNITION_LANGUAGE_MAP.put("chinese", "zh-CN");
        TRANSLATION_LANGUAGE_MAP.put("chinese", "zh-Hans");

        // 英文映射
        RECOGNITION_LANGUAGE_MAP.put("english", "en-US");
        TRANSLATION_LANGUAGE_MAP.put("english", "en");

        // 日文映射
        RECOGNITION_LANGUAGE_MAP.put("japanese", "ja-JP");
        TRANSLATION_LANGUAGE_MAP.put("japanese", "ja");

        // 法文映射
        RECOGNITION_LANGUAGE_MAP.put("french", "fr-FR");
        TRANSLATION_LANGUAGE_MAP.put("french", "fr");

        // 西班牙文映射
        RECOGNITION_LANGUAGE_MAP.put("spanish", "es-ES");
        TRANSLATION_LANGUAGE_MAP.put("spanish", "es");

        // 德文映射
        RECOGNITION_LANGUAGE_MAP.put("german", "de-DE");
        TRANSLATION_LANGUAGE_MAP.put("german", "de");

        // 韩文映射
        RECOGNITION_LANGUAGE_MAP.put("korean", "ko-KR");
        TRANSLATION_LANGUAGE_MAP.put("korean", "ko");

        // 俄文映射
        RECOGNITION_LANGUAGE_MAP.put("russian", "ru-RU");
        TRANSLATION_LANGUAGE_MAP.put("russian", "ru");

        // 意大利文映射
        RECOGNITION_LANGUAGE_MAP.put("italian", "it-IT");
        TRANSLATION_LANGUAGE_MAP.put("italian", "it");

        // 葡萄牙文映射
        RECOGNITION_LANGUAGE_MAP.put("portuguese", "pt-BR");
        TRANSLATION_LANGUAGE_MAP.put("portuguese", "pt");

        // 阿拉伯文映射
        RECOGNITION_LANGUAGE_MAP.put("arabic", "ar-SA");
        TRANSLATION_LANGUAGE_MAP.put("arabic", "ar");

        // 印地文映射
        RECOGNITION_LANGUAGE_MAP.put("hindi", "hi-IN");
        TRANSLATION_LANGUAGE_MAP.put("hindi", "hi");
    }

    /**
     * 将业务语言代码转换为Azure语音识别所需的语言代码
     * 
     * @param businessLang 业务语言代码(如chinese、english)
     * @return Azure识别语言代码(如zh-CN、en-US)，如果未找到映射则返回原值
     */
    public static String toRecognitionLanguage(String businessLang) {
        if (businessLang == null) {
            return null;
        }
        String lowerLang = businessLang.toLowerCase();
        return RECOGNITION_LANGUAGE_MAP.getOrDefault(lowerLang, businessLang);
    }

    /**
     * 将业务语言代码转换为Azure翻译所需的语言代码
     * 
     * @param businessLang 业务语言代码(如chinese、english)
     * @return Azure翻译语言代码(如zh-Hans、en)，如果未找到映射则返回原值
     */
    public static String toTranslationLanguage(String businessLang) {
        if (businessLang == null) {
            return null;
        }
        String lowerLang = businessLang.toLowerCase();
        return TRANSLATION_LANGUAGE_MAP.getOrDefault(lowerLang, businessLang);
    }

    /**
     * 将Azure识别语言代码转换回业务语言代码
     * 
     * @param azureLang Azure识别语言代码(如zh-CN、en-US)
     * @return 业务语言代码(如chinese、english)，如果未找到映射则返回原值
     */
    public static String fromRecognitionLanguage(String azureLang) {
        if (azureLang == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : RECOGNITION_LANGUAGE_MAP.entrySet()) {
            if (entry.getValue().equals(azureLang)) {
                return entry.getKey();
            }
        }
        return azureLang;
    }
}
