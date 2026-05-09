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
package com.hojoai.webmvc.omniasst.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.page.IPage;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.api.dto.ScheduleCreateRequest;
import com.hojoai.webmvc.omniasst.meeting.common.GlobalConstants;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.ScheduleServiceClient;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.*;
import com.hojoai.webmvc.omniasst.meeting.util.MeetingAttendeeOrderUtil;
import com.hojoai.webmvc.omniasst.meeting.util.SpeakerDisplayUtil;
import com.hojoai.webmvc.omniasst.meeting.util.StorageUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import xyz.downgoon.snowflake.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingService} 实现：列表、详情、待办、打标与说话人重命名等。
 */
@Service
public class MeetingServiceImpl implements MeetingService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingServiceImpl.class);

    @Resource
    private MeetingDAO meetingDAO;
    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private ManagementServiceClient managementServiceClient;
    @Resource
    private ScheduleServiceClient scheduleServiceClient;
    @Resource
    private Snowflake snowflake;

    @Override
    public IPage<MeetingListResponse> listMeetings(MeetingListRequest request) {
        long pageId = request.getPageId();
        long pageSize = request.getPageSize();
        String keyword = request.getKeyword();
        Integer summaryStatus = request.getSummaryStatus();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        Page<Meeting> page = new Page<>(pageId, pageSize);
        LambdaQueryWrapper<Meeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Meeting::getUserId, request.getUserId());

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Meeting::getTitle, keyword);
        }

        if (summaryStatus != null) {
            wrapper.eq(Meeting::getSummaryStatus, summaryStatus);
        }

        boolean desc = StringUtils.equalsIgnoreCase(sortOrder, GlobalConstants.SORT_ORDER_DESC);
        if ("createdAt".equalsIgnoreCase(sortField) || "created_at".equalsIgnoreCase(sortField)) {
            wrapper.orderBy(true, !desc, Meeting::getCreatedAt);
        } else {
            // 默认按 startTime 倒序
            wrapper.orderByDesc(Meeting::getStartTime);
        }

        Page<Meeting> result = meetingDAO.selectMeetingPage(page, wrapper);
        List<MeetingListResponse> content = result.getRecords().stream().map(item -> {
            MeetingListResponse response = new MeetingListResponse();
            response.setMeetingId(item.getMeetingId());
            response.setUserId(item.getUserId());
            response.setTitle(item.getTitle());
            response.setStartTime(item.getStartTime());
            response.setEndTime(item.getEndTime());
            response.setTotalDuration(item.getTotalDuration());
            response.setSummaryStatus(item.getSummaryStatus());
            response.setCreatedAt(DateUtil.formatAsSecond(item.getCreatedAt()));
            response.setSummary(item.getSummary());
            response.setKnowledgeCount(item.getKnowledgeCount() != null ? item.getKnowledgeCount() : 0);
            response.setEstimatedAiTime(2);
            return response;
        }).toList();

        return IPage.newInstance((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), content);
    }

    @Override
    public MeetingDetailResponse getMeetingDetail(String meetingId) {
        Meeting meeting = meetingDAO.selectByMeetingId(meetingId);
        if (meeting == null) {
            return null;
        }

        MeetingDetailResponse response = new MeetingDetailResponse();
        response.setMeetingId(meeting.getMeetingId());
        response.setUserId(meeting.getUserId());
        response.setTitle(meeting.getTitle());
        response.setMeetingType(meeting.getMeetingType());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setTotalDuration(meeting.getTotalDuration());
        response.setSummaryStatus(meeting.getSummaryStatus());
        response.setCreatedAt(DateUtil.formatAsSecond(meeting.getCreatedAt()));
        if (meeting.getUpdatedAt() != null) {
            response.setUpdatedAt(DateUtil.formatAsSecond(meeting.getUpdatedAt()));
        }

        String ossUrl = storageUtil.getPresignedUrlForObjectKey(meeting.getOssUrl());
        response.setOssUrl(ossUrl);
        
        if (StringUtils.isNotBlank(meeting.getSegmentsJson())) {
            List<MeetingSegment> segments = JacksonUtil.json2Bean(meeting.getSegmentsJson(), new TypeReference<List<MeetingSegment>>() {});
            if (segments != null) {
                for (MeetingSegment s : segments) {
                    s.setMarked(StringUtils.isNotBlank(s.getMarkJson()));
                }
                enrichMeetingDetailSegments(segments, meeting.getUserId());
            }
            response.setSegments(segments);
            if (segments != null && !segments.isEmpty()) {
                response.setAttendees(MeetingAttendeeOrderUtil.buildOrderedAttendees(segments, meeting.getUserId()));
                response.setSegmentMarkTimes(segments.stream()
                        .filter(s -> StringUtils.isNotBlank(s.getMarkJson()))
                        .map(MeetingSegment::getStartTime)
                        .filter(Objects::nonNull)
                        .toList());
            } else {
                response.setSegmentMarkTimes(Collections.emptyList());
            }
        } else if (StringUtils.isNotBlank(meeting.getAttendees())) {
            response.setAttendees(JacksonUtil.json2Bean(meeting.getAttendees(), new TypeReference<List<String>>() {}));
        }
        if (response.getSegmentMarkTimes() == null) {
            response.setSegmentMarkTimes(Collections.emptyList());
        }

        response.setSummary(meeting.getSummary());
        response.setBexInterpretation(meeting.getBexInterpretation());
        
        // Parse chapterSkim JSON
        if (StringUtils.isNotBlank(meeting.getChapterSkim())) {
            response.setChapterSkim(JacksonUtil.json2Bean(meeting.getChapterSkim(), new TypeReference<List<MeetingChapterSkimItem>>() {}));
        }
        
        // Parse coreTopics JSON
        if (StringUtils.isNotBlank(meeting.getCoreTopics())) {
            response.setCoreTopics(JacksonUtil.json2Bean(meeting.getCoreTopics(), new TypeReference<List<CoreTopicItem>>() {}));
        }
        
        // Parse relatedMaterials JSON
        if (StringUtils.isNotBlank(meeting.getRelatedMaterials())) {
            response.setRelatedMaterials(JacksonUtil.json2Bean(meeting.getRelatedMaterials(), new TypeReference<List<String>>() {}));
        }
        
        // Parse knowledge JSON
        if (StringUtils.isNotBlank(meeting.getKnowledge())) {
            response.setKnowledge(JacksonUtil.json2Bean(meeting.getKnowledge(), new TypeReference<List<String>>() {}));
        }
        
        // Parse attendeeBexAnalysis JSON
        if (StringUtils.isNotBlank(meeting.getAttendeeBexAnalysis())) {
            response.setAttendeeBexAnalysis(JacksonUtil.json2Bean(meeting.getAttendeeBexAnalysis(), new TypeReference<List<AttendeeBexAnalysisItem>>() {}));
        }
        
        // Parse bexAdvice JSON
        if (StringUtils.isNotBlank(meeting.getBexAdvice())) {
            response.setBexAdvice(JacksonUtil.json2Bean(meeting.getBexAdvice(), new TypeReference<BexAdvice>() {}));
        }
        
        // Parse todo JSON
        if (StringUtils.isNotBlank(meeting.getTodo())) {
            response.setTodo(JacksonUtil.json2Bean(meeting.getTodo(), new TypeReference<List<TodoItem>>() {}));
        }

        return response;
    }

    /**
     * 会议详情：仅会议归属用户（speakerId 与会议 userId 一致）在存在头像时返回可访问 URL；其余说话人不返回头像 URL。
     * {@code speakerAvatarInitial} 始终按当前展示名计算，便于改名后前端绘制默认头像。
     */
    private void enrichMeetingDetailSegments(List<MeetingSegment> segments, String meetingOwnerUserId) {
        if (segments == null || segments.isEmpty()) {
            return;
        }
        String ownerAvatarUrl = resolveMeetingOwnerAvatarUrl(meetingOwnerUserId, segments);
        for (MeetingSegment s : segments) {
            s.setSpeakerAvatarInitial(SpeakerDisplayUtil.avatarInitialFromSpeakerName(s.getSpeakerName()));
            if (StringUtils.isNotBlank(meetingOwnerUserId) && meetingOwnerUserId.equals(s.getSpeakerId())
                    && StringUtils.isNotBlank(ownerAvatarUrl)) {
                s.setSpeakerAvatar(ownerAvatarUrl);
            } else {
                s.setSpeakerAvatar(null);
            }
        }
    }

    private String resolveMeetingOwnerAvatarUrl(String meetingOwnerUserId, List<MeetingSegment> segments) {
        if (StringUtils.isBlank(meetingOwnerUserId)) {
            return null;
        }
        String raw = null;
        try {
            Response<String> res = managementServiceClient.getUserAvatar(meetingOwnerUserId);
            if (res != null && StringUtils.isNotBlank(res.getResContent())) {
                raw = res.getResContent().trim();
            }
        } catch (Exception e) {
            logger.debug("[会议详情] 拉取归属用户头像失败 userId={}: {}", meetingOwnerUserId, e.getMessage());
        }
        if (StringUtils.isBlank(raw) && segments != null) {
            raw = segments.stream()
                    .filter(s -> meetingOwnerUserId.equals(s.getSpeakerId()))
                    .map(MeetingSegment::getSpeakerAvatar)
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElse(null);
        }
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String presigned = storageUtil.getPresignedUrlForObjectKey(raw);
        if (StringUtils.isNotBlank(presigned)) {
            return presigned;
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        return null;
    }

    @Override
    public void updateTodoStatus(TodoStatusUpdateRequest request) {
        Meeting meeting = meetingDAO.selectByMeetingId(request.getMeetingId());
        List<TodoItem> todoList = JacksonUtil.json2Bean(meeting.getTodo(), new TypeReference<List<TodoItem>>() {});
        if (CollectionUtils.isEmpty(todoList)) {
            logger.info("会议:{} 待办项为空", request.getMeetingId());
            return;
        }
        todoList.stream()
                .filter(todo -> request.getTodoId().equals(todo.getId()))
                .findFirst()
                .ifPresent(todo -> {
                    todo.setStatus(request.getStatus());
                    if (request.getStatus() == 1) {
                        ScheduleCreateRequest scheduleRequest = new ScheduleCreateRequest();
                        scheduleRequest.setUserId(request.getUserId());
                        scheduleRequest.setMeetingId(meeting.getMeetingId());
                        scheduleRequest.setMeetingTitle(meeting.getTitle());
                        scheduleRequest.setQueryStr("时间: " + todo.getDatetime() + ",任务: " + todo.getEvent());
                        logger.info("会议总结待办项关联日程, 用户id: {}, 发送请求: {}", request.getUserId(), JacksonUtil.bean2Json(scheduleRequest));
                        Response<String> scheduleResponse = scheduleServiceClient.createSchedule(request.getUserId(), scheduleRequest);
                        logger.info("会议总结待办项关联日程, 接收响应: {}", JacksonUtil.bean2Json(scheduleResponse));
                        if (Objects.isNull(scheduleResponse) || !ResponseStatusBasic.OK.getResCode().equals(scheduleResponse.getResCode()) || Objects.isNull(scheduleResponse.getResContent())) {
                            logger.info("会议:{} 待办项:{} 关联日程失败", request.getMeetingId(), todo.getId());
                            throw new RuntimeException("会议待办项关联日程失败");
                        }
                        todo.setScheduleId(scheduleResponse.getResContent());
                    }
                    meeting.setTodo(JacksonUtil.bean2Json(todoList));
                    meetingDAO.updateMeetingByMeetingId(meeting);
                });
    }

    @Override
    public void completeTodo(TodoStatusUpdateRequest request) {
        Meeting meeting = meetingDAO.selectByMeetingId(request.getMeetingId());
        List<TodoItem> todoList = JacksonUtil.json2Bean(meeting.getTodo(), new TypeReference<List<TodoItem>>() {});
        if (CollectionUtils.isEmpty(todoList)) {
            return;
        }
        todoList.stream()
                .filter(todo -> request.getTodoId().equals(todo.getId()))
                .findFirst()
                .ifPresent(todo -> {
                    todo.setCompleted(request.getCompleted());
                    meeting.setTodo(JacksonUtil.bean2Json(todoList));
                    meetingDAO.updateMeetingByMeetingId(meeting);
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameSpeaker(MeetingSpeakerRenameRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getSpeakerId(), "speakerId");
        Asserts.notBlank(request.getSpeakerName(), "speakerName");

        Meeting meeting = meetingDAO.selectByMeetingId(request.getMeetingId());
        if (meeting == null) {
            throw new IllegalArgumentException("会议不存在");
        }
        if (!Objects.equals(meeting.getUserId(), request.getUserId())) {
            throw new IllegalArgumentException("无权操作该会议");
        }

        String newName = request.getSpeakerName().trim();
        int updated = meetingSegmentDAO.updateSpeakerNameByMeetingIdAndSpeakerId(
                request.getMeetingId(), request.getSpeakerId(), newName);
        if (updated <= 0) {
            logger.warn("发言人改名未更新任何段落 meetingId={}, speakerId={}", request.getMeetingId(), request.getSpeakerId());
        }

        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(request.getMeetingId());
        meeting.setSegmentsJson(JacksonUtil.bean2Json(segments));
        meeting.setAttendees(JacksonUtil.bean2Json(MeetingAttendeeOrderUtil.buildOrderedAttendees(segments, meeting.getUserId())));
        meetingDAO.updateMeetingByMeetingId(meeting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingDetailMarkResponse markOnMeetingDetail(MeetingDetailMarkRequest request) {
        Asserts.notNull(request, "request");
        Asserts.notBlank(request.getMeetingId(), "meetingId");
        Asserts.notBlank(request.getUserId(), "userId");
        Asserts.notBlank(request.getSegmentId(), "segmentId");
        Asserts.notNull(request.getOffsetMs(), "offsetMs");
        long offsetMs = request.getOffsetMs();
        if (offsetMs < 0) {
            throw new IllegalArgumentException("offsetMs 无效");
        }

        Meeting meeting = meetingDAO.selectByMeetingId(request.getMeetingId());
        if (meeting == null) {
            throw new IllegalArgumentException("会议不存在");
        }
        if (!Objects.equals(meeting.getUserId(), request.getUserId())) {
            throw new IllegalArgumentException("无权操作该会议");
        }

        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(request.getMeetingId());
        if (CollectionUtils.isEmpty(segments)) {
            throw new IllegalArgumentException("会议无段落数据");
        }

        MeetingSegment target = segments.stream()
                .filter(s -> request.getSegmentId().equals(s.getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("段落不存在"));

        if (!offsetWithinSegmentForMark(offsetMs, target)) {
            throw new IllegalArgumentException("打标位置不在该段落时间范围内");
        }

        for (MeetingSegment s : segments) {
            if (s.getSegmentId().equals(target.getSegmentId())) {
                continue;
            }
            if (StringUtils.isBlank(s.getMarkJson())) {
                continue;
            }
            Long existingMs = resolveStoredMarkTimelineMs(s);
            if (existingMs == null) {
                continue;
            }
            if (Math.abs(existingMs - offsetMs) < GlobalConstants.MEETING_MARK_MIN_INTERVAL_MS) {
                throw new IllegalArgumentException("打标距离过近，无法重复打标");
            }
        }

        long now = System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("offsetMs", offsetMs);
        payload.put("markedAt", now);
        String markJson = JacksonUtil.bean2Json(payload);

        int updated = meetingSegmentDAO.updateMarkJsonByMeetingIdAndSegmentId(
                request.getMeetingId(), target.getSegmentId(), markJson);
        if (updated == 0) {
            throw new IllegalArgumentException("更新打标失败，段落可能未入库");
        }

        List<MeetingSegment> refreshed = meetingSegmentDAO.selectByMeetingId(request.getMeetingId());
        meeting.setSegmentsJson(JacksonUtil.bean2Json(refreshed));
        meeting.setAttendees(JacksonUtil.bean2Json(MeetingAttendeeOrderUtil.buildOrderedAttendees(refreshed, meeting.getUserId())));
        meetingDAO.updateMeetingByMeetingId(meeting);

        return new MeetingDetailMarkResponse("mark-" + snowflake.nextId());
    }

    private static boolean offsetWithinSegmentForMark(long offsetMs, MeetingSegment segment) {
        Long start = segment.getStartTime();
        Long end = segment.getEndTime();
        long lo = start != null ? start : 0L;
        if (end == null) {
            return offsetMs >= lo;
        }
        return offsetMs >= lo && offsetMs <= end;
    }

    /** 与其它打标比较间隔：优先 mark_json.offsetMs，否则段 startTime（旧数据） */
    private static Long resolveStoredMarkTimelineMs(MeetingSegment segment) {
        Long fromJson = parseOffsetMsFromMarkJson(segment.getMarkJson());
        if (fromJson != null) {
            return fromJson;
        }
        return segment.getStartTime();
    }

    private static Long parseOffsetMsFromMarkJson(String markJson) {
        if (StringUtils.isBlank(markJson)) {
            return null;
        }
        try {
            Map<String, Object> map = JacksonUtil.json2Bean(markJson, new TypeReference<Map<String, Object>>() {});
            if (map == null || !map.containsKey("offsetMs")) {
                return null;
            }
            Object o = map.get("offsetMs");
            if (o instanceof Number) {
                return ((Number) o).longValue();
            }
            if (o != null) {
                return Long.parseLong(o.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
