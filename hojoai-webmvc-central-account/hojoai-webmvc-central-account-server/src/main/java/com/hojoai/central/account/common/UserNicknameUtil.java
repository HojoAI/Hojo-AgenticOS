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
package com.hojoai.central.account.common;

import java.util.List;
import java.util.Random;

/**
 * 新用户默认昵称：从预设词库中随机选取。
 */
public class UserNicknameUtil {

    /** 候选昵称词库 */
    private static final List<String> USER_NICKNAME_LIST = List.of("松岚黛影", "雾玦青砂", "烟竹灰蓝", "霜槿浅紫", "月瓷青釉", "云岫浅黛", "杉影冷褐", "澜石雾蓝", "朝槐暖米", "松烟紫藤", "暮岚青黛", "檀月烟灰", "露笺青墨", "栖霞浅棕", "尘霁银蓝", "嵐松石绿", "墨杉青灰", "霁雪烟紫", "云砾白砂", "青槿暮粉");

    /**
     * @return 随机默认昵称
     */
    public static String getRandomUserNickname() {
        int index = new Random().nextInt(USER_NICKNAME_LIST.size());
        return USER_NICKNAME_LIST.get(index);
    }
}
