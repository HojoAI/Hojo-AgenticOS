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
package com.hojoai.omniasst.focus.domain.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hojoai.central.provider.grpc.FilmInfo;
import com.hojoai.central.provider.grpc.GetFilmListRequestDTO;
import com.hojoai.omniasst.focus.common.assembler.RuleMovieAssembler;
import com.hojoai.omniasst.focus.common.enums.TypeEnum;
import com.hojoai.omniasst.focus.common.enums.UrlEnum;
import com.hojoai.omniasst.focus.configure.IConfigurationForGrpc;
import com.hojoai.omniasst.focus.domain.AIDomainService;
import com.hojoai.omniasst.focus.domain.ManualCreateScheduleDataParser;
import com.hojoai.omniasst.focus.domain.strategy.ScheduleStrategy;
import com.hojoai.omniasst.focus.dto.request.AITitleHttpRequestDTO;
import com.hojoai.omniasst.focus.dto.request.ManualScheduleRequestDTO;
import com.hojoai.omniasst.focus.dto.response.AIMovieNameResponseDTO;
import com.hojoai.omniasst.focus.dto.val.MovieVal;
import com.hojoai.omniasst.focus.dto.val.SysScheduleVal;
import com.hojoai.omniasst.focus.repository.mapper.RuleMovieMapper;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMovieMapper;
import com.hojoai.omniasst.focus.repository.po.RuleMoviePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleMoviePO;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.repository.po.ScheduleRulePO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.downgoon.snowflake.Snowflake;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 手动创建电影日程策略实现
 *
 * @author My
 * @version 1.0 2026/03/03
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
@Component
public class MovieStrategyImpl implements ScheduleStrategy<RuleMoviePO, ScheduleMoviePO> {

    @Resource
    private IConfigurationForGrpc iConfigurationForGrpc;

    @Resource
    private AIDomainService aIDomainService;

    @Resource
    private RuleMovieMapper ruleMovieMapper;

    @Resource
    private ScheduleMovieMapper scheduleMovieMapper;

    @Resource
    private Snowflake snowflake;

    @Override
    public Integer getType() {
        return TypeEnum.MOVIE.getCode();
    }

