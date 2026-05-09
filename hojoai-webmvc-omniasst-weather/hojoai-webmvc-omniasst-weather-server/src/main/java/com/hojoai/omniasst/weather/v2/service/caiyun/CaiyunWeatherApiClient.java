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
import com.hojoai.omniasst.weather.v2.config.CaiyunWeatherProperties;
import com.hojoai.omniasst.weather.v2.entity.CaiyunWeatherResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 彩云天气 v2.6 综合接口 HTTP 客户端
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class CaiyunWeatherApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CaiyunWeatherApiClient.class);

    private final CloseableHttpClient httpClient;
    private final CaiyunWeatherProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * @param httpClientManager 共享 HTTP 客户端
     * @param properties        token、baseUrl、dailysteps 等
     * @param objectMapper      反序列化响应
     */
    public CaiyunWeatherApiClient(com.hojoai.omniasst.weather.service.dify.HttpClientManager httpClientManager,
                                  CaiyunWeatherProperties properties,
                                  ObjectMapper objectMapper) {
        this.httpClient = httpClientManager.getHttpClient();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用综合接口，仅请求实时 + 逐日，不请求逐时数据。
     * 不传 hourlysteps，彩云 API 不会返回逐时预报；传 hourlysteps=0 会 422，故不传该参数。
     * GET /v2.6/{token}/{lon},{lat}/weather?dailysteps=&amp;alert=false&amp;lang=&amp;unit=
     *
     * @param longitude  经度
     * @param latitude   纬度
     * @param dailysteps 逐日天数 [1,15]，null 用配置默认
     * @return 解析后的响应（仅使用 result.realtime、result.daily），失败或 status!=ok 抛异常
     * @throws IllegalStateException     {@code caiyun.weather.token} 未配置
     * @throws CaiyunWeatherApiException HTTP 非 200、响应 status 非 ok 或其它调用失败
     */
    public CaiyunWeatherResponse getWeather(double longitude, double latitude, Integer dailysteps) {
        String token = properties.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("caiyun.weather.token 未配置");
        }

        // 仅实时+逐日：dailysteps [1,15]，不传 hourlysteps 以不请求逐时
        int ds = dailysteps != null ? dailysteps : properties.getDailysteps();
        if (ds < 1) ds = 1;
        if (ds > 15) ds = 15;

        String path = String.format("/%s/%s/%.4f,%.4f/weather",
                properties.getApiVersion(), token, longitude, latitude);

        try {
            URI uri = new URIBuilder(properties.getBaseUrl() + path)
                    .setCharset(StandardCharsets.UTF_8)
                    .addParameter("dailysteps", String.valueOf(ds))
                    .addParameter("alert", "false")
                    .addParameter("lang", properties.getLang())
                    .addParameter("unit", properties.getUnit())
                    .build();

            HttpGet get = new HttpGet(uri);
            return httpClient.execute(get, response -> {
                int code = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (code != 200) {
                    logger.warn("彩云天气 API 非 200: code={}, body={}", code, body);
                    throw new CaiyunWeatherApiException("HTTP " + code, body);
                }
                CaiyunWeatherResponse parsed = objectMapper.readValue(body, CaiyunWeatherResponse.class);
                if (parsed == null || !"ok".equalsIgnoreCase(parsed.getStatus())) {
                    logger.warn("彩云天气 API status 非 ok: {}", body);
                    throw new CaiyunWeatherApiException("status not ok", body);
                }
                return parsed;
            });
        } catch (CaiyunWeatherApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("调用彩云天气综合接口异常: lon={}, lat={}", longitude, latitude, e);
            throw new CaiyunWeatherApiException(e.getMessage(), null);
        }
    }
}
