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
package com.hojoai.omniasst.focus.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日程表Mapper接口
 *
 * @author My
 * @version 1.0 2024/12/29
 * @since hojoai-webmvc-omniasst.focus 1.0.0
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<SchedulePO> {

    /**
     * 查询所有不重复的用户ID
     *
     * @return 用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM focus_schedule WHERE is_delete = 0")
    List<String> selectAllUserIds();

}

