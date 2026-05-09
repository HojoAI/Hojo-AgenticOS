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
package com.hojoai.webmvc.omniasst.meeting.configure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.mybatis.spring.MyBatisSystemException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将参数类异常转为 400 并返回 message；数据库异常单独处理，避免联调/预发只看到无说明的 500。
 */
@RestControllerAdvice
@Order(100)
public class GlobalApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    /**
     * 参数或业务前置校验失败（{@link IllegalArgumentException}）时返回 400。
     *
     * @param ex 异常，message 将透传给客户端
     * @return JSON body，含 status、error、message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return jsonError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 请求体无法反序列化为控制器方法参数时返回 400（如非法 JSON、字段类型不匹配）。
     *
     * @param ex Spring MVC 反序列化异常
     * @return JSON body，含固定中文说明
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return jsonError(HttpStatus.BAD_REQUEST, "请求体不是合法 JSON 或与接口字段不匹配");
    }

    /**
     * 数据库访问或 MyBatis 调用失败时返回 409（唯一冲突）或 500，并记录服务端日志。
     *
     * @param ex 数据访问异常
     * @return JSON body；重复键等业务冲突时为 409，其它为 500 并附带简要数据库提示（已截断）
     */
    @ExceptionHandler({DataAccessException.class, MyBatisSystemException.class})
    public ResponseEntity<Map<String, Object>> handleDataAccess(Exception ex) {
        logger.error("数据库访问异常", ex);
        if (ex instanceof DuplicateKeyException) {
            return jsonError(HttpStatus.CONFLICT, "数据冲突（可能重复提交），请避免重复登记同一资源");
        }
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String hint = root.getMessage() != null ? root.getMessage() : ex.getMessage();
        return jsonError(HttpStatus.INTERNAL_SERVER_ERROR,
                "数据存储失败（预发请核对库表、连接与 sql_mode）。详情：" + sanitizeDbMessage(hint));
    }

    private static String sanitizeDbMessage(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.replaceAll("\\s+", " ").trim();
        return s.length() > 280 ? s.substring(0, 280) + "…" : s;
    }

    private static ResponseEntity<Map<String, Object>> jsonError(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message != null ? message : "");
        return ResponseEntity.status(status).body(body);
    }
}
