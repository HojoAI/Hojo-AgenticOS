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
package com.hojoai.omniasst.focus.service;

import com.hojoai.common.response.Response;
import com.hojoai.omniasst.focus.dto.request.ManualModifyScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.request.SysScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.SysModifyScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.response.SysScheduleResponseDTO;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * The ScheduleRuleService class for ScheduleRuleService.
 *
 * @author My
 * @version 1.0, 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface ScheduleRuleService {

    /**
     * 手动创建日程（根据 type 分发到会议/运动/电影/航班/火车等领域服务）
     *
     * @param userId    用户ID
     * @param requestDTO 手动创建请求
     * @return 日程ID
     */
    Pair<ScheduleRulePO, List<SchedulePO>> manualCreateSchedule(String userId, ManualScheduleRequestDTO requestDTO);

    /**
     * 系统日历同步
     *
     * @param userId        用户id
     * @return PA同步系统日历
     */
    void sysAsyncScheduleHandle(String userId, SysScheduleRequestDTO requestDTO);

    /**
     * 获取系统同步日历更新的数据
     *
     * @param userId
     * @param requestDTO
     * @return
     */
    Response<SysScheduleResponseDTO> sysSyncScheduleHandle(String userId, SysScheduleRequestDTO requestDTO);

    /**
     * 编辑日程
     *
     * @param scheduleId
     * @param modifyType
     * @param requestDTO
     * @return
     */
    Response<SysModifyScheduleResponseDTO> manualModifySchedule(String scheduleId, String modifyType, ManualModifyScheduleRequestDTO requestDTO);

    /**
     * 移除重复日程
     * @param userId
     * @param addScheduleList
     */
    void removeRepeatScheduleList(String userId, List<SysScheduleVal> addScheduleList);

}
