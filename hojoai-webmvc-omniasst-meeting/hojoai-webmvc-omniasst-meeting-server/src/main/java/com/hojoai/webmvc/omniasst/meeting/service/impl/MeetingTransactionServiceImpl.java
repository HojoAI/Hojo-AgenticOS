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

import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingTransactionService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingChapterSkimItem;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSummaryResponse;
import com.hojoai.webmvc.omniasst.meeting.util.TimeFormatUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * {@link com.hojoai.webmvc.omniasst.meeting.service.MeetingTransactionService} 实现：事务边界内更新会议与纪要等。
 */
@Service
public class MeetingTransactionServiceImpl implements MeetingTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingTransactionService.class);

    private static final Random RANDOM = new Random();

    @Resource
    private MeetingDAO meetingDAO;

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
    public void deleteSegments(String meetingId) {
        logger.info("[会议结果持久化] needSave=0, 准备删除段数据, meetingId: {}", meetingId);
        int deletedCount = meetingSegmentDAO.deleteByMeetingId(meetingId);
        logger.info("[会议结果持久化] needSave=0, 已删除{}个段数据, meetingId: {}", deletedCount, meetingId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
    public void insertInitialMeeting(Meeting meeting) {
        meetingDAO.insertMeeting(meeting);
        logger.info("[会议结果持久化] 初始会议记录插入完成, meetingId: {}, userId: {}, ossUrl: {}", 
            meeting.getMeetingId(), meeting.getUserId(), meeting.getOssUrl());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
    public void updateMeetingSummary(Meeting meeting) {
        logger.info("[会议结果持久化] 开始更新会议摘要, meetingId: {}", meeting.getMeetingId());
        meetingDAO.updateMeetingByMeetingId(meeting);
        logger.info("[会议结果持久化] 会议摘要更新完成, meetingId: {}", meeting.getMeetingId());
    }

    public void buildMeetingData(Meeting meeting, MeetingSummaryResponse summaryResponse) {
        meeting.setSummary(summaryResponse.getBasicInfo().getSummary());
        meeting.setBexInterpretation(summaryResponse.getBasicInfo().getBexInterpretation());
        meeting.setMeetingType(summaryResponse.getBasicInfo().getMeetingType());

        meeting.setKnowledge("[]");
        meeting.setKnowledgeCount(RANDOM.nextInt(16)); // Random 0-15

        meeting.setAttendees(JacksonUtil.bean2Json(summaryResponse.getBasicInfo().getAttendees()));
        meeting.setCoreTopics(JacksonUtil.bean2Json(summaryResponse.getCoreTopics()));
        meeting.setRelatedMaterials(JacksonUtil.bean2Json(summaryResponse.getRelatedMaterials()));
        meeting.setAttendeeBexAnalysis(JacksonUtil.bean2Json(summaryResponse.getAttendeeBexAnalysis()));
        meeting.setBexAdvice(JacksonUtil.bean2Json(summaryResponse.getBexAdvice()));
        meeting.setTodo(JacksonUtil.bean2Json(summaryResponse.getTodo()));

        // HH:MM:SS 转毫秒级时间
        List<MeetingChapterSkimItem> chapterSkimList = summaryResponse.getChapterSkim().stream().map(item -> {
            MeetingChapterSkimItem dto = new MeetingChapterSkimItem();
            dto.setTimestamp(TimeFormatUtil.hhmmssToMillis(item.getTimestamp()));
            dto.setBexVibe(item.getBexVibe());
            dto.setBexInterpretation(item.getBexInterpretation());
            dto.setContent(item.getContent());
            return dto;
        }).collect(Collectors.toList());
        meeting.setChapterSkim(JacksonUtil.bean2Json(chapterSkimList));
        logger.info("[会议结果持久化] 会议数据组装完成, meetingId: {}", meeting.getMeetingId());
    }

    @Override
    public Meeting selectMeetingById(String meetingId) {
        return meetingDAO.selectByMeetingId(meetingId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
    @Override
    public void deleteMeeting(String meetingId) {
        logger.info("[会议结果持久化] 准备删除会议记录, meetingId: {}", meetingId);
        int deletedCount = meetingDAO.deleteByMeetingId(meetingId);
        logger.info("[会议结果持久化] 已删除会议记录, meetingId: {}, deletedCount: {}", meetingId, deletedCount);
    }
}
