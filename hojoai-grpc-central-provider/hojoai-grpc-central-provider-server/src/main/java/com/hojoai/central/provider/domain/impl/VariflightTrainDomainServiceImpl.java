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
package com.hojoai.central.provider.domain.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hojoai.central.provider.domain.VariflightTrainDomainService;
import com.hojoai.central.provider.dto.response.train.VariflightTrainTableResponse;
import com.hojoai.central.provider.utils.TokenUtil;
import com.hojoai.common.jackson.JacksonUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.TreeMap;

/**
 * 飞常准火车时刻表 HTTP 调用实现：支持车次+站到站、站到站、城市到城市等查询维度。
 */
@Service
public class VariflightTrainDomainServiceImpl implements VariflightTrainDomainService {

    private static final Logger logger = LoggerFactory.getLogger(VariflightTrainDomainServiceImpl.class);

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private String variflightTrainBaseUrl;
    @Resource
    private String variflightAppId;
    @Resource
    private String variflightAppSecurity;

    /**
     * 按车次号、出发站、到达站与日期查询时刻表。
     */
    @Override
    public VariflightTrainTableResponse getByTrainNumberAndStations(String tnum, String tdep, String tarr, String date) {
        if (tnum == null || tnum.isBlank() || tdep == null || tdep.isBlank() || tarr == null || tarr.isBlank() || date == null || date.isBlank()) {
            throw new IllegalArgumentException("tnum, tdep, tarr, date are required");
        }
        Map<String, String> signParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signParams.put("appid", variflightAppId);
        signParams.put("date", date);
        signParams.put("tarr", tarr);
        signParams.put("tdep", tdep);
        signParams.put("tnum", tnum);
        return get(signParams);
    }

    /**
     * 按出发站、到达站与日期查询时刻表（无车次号）。
     */
    @Override
    public VariflightTrainTableResponse getByStationsAndDate(String tdep, String tarr, String date) {
        if (tdep == null || tdep.isBlank() || tarr == null || tarr.isBlank() || date == null || date.isBlank()) {
            throw new IllegalArgumentException("tdep, tarr, date are required");
        }
        Map<String, String> signParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signParams.put("appid", variflightAppId);
        signParams.put("date", date);
        signParams.put("tarr", tarr);
        signParams.put("tdep", tdep);
        return get(signParams);
    }

    /**
     * 按出发城市、到达城市与日期查询时刻表。
     */
    @Override
    public VariflightTrainTableResponse getByCitiesAndDate(String cdep, String carr, String date) {
        if (cdep == null || cdep.isBlank() || carr == null || carr.isBlank() || date == null || date.isBlank()) {
            throw new IllegalArgumentException("cdep, carr, date are required");
        }
        Map<String, String> signParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signParams.put("appid", variflightAppId);
        signParams.put("carr", carr);
        signParams.put("cdep", cdep);
        signParams.put("date", date);
        return get(signParams);
    }

    // 生成 token、组装查询串并调用火车时刻表接口；失败时根据 error_code 抛出业务异常
    private VariflightTrainTableResponse get(Map<String, String> signParams) {
        String token = TokenUtil.generateToken(signParams, variflightAppSecurity);
        signParams.put("token", token);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        signParams.forEach(params::add);
        String url = UriComponentsBuilder.fromUriString(variflightTrainBaseUrl).queryParams(params).build().toUriString();

        logger.info("VariflightTrainDomainServiceImpl.getTrain.url is -> {}", url);

        String body = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();

        logger.info("VariflightTrainDomainServiceImpl.getTrain.response is -> {}", body);

        if (body == null || body.isBlank()) {
            throw new IllegalStateException("empty response");
        }
        JsonNode node = JacksonUtil.json2Bean(body, JsonNode.class);
        if (node.has("error_code")) {
            int code = node.get("error_code").asInt();
            String msg = node.has("error") ? node.get("error").asText() : "error_code " + code;
            logger.warn("variflight train error: code={}, msg={}", code, msg);
            throw new IllegalStateException(msg);
        }
        return JacksonUtil.json2Bean(body, VariflightTrainTableResponse.class);
    }

}
