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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The rtc engine manager class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/23
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class IRTCEngineManager {

    private static final Logger logger = LoggerFactory.getLogger(IRTCEngineManager.class);

    private static final Map<String, DingRtcEngine> engineMap = new ConcurrentHashMap<>();

    public static void putEngine(String channelId, DingRtcEngine engine) {
        logger.info("execute put rtc engine, channelId: {},", channelId);
        engineMap.put(channelId, engine);
    }

    public static void removeEngine(String channelId) {
        logger.info("execute remove rtc engine, channelId: {}", channelId);
        engineMap.remove(channelId);
    }

    public static DingRtcEngine getEngine(String channelId) {
        logger.info("execute get rtc engine, channelId: {}", channelId);
        return engineMap.get(channelId);
    }
}
