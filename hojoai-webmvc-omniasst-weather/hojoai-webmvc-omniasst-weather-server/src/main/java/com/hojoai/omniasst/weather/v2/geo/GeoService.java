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
package com.hojoai.omniasst.weather.v2.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.omniasst.weather.v2.config.AmapGeoProperties;
import com.hojoai.omniasst.weather.v2.slot.SlotExtractionResult;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 经纬度与地名解析：
 * <ul>
 *   <li>提槽有地址：高德地理编码（地址→经纬度+地名），未配置或失败时回退占位坐标；</li>
 *   <li>提槽无地址但有经纬度（来自 inputs）：高德逆地理编码（经纬度→城市/地址），保证 GeoResult 三字段均有值。</li>
 * </ul>
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Service
public class GeoService {

    private static final Logger logger = LoggerFactory.getLogger(GeoService.class);

    /** 高德地理编码路径：地址 → 经纬度 */
    private static final String AMAP_GEO_PATH = "/v3/geocode/geo";
    /** 高德逆地理编码路径：经纬度 → 地址/行政区 */
    private static final String AMAP_REGEO_PATH = "/v3/geocode/regeo";

    private final CloseableHttpClient httpClient;
    private final AmapGeoProperties amapGeoProperties;
    private final ObjectMapper objectMapper;

    /**
     * @param httpClientManager 共享 HTTP 客户端（连接池）
     * @param amapGeoProperties 高德 Web 服务 key、baseUrl 等
     * @param objectMapper      JSON 解析
     */
    public GeoService(com.hojoai.omniasst.weather.service.dify.HttpClientManager httpClientManager,
                      AmapGeoProperties amapGeoProperties,
                      ObjectMapper objectMapper) {
        this.httpClient = httpClientManager.getHttpClient();
        this.amapGeoProperties = amapGeoProperties;
        this.objectMapper = objectMapper;
    }

    private static final String KEY_LOCATION = "location";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_LNG = "lng";

    /**
     * 根据槽位与 inputs 解析经纬度
     *
     * @param slot   槽位（location 为空则用 inputs）
     * @param inputs 请求中的 inputs，可含 latitude/longitude 或 lat/lon/lng
     * @return 经纬度及可选地名
     * @throws IllegalArgumentException 槽位无地点且 inputs 无法解析出经纬度时
     */
    public GeoResult resolve(SlotExtractionResult slot, Map<String, Object> inputs) {
        String location = slot != null ? slot.getLocation() : null;
        if (location == null || location.isBlank()) {
            return fromInputs(inputs);
        }
        // 地点非空：优先高德地理编码，未配置或失败时回退占位坐标
        return geocode(location);
    }

