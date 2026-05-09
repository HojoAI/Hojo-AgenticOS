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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hojoai.central.provider.domain.VariflightDomainService;
import com.hojoai.central.provider.dto.response.flight.VariflightFlightResponse;
import com.hojoai.central.provider.utils.TokenUtil;
import com.hojoai.common.jackson.JacksonUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.TreeMap;

/**
 * 飞常准航班动态 HTTP 调用实现：按 appid + 参数签名请求第三方接口并解析为领域模型。
 */
@Service
public class VariflightDomainServiceImpl implements VariflightDomainService {

    private static final Logger logger = LoggerFactory.getLogger(VariflightDomainServiceImpl.class);

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private String variflightBaseUrl;
    @Resource
    private String variflightAppId;
    @Resource
    private String variflightAppSecurity;

    /**
     * 按航班号与日期查询航班动态；可选语言参数透传给飞常准。
     *
     * @param fnum 航班号，非空
     * @param date 日期（与第三方约定格式一致），非空
     * @param lang 语言，可空
     * @return 解析后的航班信息
     * @throws IllegalArgumentException 缺少必填参数
     * @throws IllegalStateException 空响应、业务错误码或未找到航班
     */
    @Override
    public VariflightFlightResponse getFlightByNumberAndDate(String fnum, String date, String lang) {
        if (fnum == null || fnum.isBlank() || date == null || date.isBlank()) {
            throw new IllegalArgumentException("fnum and date are required");
        }
        Map<String, String> signParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        signParams.put("appid", variflightAppId);
        signParams.put("date", date);
        signParams.put("fnum", fnum);
        if (lang != null && !lang.isBlank()) {
            signParams.put("lang", lang);
        }
        String token = TokenUtil.generateToken(signParams, variflightAppSecurity);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(variflightBaseUrl)
                .queryParam("appid", variflightAppId)
                .queryParam("date", date)
                .queryParam("fnum", fnum)
                .queryParam("token", token);
        if (lang != null && !lang.isBlank()) {
            urlBuilder.queryParam("lang", lang);
        }
        String url = urlBuilder.build().toUriString();

        logger.info("VariflightDomainServiceImpl.getFlightByNumberAndDate.url is -> {}", url);

        String body = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();

        logger.info("VariflightDomainServiceImpl.getFlightByNumberAndDate.response is -> {}", body);

        if (body == null || body.isBlank()) {
            throw new IllegalStateException("empty response");
        }
        JsonNode node = JacksonUtil.json2Bean(body, JsonNode.class);
        if (node.isObject() && node.has("error_code")) {
            int code = node.get("error_code").asInt();
            String msg = node.has("error") ? node.get("error").asText() : "error_code " + code;
            logger.warn("variflight error: code={}, msg={}", code, msg);
            throw new IllegalStateException(msg);
        }
        // 接口成功时返回的是数组 [{...}]，取第一个元素转换
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            if (arrayNode.isEmpty()) {
                throw new IllegalStateException("flight not found");
            }
            return JacksonUtil.json2Bean(arrayNode.get(0).toString(), VariflightFlightResponse.class);
        }
        return JacksonUtil.json2Bean(body, VariflightFlightResponse.class);
    }

}
