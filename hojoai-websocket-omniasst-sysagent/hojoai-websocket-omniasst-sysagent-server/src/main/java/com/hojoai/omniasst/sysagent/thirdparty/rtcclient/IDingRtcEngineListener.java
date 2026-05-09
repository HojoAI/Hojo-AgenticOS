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
package com.hojoai.omniasst.sysagent.thirdparty.rtcclient;

import com.ding.rtc.DingRtcEngine;
import com.ding.rtc.DingRtcEngineListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The ding rtc engine listener.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class IDingRtcEngineListener extends DingRtcEngineListener {

    private static final Logger logger = LoggerFactory.getLogger(IDingRtcEngineListener.class);

    private DingRtcEngine engine;

    @Override
    public void onJoinChannelResult(int result, String channel, String userId, int elapsed) {
        logger.info("on join channel result, result: {}, channel: {}, userId: {}, elapsed: {}", result, channel, userId, elapsed);
    }

    @Override
    public void onLeaveChannelResult(int result) {
        logger.info("on leave channel result, result: {}", result);
    }

    @Override
    public void onRemoteUserOnLineNotify(String uid, int elapsed) {
        logger.info("on remote user on line notify, uid: {}, elapsed: {}", uid, elapsed);
    }

    @Override
    public void onRemoteUserOffLineNotify(String uid, DingRtcEngine.DingRtcUserOfflineReason reason) {
        logger.info("on remote user off line notify, uid: {}, reason: {}", uid, reason.getValue());

        engine.leaveChannel();
        engine.destroy();
    }

    public void setEngine(DingRtcEngine engine) {
        this.engine = engine;
    }
}