    /**
     * 从 inputs 解析经纬度；若仅有经纬度无地名，则调用高德逆地理编码补全 locationName，保证 GeoResult 三字段均有值。
     */
    private GeoResult fromInputs(Map<String, Object> inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("未提供地点且 inputs 为空，无法解析经纬度");
        }
        // 优先：location 为 "经度,纬度" 拼接字符串
        GeoResult fromLocationStr = parseLocationString(inputs.get(KEY_LOCATION));
        if (fromLocationStr != null) {
            return ensureLocationName(fromLocationStr.getLongitude(), fromLocationStr.getLatitude(), fromLocationStr.getLocationName());
        }
        Double lat = getDouble(inputs, KEY_LATITUDE, KEY_LAT);
        Double lon = getDouble(inputs, KEY_LONGITUDE, KEY_LON, KEY_LNG);
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("inputs 中未找到经纬度，请提供 location(经度,纬度) 或 latitude/longitude 或 lat/lon");
        }
        logger.debug("从 inputs 解析经纬度: lat={}, lon={}", lat, lon);
        return ensureLocationName(lon, lat, null);
    }

    /**
     * 若 locationName 为空则通过高德逆地理编码获取城市/地址，保证返回的 GeoResult 三字段均有值。
     */
    private GeoResult ensureLocationName(double longitude, double latitude, String existingLocationName) {
        if (existingLocationName != null && !existingLocationName.isBlank()) {
            return new GeoResult(longitude, latitude, existingLocationName);
        }
        String name = regeoByAmap(longitude, latitude);
        if (name != null && !name.isBlank()) {
            return new GeoResult(longitude, latitude, name);
        }
        return new GeoResult(longitude, latitude, "当前位置");
    }

    /** 解析 inputs.location 格式："经度,纬度" */
    private GeoResult parseLocationString(Object locationObj) {
        if (locationObj == null) return null;
        String s = locationObj.toString().trim();
        if (s.isEmpty()) return null;
        int comma = s.indexOf(',');
        if (comma <= 0 || comma == s.length() - 1) return null;
        try {
            double lon = Double.parseDouble(s.substring(0, comma).trim());
            double lat = Double.parseDouble(s.substring(comma + 1).trim());
            logger.debug("从 inputs.location 解析经纬度: lon={}, lat={}", lon, lat);
            return new GeoResult(lon, lat, null);
        } catch (NumberFormatException e) {
            logger.warn("inputs.location 格式非法，非 经度,纬度: {}", s);
            return null;
        }
    }

    private Double getDouble(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v == null) continue;
            if (v instanceof Number) {
                return ((Number) v).doubleValue();
            }
            try {
                return Double.parseDouble(v.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 地理编码：优先调用高德 API，未配置 key 或请求失败时回退占位坐标。
     *
     * @param address 城市名或结构化地址
     * @return 经纬度及可选地名
     */
    private GeoResult geocode(String address) {
        GeoResult fromAmap = geocodeByAmap(address);
        if (fromAmap != null) {
            return fromAmap;
        }
        return geocodeFallback(address);
    }

    /**
     * 调用高德地理编码 API：GET /v3/geocode/geo?address=xxx&key=xxx
     * 返回 status=1 且 geocodes[0].location 为 "经度,纬度" 时解析为 GeoResult，否则返回 null。
     */
    private GeoResult geocodeByAmap(String address) {
        String key = amapGeoProperties != null ? amapGeoProperties.getKey() : null;
        if (key == null || key.isBlank()) {
            logger.debug("高德地理编码未配置 key，使用占位坐标");
            return null;
        }
        if (address == null || address.isBlank()) {
            return null;
        }
        String baseUrl = amapGeoProperties.getBaseUrl();
        if (baseUrl == null) baseUrl = "https://restapi.amap.com";
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String url = base + AMAP_GEO_PATH;
        try {
            URI uri = new URIBuilder(url)
                    .setCharset(StandardCharsets.UTF_8)
                    .addParameter("address", address)
                    .addParameter("key", key.trim())
                    .build();
            HttpGet get = new HttpGet(uri);
            String responseBody = httpClient.execute(get, response -> {
                int code = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (code != 200) {
                    logger.warn("高德地理编码 API 非 200: code={}, body={}", code, body);
                    return null;
                }
                return body;
            });
            if (responseBody == null || responseBody.isBlank()) return null;

            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText("");
            if (!"1".equals(status)) {
                String info = root.path("info").asText("");
                logger.warn("高德地理编码失败: status={}, info={}, address={}", status, info, address);
                return null;
            }
            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.isEmpty()) {
                logger.warn("高德地理编码无结果: address={}", address);
                return null;
            }
            JsonNode firstGeo = geocodes.get(0);
            String locationStr = firstGeo.path("location").asText();
            if (locationStr.isBlank()) return null;
            int comma = locationStr.indexOf(',');
            if (comma <= 0 || comma == locationStr.length() - 1) return null;
            double lon = Double.parseDouble(locationStr.substring(0, comma).trim());
            double lat = Double.parseDouble(locationStr.substring(comma + 1).trim());
            String formattedAddress = firstGeo.path("formatted_address").asText();
            if (formattedAddress.isBlank()) formattedAddress = address;
            logger.info("高德地理编码: address={}, lon={}, lat={}, formatted={}", address, lon, lat, formattedAddress);
            return new GeoResult(lon, lat, formattedAddress);
        } catch (Exception e) {
            logger.warn("高德地理编码请求异常: address={}", address, e);
            return null;
        }
    }

    /**
     * 高德逆地理编码：根据经纬度获取城市/地址信息。
     * 请求 GET /v3/geocode/regeo?location=经度,纬度&key=xxx
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 格式化地址或省市区拼接，失败返回 null
     */
    private String regeoByAmap(double longitude, double latitude) {
        String key = amapGeoProperties != null ? amapGeoProperties.getKey() : null;
        if (key == null || key.isBlank()) {
            logger.debug("高德逆地理编码未配置 key");
            return null;
        }
        String baseUrl = amapGeoProperties.getBaseUrl();
        if (baseUrl == null) baseUrl = "https://restapi.amap.com";
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String url = base + AMAP_REGEO_PATH;
        String location = longitude + "," + latitude;
        try {
            URI uri = new URIBuilder(url)
                    .setCharset(StandardCharsets.UTF_8)
                    .addParameter("location", location)
                    .addParameter("key", key.trim())
                    .build();
            HttpGet get = new HttpGet(uri);
            String responseBody = httpClient.execute(get, response -> {
                int code = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (code != 200) {
                    logger.warn("高德逆地理编码 API 非 200: code={}, body={}", code, body);
                    return null;
                }
                return body;
            });
            if (responseBody == null || responseBody.isBlank()) return null;

            JsonNode root = objectMapper.readTree(responseBody);
            if (!"1".equals(root.path("status").asText(""))) {
                logger.warn("高德逆地理编码失败: status={}, info={}, location={}", root.path("status").asText(), root.path("info").asText(), location);
                return null;
            }
            JsonNode regeocode = root.path("regeocode");
            if (regeocode.isMissingNode() || regeocode.isNull()) return null;

            // 优先使用省/市/区拼接，仅到区一级，避免 formatted_address 过长（如含街道、门牌）
            JsonNode addr = regeocode.path("addressComponent");
            if (addr.isObject()) {
                String province = addr.path("province").asText("").trim();
                String city = addr.path("city").asText("").trim();
                String district = addr.path("district").asText("").trim();
                StringBuilder sb = new StringBuilder();
                if (!province.isEmpty()) sb.append(province);
                if (!city.isEmpty() && !city.equals(province)) sb.append(city);
                if (!district.isEmpty()) sb.append(district);
                if (sb.length() > 0) {
                    String name = sb.toString();
                    logger.info("高德逆地理编码: location={}, name(省市区)={}", location, name);
                    return name;
                }
            }
            String formatted = regeocode.path("formatted_address").asText(null);
            if (formatted != null && !formatted.isBlank()) {
                logger.info("高德逆地理编码: location={}, formatted_address={}", location, formatted);
                return formatted;
            }
            return null;
        } catch (Exception e) {
            logger.warn("高德逆地理编码请求异常: location={}", location, e);
            return null;
        }
    }

    /** 占位：无 API 或失败时返回常见城市坐标或默认北京 */
    private GeoResult geocodeFallback(String address) {
        if (address != null && !address.isBlank()) {
            if (address.contains("北京")) return new GeoResult(116.4074, 39.9042, "北京市");
            if (address.contains("上海")) return new GeoResult(121.4737, 31.2304, "上海市");
            if (address.contains("广州")) return new GeoResult(113.2644, 23.1291, "广州市");
            if (address.contains("深圳")) return new GeoResult(114.0579, 22.5431, "深圳市");
            if (address.contains("杭州")) return new GeoResult(120.1551, 30.2741, "杭州市");
            if (address.contains("成都")) return new GeoResult(104.0665, 30.5723, "成都市");
        }
        return new GeoResult(116.4074, 39.9042, address != null ? address : "当前位置");
    }
}
