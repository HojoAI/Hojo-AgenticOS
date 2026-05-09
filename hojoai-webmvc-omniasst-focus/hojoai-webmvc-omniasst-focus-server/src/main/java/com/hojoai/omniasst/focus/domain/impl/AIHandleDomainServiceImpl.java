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
package com.hojoai.omniasst.focus.domain.impl;

import com.hojoai.omniasst.focus.domain.AIHandleDomainService;
import com.hojoai.omniasst.focus.dto.response.AIScheduleDetailSuggestionResponseDTO;
import com.hojoai.omniasst.focus.dto.response.AISuggestionVal;
import com.hojoai.omniasst.focus.dto.val.AIDetailSugVal;
import com.hojoai.omniasst.focus.repository.ScheduleDetailRepository;
import com.hojoai.omniasst.focus.repository.po.ScheduleDetailPO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2026/01/16
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class AIHandleDomainServiceImpl implements AIHandleDomainService {

    private static final Logger logger = LoggerFactory.getLogger(AIHandleDomainServiceImpl.class);

    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;

    @Override
    public void scheduleSuggestionHandle(SchedulePO schedulePO, AISuggestionVal aiSuggestionVal, AIScheduleDetailSuggestionResponseDTO aiScheduleResponseDTO) {
        AISuggestionVal.updateSchedulesWithAISuggestions(schedulePO, aiSuggestionVal);

        updateScheduleDetailHandle(schedulePO, aiScheduleResponseDTO);
    }

    private void updateScheduleDetailHandle(SchedulePO schedule, AIScheduleDetailSuggestionResponseDTO detailSuggestion) {
        if (detailSuggestion != null) {
            if (detailSuggestion.getAiType() != null) {
                schedule.setAiDetailType(detailSuggestion.getAiType());
            }

            AIDetailSugVal aiDetailSugVal = detailSuggestion.getAiDetailSugVal();
            if (aiDetailSugVal != null) {
                if (aiDetailSugVal.getTitle() != null) {
                    schedule.setAiDetailTitle(aiDetailSugVal.getTitle());
                }

                // 3. 创建 focus_schedule_detail 数据
                if (aiDetailSugVal.getHighlights() != null && !aiDetailSugVal.getHighlights().isEmpty()) {
                    createScheduleDetails(schedule.getId(), aiDetailSugVal.getHighlights());
                }
            }
        }
    }

    /**
     * 创建日程详情记录
     *
     * @param scheduleId 日程ID
     * @param highlights 高亮列表
     */
    private void createScheduleDetails(Long scheduleId, List<String> highlights) {
        try {
            for (String highlight : highlights) {
                if (highlight != null && !highlight.trim().isEmpty()) {
                    ScheduleDetailPO detailPO = new ScheduleDetailPO();
                    detailPO.setScheduleId(scheduleId);
                    detailPO.setHighLight(highlight);

                    scheduleDetailRepository.create(detailPO);
                    logger.info("创建日程详情成功, scheduleId={}, highlight={}", scheduleId, highlight);
                }
            }
        } catch (Exception e) {
            logger.error("创建日程详情失败, scheduleId={}", scheduleId, e);
        }
    }

}
