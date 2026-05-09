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
package com.hojoai.omniasst.focus.dto.response;

import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;

/**
 * The SysModifyScheduleResponseDTO class for SysModifyScheduleResponseDTO.
 *
 * @author My
 * @version 1.0, 2026/03/20
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class SysModifyScheduleResponseDTO {

    private SysScheduleVal updateSchedule;

    private Long lastSyncTime;

    public SysScheduleVal getUpdateSchedule() {
        return updateSchedule;
    }

    public void setUpdateSchedule(SysScheduleVal updateSchedule) {
        this.updateSchedule = updateSchedule;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

}
