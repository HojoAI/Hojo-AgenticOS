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
package com.hojoai.omniasst.focus.repository;

import com.hojoai.omniasst.focus.repository.po.ScheduleDetailPO;

import java.util.List;

/**
 * 日程详情Repository接口
 *
 * @author My
 * @version 1.0 2026/01/06
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public interface ScheduleDetailRepository {

    /**
     * 创建日程详情
     *
     * @param scheduleDetailPO 日程详情PO
     * @return 日程详情ID
     */
    Long create(ScheduleDetailPO scheduleDetailPO);

    /**
     * 更新日程详情
     *
     * @param scheduleDetailPO 日程详情PO
     * @return 是否更新成功
     */
    Boolean update(ScheduleDetailPO scheduleDetailPO);

    /**
     * 根据日程ID查询详情列表
     *
     * @param scheduleId 日程ID
     * @return 日程详情列表
     */
    List<ScheduleDetailPO> listByScheduleId(Long scheduleId);

    /**
     * 根据日程ID删除详情（逻辑删除）
     *
     * @param scheduleId 日程ID
     * @return 删除的记录数
     */
    int deleteByScheduleId(Long scheduleId);
}

