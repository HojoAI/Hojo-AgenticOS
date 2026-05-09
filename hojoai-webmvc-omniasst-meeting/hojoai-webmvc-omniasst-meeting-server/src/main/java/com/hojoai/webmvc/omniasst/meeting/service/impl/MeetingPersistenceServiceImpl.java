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

import com.alibaba.nacos.shaded.com.google.common.base.Stopwatch;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.common.api.conversation.ConversationPushRequest;
import com.hojoai.omniasst.common.api.conversation.MessageContentItem;
import com.hojoai.omniasst.common.constans.conversation.AgentType;
import com.hojoai.omniasst.common.constans.conversation.ContextRole;
import com.hojoai.omniasst.common.constans.conversation.MessageType;
import com.hojoai.omniasst.common.constans.conversation.SubType;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.ContentVal;
import com.hojoai.webmvc.omniasst.meeting.api.dto.ContentValCard;
import com.hojoai.webmvc.omniasst.meeting.api.dto.ConversationInfo;
import com.hojoai.webmvc.omniasst.meeting.api.dto.MessageCenterCreateTaskRequest;
import com.hojoai.webmvc.omniasst.meeting.api.dto.PAPushRequestDTO;
import com.hojoai.webmvc.omniasst.meeting.common.GlobalConstants;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.MeetingSegmentDAO;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.Meeting;
import com.hojoai.webmvc.omniasst.meeting.dao.meeting.po.MeetingSegment;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingPersistenceService;
import com.hojoai.webmvc.omniasst.meeting.service.MeetingTransactionService;
import com.hojoai.webmvc.omniasst.meeting.service.ModelService;
import com.hojoai.webmvc.omniasst.meeting.service.bean.MeetingSummaryResponse;
import com.hojoai.webmvc.omniasst.meeting.util.MeetingAttendeeOrderUtil;
import com.hojoai.webmvc.omniasst.meeting.util.MeetingTitleUtil;
import com.hojoai.webmvc.omniasst.meeting.util.TimeFormatUtil;
import com.hojoai.webmvc.omniasst.meeting.websocket.bean.dto.MeetingResultMessage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.downgoon.snowflake.Snowflake;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 会议持久化与衍生逻辑：段落落库、纪要生成、消息中心 push、与会者顺序等。
 */
