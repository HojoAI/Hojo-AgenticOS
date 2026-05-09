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
package com.hojoai.omniasst.focus.dto.request;

import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;

import java.util.List;

/**
 * 系统日程同步请求 DTO
 *
 * @author My
 * @version 1.0
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class SysScheduleRequestDTO {

    /** 同步日程数据 */
    private List<SysScheduleVal> addScheduleList;

    /** 同步日程数据 */
    private List<SysScheduleVal> updateScheduleList;

    /** 删除日程数据id，type == 3 时必填 */
    private List<String> delScheduleIdList;

    /** 上次同步时间 */
    private Long lastSyncTime;

    public SysScheduleRequestDTO() {
    }

    public List<SysScheduleVal> getAddScheduleList() {
        return addScheduleList;
    }

    public void setAddScheduleList(List<SysScheduleVal> addScheduleList) {
        this.addScheduleList = addScheduleList;
    }

    public List<SysScheduleVal> getUpdateScheduleList() {
        return updateScheduleList;
    }

    public void setUpdateScheduleList(List<SysScheduleVal> updateScheduleList) {
        this.updateScheduleList = updateScheduleList;
    }

    public List<String> getDelScheduleIdList() {
        return delScheduleIdList;
    }

    public void setDelScheduleIdList(List<String> delScheduleIdList) {
        this.delScheduleIdList = delScheduleIdList;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

}
