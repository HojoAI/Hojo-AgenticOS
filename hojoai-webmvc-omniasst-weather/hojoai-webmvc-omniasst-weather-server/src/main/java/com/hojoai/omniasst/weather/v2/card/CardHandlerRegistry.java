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
package com.hojoai.omniasst.weather.v2.card;

import com.hojoai.omniasst.weather.v2.card.handler.WeatherCardHandler;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 卡片类型 → Handler 注册表
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class CardHandlerRegistry {

    private final List<WeatherCardHandler> handlers;
    private final Map<WeatherCardType, WeatherCardHandler> map = new ConcurrentHashMap<>();

    /**
     * @param handlers Spring 注入的全部 {@link WeatherCardHandler} 实现，在 {@link #init()} 中按 {@link WeatherCardHandler#getCardType()} 注册
     */
    public CardHandlerRegistry(List<WeatherCardHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * 启动时建立 {@link WeatherCardType} → Handler 映射。
     */
    @PostConstruct
    public void init() {
        for (WeatherCardHandler h : handlers) {
            map.put(h.getCardType(), h);
        }
    }

    /**
     * @param cardType 槽位解析得到的卡片类型
     * @return 对应处理器；未注册时返回 null，调用方需兜底（如默认 Overview）
     */
    public WeatherCardHandler getHandler(WeatherCardType cardType) {
        return map.get(cardType);
    }
}
