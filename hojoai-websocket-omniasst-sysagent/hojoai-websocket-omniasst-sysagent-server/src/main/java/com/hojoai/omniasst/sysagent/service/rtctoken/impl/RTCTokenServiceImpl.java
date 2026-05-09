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
package com.hojoai.omniasst.sysagent.service.rtctoken.impl;

import com.ding.rtc.DingRtcEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.omniasst.sysagent.configure.IConfiguration;
import com.hojoai.omniasst.sysagent.service.rtctoken.RTCTokenService;
import com.hojoai.omniasst.sysagent.service.rtctoken.bean.BuildRTCTokenMessage;
import com.hojoai.omniasst.sysagent.service.rtctoken.bean.BuildRTCTokenResultMessage;
import com.hojoai.omniasst.sysagent.thirdparty.rtcclient.IAudioFrameObserver;
import com.hojoai.omniasst.sysagent.thirdparty.rtcclient.IDingRtcEngineListener;
import com.hojoai.omniasst.sysagent.thirdparty.rtcclient.IRTCEngineManager;
import com.hojoai.omniasst.sysagent.thirdparty.rtctoken.AppToken;
import com.hojoai.omniasst.sysagent.thirdparty.rtctoken.RTCService;
import com.hojoai.omniasst.sysagent.interaction.websocket.IWebSocketHandler;
import com.hojoai.omniasst.sysagent.interaction.websocket.bean.IMessage;
import com.hojoai.omniasst.sysagent.interaction.websocket.bean.IMessageEvent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

/**
 * The service implements class for rtc token.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class RTCTokenServiceImpl implements RTCTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RTCTokenServiceImpl.class);

    /** 频道/用户 ID 拼接分隔符。 */
    private static final String NAME_SEPARATOR = "-";
    /** 频道 ID 前缀。 */
    private static final String CHANNEL_PREFIX = "Channel";
    /** 客户端用户 ID 前缀。 */
    private static final String CLIENT_PREFIX = "Client";
    /** 服务端用户 ID 前缀。 */
    private static final String SERVER_PREFIX = "Server";

    @Resource
    private IConfiguration iConfiguration;

    @Resource
    private IDingRtcEngineListener iDingRtcEngineListener;

    @Resource
    private IAudioFrameObserver iAudioFrameObserver;

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildRTCToken(WebSocketSession session, String message) {
        IMessage<BuildRTCTokenMessage> iMessage = JacksonUtil.json2Bean(message, new TypeReference<>() {
        });
        String deviceId = iMessage.getEventContent().getDeviceId();
        Asserts.notBlank(deviceId, "deviceId");

        String appId = iConfiguration.getRtcAppId();
        String appKey = iConfiguration.getRtcAppKey();
        String channelId = CHANNEL_PREFIX + NAME_SEPARATOR + deviceId;
        String clientUserId = CLIENT_PREFIX + NAME_SEPARATOR + deviceId;
        String serverUserId = SERVER_PREFIX + NAME_SEPARATOR + deviceId;
        int expiredTimestamp = (int) (System.currentTimeMillis() / 1000) + iConfiguration.getRtcTokenExpiredTimestamp();

        String rtcTokenForServer = executeBuildTCToken(appId, appKey, expiredTimestamp, channelId, serverUserId);
        String rtcTokenForClient = executeBuildTCToken(appId, appKey, expiredTimestamp, channelId, clientUserId);

        processRTCTokenForServer(session, deviceId, appId, channelId, serverUserId, rtcTokenForServer);
        processRTCTokenForClient(session, deviceId, appId, channelId, clientUserId, rtcTokenForClient);
    }

    private String executeBuildTCToken(String appId, String appKey, int expiredTimestamp, String channelId, String userId) {
        AppToken appToken = new AppToken(appId, appKey, expiredTimestamp);
        RTCService rtcService = new RTCService(channelId, userId);

        appToken.addService(rtcService);
        String rtcToken;
        try {
            rtcToken = appToken.buildTokenString();
        } catch (Throwable e) {
            throw new UnsupportedOperationException("build rtc token failed", e);
        }
        if (rtcToken == null || rtcToken.isEmpty()) {
            throw new UnsupportedOperationException("build rtc token failed, rtc token empty");
        }

        logger.info("build rtc token success, appId: {}, channelId: {}, userId: {}, token: {}", appId, channelId, userId, rtcToken);
        return rtcToken;
    }

    private void processRTCTokenForServer(WebSocketSession session, String deviceId, String appId, String channelId, String serverUserId, String rtcTokenForServer) {
        // 1.创建引擎
        DingRtcEngine engine = DingRtcEngine.createInstance(iDingRtcEngineListener, channelId);
        iDingRtcEngineListener.setEngine(engine);

        // 2.注册回调
        iAudioFrameObserver.setEngine(engine);
        engine.registerAudioFrameObserver(iAudioFrameObserver);
        DingRtcEngine.DingRtcAudioFrameObserverConfig observerConfig = new DingRtcEngine.DingRtcAudioFrameObserverConfig();
        observerConfig.sampleRate = DingRtcEngine.DingRtcAudioSampleRate.DingRtcAudioSampleRate_16000;
        observerConfig.channels = DingRtcEngine.DingRtcAudioNumChannelType.DingRtcMonoAudio;
        observerConfig.permission = DingRtcEngine.DingRtcAudioFramePermission.DingRtcAudioFramePermissionReadAndWrite;
        engine.enableAudioFrameObserver(true, DingRtcEngine.DingRtcAudioObservePosition.RtcEngineAudioPositionRemoteUser, observerConfig);

        // 3.订阅远端
        engine.subscribeAllRemoteAudioStreams(true);
        engine.subscribeAllRemoteVideoStreams(false);

        // 4.加入频道
        DingRtcEngine.DingRtcAuthInfo dingRtcAuthInfo = new DingRtcEngine.DingRtcAuthInfo();
        dingRtcAuthInfo.appId = appId;
        dingRtcAuthInfo.token = rtcTokenForServer;
        dingRtcAuthInfo.channelId = channelId;
        dingRtcAuthInfo.userId = serverUserId;
        engine.joinChannel(dingRtcAuthInfo, serverUserId);

        // 5.开启推流
        engine.setExternalAudioSource(true, DingRtcEngine.DingRtcAudioSampleRate.DingRtcAudioSampleRate_16000.getValue(), DingRtcEngine.DingRtcAudioNumChannelType.DingRtcMonoAudio.getValue());
        engine.setExternalVideoSource(false, DingRtcEngine.DingRtcVideoTrack.DingRtcVideoTrackCamera);

        // 6.缓存引擎
        IRTCEngineManager.putEngine(channelId, engine);
    }

    private void processRTCTokenForClient(WebSocketSession session, String deviceId, String appId, String channelId, String clientUserId, String rtcTokenForClient) {
        BuildRTCTokenResultMessage buildRTCTokenResultMessage = new BuildRTCTokenResultMessage();
        buildRTCTokenResultMessage.setDeviceId(deviceId);
        buildRTCTokenResultMessage.setAppId(appId);
        buildRTCTokenResultMessage.setChannelId(channelId);
        buildRTCTokenResultMessage.setUserId(clientUserId);
        buildRTCTokenResultMessage.setRtcToken(rtcTokenForClient);

        IMessage.newInstance(IMessageEvent.BUILD_RTC_TOKEN_RESULT, buildRTCTokenResultMessage);
        IWebSocketHandler.sendText(session, JacksonUtil.bean2Json(buildRTCTokenResultMessage));
    }
}
