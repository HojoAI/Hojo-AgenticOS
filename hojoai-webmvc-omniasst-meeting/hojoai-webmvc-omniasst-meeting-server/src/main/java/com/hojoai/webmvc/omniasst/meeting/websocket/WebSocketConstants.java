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
package com.hojoai.webmvc.omniasst.meeting.websocket;

public class WebSocketConstants {
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    /** 与 URL 参数 conversationId 对应 */
    public static final String HEADER_CONVERSATION_ID = "X-Conversation-Id";
    /** 续接时传入已有会议 ID，与 URL 参数 meetingId 对应 */
    public static final String HEADER_MEETING_ID = "X-Meeting-Id";
    public static final String ATTRIBUTE_DEVICE_ID = "deviceId";
    public static final String ATTRIBUTE_SESSION_ID = "sessionId";
    public static final String ATTRIBUTE_USER_ID = "userId";
    public static final String ATTRIBUTE_CONVERSATION_ID = "conversationId";
    public static final String ATTRIBUTE_MEETING_ID = "meetingId";
    /** 端侧续接时上报的录音累计时长（毫秒），URL 参数 clientRecordElapsedMs */
    public static final String ATTRIBUTE_CLIENT_RECORD_ELAPSED_MS = "clientRecordElapsedMs";
}

