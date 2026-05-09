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

import com.hojoai.omniasst.weather.v2.entity.DailyLifeIndexV3Dto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 按解析出的日期过滤 v3 生活指数数据，与彩云逐日过滤逻辑一致。
 *
 * @author wangjian
 * @since 2026-02-03
 */
@Service
public class LifeIndexV3FilterService {

    private static final Logger logger = LoggerFactory.getLogger(LifeIndexV3FilterService.class);

    /**
     * 仅保留 date 在 targetDates 内的生活指数日数据。
     *
     * @param data        v3 返回的按日生活指数列表
     * @param targetDates 要保留的日期（与 query 解析出的日期一致）
     * @return 过滤后的列表；data 为空或 targetDates 为空时返回空列表
     */
    public List<DailyLifeIndexV3Dto> filterByDates(List<DailyLifeIndexV3Dto> data, List<LocalDate> targetDates) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        if (targetDates == null || targetDates.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> dateSet = targetDates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toSet());

        List<DailyLifeIndexV3Dto> filtered = data.stream()
                .filter(item -> {
                    String d = item.getDate();
                    return d != null && dateSet.contains(normalizeDate(d));
                })
                .collect(Collectors.toList());

        logger.info("v3 生活指数已按日期过滤: 保留日期={}, 过滤后条数={}", dateSet, filtered.size());
        return filtered;
    }

    private static String normalizeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        if (dateStr.length() >= 10) {
            return dateStr.substring(0, 10);
        }
        return dateStr;
    }
}
