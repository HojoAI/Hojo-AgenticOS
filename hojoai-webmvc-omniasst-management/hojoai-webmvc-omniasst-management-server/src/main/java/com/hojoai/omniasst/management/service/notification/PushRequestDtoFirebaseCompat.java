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
package com.hojoai.omniasst.management.service.notification;

import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 部分 central-provider 版本的 {@link PushRequestDTO} 未生成 {@code getFirebaseData()}，
 * 但序列化仍需要读写 firebaseData。此处用反射兼容，避免本工程编译期依赖不存在的方法。
 */
public final class PushRequestDtoFirebaseCompat {

    private static final Logger logger = LoggerFactory.getLogger(PushRequestDtoFirebaseCompat.class);

    private PushRequestDtoFirebaseCompat() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getFirebaseData(PushRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        try {
            Method m = dto.getClass().getMethod("getFirebaseData");
            Object v = m.invoke(dto);
            return (Map<String, String>) v;
        } catch (NoSuchMethodException ignored) {
            // ignore
        } catch (Exception e) {
            logger.warn("[push-dto-compat] getFirebaseData() 调用失败", e);
        }

        try {
            Field f = dto.getClass().getDeclaredField("firebaseData");
            f.setAccessible(true);
            Object v = f.get(dto);
            return (Map<String, String>) v;
        } catch (Exception e) {
            logger.warn("[push-dto-compat] 读取 firebaseData 字段失败", e);
            return null;
        }
    }

    public static void setFirebaseData(PushRequestDTO dto, Map<String, String> firebaseData) {
        if (dto == null) {
            return;
        }
        try {
            Method m = dto.getClass().getMethod("setFirebaseData", Map.class);
            m.invoke(dto, firebaseData);
            return;
        } catch (NoSuchMethodException ignored) {
            // ignore
        } catch (Exception e) {
            logger.warn("[push-dto-compat] setFirebaseData(Map) 调用失败", e);
        }

        try {
            Field f = dto.getClass().getDeclaredField("firebaseData");
            f.setAccessible(true);
            f.set(dto, firebaseData);
        } catch (Exception e) {
            logger.error("[push-dto-compat] 写入 firebaseData 字段失败", e);
        }
    }
}
