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
package com.hojoai.omniasst.sysagent.configure;

import com.hojoai.omniasst.sysagent.interaction.websocket.IHandshakeInterceptor;
import com.hojoai.omniasst.sysagent.interaction.websocket.ITextWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * The web socket configurer class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Configuration
@EnableWebSocket
public class IWebSocketConfigurer implements WebSocketConfigurer {

    @Resource
    private ITextWebSocketHandler iTextWebSocketHandler;

    @Resource
    private IHandshakeInterceptor iHandshakeInterceptor;

    /**
     * 注册文本 WebSocket 端点 {@code /omniasst-sysagent}，并挂载握手拦截器、允许跨域。
     *
     * @param registry Spring WebSocket 注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(iTextWebSocketHandler, "/omniasst-sysagent").addInterceptors(iHandshakeInterceptor).setAllowedOrigins("*");
    }
}
