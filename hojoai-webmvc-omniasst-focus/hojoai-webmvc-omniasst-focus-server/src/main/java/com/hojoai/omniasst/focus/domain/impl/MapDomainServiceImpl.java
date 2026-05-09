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
package com.hojoai.omniasst.focus.domain.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.omniasst.focus.dto.val.LocationInfoVal;
import com.hojoai.omniasst.focus.repository.mapper.MapSearchMapper;
import com.hojoai.omniasst.focus.repository.po.MapSearchPO;
import com.hojoai.omniasst.focus.domain.MapDomainService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MapDomainServiceImpl implements MapDomainService {

    private static final int RECENT_LIMIT = 5;

    @Resource
    private MapSearchMapper mapSearchMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public void save(String userId, LocationInfoVal request) {
        if (request == null || StringUtils.isBlank(userId)) {
            return;
        }
        // 若 request 中所有数据都为空（null/空字符串）或 BigDecimal 字段值等于 0，则直接返回
        if (isAllEmpty(request)) {
            return;
        }

        MapSearchPO po = new MapSearchPO();
        po.setId(snowflake.nextId());
        po.setUserId(userId);
        po.setName(request.getName());
        po.setLocationInfo(request.getLocation());
        po.setLongitude(request.getLongitude());
        po.setLatitude(request.getLatitude());
        mapSearchMapper.insert(po);
    }

    @Override
    public List<LocationInfoVal> listRecent(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        List<MapSearchPO> list = mapSearchMapper.selectList(
                new LambdaQueryWrapper<MapSearchPO>().eq(MapSearchPO::getUserId, userId)
                        .orderByDesc(MapSearchPO::getId));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        // 按 name / locationInfo / (longitude, latitude) 去重，保留先出现的（即最近的一条）
        Set<String> seenNames = new HashSet<>();
        Set<String> seenLocationInfos = new HashSet<>();
        Set<String> seenCoords = new HashSet<>();
        List<LocationInfoVal> result = new ArrayList<>();
        for (MapSearchPO po : list) {
            if (result.size() >= RECENT_LIMIT) {
                break;
            }
            if (isDuplicate(po, seenNames, seenLocationInfos, seenCoords)) {
                continue;
            }
            if (StringUtils.isNotBlank(po.getName())) {
                seenNames.add(po.getName());
            }
            if (StringUtils.isNotBlank(po.getLocationInfo())) {
                seenLocationInfos.add(po.getLocationInfo());
            }
            if (po.getLongitude() != null && po.getLatitude() != null) {
                seenCoords.add(coordKey(po.getLongitude(), po.getLatitude()));
            }
            result.add(toItem(po));
        }
        return result;
    }

    /** 判断 request 中所有数据是否都为空（null/空字符串）或 BigDecimal 字段等于 0 */
    private boolean isAllEmpty(LocationInfoVal request) {
        return StringUtils.isBlank(request.getName())
                && StringUtils.isBlank(request.getLocation())
                && StringUtils.isBlank(request.getProvince())
                && StringUtils.isBlank(request.getCity())
                && StringUtils.isBlank(request.getDistrict())
                && StringUtils.isBlank(request.getTownship())
                && (request.getLongitude() == null || request.getLongitude().compareTo(BigDecimal.ZERO) == 0)
                && (request.getLatitude() == null || request.getLatitude().compareTo(BigDecimal.ZERO) == 0);
    }

    /** 若与已出现记录的 name / locationInfo / (longitude,latitude) 任一相同则视为重复 */
    private boolean isDuplicate(MapSearchPO po, Set<String> seenNames, Set<String> seenLocationInfos, Set<String> seenCoords) {
        if (StringUtils.isNotBlank(po.getName()) && seenNames.contains(po.getName())) {
            return true;
        }
        if (StringUtils.isNotBlank(po.getLocationInfo()) && seenLocationInfos.contains(po.getLocationInfo())) {
            return true;
        }
        if (po.getLongitude() != null && po.getLatitude() != null && seenCoords.contains(coordKey(po.getLongitude(), po.getLatitude()))) {
            return true;
        }
        return false;
    }

    private String coordKey(BigDecimal longitude, BigDecimal latitude) {
        return longitude.toPlainString() + "_" + latitude.toPlainString();
    }

    @Override
    public void deleteById(Long id, String userId) {
        if (id == null || StringUtils.isBlank(userId)) {
            return;
        }
        MapSearchPO po = mapSearchMapper.selectOne(new LambdaQueryWrapper<MapSearchPO>().eq(MapSearchPO::getId, id).eq(MapSearchPO::getUserId, userId));
        if (po != null) {
            mapSearchMapper.deleteById(id);
        }
    }

    @Override
    public void deleteAllByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        mapSearchMapper.delete(new LambdaQueryWrapper<MapSearchPO>().eq(MapSearchPO::getUserId, userId));
    }

    private LocationInfoVal toItem(MapSearchPO po) {
        if (po == null) return null;
        LocationInfoVal val = new LocationInfoVal();
        val.setName(po.getName());
        val.setLocation(po.getLocationInfo());
        val.setLongitude(po.getLongitude());
        val.setLatitude(po.getLatitude());
        return val;
    }
}
