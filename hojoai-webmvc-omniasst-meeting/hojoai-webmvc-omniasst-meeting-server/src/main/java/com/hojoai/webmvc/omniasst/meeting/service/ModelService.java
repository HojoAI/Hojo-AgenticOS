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
package com.hojoai.webmvc.omniasst.meeting.service;

import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSummaryResponse;

import java.util.List;

/**
 * The ModelService interface
 *
 * @author lianghay
 * @version 1.0, 2026/01/04
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */
public interface ModelService {

    /**
     * 调用 Hojo 会议总结接口，返回会议总结结果。
     *
     * @param transcriptContent 会议逐字稿内容，格式：发言人：文本\n
     * @param attendeeIdentity  发言人名称集合
     * @param myIdentity        录制者名字，无则空字符串
     * @return 会议总结，失败时返回 null
     */
    MeetingSummaryResponse getMeetingSummaryResult(String transcriptContent, List<String> attendeeIdentity, String myIdentity);

    String getMeetingTitleResult(String query);
}