@Service
public class MeetingPersistenceServiceImpl implements MeetingPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingPersistenceServiceImpl.class);

    @Resource
    private MeetingDAO meetingDAO;

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    @Resource
    private ModelService modelService;

    @Resource
    private MeetingTransactionService meetingTransactionService;

    @Resource
    private ManagementServiceClient managementServiceClient;

    @Resource
    private Snowflake snowflake;

    @Override
    public void initMeetingInfo(MeetingResultMessage message) {
        String meetingId = message.getMeetingId();
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);

        Meeting initialMeeting = new Meeting();
        initialMeeting.setMeetingId(meetingId);
        initialMeeting.setUserId(message.getUserId());
        initialMeeting.setStartTime(message.getStartTime());
        initialMeeting.setEndTime(message.getEndTime());
        initialMeeting.setTotalDuration(message.getTotalDuration());
        initialMeeting.setSummaryStatus(0);
        initialMeeting.setOssUrl(message.getOssUrl());
        initialMeeting.setSegmentsJson(JacksonUtil.bean2Json(segments));
        if (meetingDAO.selectByMeetingId(meetingId) != null) {
            meetingDAO.updateMeetingByMeetingId(initialMeeting);
            logger.info("[会议结果持久化] 会议记录已存在，更新初始快照, meetingId: {}", meetingId);
        } else {
            meetingTransactionService.insertInitialMeeting(initialMeeting);
        }
    }

    @Override
    public void updateMeetingOssUrl(String meetingId, String ossUrl) {
        meetingDAO.updateMeetingOssUrl(meetingId, ossUrl);
    }

    @Override
    public void persistMeetingResult(MeetingResultMessage message, @SuppressWarnings("unused") String conversationId) {
        if (message == null) {
            logger.warn("[会议结果持久化] 消息为空, 忽略");
            return;
        }
        validateMessage(message);
        String meetingId = message.getMeetingId();
        Integer needSave = message.getNeedSave();
        logger.info("[会议结果持久化] 开始处理, meetingId: {}, needSave: {}, userId: {}", meetingId, needSave, message.getUserId());

        if (needSave != null && needSave == 0) {
            meetingTransactionService.deleteSegments(meetingId);
            return;
        }

        String userId = message.getUserId();
        Long totalDuration = message.getTotalDuration();

        logger.info("[会议结果持久化] 查询段数据, meetingId: {}", meetingId);
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        if (segments == null || segments.isEmpty()) {
            logger.warn("[会议结果持久化] 未找到段数据, 清理会议记录, meetingId: {}", meetingId);
            meetingTransactionService.deleteMeeting(meetingId);
            return;
        }
        logger.info("[会议结果持久化] 找到{}个段数据, meetingId: {}", segments.size(), meetingId);

        String messageCenterTaskId = null;
        boolean pipelineSuccess = false;
        Meeting initialMeeting = null;

        try {
            String query = buildModelQuery(segments, userId);
            messageCenterTaskId = startMessageCenterTaskAndNotify(userId, meetingId);

            Stopwatch meetingTitleStopWatch = Stopwatch.createStarted();
            String title = MeetingTitleUtil.truncateToMaxChars(modelService.getMeetingTitleResult(query), 25);
            meetingTitleStopWatch.stop();
            logger.info("[会议结果持久化] 会议标题已获取, meetingId: {}, summaryStatus: 0", meetingId);

            String transcriptContent = buildTranscriptContent(segments);
            List<String> attendeeIdentity = segments.stream().map(MeetingSegment::getSpeakerName).filter(Objects::nonNull).distinct().collect(toList());
            String myIdentity = resolveMyIdentity(segments, userId);

            Stopwatch meetingSummaryStopWatch = Stopwatch.createStarted();
            logger.info("[会议结果持久化] Hojo会议总结, transcriptContent长度: {}, attendeeIdentity: {}, myIdentity: {}", transcriptContent.length(), attendeeIdentity, myIdentity);
            MeetingSummaryResponse summaryResponse = modelService.getMeetingSummaryResult(transcriptContent, attendeeIdentity, myIdentity);
            meetingSummaryStopWatch.stop();
            logger.info("[会议结果持久化] Hojo会议总结耗时: {} ms", meetingSummaryStopWatch.elapsed(TimeUnit.MILLISECONDS));

            if (summaryResponse == null) {
                logger.error("[会议结果持久化] Hojo会议总结返回为空, meetingId: {}, 使用占位摘要降级入库", meetingId);
                initialMeeting = buildDegradedMeetingAfterSummaryFailure(meetingId, message, segments, totalDuration, userId, title);
                if (initialMeeting != null) {
                    logger.info("[会议结果持久化] 降级摘要入库, meetingId: {}", meetingId);
                    meetingTransactionService.updateMeetingSummary(initialMeeting);
                    pipelineSuccess = true;
                }
            } else {
                logger.info("[会议结果持久化] 会议总结解析成功, meetingId: {}", meetingId);
                enrichTodoWithAssigneeId(summaryResponse.getTodo(), segments, userId);

                initialMeeting = meetingTransactionService.selectMeetingById(meetingId);
                logger.info("[会议结果持久化] 数据库记录更新, meetingId: {}, 查询会议初始化数据: {}", initialMeeting.getMeetingId(), JacksonUtil.bean2Json(initialMeeting));
                initialMeeting.setMeetingId(meetingId);
                initialMeeting.setTitle(title);
                initialMeeting.setSummaryStatus(1);
                // 详情接口从 meetings.total_duration / segments_json 读取；段表仅 insert 时不会自动同步这两列
                initialMeeting.setTotalDuration(totalDuration);
                initialMeeting.setStartTime(message.getStartTime());
                initialMeeting.setEndTime(message.getEndTime());
                initialMeeting.setSegmentsJson(JacksonUtil.bean2Json(segments));
                meetingTransactionService.buildMeetingData(initialMeeting, summaryResponse);
                initialMeeting.setAttendees(JacksonUtil.bean2Json(MeetingAttendeeOrderUtil.buildOrderedAttendees(segments, userId)));
                logger.info("[会议结果持久化] 更新数据库数据, meetingId: {}, 入库数据: {}", initialMeeting.getMeetingId(), JacksonUtil.bean2Json(initialMeeting));
                meetingTransactionService.updateMeetingSummary(initialMeeting);

                pipelineSuccess = true;
            }
        } catch (Exception e) {
            logger.error("[会议结果持久化] 处理异常, meetingId: {}", meetingId, e);
        } finally {
            if (messageCenterTaskId != null && !pipelineSuccess) {
                safeCompleteMessageCenterTask(messageCenterTaskId, meetingId);
            }
        }

        if (pipelineSuccess && initialMeeting != null) {
            if (messageCenterTaskId != null) {
                safeCompleteMessageCenterTask(messageCenterTaskId, meetingId);
                pushFcmTaskFinishedSilently(userId, messageCenterTaskId, meetingId);
            }
            pushMeetingReadyMessageCenterSilently(userId, meetingId, initialMeeting);
        }

        logger.info("[会议结果持久化] 创建会议成功, meetingId: {}, userId: {}, segments: {}, totalDuration: {}ms", meetingId, userId, segments.size(), totalDuration);
    }

    /**
     * Hojo 摘要服务失败或返回无法解析时，仍写入标题、时长、段信息与占位摘要，避免会议长期停留在 summaryStatus=0。
     */
    private Meeting buildDegradedMeetingAfterSummaryFailure(String meetingId, MeetingResultMessage message,
            List<MeetingSegment> segments, Long totalDuration, String userId, String title) {
        Meeting meeting = meetingTransactionService.selectMeetingById(meetingId);
        if (meeting == null) {
            logger.error("[会议结果持久化] 降级失败, 会议记录不存在, meetingId: {}", meetingId);
            return null;
        }
        meeting.setMeetingId(meetingId);
        meeting.setTitle(title);
        meeting.setSummaryStatus(1);
        meeting.setTotalDuration(totalDuration);
        meeting.setStartTime(message.getStartTime());
        meeting.setEndTime(message.getEndTime());
        meeting.setSegmentsJson(JacksonUtil.bean2Json(segments));
        meeting.setSummary("会议摘要服务暂时不可用或返回格式异常，未能生成结构化摘要，请查看下方转写原文。");
        meeting.setBexInterpretation("");
        meeting.setMeetingType("");
        meeting.setKnowledge("[]");
        meeting.setKnowledgeCount(0);
        meeting.setCoreTopics("[]");
        meeting.setRelatedMaterials("[]");
        meeting.setAttendeeBexAnalysis("[]");
        meeting.setBexAdvice("[]");
        meeting.setTodo("[]");
        meeting.setChapterSkim("[]");
        meeting.setAttendees(JacksonUtil.bean2Json(MeetingAttendeeOrderUtil.buildOrderedAttendees(segments, userId)));
        return meeting;
    }

    /**
     * 创建消息中心进程任务并下发 FCM 静默任务推送；失败不抛异常、不阻塞会议主流程。
     *
     * @return taskId，创建失败返回 {@code null}
     */
    private String startMessageCenterTaskAndNotify(String userId, String meetingId) {
        try {
            MessageCenterCreateTaskRequest req = new MessageCenterCreateTaskRequest();
            req.setUserId(userId);
            req.setType(GlobalConstants.PUSH_CARD_TYPE);
            req.setTitle(GlobalConstants.MESSAGE_CENTER_TASK_TITLE);
            req.setProgressText("正在生成标题与会议摘要...");
            req.setRelatedId(meetingId);

            logger.info("[meeting] 消息中心创建进程任务, meetingId: {}, request: {}", meetingId, JacksonUtil.bean2Json(req));
            Response<String> res = managementServiceClient.createMessageCenterTask(req);
            if (res == null || !ResponseStatusBasic.OK.getResCode().equals(res.getResCode())
                    || StringUtils.isBlank(res.getResContent())) {
                logger.warn("[meeting] 消息中心创建进程任务失败, meetingId: {}, response: {}", meetingId, JacksonUtil.bean2Json(res));
                return null;
            }
            String taskId = res.getResContent().trim();
            logger.info("[meeting] 消息中心创建进程任务成功, meetingId: {}, taskId: {}", meetingId, taskId);
            pushFcmTaskProgressSilently(userId, taskId, meetingId);
            return taskId;
        } catch (Exception e) {
            logger.error("[消息中心] 创建进程任务异常, meetingId: {}", meetingId, e);
            return null;
        }
    }

    private void pushFcmTaskProgressSilently(String userId, String taskId, String meetingId) {
        try {
            PAPushRequestDTO push = new PAPushRequestDTO();
            push.setUserIdList(Lists.newArrayList(userId));
            push.setType(GlobalConstants.PUSH_CARD_TYPE);
            push.setBroadcast(Boolean.FALSE);
            push.setAction(GlobalConstants.MESSAGE_CENTER_WEB_MESSAGE_ACTION);
            push.setTitle(GlobalConstants.MESSAGE_CENTER_TASK_TITLE);
            push.setContent(StringUtils.EMPTY);
            push.setPushChannel(GlobalConstants.PUSH_CHANNEL_FCM);
            push.setMessageType(GlobalConstants.MESSAGE_CENTER_TYPE_TASK);
            push.setRelatedId(taskId);
            push.setSilent(Boolean.TRUE);
            logger.info("[meeting] FCM 任务进度推送, meetingId: {}, taskId: {}, request: {}", meetingId, taskId, JacksonUtil.bean2Json(push));
            managementServiceClient.push(push);
            pushFcmMeetingListRefreshSilently(userId, meetingId);
        } catch (Exception e) {
            logger.error("[meeting] FCM 任务进度推送异常, meetingId: {}, taskId: {}", meetingId, taskId, e);
        }
    }

    private void safeCompleteMessageCenterTask(String taskId, String meetingId) {
        try {
            logger.info("[meeting] 完成消息中心进程任务, meetingId: {}, taskId: {}", meetingId, taskId);
            managementServiceClient.completeMessageCenterTask(taskId);
        } catch (Exception e) {
            logger.error("[meeting] 完成消息中心进程任务异常, meetingId: {}, taskId: {}", meetingId, taskId, e);
        }
    }

    /**
     * 任务完成后：FCM 静默「任务结束」推送，端侧收到 status=finished 后刷新进程任务区域。
     */
    private void pushFcmTaskFinishedSilently(String userId, String taskId, String meetingId) {
        try {
            PAPushRequestDTO push = new PAPushRequestDTO();
            push.setUserIdList(Lists.newArrayList(userId));
            push.setType(GlobalConstants.PUSH_CARD_TYPE);
            push.setBroadcast(Boolean.FALSE);
            push.setAction(GlobalConstants.MESSAGE_CENTER_WEB_MESSAGE_ACTION);
            push.setTitle("会议纪要已生成");
            push.setContent("纪要已生成，点击查看详情");
            push.setPushChannel(GlobalConstants.PUSH_CHANNEL_FCM);
            push.setMessageType(GlobalConstants.MESSAGE_CENTER_TYPE_TASK);
            push.setRelatedId(taskId);
            push.setSilent(Boolean.TRUE);
            logger.info("[meeting] FCM 任务完成推送, meetingId: {}, taskId: {}, request: {}", meetingId, taskId, JacksonUtil.bean2Json(push));
            managementServiceClient.push(push);
        } catch (Exception e) {
            logger.error("[meeting] FCM 任务完成推送异常, meetingId: {}, taskId: {}", meetingId, taskId, e);
        }
    }

    /**
     * FCM 静默「页面刷新」推送，通知前端刷新会议列表。
     * payload 完全静态，直接预组装 firebaseData，不经过 management 的数据聚合逻辑。
     */
    private void pushFcmMeetingListRefreshSilently(String userId, String meetingId) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("pageName", GlobalConstants.PAGE_NAME_MEETING_LIST);

            Map<String, String> firebaseData = new LinkedHashMap<>();
            firebaseData.put("action", GlobalConstants.REFRESH_PAGE_ACTION);
            firebaseData.put("params", JacksonUtil.bean2Json(params));

            PAPushRequestDTO push = new PAPushRequestDTO();
            push.setUserIdList(Lists.newArrayList(userId));
            push.setType(GlobalConstants.PUSH_CARD_TYPE);
            push.setBroadcast(Boolean.FALSE);
            push.setPushChannel(GlobalConstants.PUSH_CHANNEL_FCM);
            push.setSilent(Boolean.TRUE);
            push.setFirebaseData(firebaseData);
            logger.info("[meeting] FCM 页面刷新推送, meetingId: {}, userId: {}", meetingId, userId);
            managementServiceClient.push(push);
        } catch (Exception e) {
            logger.error("[meeting] FCM 页面刷新推送异常, meetingId: {}, userId: {}", meetingId, userId, e);
        }
    }

    /**
     * 纪要落库成功后：FCM 静默「完成消息」（management 侧落库 + totalUnread），不阻塞会议流程。
     */
    private void pushMeetingReadyMessageCenterSilently(String userId, String meetingId, Meeting meeting) {
        try {
            PAPushRequestDTO push = new PAPushRequestDTO();
            push.setUserIdList(Lists.newArrayList(userId));
            push.setType(GlobalConstants.PUSH_CARD_TYPE);
            push.setBroadcast(Boolean.FALSE);
            push.setAction(GlobalConstants.MESSAGE_CENTER_WEB_MESSAGE_ACTION);
            push.setTitle("会议纪要已生成");
            push.setContent(StringUtils.defaultString(meeting.getSummary()));
            push.setPushChannel(GlobalConstants.PUSH_CHANNEL_FCM);
            push.setMessageType(GlobalConstants.MESSAGE_CENTER_TYPE_MESSAGE);
            push.setRelatedId(meetingId);
            push.setRelatedTitle(meeting.getTitle());
            push.setLandingPage(GlobalConstants.MEETING_DETAIL_LANDING_PREFIX + meetingId);
            push.setSilent(Boolean.TRUE);
            logger.info("[meeting] FCM 完成消息推送, meetingId: {}, request: {}", meetingId, JacksonUtil.bean2Json(push));
            managementServiceClient.push(push);
            pushFcmMeetingListRefreshSilently(userId, meetingId);
        } catch (Exception e) {
            logger.error("[meeting] FCM 完成消息推送异常, meetingId: {}", meetingId, e);
        }
    }

    private String buildModelQuery(List<MeetingSegment> segments, String userId) {
        boolean foundMatch = Objects.nonNull(userId) && segments.stream().anyMatch(segment -> userId.equals(segment.getSpeakerId()));
        
        // 生成会议逐字稿内容（保留"（我）"标识）
        String meetingcontent = segments.stream()
                .map(segment -> {
                    String speakerName = segment.getSpeakerName();
                    String startTime = TimeFormatUtil.formatMsToHHMMSS(segment.getStartTime());
                    String text = Objects.nonNull(segment.getText()) ? segment.getText() : "";
                    if (foundMatch && userId.equals(segment.getSpeakerId())) {
                        return String.format("%s（我） %s\n%s", speakerName, startTime, text);
                    }
                    return String.format("%s %s\n%s", speakerName, startTime, text);
                })
                .collect(Collectors.joining("\n"));
        
        // 获取当前用户的名字（owner）
        String owner = "";
        if (Objects.nonNull(userId)) {
            // 首先尝试从对话人中匹配
            owner = segments.stream()
                    .filter(segment -> userId.equals(segment.getSpeakerId()))
                    .map(MeetingSegment::getSpeakerName)
                    .findFirst()
                    .orElse("");
            try {
                if (StringUtils.isEmpty(owner)) {
                    Response<String> response = managementServiceClient.getUserName(userId);
                    if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getResContent())) {
                        owner = response.getResContent();
                    }
                }
            } catch (Exception e) {
                logger.error("[会议结果持久化] 组装用户query匹配发言人失败: {}", e.getMessage());
            }
        }
        // 若匹配不到我的名称，则小节标题改为「# 我不在会议中」；否则为「# 我的身份」+ 名字
        String identitySection = StringUtils.isEmpty(owner) ? "# 我不在会议中" : "# 我的身份\n" + owner;

        // 生成模板格式
        return String.format("# 会议逐字稿内容\n%s\n\n\n%s", meetingcontent, identitySection);
    }

    /** 构建 Hojo 接口所需的纯文本逐字稿，格式：发言人：文本\n */
    private String buildTranscriptContent(List<MeetingSegment> segments) {
        return segments.stream()
                .map(segment -> {
                    String speakerName = segment.getSpeakerName() != null ? segment.getSpeakerName() : "";
                    String text = segment.getText() != null ? segment.getText() : "";
                    return speakerName + "：" + text;
                })
                .collect(Collectors.joining("\n"));
    }

    private String resolveMyIdentity(List<MeetingSegment> segments, String userId) {
        if (StringUtils.isBlank(userId)) {
            return StringUtils.EMPTY;
        }
        String owner = segments.stream().filter(segment -> userId.equals(segment.getSpeakerId())).map(MeetingSegment::getSpeakerName).findFirst().orElse(StringUtils.EMPTY);
        if (StringUtils.isNotEmpty(owner)) {
            return owner;
        }
        try {
            Response<String> response = managementServiceClient.getUserName(userId);
            if (Objects.nonNull(response) && StringUtils.isNotBlank(response.getResContent())) {
                return response.getResContent();
            }
        } catch (Exception e) {
            logger.error("[会议结果持久化] 解析录制者名字失败: {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    private void enrichTodoWithAssigneeId(List<MeetingSummaryResponse.TodoItem> todoItems, List<MeetingSegment> segments, String userId) {
        if (Objects.isNull(todoItems) || Objects.isNull(segments)) {
            return;
        }
        Map<String, String> nameToIdMap = segments.stream()
                .filter(s -> Objects.nonNull(s.getSpeakerName()) && Objects.nonNull(s.getSpeakerId()))
                .collect(Collectors.toMap(
                        MeetingSegment::getSpeakerName,
                        MeetingSegment::getSpeakerId,
                        (existing, replacement) -> existing
                ));
        
        todoItems.forEach(todo -> {
            if (Objects.isNull(todo.getId())) {
                todo.setId(String.valueOf(snowflake.nextId()));
            }
            if (Objects.isNull(todo.getStatus())) {
                todo.setStatus(0);
            }
            if (Objects.isNull(todo.getCompleted())) {
                todo.setCompleted(0);
            }
            if (Objects.nonNull(todo.getAssignee()) && Objects.isNull(todo.getAssigneeId())) {
                todo.setAssigneeId(nameToIdMap.get(todo.getAssignee()));
            }
            if (Objects.nonNull(userId) && Objects.nonNull(todo.getAssigneeId())) {
                todo.setIsRecorderTodo(userId.equals(todo.getAssigneeId()));
            } else {
                todo.setIsRecorderTodo(false);
            }
        });
    }

    private void validateMessage(MeetingResultMessage message) {
        Asserts.notBlank(message.getMeetingId(), "meetingId");
        Asserts.notNull(message.getNeedSave(), "needSave");

        Integer needSave = message.getNeedSave();
        if (needSave != null && needSave == 1) {
            Asserts.notBlank(message.getUserId(), "userId");
            Asserts.notNull(message.getTotalDuration(), "totalDuration");
            Asserts.gteZero(message.getTotalDuration(), "totalDuration");
            Asserts.notNull(message.getStartTime(), "startTime");
            Asserts.notNull(message.getEndTime(), "endTime");
        }
    }

    /**
     * 历史逻辑：友盟 + 主对话流推送会议卡片。海外版已改由消息中心 + FCM，本方法暂不再调用，保留友盟时代实现供对照。
     *
     * @deprecated 由 {@link #pushMeetingReadyMessageCenterSilently} 与消息中心任务链路替代
     */
    @Deprecated
    private void pushMeetingResultToApp(String userId, String conversationId, Meeting initialMeeting, MeetingSummaryResponse summaryResponse) {
        ContentValCard.CardData cardData = new ContentValCard.CardData();
        cardData.setRecordingId(initialMeeting.getMeetingId());
        cardData.setTitle(initialMeeting.getTitle());
        cardData.setSubtitle(String.format("内容包含%d个知识碎片，%d个待办", initialMeeting.getKnowledgeCount(), Objects.nonNull(summaryResponse.getTodo()) ? summaryResponse.getTodo().size() : 0));

        ContentValCard contentValCard = new ContentValCard();
        contentValCard.setData(cardData);

        ContentVal cardContentVal = new ContentVal();
        cardContentVal.setType("text");
        cardContentVal.setSubType(SubType.CARD.getValue());
        cardContentVal.setText(JacksonUtil.bean2Json(contentValCard));

        ContentVal bubbleContentVal = new ContentVal();
        bubbleContentVal.setType("text");
        bubbleContentVal.setSubType(SubType.BUBBLE.getValue());
        bubbleContentVal.setText("<tag_meeting>刚刚的录音已经为你处理完啦，快来看看吧！");

        ConversationInfo conversationInfo = new ConversationInfo();
        conversationInfo.setConversationId(conversationId);
        conversationInfo.setMeetingId(initialMeeting.getMeetingId());
        conversationInfo.setRole("assistant");
        conversationInfo.setTimestamp(System.currentTimeMillis());
        conversationInfo.setCreatedAt(Instant.ofEpochMilli(initialMeeting.getStartTime()).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        conversationInfo.setContentJson(Lists.newArrayList(bubbleContentVal, cardContentVal));

        PAPushRequestDTO paPushRequestDTO = new PAPushRequestDTO();
        paPushRequestDTO.setUserIdList(Lists.newArrayList(userId));
        paPushRequestDTO.setType(GlobalConstants.PUSH_CARD_TYPE);
        paPushRequestDTO.setBroadcast(Boolean.FALSE);
        paPushRequestDTO.setAction(GlobalConstants.PUSH_CARD_ACTION);
        paPushRequestDTO.setTitle(initialMeeting.getTitle());
        paPushRequestDTO.setContent(initialMeeting.getSummary());
        paPushRequestDTO.setConversationInfo(conversationInfo);

        logger.info("[会议结果持久化] 会议卡片推送: {}", JacksonUtil.bean2Json(paPushRequestDTO));
        managementServiceClient.push(paPushRequestDTO);

        MessageContentItem bubbleMessageContentItem = new MessageContentItem();
        bubbleMessageContentItem.setType(MessageType.TEXT.getValue());
        bubbleMessageContentItem.setText("<tag_meeting>刚刚的录音已经为你处理完啦，快来看看吧！");
        bubbleMessageContentItem.setUrl(StringUtils.EMPTY);
        bubbleMessageContentItem.setAgent(AgentType.MEETING.getValue());
        bubbleMessageContentItem.setRole(ContextRole.AGENT.getValue());
        bubbleMessageContentItem.setSubType(SubType.BUBBLE.getValue());
        bubbleMessageContentItem.setMetadata(Maps.newHashMap());

        MessageContentItem cardMessageContentItem = new MessageContentItem();
        cardMessageContentItem.setType(MessageType.TEXT.getValue());
        cardMessageContentItem.setText(JacksonUtil.bean2Json(contentValCard));
        cardMessageContentItem.setUrl(StringUtils.EMPTY);
        cardMessageContentItem.setAgent(AgentType.MEETING.getValue());
        cardMessageContentItem.setRole(ContextRole.AGENT.getValue());
        cardMessageContentItem.setSubType(SubType.CARD.getValue());
        cardMessageContentItem.setMetadata(Maps.newHashMap());

        ConversationPushRequest pushMessageRequest = new ConversationPushRequest();
        pushMessageRequest.setUserId(userId);
        pushMessageRequest.setTitle(initialMeeting.getTitle());
        pushMessageRequest.setConversationId(conversationId);
        pushMessageRequest.setContent(Lists.newArrayList(bubbleMessageContentItem, cardMessageContentItem));
        managementServiceClient.pushMessage(pushMessageRequest);
        logger.info("[会议结果持久化] 会议消息推送: {}", JacksonUtil.bean2Json(pushMessageRequest));

    }

}
