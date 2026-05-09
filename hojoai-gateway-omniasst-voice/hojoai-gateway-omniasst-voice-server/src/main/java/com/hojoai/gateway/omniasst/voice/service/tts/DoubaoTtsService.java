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
package com.hojoai.gateway.omniasst.voice.service.tts;

import com.hojoai.gateway.omniasst.voice.doubao.speech.protocol.SpeechWebSocketClient;

import java.util.function.Consumer;

/**
 * 豆包双向流式 TTS：支持单次短连接合成，或复用 {@link SpeechWebSocketClient} 多轮合成。
 */
public interface DoubaoTtsService {

    /**
     * 单次完整流程：建连 → 会话 → 按协议发送文本 → 收音频 → 关连。
     *
     * @param text     待合成文本
     * @param voice    音色 ID
     * @param encoding 音频编码，如 pcm
     * @param callback 可选，边下边播音频块
     * @return 拼接后的完整音频；无数据时可能为空数组
     * @throws Exception 配置缺失、网络或协议错误
     */
    byte[] synthesize(String text, String voice, String encoding, Consumer<byte[]> callback) throws Exception;

    /**
     * 创建并连接豆包 WebSocket 客户端（发送 START_CONNECTION），与当前 TTS WebSocket 会话保持一个连接复用。
     *
     * @param voiceId 音色ID，用于确定 resource-id
     * @return 已连接并已发送 startConnection 的客户端
     */
    SpeechWebSocketClient createAndConnectClient(String voiceId) throws Exception;

    /**
     * 在已建连的 client 上开启豆包 TTS session（START_SESSION + 等待 SESSION_STARTED）。
     * 与 {@link #createAndConnectClient} 配合：单条上游 WebSocket 连接只应调用一次，直至 {@link #finishSessionBeforeClose} 或换 client。
     *
     * @param client           已由 createAndConnectClient 建立连接
     * @param voiceId          音色 ID
     * @param encoding         音频格式（如 pcm）
     * @param explicitLanguage 显式语言（如 zh-cn）
     * @param sessionId        豆包 session id，建议与上游 WebSocket {@code session.getId()} 一致
     */
    void openSession(SpeechWebSocketClient client, String voiceId, String encoding, String explicitLanguage, String sessionId) throws Exception;

    /**
     * 在关闭或替换 client 前结束豆包 session：FINISH_SESSION 并 drain 至 SESSION_FINISHED。
     */
    void finishSessionBeforeClose(SpeechWebSocketClient client, String sessionId) throws Exception;

    /**
     * 同 {@link #finishSessionBeforeClose(SpeechWebSocketClient, String)}，可指定单次 poll 的超时（秒），用于 isEnd 等需尽快结束的场景。
     */
    void finishSessionBeforeClose(SpeechWebSocketClient client, String sessionId, long drainTimeoutSeconds) throws Exception;

    /**
     * 在已开启的 session 中发送一段文本，不阻塞等待单次结束事件。
     */
    void sendTaskRequest(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId) throws Exception;

    /**
     * 仅发送 FINISH_SESSION，不负责 drain；适用于已有独立收包线程的场景。
     */
    void requestSessionFinish(SpeechWebSocketClient client, String sessionId) throws Exception;

    /**
     * 使用已连接的客户端合成语音（单连接单 session：仅 TASK_REQUEST，收包至单次合成结束事件如 TTS_ENDED）。
     *
     * @param client           由 createAndConnectClient 返回的客户端
     * @param text             要合成的文本
     * @param voiceId          音色ID
     * @param encoding         音频格式
     * @param explicitLanguage 显式语言（如 zh-cn），为空时默认 zh-cn
     * @param sessionId        豆包单次合成会话 ID，建议与上游 WebSocket {@code session.getId()} 一致，便于链路关联
     * @param callback         音频数据回调
     * @return 完整音频数据
     */
    byte[] synthesizeWithClient(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId, Consumer<byte[]> callback) throws Exception;

    /**
     * 同 {@link #synthesizeWithClient(SpeechWebSocketClient, String, String, String, String, String, Consumer)}，可指定单次收包 poll 超时（秒）；isEnd=1 时宜用较短值以免末尾长时间阻塞。
     */
    byte[] synthesizeWithClient(SpeechWebSocketClient client, String text, String voiceId, String encoding, String explicitLanguage, String sessionId, Consumer<byte[]> callback, long receivePollTimeoutSeconds) throws Exception;

    /**
     * 关闭豆包客户端（发送 FINISH_CONNECTION 并关闭连接）。
     *
     * @param client 要关闭的客户端
     */
    void closeClient(SpeechWebSocketClient client);
}