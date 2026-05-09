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
package com.hojoai.omniasst.management.service.storage.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 音频流上传客户端
 * 用于流式传输PCM二进制流到OSS
 * 
 * 使用示例：
 * <pre>
 * AudioStreamUploadClient client = new AudioStreamUploadClient("http://your-service-host");
 * 
 * // 开始上传
 * long position = 0;
 * 
 * // 上传第一个PCM片段
 * byte[] pcmData1 = ...; // 第一个PCM片段
 * position = client.appendAudioSegment("audio/recording.pcm", position, pcmData1, "audio/pcm");
 * 
 * // 上传第二个PCM片段
 * byte[] pcmData2 = ...; // 第二个PCM片段
 * position = client.appendAudioSegment("audio/recording.pcm", position, pcmData2, "audio/pcm");
 * 
 * // 继续上传更多片段...
 * </pre>
 */
public class AudioStreamUploadClient {

    private static final Logger logger = LoggerFactory.getLogger(AudioStreamUploadClient.class);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param baseUrl 管理服务的Base URL，例如：http://localhost:8080
     */
    public AudioStreamUploadClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 追加音频片段到OSS
     *
     * @param path        文件路径，例如：audio/recording.pcm
     * @param position    追加上传位置（首次上传传0，后续使用上次返回的position）
     * @param pcmData     PCM二进制数据
     * @param contentType 内容类型，建议使用 "audio/pcm" 或 "application/octet-stream"
     * @return 下次上传的position（从响应头 x-oss-next-append-position 获取）
     * @throws IOException 如果上传失败
     */
    public long appendAudioSegment(String path, long position, byte[] pcmData, String contentType) throws IOException {
        try {
            // 1. 获取预签名URL
            AppendUploadResponse response = getAppendUploadUrl(path, position, contentType);
            String appendUrl = response.getAppendUrl();
            
            logger.debug("Got append URL for path: {}, position: {}", path, position);

            // 2. 使用预签名URL上传PCM数据
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(appendUrl))
                    .method("POST", HttpRequest.BodyPublishers.ofByteArray(pcmData))
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> uploadResponse = httpClient.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

            // 3. 检查上传是否成功
            int statusCode = uploadResponse.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("Upload failed with status code: " + statusCode + ", response: " + uploadResponse.body());
            }

            // 4. 从响应头获取下次上传位置
            String nextPositionHeader = uploadResponse.headers().firstValue("x-oss-next-append-position")
                    .orElse(uploadResponse.headers().firstValue("X-Oss-Next-Append-Position").orElse(null));

            if (nextPositionHeader == null) {
                // 如果没有响应头，使用当前position + 数据长度作为估算
                long estimatedNextPosition = position + pcmData.length;
                logger.warn("x-oss-next-append-position header not found, using estimated position: {}", estimatedNextPosition);
                return estimatedNextPosition;
            }

            long nextPosition = Long.parseLong(nextPositionHeader);
            logger.debug("Upload successful, next position: {}", nextPosition);
            return nextPosition;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    /**
     * 从InputStream流式上传PCM数据
     * 适合处理大文件或连续流
     *
     * @param path        文件路径
     * @param inputStream PCM数据输入流
     * @param contentType 内容类型
     * @param chunkSize   每次上传的块大小（字节），建议 64KB - 1MB
     * @return 最终文件的总字节数
     * @throws IOException 如果上传失败
     */
    public long appendAudioStream(String path, InputStream inputStream, String contentType, int chunkSize) throws IOException {
        long position = 0;
        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] chunk;
            if (bytesRead == buffer.length) {
                chunk = buffer;
            } else {
                // 如果读取的数据少于buffer大小，创建新数组
                chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            }

            position = appendAudioSegment(path, position, chunk, contentType);
            logger.debug("Uploaded chunk, size: {}, next position: {}", bytesRead, position);
        }

        return position;
    }

    /**
     * 获取追加上传的预签名URL
     *
     * @param path        文件路径
     * @param position    追加上传位置
     * @param contentType 内容类型
     * @return 预签名URL响应
     * @throws IOException 如果获取失败
     */
    private AppendUploadResponse getAppendUploadUrl(String path, long position, String contentType) throws IOException {
        try {
            String url = String.format("%s/v0/storage/append-upload?path=%s&position=%d&contentType=%s",
                    baseUrl, encodePath(path), position, encodeContentType(contentType));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to get append upload URL, status: " + response.statusCode() + ", response: " + response.body());
            }

            // 解析响应
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            
            // 检查响应结构（可能是包装在Response对象中）
            Object data = responseMap.get("data");
            if (data == null) {
                data = responseMap; // 如果没有data字段，直接使用整个响应
            }

            AppendUploadResponse appendResponse = objectMapper.convertValue(data, AppendUploadResponse.class);
            return appendResponse;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    /**
     * URL编码路径
     */
    private String encodePath(String path) {
        try {
            return java.net.URLEncoder.encode(path, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * URL编码ContentType
     */
    private String encodeContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return "";
        }
        try {
            return java.net.URLEncoder.encode(contentType, "UTF-8");
        } catch (Exception e) {
            return contentType;
        }
    }

    /**
     * 响应对象
     */
    public static class AppendUploadResponse {
        private String path;
        private String appendUrl;
        private Long nextPosition;
        private Long contentLength;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAppendUrl() {
            return appendUrl;
        }

        public void setAppendUrl(String appendUrl) {
            this.appendUrl = appendUrl;
        }

        public Long getNextPosition() {
            return nextPosition;
        }

        public void setNextPosition(Long nextPosition) {
            this.nextPosition = nextPosition;
        }

        public Long getContentLength() {
            return contentLength;
        }

        public void setContentLength(Long contentLength) {
            this.contentLength = contentLength;
        }
    }
}

