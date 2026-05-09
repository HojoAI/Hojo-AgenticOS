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

import com.hojoai.omniasst.weather.v2.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按解析出的日期对彩云逐日数据进行剔除：仅保留指定日期的数据。
 * 例如彩云返回 7 天，用户问「明天」则只保留明天的数据。
 *
 * @author wangjian
 * @since 2026-02-03
 */
@Service
public class CaiyunDailyFilterService {

    private static final Logger logger = LoggerFactory.getLogger(CaiyunDailyFilterService.class);

    /**
     * 按目标日期过滤彩云 result：仅保留 daily 中日期在 targetDates 内的项。
     * 若 targetDates 为空，则不做过滤，返回原 result（不拷贝）。
     *
     * @param result       彩云 API 返回的 result
     * @param targetDates  要保留的日期（如从 Natty 解析出的单日或区间内日期）
     * @return 过滤后的 result（可能为新对象或原对象）
     */
    public CaiyunWeatherResult filterByDates(CaiyunWeatherResult result, List<LocalDate> targetDates) {
        if (result == null) {
            return null;
        }
        if (targetDates == null || targetDates.isEmpty()) {
            return result;
        }
        Set<String> dateSet = targetDates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toSet());

        CaiyunDailyDto daily = result.getDaily();
        if (daily == null) {
            return result;
        }

        CaiyunDailyDto filteredDaily = new CaiyunDailyDto();
        filteredDaily.setStatus(daily.getStatus());

        filterList(daily.getAstro(), filteredDaily::setAstro, CaiyunAstroDto::getDate, dateSet);
        filterList(daily.getTemperature(), filteredDaily::setTemperature, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getTemperature08h20h(), filteredDaily::setTemperature08h20h, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getTemperature20h32h(), filteredDaily::setTemperature20h32h, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getPrecipitation(), filteredDaily::setPrecipitation, CaiyunDailyPrecipitationDto::getDate, dateSet);
        filterList(daily.getPrecipitation08h20h(), filteredDaily::setPrecipitation08h20h, CaiyunDailyPrecipitationDto::getDate, dateSet);
        filterList(daily.getPrecipitation20h32h(), filteredDaily::setPrecipitation20h32h, CaiyunDailyPrecipitationDto::getDate, dateSet);
        filterList(daily.getWind(), filteredDaily::setWind, CaiyunDailyWindDto::getDate, dateSet);
        filterList(daily.getWind08h20h(), filteredDaily::setWind08h20h, CaiyunDailyWindDto::getDate, dateSet);
        filterList(daily.getWind20h32h(), filteredDaily::setWind20h32h, CaiyunDailyWindDto::getDate, dateSet);
        filterList(daily.getHumidity(), filteredDaily::setHumidity, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getCloudrate(), filteredDaily::setCloudrate, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getPressure(), filteredDaily::setPressure, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getVisibility(), filteredDaily::setVisibility, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getDswrf(), filteredDaily::setDswrf, CaiyunDailyValueDto::getDate, dateSet);
        filterList(daily.getSkycon(), filteredDaily::setSkycon, CaiyunDailySkyconDto::getDate, dateSet);
        filterList(daily.getSkycon08h20h(), filteredDaily::setSkycon08h20h, CaiyunDailySkyconDto::getDate, dateSet);
        filterList(daily.getSkycon20h32h(), filteredDaily::setSkycon20h32h, CaiyunDailySkyconDto::getDate, dateSet);

        if (daily.getAirQuality() != null) {
            CaiyunDailyAirQualityDto aq = daily.getAirQuality();
            CaiyunDailyAirQualityDto filteredAq = new CaiyunDailyAirQualityDto();
            filterList(aq.getAqi(), filteredAq::setAqi, CaiyunDailyAqiItemDto::getDate, dateSet);
            filterList(aq.getPm25(), filteredAq::setPm25, CaiyunDailyPm25ItemDto::getDate, dateSet);
            filteredDaily.setAirQuality(filteredAq);
        }

        if (daily.getLifeIndex() != null) {
            CaiyunDailyLifeIndexDto life = daily.getLifeIndex();
            CaiyunDailyLifeIndexDto filteredLife = new CaiyunDailyLifeIndexDto();
            filterList(life.getUltraviolet(), filteredLife::setUltraviolet, CaiyunDailyLifeIndexItemDto::getDate, dateSet);
            filterList(life.getCarWashing(), filteredLife::setCarWashing, CaiyunDailyLifeIndexItemDto::getDate, dateSet);
            filterList(life.getDressing(), filteredLife::setDressing, CaiyunDailyLifeIndexItemDto::getDate, dateSet);
            filterList(life.getComfort(), filteredLife::setComfort, CaiyunDailyLifeIndexItemDto::getDate, dateSet);
            filterList(life.getColdRisk(), filteredLife::setColdRisk, CaiyunDailyLifeIndexItemDto::getDate, dateSet);
            filteredDaily.setLifeIndex(filteredLife);
        }

        CaiyunWeatherResult filtered = new CaiyunWeatherResult();
        filtered.setRealtime(result.getRealtime());
        filtered.setDaily(filteredDaily);
        filtered.setPrimary(result.getPrimary());
        filtered.setForecastKeypoint(result.getForecastKeypoint());

        // 日志：保留日期是“目标保留”的日期；逐日条数取决于彩云 API 原始数据里是否包含这些日期，若某日无数据则过滤后条数会少于保留日期数
        int tempSize = filteredDaily.getTemperature() != null ? filteredDaily.getTemperature().size() : 0;
        List<String> tempDates = filteredDaily.getTemperature() == null ? Collections.emptyList()
                : filteredDaily.getTemperature().stream()
                .map(CaiyunDailyValueDto::getDate)
                .map(CaiyunDailyFilterService::normalizeDate)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        logger.info("彩云逐日数据已按日期剔除: 保留日期={}(共{}天), temperature 过滤后条数={}, temperature 实际保留日期={}",
                dateSet, dateSet.size(), tempSize, tempDates);

        return filtered;
    }

    // 对 daily 下各 List 字段统一过滤：仅保留元素日期（规范化后 yyyy-MM-dd）落在 targetDates 集合内的项
    private static <T> void filterList(List<T> source, java.util.function.Consumer<List<T>> setter,
                                      Function<T, String> getDate, Set<String> dateSet) {
        if (source == null) {
            return;
        }
        List<T> filtered = source.stream()
                .filter(item -> {
                    String d = getDate.apply(item);
                    return d != null && dateSet.contains(normalizeDate(d));
                })
                .collect(Collectors.toList());
        setter.accept(filtered);
    }

    /** 彩云可能返回 "2026-02-04" 或 "2026-02-04T00:00+08:00"，统一为 yyyy-MM-dd */
    private static String normalizeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        if (dateStr.length() >= 10) {
            return dateStr.substring(0, 10);
        }
        return dateStr;
    }
}
