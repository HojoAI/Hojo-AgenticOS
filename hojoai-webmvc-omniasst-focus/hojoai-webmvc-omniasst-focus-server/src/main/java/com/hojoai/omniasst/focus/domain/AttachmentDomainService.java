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
package com.hojoai.omniasst.focus.domain;

import com.hojoai.omniasst.focus.dto.val.FileVal;
import com.hojoai.omniasst.focus.repository.po.ScheduleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

import java.util.List;

/**
 * 附件领域服务：管理规则附件（focus_rule_attachment）与日程附件（focus_schedule_attachment）
 *
 * @author My
 * @version 1.0 2026/03/23
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public interface AttachmentDomainService {

    /**
     * 保存规则附件及对应日程附件
     *
     * @param ruleId         规则 ID
     * @param fileList       附件列表
     * @param schedulePOList 已创建的日程实例列表，每条日程都会关联相同的附件
     */
    List<ScheduleAttachmentPO> saveAttachment(Long ruleId, List<FileVal> fileList, List<SchedulePO> schedulePOList);

    /**
     * 保存日程实例附件
     *
     * @param schedule
     * @param fileList
     */
    List<ScheduleAttachmentPO> saveAttachmentBySchedule(SchedulePO schedule, List<FileVal> fileList);

    /**
     * 根据日程 ID 列表查询日程附件
     *
     * @param scheduleIdList 日程 ID 列表
     * @return ScheduleAttachmentPO 列表
     */
    List<ScheduleAttachmentPO> queryByScheduleIdList(List<Long> scheduleIdList);

    /**
     * 根据日程 ID 列表删除日程附件
     *
     * @param scheduleIdList 日程 ID 列表
     */
    void deleteByScheduleIdList(List<Long> scheduleIdList);

    /**
     * 根据规则id删除日程附件
     *
     * @param ruleId    规则id
     */
    void deleteRuleId(Long ruleId);

}
