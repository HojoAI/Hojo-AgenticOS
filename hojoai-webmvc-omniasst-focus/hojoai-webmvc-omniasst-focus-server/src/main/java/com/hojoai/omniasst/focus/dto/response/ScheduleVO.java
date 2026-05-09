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

/**
 * 日程值对象（API 文档 5.3.3 ScheduleVO）
 * 1-航班 2-火车 3-酒店 4-门票 5-会议 6-其他
 *
 * @author My
 * @version 1.0 2026/03/10
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
public class ScheduleVO {

    /** 日程ID */
    private String id;
    /** 日程日期 yyyy-MM-dd */
    private String date;
    /** 日程类型：1-航班 2-火车 3-酒店 4-门票 5-会议 6-其他 */
    private Integer type;
    /** 用户计划（标题） */
    private String userPlan;
    /** 日程ID */
    private String scheduleId;
    /** 日程状态 */
    private Integer status;
    /** 备注信息 */
    private String remark;
    /** 业务编码 */
    private String code;
    /** 创建日期 */
    private String createDate;
    /** 更新日期 */
    private String updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUserPlan() {
        return userPlan;
    }

    public void setUserPlan(String userPlan) {
        this.userPlan = userPlan;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
