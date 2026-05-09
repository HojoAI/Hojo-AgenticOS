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
package com.hojoai.omniasst.sysagent.service.speech;

import com.hojoai.omniasst.sysagent.service.speech.bean.TTSRequest;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

/**
 * The service interface for tts.
 *
 * @author kevanwong
 * @version 1.0, 2025/11/15
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public interface TTSService {

    /**
     * 将文本转为语音音频流（二进制分片），由调用方消费 {@link Flux}。
     *
     * @param request 待合成文本、语种、音色与请求 ID 等
     * @return 音频二进制流；错误时由实现记录日志并走 Reactor 错误信号
     */
    Flux<DataBuffer> tts(TTSRequest request);
}
