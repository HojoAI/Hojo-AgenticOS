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
package com.hojoai.webmvc.omniasst.meeting.websocket.v1;

public class AsrWebSocketEvents {

    // ========== 请求事件 ==========
    
    /**
     * 配置请求
     */
    public static final String REQ_ASR_CONFIG = "REQ_ASR_CONFIG";
    
    /**
     * 音频数据请求
     */
    public static final String REQ_ASR_AUDIO = "REQ_ASR_AUDIO_DATA";
    
    /**
     * 心跳请求
     */
    public static final String REQ_HEARTBEAT = "REQ_HEARTBEAT";
    
    /**
     * 停止识别请求
     */
    public static final String REQ_ASR_STOP = "REQ_ASR_STOP";
    
    // ========== 响应事件 ==========
    
    /**
     * 配置响应
     */
    public static final String BACK_ASR_CONFIG = "BACK_ASR_CONFIG";
    
    /**
     * 配置确认响应
     */
    public static final String BACK_ASR_CONFIG_ACK = "BACK_ASR_CONFIG_ACK";
    
    /**
     * ASR文本响应
     */
    public static final String BACK_ASR_TEXT = "BACK_ASR_TEXT";
    
    /**
     * 心跳响应
     */
    public static final String BACK_HEARTBEAT = "BACK_HEARTBEAT";
    
    /**
     * 错误响应
     */
    public static final String BACK_ERROR = "BACK_ERROR";
    
    /**
     * 识别完成响应
     */
    public static final String BACK_ASR_COMPLETE = "BACK_ASR_COMPLETE";

    private AsrWebSocketEvents() {
    }
}
