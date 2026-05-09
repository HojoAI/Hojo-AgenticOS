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

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.management.dao.meeting.MeetingDAO;
import com.hojoai.omniasst.management.dao.meeting.MeetingSegmentDAO;
import com.hojoai.omniasst.management.dao.meeting.po.Meeting;
import com.hojoai.omniasst.management.dao.meeting.po.MeetingSegment;
import com.hojoai.omniasst.management.service.meeting.MeetingPersistenceService;
import com.hojoai.omniasst.management.service.meeting.bean.MeetingResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * The service implementation for meeting persistence.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Service
public class MeetingPersistenceServiceImpl implements MeetingPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingPersistenceServiceImpl.class);

    private static final String DEFAULT_TITLE = "新的录音";
    
    private static final Random RANDOM = new Random();

    @Resource
    private MeetingDAO meetingDAO;

    @Resource
    private MeetingSegmentDAO meetingSegmentDAO;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {Exception.class})
    public void persistMeetingResult(MeetingResultMessage message) {
        if (message == null) {
            logger.warn("Meeting result message is null, ignore.");
            return;
        }

        validateMessage(message);

        String meetingId = message.getMeetingId();
        Integer needSave = message.getNeedSave();

        // Handle needSave == 0: delete segments and do not create meeting records
        if (needSave != null && needSave == 0) {
            int deletedCount = meetingSegmentDAO.deleteByMeetingId(meetingId);
            logger.info("Meeting needSave=0, deleted {} segments for meetingId: {}", deletedCount, meetingId);
            return;
        }

        // Handle needSave == 1: create meeting records from segments
        String userId = message.getUserId();
        Long totalDuration = message.getTotalDuration();
        Long startTime = message.getStartTime();
        Long endTime = message.getEndTime();
        String ossUrl = message.getOssUrl();

        // Check if meeting already exists
        Meeting meeting = meetingDAO.selectByMeetingId(meetingId);
        if (meeting != null) {
            logger.info("Meeting already exists, skip persistence for meetingId: {}", meetingId);
            return;
        }

        // Query segments from meeting_segments table
        List<MeetingSegment> segments = meetingSegmentDAO.selectByMeetingId(meetingId);
        if (segments == null || segments.isEmpty()) {
            logger.warn("No segments found for meetingId: {}, skip creating meeting", meetingId);
            return;
        }

        // Convert segments to JSON
        String segmentsJson = JacksonUtil.bean2Json(segments);

        // Create meeting with all fields
        Meeting newMeeting = new Meeting();
        newMeeting.setMeetingId(meetingId);
        newMeeting.setUserId(userId);
        newMeeting.setTitle(DEFAULT_TITLE);
        newMeeting.setStartTime(startTime);
        newMeeting.setEndTime(endTime);
        newMeeting.setTotalDuration(totalDuration);
        newMeeting.setSummaryStatus(RANDOM.nextInt(2)); // Random 0 or 1
        newMeeting.setOssUrl(ossUrl);
        newMeeting.setSegmentsJson(segmentsJson);
        
        // 会议摘要等能力未异步接入前，写入占位内容以满足列表/详情展示与联调
        fillPlaceholderMeetingEnrichment(newMeeting);
        
        meetingDAO.insertMeeting(newMeeting);
        logger.info("Create meeting success, meetingId: {}, segments count: {}", meetingId, segments.size());
    }

    private void validateMessage(MeetingResultMessage message) {
        Asserts.notBlank(message.getMeetingId(), "meetingId");
        Asserts.notNull(message.getNeedSave(), "needSave");

        Integer needSave = message.getNeedSave();
        if (needSave != null && needSave == 1) {
            Asserts.notBlank(message.getUserId(), "userId");
            Asserts.notBlank(message.getOssUrl(), "ossUrl");
            Asserts.notNull(message.getTotalDuration(), "totalDuration");
            Asserts.gteZero(message.getTotalDuration(), "totalDuration");
            Asserts.notNull(message.getStartTime(), "startTime");
            Asserts.notNull(message.getEndTime(), "endTime");
        }
    }

    /**
     * 在全文摘要、章节速览等 AI 能力由独立流水线产出并回写前，为新建会议写入占位数据，
     * 保证非空字段与前端展示一致；生产环境可在接入异步任务后改为空值或真实结果。
     *
     * @param meeting 待持久化的新会议实体
     */
    private void fillPlaceholderMeetingEnrichment(Meeting meeting) {
        // 占位标题与摘要（后续可由异步任务覆盖）
        meeting.setTitle("Q3 产品运营复盘及 Q4 \"智能助手\"功能上线计划汇报");
        meeting.setSummary("在 Q3 核心数据未达标的压力下，通过砍掉非核心功能（语音指令），强行换取 Q4 核心功能（智能助手）提前半个月上线，并确定了\"先验证后定价\"的敏捷策略。");
        meeting.setBexInterpretation("这是一场\"行政施压与生存博弈\"的会议。表面上是听取汇报，实际上李总（老板）带着极强的不满和掌控欲，对你（张三）的商业敏感度进行了一次\"压力面试\"。会议的后半程实际上已经变成了李总接管指挥权，你从\"汇报者\"变成了\"执行记录员\"。");

        // Mock knowledge (empty array)
        meeting.setKnowledge("[]");
        meeting.setKnowledgeCount(RANDOM.nextInt(16)); // Random 0-15

        // Mock attendees
        meeting.setAttendees("[\"张三\",\"李总\",\"王芳\",\"陈工\"]");

        // Mock chapterSkim (timestamp in milliseconds)
        String chapterSkim = "[" +
            "{\"timestamp\":0,\"content\":\"张三开场，并对Q3 数据汇报（流失率异常），Q4 规划及 Go-to-Market 策略汇报。\",\"BexVibe\":\"【中度紧张】\",\"BexInterpretation\":\"开场仅 1 分钟，李总直接打断汇报，质问流失率。这是给你\\\"下马威\\\"，奠定了整场会议严肃压抑的基调。\"}," +
            "{\"timestamp\":780000,\"content\":\"李总挑战 20% 提价逻辑。\",\"BexVibe\":\"【高密度区】\",\"BexInterpretation\":\"关于\\\"定价策略\\\"的激烈交锋。这里不仅推翻了原本的涨价计划，还直接产出了\\\"MVP 验证\\\"的新策略，商业决策密度极高。\"}," +
            "{\"timestamp\":960000,\"content\":\"营销策略调整：否决抖音，转投 SEM。\"}," +
            "{\"timestamp\":1020000,\"content\":\"销售赋能危机：李总批评培训未前置。\",\"BexVibe\":\"【极度紧张】\",\"BexInterpretation\":\"李总严厉批评销售培训滞后：\\\"太晚了！\\\"。这里涉及到了执行力的质疑，是作为下属最危险的时刻，你的回应显露了明显的慌张。\"}," +
            "{\"timestamp\":1080000,\"content\":\"上线时间生死线：砍功能，保 11.1 上线。\",\"BexVibe\":\"【高密度区】\",\"BexInterpretation\":\"关于\\\"技术排期 vs 功能取舍\\\"的博弈。通过砍掉语音功能换取时间，这是典型的用范围（Scope）换时间（Time）的项目管理决策。\"}," +
            "{\"timestamp\":1200000,\"content\":\"散会及 Action 确认。\"}" +
            "]";
        meeting.setChapterSkim(chapterSkim);

        // Mock coreTopics
        String coreTopics = "[" +
            "{\"topic\":\"Q3客户流失原因讨论\",\"content\":\"我（张三）认为UI Q3客户流失原因是改版不习惯以及竞对价格战，李总质疑 UI 改版决策为何要一刀切，陈工解释是架构升级太急，没做灰度。最终达成共识：Q4 必须做灰度发布，不再追究 Q3 责任，向前看。。\"}," +
            "{\"topic\":\"Q4新功能定价讨论\",\"content\":\"我（张三）认为应该涨价 20%（依据问卷），李总质疑问卷真实性，要求真金白银测试，王芳建议免费赠送以保续费。结论：本周三前找 10 个客户做 MVP 测试，根据意愿度决定是收费还是赠送。\"}" +
            "]";
        meeting.setCoreTopics(coreTopics);

        // Mock relatedMaterials
        meeting.setRelatedMaterials("[\"关于智能助手功能的产品prd文档\",\"Andrey kaparthy最近在yutube上的采访\"]");

        // Mock attendeeBexAnalysis
        String attendeeBexAnalysis = "[" +
            "{\"name\":\"李总\",\"engagement\":\"全程高能在线\",\"role\":\"主导者、决策者\",\"subtext\":\"\\\"我不看苦劳，只看功劳。你的 PPT 做得再花哨，不知道怎么卖钱就是废纸。我看你像是在'自嗨'。\\\" —— 意图：商业思维纠偏，施压以求结果。\",\"meVs\":\"考察与被考察。你在透支信任分，需要尽快通过 Action 证明执行力。\"}," +
            "{\"name\":\"王芳\",\"engagement\":\"全程高能在线\",\"role\":\"主导者、决策者\",\"subtext\":\"\\\"我不看苦劳，只看功劳。你的 PPT 做得再花哨，不知道怎么卖钱就是废纸。我看你像是在'自嗨'。\\\" —— 意图：商业思维纠偏，施压以求结果。\",\"meVs\":\"考察与被考察。你在透支信任分，需要尽快通过 Action 证明执行力。\"}" +
            "]";
        meeting.setAttendeeBexAnalysis(attendeeBexAnalysis);

        // Mock bexAdvice (注意：keyPoint 拼写正确)
        String bexAdvice = "{" +
            "\"deductions\":[" +
            "{\"keyPoint\":\"商业闭环缺失\",\"keyContent\":\"被李总问住\\\"凭什么涨价 20%\\\"是致命伤。作为产品负责人，不能只看功能，要看商业。建议：下次汇报前，必须准备好竞品定价对比表和真实的客户预访谈录音，不能只拿问卷说事。\"}," +
            "{\"keyPoint\":\"销售赋能脱节\",\"keyContent\":\"产品都没给销售培训，老板会认为你在\\\"造炮弹但不管谁去打仗\\\"。建议：产品规划不仅是 PRD，还包括 Sales Kit（销售工具包）。\"}" +
            "]," +
            "\"credits\":[" +
            "{\"keyPoint\":\"决策果断\",\"keyContent\":\"在李总施压时间点，陈工提出砍功能时，你没有纠结\\\"完美主义\\\"，立刻同意砍掉语音功能。这表现了良好的大局观（Trade-off 能力），这一点是加分的。\"}" +
            "]" +
            "}";
        meeting.setBexAdvice(bexAdvice);

        // Mock
        String todo = "[" +
            "{\"assignee\":\"张三\",\"datetime\":\"本周三前\",\"event\":\"联系 10 家意向客户测试\\\"智能助手\\\"定价接受度，本周三前产出访谈结论\"}," +
            "{\"assignee\":\"陈工\",\"datetime\":\"明天(周二)\",\"event\":\"砍掉语音功能，重新规划 11 月 1 日上线排期， 发邮件给李总\"}" +
            "]";
        meeting.setTodo(todo);

        logger.info("Placeholder meeting enrichment applied, meetingId: {}", meeting.getMeetingId());
    }
}

