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
package com.hojoai.omniasst.weather.v2.slot;

/**
 * 意图/槽位解析结果（地点、单日/多日、卡片类型）
 *
 * @author wangjian
 * @since 2026-02-02
 */
public class SlotExtractionResult {

    /** 地点，可为空（则用 inputs 经纬度） */
    private String location;

    /** 单日/多日 */
    private DayScope dayScope;

    /** 卡片类型 */
    private WeatherCardType cardType;

    public SlotExtractionResult() {
    }

    public SlotExtractionResult(String location, DayScope dayScope, WeatherCardType cardType) {
        this.location = location;
        this.dayScope = dayScope;
        this.cardType = cardType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DayScope getDayScope() {
        return dayScope;
    }

    public void setDayScope(DayScope dayScope) {
        this.dayScope = dayScope;
    }

    public WeatherCardType getCardType() {
        return cardType;
    }

    public void setCardType(WeatherCardType cardType) {
        this.cardType = cardType;
    }
}
