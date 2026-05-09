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
package com.hojoai.omniasst.weather.v2.support;

import com.hojoai.omniasst.weather.v2.entity.CaiyunWindDto;
import org.springframework.stereotype.Component;

/**
 * 风速风向格式化：m/s + 角度 → "Xmph, 西南风"
 *
 * @author wangjian
 * @since 2026-02-02
 */
@Component
public class WindFormatter {

    private static final double MPS_TO_MPH = 2.23694;
    private static final String[] DIRECTIONS = {
            "北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"
    };

    /**
     * 格式化为 "Xmph, 西南风"（速度 m/s → mph 取整，角度 → 八方向）
     */
    public String format(CaiyunWindDto wind) {
        if (wind == null) return "—";
        Double speed = wind.getSpeed();
        Double direction = wind.getDirection();
        int mph = speed != null ? (int) Math.round(speed * MPS_TO_MPH) : 0;
        String dir = directionToChinese(direction);
        return mph + "mph, " + dir;
    }

    /** 仅风向，如 "东南风" */
    public String formatDirection(CaiyunWindDto wind) {
        if (wind == null) return "—";
        String dir = directionToChinese(wind.getDirection());
        return "—".equals(dir) ? "—" : dir;
    }

    /** 蒲福风级 0–12（根据 m/s 换算） */
    public String formatWindLevel(CaiyunWindDto wind) {
        if (wind == null || wind.getSpeed() == null) return "—";
        int level = mpsToBeaufort(wind.getSpeed());
        return String.valueOf(level);
    }

    private static int mpsToBeaufort(double mps) {
        if (mps <= 0.2) return 0;
        if (mps <= 1.5) return 1;
        if (mps <= 3.3) return 2;
        if (mps <= 5.4) return 3;
        if (mps <= 7.9) return 4;
        if (mps <= 10.7) return 5;
        if (mps <= 13.8) return 6;
        if (mps <= 17.1) return 7;
        if (mps <= 20.7) return 8;
        if (mps <= 24.4) return 9;
        if (mps <= 28.4) return 10;
        if (mps <= 32.6) return 11;
        return 12;
    }

    private String directionToChinese(Double degrees) {
        if (degrees == null) return "—";
        // 0~360 → 0~7 索引，每 45 度一档
        int idx = (int) Math.round(degrees / 45.0) % 8;
        if (idx < 0) idx += 8;
        return DIRECTIONS[idx];
    }
}
