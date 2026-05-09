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
package com.hojoai.omniasst.focus.common.assembler;

import com.hojoai.omniasst.focus.dto.val.FileVal;
import com.hojoai.omniasst.focus.repository.po.RuleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.ScheduleAttachmentPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;

/**
 * 规则附件与日程附件的构建逻辑
 *
 * @author My
 * @version 1.0 2026/03/23
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleAttachmentAssembler {

    private RuleAttachmentAssembler() {
    }

    /**
     * 根据 FileVal 构建 RuleAttachmentPO
     *
     * @param id
     * @param ruleId 规则 ID
     * @param file   附件值对象
     * @return RuleAttachmentPO
     */
    public static RuleAttachmentPO buildRuleAttachmentPO(long id, Long ruleId, FileVal file) {
        RuleAttachmentPO po = new RuleAttachmentPO();
        po.setId(id);
        po.setRuleId(ruleId);
        po.setUid(file.getUid());
        po.setName(file.getName());
        po.setStatus(file.getStatus());
        po.setSize(file.getSize());
        po.setType(file.getType());
        po.setUrl(file.getUrl());
        po.setThumbUrl(file.getThumbUrl());
        return po;
    }

    /**
     * 根据已保存的 RuleAttachmentPO 和日程 PO 构建 ScheduleAttachmentPO
     *
     * @param id             附件id
     * @param schedule       日程 PO
     * @param ruleAttachment 规则附件 PO
     * @return ScheduleAttachmentPO
     */
    public static ScheduleAttachmentPO buildScheduleAttachmentPO(long id, SchedulePO schedule, RuleAttachmentPO ruleAttachment) {
        ScheduleAttachmentPO po = new ScheduleAttachmentPO();
        po.setId(id);
        po.setRuleId(ruleAttachment.getRuleId());
        po.setScheduleId(schedule.getId());
        po.setUid(ruleAttachment.getUid());
        po.setName(ruleAttachment.getName());
        po.setStatus(ruleAttachment.getStatus());
        po.setSize(ruleAttachment.getSize());
        po.setType(ruleAttachment.getType());
        po.setUrl(ruleAttachment.getUrl());
        po.setThumbUrl(ruleAttachment.getThumbUrl());
        return po;
    }

    /**
     * 根据已保存的 RuleAttachmentPO 和日程 PO 构建 ScheduleAttachmentPO
     *
     * @param id             附件id
     * @param schedulePO     日程 PO
     * @param file           附件 val
     * @return ScheduleAttachmentPO
     */
    public static ScheduleAttachmentPO buildScheduleAttachmentPO(long id, SchedulePO schedulePO, FileVal file) {
        ScheduleAttachmentPO po = new ScheduleAttachmentPO();
        po.setId(id);
        po.setRuleId(schedulePO.getRuleId());
        po.setScheduleId(schedulePO.getId());
        po.setUid(file.getUid());
        po.setName(file.getName());
        po.setStatus(file.getStatus());
        po.setSize(file.getSize());
        po.setType(file.getType());
        po.setUrl(file.getUrl());
        po.setThumbUrl(file.getThumbUrl());
        return po;
    }
}
