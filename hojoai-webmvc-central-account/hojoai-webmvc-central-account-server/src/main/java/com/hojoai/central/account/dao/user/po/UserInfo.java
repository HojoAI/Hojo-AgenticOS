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
package com.hojoai.central.account.dao.user.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户基础信息，映射表 user_info。
 */
@TableName("user_info")
public class UserInfo implements Serializable {

    /** 自增主键 */
    @TableField("incr_id")
    private Long incrId;

    /** 业务用户 ID */
    @TableField("user_id")
    private String userId;

    /** 手机号 */
    @TableField("user_mobile")
    private String userMobile;

    /** 昵称 */
    @TableField("user_nickname")
    private String userNickname;

    /** 个人简介 */
    @TableField("user_description")
    private String userDescription;

    /** 头像 URL */
    @TableField("user_head_image")
    private String userHeadImage;

    /** 禁用状态，见 {@link com.hojoai.central.account.common.DisabledStatus} */
    @TableField("disabled_status")
    private Integer disabledStatus;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    public Long getIncrId() {
        return incrId;
    }

    public void setIncrId(Long incrId) {
        this.incrId = incrId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserHeadImage() {
        return userHeadImage;
    }

    public void setUserHeadImage(String userHeadImage) {
        this.userHeadImage = userHeadImage;
    }

    public Integer getDisabledStatus() {
        return disabledStatus;
    }

    public void setDisabledStatus(Integer disabledStatus) {
        this.disabledStatus = disabledStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
