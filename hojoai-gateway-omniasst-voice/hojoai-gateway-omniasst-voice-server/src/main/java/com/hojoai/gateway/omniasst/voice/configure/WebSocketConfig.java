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
package com.hojoai.gateway.omniasst.voice.configure;

import com.hojoai.gateway.omniasst.voice.websocket.*;
import com.hojoai.gateway.omniasst.voice.websocket.v1.AsrWebSocketHandler;
import com.hojoai.gateway.omniasst.voice.websocket.v1.HeadphoneAsrWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 端点注册与容器参数配置。
 * 注册 TTS、ASR 翻译、ASR v1 等 Handler，并统一握手拦截与缓冲区、超时限制。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private TtsWebSocketHandler ttsWebSocketHandler;
    @Resource
    private AsrTranslateWebSocketHandler asrTranslateWebSocketHandler;
    @Resource
    private AsrWebSocketHandler asrWebSocketHandler;
    @Resource
    private HeadphoneAsrWebSocketHandler headphoneAsrWebSocketHandler;
    @Resource
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    /**
     * 注册各业务 WebSocket 路径及握手拦截器。
     *
     * @param registry Spring WebSocket 注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ttsWebSocketHandler, "/common/tts").addInterceptors(handshakeInterceptor).setAllowedOrigins("*");
        registry.addHandler(asrTranslateWebSocketHandler, "/asr/translate").addInterceptors(handshakeInterceptor).setAllowedOrigins("*");
        registry.addHandler(asrWebSocketHandler, "/asr/v1").addInterceptors(handshakeInterceptor).setAllowedOrigins("*");
        registry.addHandler(headphoneAsrWebSocketHandler, "/asr/v1/headphone").addInterceptors(handshakeInterceptor).setAllowedOrigins("*");
    }

    /**
     * 调整 Tomcat WebSocket 容器：消息缓冲、会话空闲与异步发送超时。
     *
     * @return 容器工厂 Bean
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 100);
        container.setMaxBinaryMessageBufferSize(1024 * 100);
        container.setMaxSessionIdleTimeout(30L * 60 * 1000);
        container.setAsyncSendTimeout(2L * 60 * 1000);
        return container;
    }
}

