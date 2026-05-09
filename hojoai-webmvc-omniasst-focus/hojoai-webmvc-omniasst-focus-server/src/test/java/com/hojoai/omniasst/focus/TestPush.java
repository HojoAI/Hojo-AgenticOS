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
package com.hojoai.omniasst.focus;

import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.common.enums.TagEnum;
import com.hojoai.omniasst.focus.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.focus.domain.ScheduleTaskDomainService;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMapper;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;

/**
 * 完整的会议流程集成测试
 * 测试从段消息到结果消息的完整流程
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/25
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@SpringBootTest(classes = ISpringApplication.class)
public class TestPush {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Resource
    private ScheduleTaskDomainService scheduleTaskDomainService;

    @Test
    public void testPush() {

        SchedulePO schedulePO = scheduleMapper.selectById(1334430396192002048L);

        PushRequestDTO pushRequest = buildPushRequest(schedulePO);

        // 构建gRPC请求
        PushReqDTO pushReqDTO = PushReqDTO.newBuilder()
                .setPushReqStr(JacksonUtil.bean2Json(pushRequest))
                .build();

        // 发送推送
        iConfigurationForGrpc.pushClient().push(pushReqDTO);

    }

    @Test
    public void testFocus() {
        scheduleTaskDomainService.taskHandle("", LocalDate.now(), TagEnum.FOCUS);
    }

    private PushRequestDTO buildPushRequest(SchedulePO schedulePO) {
        PushRequestDTO requestDTO = new PushRequestDTO();

        requestDTO.setUserIdList(Collections.singletonList(schedulePO.getUserId()));
        requestDTO.setTitle("日程提醒");
        requestDTO.setContent(schedulePO.getTitle());
        requestDTO.setType(AgentTypeEnum.FOCUS);
        requestDTO.setAction(AgentActionEnum.WEB_NAVIGATION);
        requestDTO.setBroadcast(false);
        requestDTO.setRoute("/?active=focus");

        return requestDTO;
    }

}

