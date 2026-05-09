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
package com.hojoai.central.provider.service.utils;

import com.hojoai.central.provider.service.common.enums.CommonEnum;

/**
 * The EnumUtil class for enum.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class EnumUtil {

    /**
     * 根据code返回对应枚举的value
     * @param code
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends CommonEnum> Object getValueByCode(Object code, Class<T> clazz) {
        for(T e : clazz.getEnumConstants()) {
        	if(e.getCode().equals(code)) {
        		return e.getValue();
        	}
        }
        return null;
    }

    /**
     * 根据code返回指定枚举
     * @param code
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends CommonEnum> T getEnumByCode(Object code, Class<T> clazz) {
        for(T e : clazz.getEnumConstants()) {
        	if(e.getCode().equals(code)) return e;
        }
        return null;
    }

    /**
     * 根据value返回对应枚举的value
     * @param value
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends CommonEnum> T getEnumByValue(Object value, Class<T> clazz) {
        for(T e : clazz.getEnumConstants()) {
        	if(e.getValue().equals(value)) return e;
        }
        return null;
    }

    /**
     * 返回指定enum的枚举
     * @param t
     * @return
     */
    public static <T extends CommonEnum> T getEnum(T t) {
    	return t;
    }

}
