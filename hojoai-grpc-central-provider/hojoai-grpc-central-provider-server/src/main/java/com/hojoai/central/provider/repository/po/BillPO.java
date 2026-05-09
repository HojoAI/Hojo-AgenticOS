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
package com.hojoai.central.provider.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
@TableName("t_pa_bill")
public class BillPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;
    /**
     * 账单编号
     */
    private String billNo;
    /**
     * 订单id
     */
    private Long orderId;
    /**
     * 订单编号
     */
    private String orderNo;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 第三方流水号
     */
    private String serialNo;
    /**
     * 账单创建日期
     */
    private Date billCreateTime;
    /**
     * 账单完成日期
     */
    private Date billCompileTime;
    /**
     * 账单状态（A-待支付；B-支付中；C-支付成功；D-支付失败）
     */
    private String billStatus;
    /**
     * 支付宝签名字符串
     */
    private String orderStr;
    /**
     * 过期时间
     */
    private Long expireTime;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return this.billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSerialNo() {
        return this.serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Date getBillCreateTime() {
        return this.billCreateTime;
    }

    public void setBillCreateTime(Date billCreateTime) {
        this.billCreateTime = billCreateTime;
    }

    public Date getBillCompileTime() {
        return this.billCompileTime;
    }

    public void setBillCompileTime(Date billCompileTime) {
        this.billCompileTime = billCompileTime;
    }

    public String getBillStatus() {
        return this.billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public String getOrderStr() {
        return orderStr;
    }

    public void setOrderStr(String orderStr) {
        this.orderStr = orderStr;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getDeleteFlag() {
        return this.deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}