    @Override
    public List<RuleMoviePO> createScheduleRule(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        MovieVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), MovieVal.class);

        AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
        aiTitleHttpRequestDTO.setEnteredTitle(requestDTO.getTitle());

        Optional.ofNullable(aIDomainService.chat(UrlEnum.SCHEDULE_GET_MOVIE_NAME, aiTitleHttpRequestDTO, AIMovieNameResponseDTO.class))
                .ifPresent(response -> data.setMovieName(response.getMovieName()));

        return List.of(createScheduleWithData(requestDTO, data, rulePO));
    }

    private RuleMoviePO createScheduleWithData(ManualScheduleRequestDTO requestDTO, MovieVal data, ScheduleRulePO rulePO) {
        RuleMoviePO po = RuleMovieAssembler.buildRuleMoviePO(snowflake.nextId(), data, rulePO);

        GetFilmListRequestDTO getFilmListRequestDTO = GetFilmListRequestDTO.newBuilder()
                .setType(0)
                .setKeyWord(data.getMovieName())
                .setPageIndex(1)
                .setPageSize(5)
                .build();

        Optional.ofNullable(iConfigurationForGrpc.movieClient().getFilmList(getFilmListRequestDTO)).ifPresent(respDTO -> {
            if (respDTO.getSuccess() && CollectionUtils.isNotEmpty(respDTO.getFilmListList())) {
                FilmInfo filmInfo = respDTO.getFilmListList().getFirst();
                po.setPicUrl(filmInfo.getPic());
                po.setDuration(filmInfo.getDuration());
                po.setMovieName(filmInfo.getName());
            } else {
                po.setMovieName(data.getMovieName());
            }
        });

        ruleMovieMapper.insert(po);
        return po;
    }

    @Override
    public List<RuleMoviePO> createScheduleRule(ScheduleRulePO scheduleRulePO, SysScheduleVal sysScheduleVal) {
        if (sysScheduleVal == null) {
            return List.of();
        }

        AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
        aiTitleHttpRequestDTO.setEnteredTitle(scheduleRulePO.getTitle());

        String movieName = aIDomainService.chat(UrlEnum.SCHEDULE_GET_MOVIE_NAME, aiTitleHttpRequestDTO, AIMovieNameResponseDTO.class).getMovieName();

        GetFilmListRequestDTO getFilmListRequestDTO = GetFilmListRequestDTO.newBuilder()
                .setType(0)
                .setKeyWord(movieName)
                .setPageIndex(1)
                .setPageSize(5)
                .build();

        RuleMoviePO po = RuleMovieAssembler.buildRuleMoviePO(snowflake.nextId(), sysScheduleVal, scheduleRulePO);
        Optional.ofNullable(iConfigurationForGrpc.movieClient().getFilmList(getFilmListRequestDTO)).ifPresent(respDTO -> {
            if (respDTO.getSuccess() && CollectionUtils.isNotEmpty(respDTO.getFilmListList())) {
                FilmInfo filmInfo = respDTO.getFilmListList().getFirst();
                po.setPicUrl(filmInfo.getPic());
                po.setDuration(filmInfo.getDuration());
                po.setMovieName(filmInfo.getName());
            } else {
                po.setMovieName(movieName);
            }
        });

        ruleMovieMapper.insert(po);
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ScheduleMoviePO> createSchedule(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (schedulePOList == null || dataRulePOList == null) {
            return List.of();
        }
        List<RuleMoviePO> ruleList = (List<RuleMoviePO>) dataRulePOList;
        List<ScheduleMoviePO> result = new ArrayList<>();

        schedulePOList.forEach(schedulePO -> {
            List<ScheduleMoviePO> scheduleMoviePOList = ruleList.stream()
                    .map(ruleMoviePO -> RuleMovieAssembler.buildScheduleMoviePO(snowflake.nextId(), schedulePO, ruleMoviePO))
                    .toList();

            result.addAll(scheduleMoviePOList);
        });

        scheduleMovieMapper.insert(result);

        return result;
    }

    @Override
    public List<ScheduleMoviePO> getScheduleData(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        return scheduleMovieMapper.selectList(
                new LambdaQueryWrapper<ScheduleMoviePO>().eq(ScheduleMoviePO::getScheduleId, scheduleId));
    }

    @Override
    public List<RuleMoviePO> getScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return ruleMovieMapper.selectList(
                new LambdaQueryWrapper<RuleMoviePO>().eq(RuleMoviePO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleRuleData(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ruleMovieMapper.delete(
                new LambdaQueryWrapper<RuleMoviePO>().eq(RuleMoviePO::getRuleId, ruleId));
    }

    @Override
    public void deleteScheduleData(List<Long> scheduleIdList) {
        if (CollectionUtils.isEmpty(scheduleIdList)) {
            return;
        }
        scheduleMovieMapper.delete(
                new LambdaQueryWrapper<ScheduleMoviePO>().in(ScheduleMoviePO::getScheduleId, scheduleIdList));
    }

    @Override
    public List<RuleMoviePO> updateScheduleRuleData(ManualScheduleRequestDTO requestDTO, ScheduleRulePO rulePO) {
        MovieVal data = ManualCreateScheduleDataParser.parseData(requestDTO.getData(), MovieVal.class);
        if (data == null) {
            return List.of();
        }
        AITitleHttpRequestDTO aiTitleHttpRequestDTO = new AITitleHttpRequestDTO();
        aiTitleHttpRequestDTO.setEnteredTitle(requestDTO.getTitle());
        Optional.ofNullable(aIDomainService.chat(UrlEnum.SCHEDULE_GET_MOVIE_NAME, aiTitleHttpRequestDTO, AIMovieNameResponseDTO.class))
                .ifPresent(response -> data.setMovieName(response.getMovieName()));

        List<RuleMoviePO> existing = ruleMovieMapper.selectList(
                new LambdaQueryWrapper<RuleMoviePO>().eq(RuleMoviePO::getRuleId, rulePO.getId()));

        RuleMoviePO po = RuleMovieAssembler.buildRuleMoviePO(
                CollectionUtils.isNotEmpty(existing) ? existing.get(0).getId() : snowflake.nextId(), data, rulePO);

        GetFilmListRequestDTO getFilmListRequestDTO = GetFilmListRequestDTO.newBuilder()
                .setType(0)
                .setKeyWord(data.getMovieName())
                .setPageIndex(1)
                .setPageSize(5)
                .build();
        Optional.ofNullable(iConfigurationForGrpc.movieClient().getFilmList(getFilmListRequestDTO)).ifPresent(respDTO -> {
            if (respDTO.getSuccess() && CollectionUtils.isNotEmpty(respDTO.getFilmListList())) {
                FilmInfo filmInfo = respDTO.getFilmListList().getFirst();
                po.setPicUrl(filmInfo.getPic());
                po.setDuration(filmInfo.getDuration());
                po.setMovieName(filmInfo.getName());
            } else {
                po.setMovieName(data.getMovieName());
            }
        });

        if (CollectionUtils.isNotEmpty(existing)) {
            ruleMovieMapper.updateById(po);
        } else {
            ruleMovieMapper.insert(po);
        }
        return List.of(po);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateScheduleData(List<SchedulePO> schedulePOList, List<?> dataRulePOList) {
        if (CollectionUtils.isEmpty(schedulePOList) || CollectionUtils.isEmpty(dataRulePOList)) {
            return;
        }
        List<RuleMoviePO> ruleList = (List<RuleMoviePO>) dataRulePOList;
        schedulePOList.forEach(schedulePO -> {
            List<ScheduleMoviePO> existing = scheduleMovieMapper.selectList(
                    new LambdaQueryWrapper<ScheduleMoviePO>().eq(ScheduleMoviePO::getScheduleId, schedulePO.getId()));
            for (int i = 0; i < ruleList.size(); i++) {
                RuleMoviePO ruleMovie = ruleList.get(i);
                if (i < existing.size()) {
                    ScheduleMoviePO updated = RuleMovieAssembler.buildScheduleMoviePO(
                            existing.get(i).getId(), schedulePO, ruleMovie);
                    scheduleMovieMapper.updateById(updated);
                } else {
                    scheduleMovieMapper.insert(RuleMovieAssembler.buildScheduleMoviePO(
                            snowflake.nextId(), schedulePO, ruleMovie));
                }
            }
            for (int i = ruleList.size(); i < existing.size(); i++) {
                scheduleMovieMapper.deleteById(existing.get(i).getId());
            }
        });
    }

}
