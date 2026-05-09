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
package com.hojoai.omniasst.management.service.meeting.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.page.IPage;
import com.hojoai.omniasst.management.common.GlobalConstants;
import com.hojoai.omniasst.management.dao.meeting.MeetingDAO;
import com.hojoai.omniasst.management.dao.meeting.po.Meeting;
import com.hojoai.omniasst.management.service.meeting.MeetingService;
import com.hojoai.omniasst.management.service.meeting.bean.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service implementation for meeting list and detail.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class MeetingServiceImpl implements MeetingService {

    @Resource
    private MeetingDAO meetingDAO;

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
            response.setEstimatedAiTime(15); // Fixed value: 15 minutes
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
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setTotalDuration(meeting.getTotalDuration());
        response.setSummaryStatus(meeting.getSummaryStatus());
        response.setCreatedAt(DateUtil.formatAsSecond(meeting.getCreatedAt()));
        if (meeting.getUpdatedAt() != null) {
            response.setUpdatedAt(DateUtil.formatAsSecond(meeting.getUpdatedAt()));
        }

        response.setOssUrl(meeting.getOssUrl());
        
        // Parse segments JSON
        if (StringUtils.isNotBlank(meeting.getSegmentsJson())) {
            response.setSegments(JacksonUtil.json2Bean(meeting.getSegmentsJson(), new TypeReference<List<MeetingSegment>>() {}));
        }
        
        // Parse attendees JSON
        if (StringUtils.isNotBlank(meeting.getAttendees())) {
            response.setAttendees(JacksonUtil.json2Bean(meeting.getAttendees(), new TypeReference<List<String>>() {}));
        }
        
        response.setSummary(meeting.getSummary());
        response.setBexInterpretation(meeting.getBexInterpretation());
        
        // Parse chapterSkim JSON
        if (StringUtils.isNotBlank(meeting.getChapterSkim())) {
            response.setChapterSkim(JacksonUtil.json2Bean(meeting.getChapterSkim(), new TypeReference<List<ChapterSkimItem>>() {}));
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
}
