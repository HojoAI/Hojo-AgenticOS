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

import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingImportTaskDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingImportTask;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 避免 MeetingImportServiceImpl 自调用导致事务不生效。
 */
@Component
public class MeetingImportTransactionHelper {

    @Resource
    private MeetingDAO meetingDAO;
    @Resource
    private MeetingImportTaskDAO meetingImportTaskDAO;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insertMeetingAndTask(Meeting meeting, MeetingImportTask task) {
        meetingDAO.insertMeeting(meeting);
        meetingImportTaskDAO.insert(task);
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyUploadComplete(MeetingImportTask task, String ossObjectKey) {
        meetingImportTaskDAO.updateByMeetingId(task);
        meetingDAO.updateMeetingOssUrl(task.getMeetingId(), ossObjectKey);
    }
}
