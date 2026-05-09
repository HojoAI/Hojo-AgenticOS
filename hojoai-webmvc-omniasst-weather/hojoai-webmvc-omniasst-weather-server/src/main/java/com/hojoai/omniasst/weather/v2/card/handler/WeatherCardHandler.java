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
package com.hojoai.omniasst.weather.v2.card.handler;

import com.hojoai.omniasst.weather.v2.card.CardContext;
import com.hojoai.omniasst.weather.v2.slot.WeatherCardType;

import java.util.Map;
import java.util.Optional;

/**
 * 天气卡片 data 映射处理器（策略接口）
 * 新增卡片类型时只需新增实现类并注册
 *
 * @author wangjian
 * @since 2026-02-02
 */
public interface WeatherCardHandler {

    /**
     * @return 本处理器对应的卡片类型，与注册表 key 一致
     */
    WeatherCardType getCardType();

    /**
     * 将彩云结果 + 上下文映射为卡片 data（可序列化为卡片第二段 text 内的 data）
     *
     * @param context 上下文
     * @return 卡片 data，key 与前端约定一致
     */
    Map<String, Object> buildCardData(CardContext context);

    /**
     * 可选：构建气泡播报文案。返回空则使用默认占位文案。
     *
     * @param context 上下文
     * @return 气泡文案，空表示使用默认
     */
    default Optional<String> buildBubbleText(CardContext context) {
        return Optional.empty();
    }
}
