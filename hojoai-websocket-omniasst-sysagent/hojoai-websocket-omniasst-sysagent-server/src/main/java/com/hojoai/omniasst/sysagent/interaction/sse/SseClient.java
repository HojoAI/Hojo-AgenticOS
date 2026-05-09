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
package com.hojoai.omniasst.sysagent.interaction.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * 基于 Apache HttpClient 的 SSE 客户端：POST JSON 后以流方式解析 {@code data:} 行并回调。
 */
@Component
public class SseClient {
    private static final Logger logger = LoggerFactory.getLogger(SseClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 向 {@code endpoint} 发起 SSE 请求，逐行解析事件并将 {@code data:} 载荷交给 {@code dataHandler}。
     *
     * @param endpoint    完整 URL
     * @param requestBody 将序列化为 JSON 的请求体
     * @param dataHandler 每条 data 行内容的消费者
     * @throws RuntimeException HTTP 非 200、IO 异常等将包装后抛出
     */
    public void sendRequest(String endpoint, Object requestBody,
                                              Consumer<String> dataHandler) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Accept", MediaType.TEXT_EVENT_STREAM_VALUE);
            httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    throw new RuntimeException("HTTP Error: " + statusCode);
                }

                try (InputStream inputStream = response.getEntity().getContent();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                        parseSseEvent(line, dataHandler);
                    }
                }

                return null;
            });

        } catch (Exception e) {
            logger.error("Error occurred while sending SSE request to endpoint: {}", endpoint, e);
            throw new RuntimeException(e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    logger.warn("Failed to close HttpClient", e);
                }
            }
        }
    }

    private void parseSseEvent(String line, Consumer<String> dataHandler) {
        if (line.startsWith("data:")) {
            String data = line.substring(5).trim(); // 移除 "data:" 前缀
            if (!data.isEmpty()) {
                dataHandler.accept(data);
            }
        } else if (line.startsWith("event:")) {
            String eventName = line.substring(6).trim(); // 移除 "event:" 前缀
            logger.debug("Received SSE event: {}", eventName);
        } else if (line.startsWith("id:")) {
            String eventId = line.substring(3).trim(); // 移除 "id:" 前缀
            logger.debug("Received SSE event ID: {}", eventId);
        } else if (line.startsWith("retry:")) {
            String retry = line.substring(6).trim(); // 移除 "retry:" 前缀
            logger.debug("Received SSE retry: {}", retry);
        } else if (line.isEmpty()) {
            logger.debug("SSE event ended");
        } else {
            logger.debug("Received SSE data line: {}", line);
        }
    }
}