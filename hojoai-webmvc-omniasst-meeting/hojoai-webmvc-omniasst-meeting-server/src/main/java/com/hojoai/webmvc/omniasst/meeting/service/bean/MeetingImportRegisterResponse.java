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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingImportRegisterResponse {

    private String meetingId;
    /** 入库使用的 OSS 对象键 */
    private String objectKey;
    /** 会议标题（转写与摘要流水线完成后与详情一致） */
    private String title;
    /** 供客户端播放的 OSS 访问 URL（预签名，与会议详情一致） */
    private String ossUrl;
    /** 录音总时长，毫秒 */
    private Long totalDuration;
    /** 转写分段，与会议详情接口 segments 字段结构一致 */
    private List<MeetingSegment> segments;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOssUrl() {
        return ossUrl;
    }

    public void setOssUrl(String ossUrl) {
        this.ossUrl = ossUrl;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public List<MeetingSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<MeetingSegment> segments) {
        this.segments = segments;
    }
}
