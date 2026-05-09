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
package com.hojoai.central.provider.utils;

/**
 * The SnowflakeIdGenerator class for id.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class SnowflakeIdGenerator {

    // 起始时间戳（2023-01-01 00:00:00）
    private final static long START_STAMP = 1672531200000L;

    // 各部分位数
    private final static long SEQUENCE_BIT = 12;   // 序列号
    private final static long MACHINE_BIT = 5;      // 机器标识
    private final static long DATACENTER_BIT = 5;   // 数据中心

    // 最大值计算
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    // 移位偏移量
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    // 成员变量
    private long datacenterId;  // 数据中心
    private long machineId;     // 机器标识
    private long sequence = 0L; // 序列号
    private long lastStamp = -1L; // 上次时间戳

    // 构造函数初始化节点ID
    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("数据中心ID范围超限");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("机器ID范围超限");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long currStamp = getCurrentTimeMillis();

        if (currStamp < lastStamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (currStamp == lastStamp) {
            // 同一毫秒内序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出处理
            if (sequence == 0) {
                currStamp = getNextMill();
            }
        } else {
            // 新毫秒重置序列号
            sequence = 0L;
        }

        lastStamp = currStamp;

        // 组合各部分生成最终ID
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    // 阻塞到下一毫秒
    private long getNextMill() {
        long mill = getCurrentTimeMillis();
        while (mill <= lastStamp) {
            mill = getCurrentTimeMillis();
        }
        return mill;
    }

    private long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}