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
package com.hojoai.webmvc.omniasst.meeting.dao.meeting.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "meeting_segments")
public class MeetingSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "meeting_id")
    private String meetingId;

    @TableField(value = "segment_id")
    private String segmentId;

    @TableField(value = "text")
    private String text;

    @TableField(value = "speaker_id")
    private String speakerId;

    @TableField(value = "speaker_name")
    private String speakerName;

    @TableField(value = "speaker_avatar")
    private String speakerAvatar;

    /**
     * 无真实头像 URL 时前端用于默认头像上的文字；不入库，由会议详情接口按当前 {@link #speakerName} 计算（含改名后）。
     */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String speakerAvatarInitial;

    @TableField(value = "start_time")
    private Long startTime;

    @TableField(value = "end_time")
    private Long endTime;

    @TableField(value = "duration")
    private Long duration;

    @TableField(value = "timestamp")
    private Long timestamp;

    /** 段落打标 JSON，如 {"offsetMs":123,"markedAt":456}；需库表含 mark_json 列 */
    @TableField(value = "mark_json")
    private String markJson;

    /**
     * 本段是否已打标；不入库。会议详情等接口由服务端根据 {@link #markJson} 填充；为 null 时不参与 JSON 序列化，避免写入 segments_json。
     */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean marked;

    @TableField(value = "created_at")
    private Date createdAt;

    public MeetingSegment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getSpeakerAvatar() {
        return speakerAvatar;
    }

    public void setSpeakerAvatar(String speakerAvatar) {
        this.speakerAvatar = speakerAvatar;
    }

    public String getSpeakerAvatarInitial() {
        return speakerAvatarInitial;
    }

    public void setSpeakerAvatarInitial(String speakerAvatarInitial) {
        this.speakerAvatarInitial = speakerAvatarInitial;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMarkJson() {
        return markJson;
    }

    public void setMarkJson(String markJson) {
        this.markJson = markJson;
    }

    public Boolean getMarked() {
        return marked;
    }

    public void setMarked(Boolean marked) {
        this.marked = marked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

