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
package com.hojoai.omniasst.management.redis;

import com.hojoai.omniasst.management.common.RedisConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The Redis multi-channel subscriber using Redisson.
 *
 * <p>It subscribes fixed channels and dispatches incoming messages
 * to concrete handlers by simple if-else logic.</p>
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@Component
public class MultiChannelSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(MultiChannelSubscriber.class);

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TranslateResultHandler translateResultHandler;

    @Resource
    private MeetingResultHandler meetingResultHandler;

    @Resource
    private MeetingSegmentHandler meetingSegmentHandler;

    @PostConstruct
    public void subscribeChannels() {
        logger.info("MultiChannelSubscriber.subscribeChannels called, instance: {}", this.hashCode());
        
        // subscribe translate result channel
        subscribeChannel(RedisConstants.CHANNEL_TRANSLATE_RESULT, translateResultHandler::handle);

        // subscribe meeting result channel (meeting end)
        subscribeChannel(RedisConstants.CHANNEL_MEETING_RESULT, meetingResultHandler::handle);

        // subscribe meeting segment channel (real-time transcription)
        subscribeChannel(RedisConstants.CHANNEL_MEETING_SEGMENT, meetingSegmentHandler::handle);
        
        logger.info("All channels subscribed successfully");
    }

    private void subscribeChannel(String channel, java.util.function.Consumer<String> handler) {
        RTopic topic = redissonClient.getTopic(channel);
        topic.addListener(String.class, (receivedChannel, message) -> {
            handler.accept(message);
        });
        logger.info("Subscribe redis channel success, channel: {}", channel);
    }
}
