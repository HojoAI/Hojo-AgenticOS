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

import com.hojoai.omniasst.focus.dto.val.MovieVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.po.RuleMoviePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleMoviePO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * 手动创建电影日程时构建 RuleMoviePO、ScheduleMoviePO 的公共逻辑
 *
 * @author My
 * @version 1.0 2026/03/05
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class RuleMovieAssembler {

    private RuleMovieAssembler() {
    }

    /**
     * 根据 MovieVal 与规则 PO 构建 RuleMoviePO（不含影片详情，需调用方设置 picUrl、duration）
     *
     * @param id     主键（由调用方生成，如 Snowflake）
     * @param data   电影数据
     * @param rulePO 规则 PO
     * @return RuleMoviePO
     */
    public static RuleMoviePO buildRuleMoviePO(long id, MovieVal data, ScheduleRulePO rulePO) {
        RuleMoviePO po = new RuleMoviePO();
        po.setId(id);
        po.setRuleId(rulePO.getId());

        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();

        po.setStartTime(data.getStartTime() != null ? dateToLocalDateTime(data.getStartTime()) : defaultStart);
        po.setEndTime(data.getEndTime() != null ? dateToLocalDateTime(data.getEndTime()) : defaultEnd);
        po.setCinemaInfo(nullToEmpty(data.getCinemaInfo()));
        po.setSeatNo(nullToEmpty(data.getSeatNo()));
        return po;
    }

    /**
     * 根据 SysScheduleVal 与规则 PO 构建 RuleMoviePO（系统同步时使用）
     */
    public static RuleMoviePO buildRuleMoviePO(long id, SysScheduleVal val, ScheduleRulePO rulePO) {
        RuleMoviePO po = new RuleMoviePO();
        po.setId(id);
        po.setRuleId(rulePO.getId());
        LocalDateTime defaultStart = rulePO.getRuleStartTime();
        LocalDateTime defaultEnd = rulePO.getRuleEndTime();
        if (val != null && val.getScheduleInfo() != null) {
            po.setStartTime(ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getStartTime()));
            po.setEndTime(ScheduleRuleAssembler.parseScheduleDateTime(val.getScheduleInfo().getEndTime()));
        }
        if (po.getStartTime() == null) {
            po.setStartTime(defaultStart);
        }
        if (po.getEndTime() == null) {
            po.setEndTime(defaultEnd);
        }
        return po;
    }

    /**
     * 根据日程 PO 与规则电影 PO 构建 ScheduleMoviePO
     *
     * @param id         主键（由调用方生成，如 Snowflake）
     * @param schedulePO 日程 PO
     * @param rule       规则电影 PO
     * @return ScheduleMoviePO
     */
    public static ScheduleMoviePO buildScheduleMoviePO(long id, SchedulePO schedulePO, RuleMoviePO rule) {
        ScheduleMoviePO po = new ScheduleMoviePO();
        po.setId(id);
        po.setScheduleId(schedulePO.getId());
        po.setStartTime(schedulePO.getStartTime());
        po.setEndTime(schedulePO.getEndTime());
        po.setMovieName(rule.getMovieName());
        po.setCinemaInfo(rule.getCinemaInfo());
        po.setSeatNo(rule.getSeatNo());
        po.setPicUrl(rule.getPicUrl());
        po.setDuration(rule.getDuration());
        return po;
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return Optional.ofNullable(date)
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);
    }
}
