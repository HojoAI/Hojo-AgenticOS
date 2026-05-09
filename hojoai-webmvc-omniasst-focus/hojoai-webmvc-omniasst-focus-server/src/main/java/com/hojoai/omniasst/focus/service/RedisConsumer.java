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
package com.hojoai.omniasst.focus.service;

import com.hojoai.central.provider.grpc.PushReqDTO;
import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.focus.common.constants.ScheduleConstants;
import com.hojoai.omniasst.focus.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMapper;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redis Key过期事件监听器
 * 监听Redis中key的过期事件，并执行相应的业务逻辑
 *
 * @author My
 * @version 2.0 2026/01/16
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class RedisConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RedisConsumer.class);

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Resource
    private RedissonClient redissonClient;

    private RQueue<String> scheduleTaskQueue;

    private ExecutorService queueConsumerExecutor;

    private volatile boolean running = true;

    /**
     * 初始化延时队列消费者
     * 启动一个线程来监听延时队列的目标队列
     */
    @PostConstruct
    public void initQueueConsumer() {
        scheduleTaskQueue = redissonClient.getQueue(ScheduleConstants.REDIS_SCHEDULE_TASK_QUEUE);
        queueConsumerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "schedule-task-queue-consumer");
            thread.setDaemon(true);
            return thread;
        });

        // 启动队列消费者
        queueConsumerExecutor.execute(() -> {
            logger.info("延时队列消费者已启动");
            while (running) {
                try {
                    // 从队列中取出消息，阻塞等待最多3秒
                    String scheduleIdStr = scheduleTaskQueue.poll();
                    if (scheduleIdStr != null && !scheduleIdStr.isEmpty()) {
                        logger.info("从延时队列中取出消息 - scheduleId: {}", scheduleIdStr);
                        processScheduleId(scheduleIdStr);
                    }
                } catch (Exception e) {
                    logger.error("处理延时队列消息失败", e);
                    // 继续处理下一条消息
                }
            }
            logger.info("延时队列消费者已停止");
        });
    }

    /**
     * 销毁队列消费者
     */
    @PreDestroy
    public void destroyQueueConsumer() {
        running = false;
        if (queueConsumerExecutor != null) {
            queueConsumerExecutor.shutdown();
            try {
                if (!queueConsumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    queueConsumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                queueConsumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 处理scheduleId（从延时队列或key过期事件）
     *
     * @param scheduleIdStr scheduleId字符串
     */
    private void processScheduleId(String scheduleIdStr) {
        // 解析scheduleId
        Long scheduleId;
        try {
            scheduleId = Long.parseLong(scheduleIdStr);
        } catch (NumberFormatException e) {
            logger.warn("scheduleId格式错误 - scheduleId: {}", scheduleIdStr);
            return;
        }

        // 查询日程信息
        SchedulePO schedulePO = scheduleMapper.selectById(scheduleId);
        if (Objects.isNull(schedulePO)) {
            logger.warn("未找到对应的日程信息 - scheduleId: {}", scheduleId);
            return;
        }

        // 发送推送通知
        sendScheduleReminder(schedulePO);
    }

    /**
     * 发送日程提醒推送
     *
     * @param schedulePO 日程信息
     */
    private void sendScheduleReminder(SchedulePO schedulePO) {
        try {
            logger.info("开始发送日程提醒推送 - scheduleId: {}, userId: {}, title: {}",
                    schedulePO.getId(), schedulePO.getUserId(), schedulePO.getTitle());

            // 构建推送请求
            PushRequestDTO pushRequest = buildPushRequest(schedulePO);

            // 构建gRPC请求
            PushReqDTO pushReqDTO = PushReqDTO.newBuilder()
                    .setPushReqStr(JacksonUtil.bean2Json(pushRequest))
                    .build();

            // 发送推送
            iConfigurationForGrpc.pushClient().push(pushReqDTO);

            logger.info("日程提醒推送发送成功 - scheduleId: {}, userId: {}",
                    schedulePO.getId(), schedulePO.getUserId());

        } catch (Exception e) {
            logger.error("发送日程提醒推送失败 - scheduleId: {}, userId: {}",
                    schedulePO.getId(), schedulePO.getUserId(), e);
            // 不抛出异常，避免影响其他key的处理
        }
    }

    /**
     * 构建推送请求DTO
     *
     * @param schedulePO 日程信息
     * @return 推送请求DTO
     */
    private PushRequestDTO buildPushRequest(SchedulePO schedulePO) {
        PushRequestDTO requestDTO = new PushRequestDTO();

        requestDTO.setUserIdList(Collections.singletonList(schedulePO.getUserId()));
        requestDTO.setTitle("日程提醒");
        requestDTO.setContent(schedulePO.getTitle());
        requestDTO.setType(AgentTypeEnum.FOCUS);
        requestDTO.setAction(AgentActionEnum.WEB_NAVIGATION);
        requestDTO.setBroadcast(false);
        requestDTO.setRoute("/?active=focus");

        return requestDTO;
    }

}
