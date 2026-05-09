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
package com.hojoai.gateway.omniasst.voice.service.translate;

import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransResponse;

/**
 * 翻译服务接口
 * 提供音频流式翻译能力
 */
public interface TranslateService {

    /**
     * 处理音频翻译请求
     * 将音频流进行语音识别和翻译
     * 
     * @param request 翻译请求
     * @return 翻译响应
     */
    AsrTransResponse translate(AsrTransRequest request);
}
