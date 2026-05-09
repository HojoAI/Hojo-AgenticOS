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
package com.hojoai.omniasst.weather.v2.service.caiyun;

import com.alibaba.fastjson2.JSON;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 彩云天气 v2.6 综合接口封装 Service
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Service
public class CaiyunWeatherService {

    private static final Logger logger = LoggerFactory.getLogger(CaiyunWeatherService.class);

    private final CaiyunWeatherApiClient apiClient;

    public CaiyunWeatherService(CaiyunWeatherApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 获取综合天气（仅实时 + 逐日），使用配置的默认 dailysteps
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 综合响应，含 result.realtime / result.daily
     * @throws CaiyunWeatherApiException  HTTP 非 200、status 非 ok 或解析失败时，由 {@link CaiyunWeatherApiClient} 抛出
     * @throws IllegalStateException      未配置 token 时
     */
    public CaiyunWeatherResponse getWeather(double longitude, double latitude) {
        return getWeather(longitude, latitude, null);
    }

    /**
     * 获取综合天气（仅实时 + 逐日），可指定逐日天数
     *
     * @param longitude  经度
     * @param latitude  纬度
     * @param dailysteps 逐日天数 [1,15]，null 用配置默认
     * @return 综合响应
     * @throws CaiyunWeatherApiException HTTP 非 200、status 非 ok 或解析失败时
     * @throws IllegalStateException     未配置 token 时
     */
    public CaiyunWeatherResponse getWeather(double longitude, double latitude, Integer dailysteps) {
        logger.info("请求彩云综合天气开始(仅实时+逐日): lon={}, lat={}, dailysteps={}",
                longitude, latitude, dailysteps);
        CaiyunWeatherResponse weather = apiClient.getWeather(longitude, latitude, dailysteps);
        logger.info("请求彩云综合天气结束: {}", JSON.toJSONString(weather));
        return weather;
    }
}
