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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.weather.v2.config.CaiyunLifeIndexV3Properties;
import com.hojoai.omniasst.weather.v2.config.CaiyunWeatherProperties;
import com.hojoai.omniasst.weather.v2.entity.DailyLifeIndexV3Dto;
import com.hojoai.omniasst.weather.v2.entity.LifeIndexV3Response;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * 彩云天气 v3 生活指数 API 客户端
 * 文档：https://docs.caiyunapp.com/weather-api/v3/meteorology/lifeindex.html
 *
 * @author wangjian
 * @since 2026-02-03
 */
@Component
public class CaiyunLifeIndexV3ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CaiyunLifeIndexV3ApiClient.class);

    private static final int DAYS = 15;
    private static final String FIELDS = "6,10";

    private final CloseableHttpClient httpClient;
    private final CaiyunLifeIndexV3Properties lifeIndexProperties;
    private final CaiyunWeatherProperties weatherProperties;
    private final ObjectMapper objectMapper;

    public CaiyunLifeIndexV3ApiClient(com.hojoai.omniasst.weather.service.dify.HttpClientManager httpClientManager,
                                      CaiyunLifeIndexV3Properties lifeIndexProperties,
                                      CaiyunWeatherProperties weatherProperties,
                                      ObjectMapper objectMapper) {
        this.httpClient = httpClientManager.getHttpClient();
        this.lifeIndexProperties = lifeIndexProperties;
        this.weatherProperties = weatherProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用 v3 生活指数接口，固定 days=15、fields=6,10（洗车、穿衣）。
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 按日的生活指数列表，失败返回空列表（不抛异常，便于主流程不受影响）
     */
    public List<DailyLifeIndexV3Dto> getLifeIndex(double longitude, double latitude) {
        String token = lifeIndexProperties.getToken();
        if (token == null || token.isBlank()) {
            token = weatherProperties.getToken();
        }
        if (token == null || token.isBlank()) {
            logger.warn("v3 生活指数: token 未配置，跳过调用");
            return Collections.emptyList();
        }

        try {
            URI uri = new URIBuilder(lifeIndexProperties.getBaseUrl() + "/v3/lifeindex")
                    .setCharset(StandardCharsets.UTF_8)
                    .addParameter("token", token)
                    .addParameter("longitude", String.valueOf(longitude))
                    .addParameter("latitude", String.valueOf(latitude))
                    .addParameter("days", String.valueOf(DAYS))
                    .addParameter("fields", FIELDS)
                    .build();

            HttpGet get = new HttpGet(uri);
            return httpClient.execute(get, response -> {
                int code = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (code != 200) {
                    logger.warn("v3 生活指数 API 非 200: code={}, body={}", code, body);
                    return Collections.emptyList();
                }
                LifeIndexV3Response parsed = objectMapper.readValue(body, LifeIndexV3Response.class);
                if (parsed == null || parsed.getData() == null) {
                    return Collections.emptyList();
                }
                logger.info("v3 生活指数调用成功: 条数={}", parsed.getData().size());
                return parsed.getData();
            });
        } catch (Exception e) {
            logger.warn("v3 生活指数调用异常: lon={}, lat={}", longitude, latitude, e);
            return Collections.emptyList();
        }
    }
}
