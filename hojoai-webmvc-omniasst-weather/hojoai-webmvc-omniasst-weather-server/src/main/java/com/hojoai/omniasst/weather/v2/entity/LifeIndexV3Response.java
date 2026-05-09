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
package com.hojoai.omniasst.weather.v2.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 彩云 v3 生活指数 API 响应根对象
 *
 * @author wangjian
 * @since 2026-02-03
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LifeIndexV3Response {

    private List<DailyLifeIndexV3Dto> data;

    public List<DailyLifeIndexV3Dto> getData() {
        return data;
    }

    public void setData(List<DailyLifeIndexV3Dto> data) {
        this.data = data;
    }
}
