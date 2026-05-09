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
package com.hojoai.webmvc.omniasst.meeting.util;

import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.meeting.api.ManagementServiceClient;
import com.hojoai.webmvc.omniasst.meeting.api.dto.AppendUploadResponse;
import com.hojoai.webmvc.omniasst.meeting.api.dto.PreSignUrlResponse;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 对象存储辅助：通过 management 预签名/追加上传，本地临时文件与 HTTP 拉流等。
 */
@Component
public class StorageUtil {
    private static final Logger logger = LoggerFactory.getLogger(StorageUtil.class);
    private static final String OSS_NEXT_POSITION_HEADER = "x-oss-next-append-position";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Value("${oss.aliyun.base-path:}")
    private String basePath;

    @Resource
    private ManagementServiceClient managementServiceClient;

    /**
     * 拼接会议侧默认 OSS 前缀（如 omniasst/voice）。
     * <p>若入参已是「完整对象键」则不再拼接，避免外部系统传入的 {@code omniasst/omim/...} 被错误拼成
     * {@code omniasst/voice/omniasst/omim/...} 导致下载 404。
     */
    private String buildObjectKey(String objectKey) {
        if (StringUtils.isBlank(objectKey)) {
            return objectKey;
        }
        String key = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        if (StringUtils.isBlank(basePath)) {
            return key;
        }
        String bp = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        if (key.startsWith(bp + "/") || key.equals(bp)) {
            return key;
        }
        // 同 Bucket 下其他业务根路径（如 omniasst/omim/...），与会议导入前缀 omniasst/voice 并列，不再拼接
        if (bp.startsWith("omniasst/") && key.startsWith("omniasst/") && !key.startsWith(bp + "/")) {
            return key;
        }
        return basePath.endsWith("/") ? basePath + key : basePath + "/" + key;
    }

    public long appendObject(String objectKey, byte[] data, long position) {
        String fullObjectKey = buildObjectKey(objectKey);
        logger.info("追加上传-开始 objectKey: {}, fullObjectKey: {}, position: {}, dataSize: {} bytes", objectKey, fullObjectKey, position, data.length);
        try {
            Response<AppendUploadResponse> response = managementServiceClient.generateAppendUploadUrl(fullObjectKey, position, "audio/pcm");
            AppendUploadResponse appendResponse = Objects.requireNonNull(response.getResContent(), "Append upload response is null");
            String appendUrl = Objects.requireNonNull(appendResponse.getAppendUrl(), "Append URL is null");
            logger.info("追加上传-获取预签名URL成功 objectKey: {}, fullObjectKey: {}, position: {}, appendUrl: {}", objectKey, fullObjectKey, position, appendUrl);

            // 注意：OSS AppendObject的预签名URL不支持Content-Type头
            // 如果生成预签名URL时没有设置Content-Type，实际请求时也不应该设置
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appendUrl))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> httpResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = httpResponse.statusCode();
            logger.info("追加上传-上传请求完成 objectKey: {}, fullObjectKey: {}, position: {}, statusCode: {}", objectKey, fullObjectKey, position, statusCode);
            
            if (statusCode < 200 || statusCode >= 300) {
                String errorBody = httpResponse.body();
                logger.error("追加上传-失败 objectKey: {}, fullObjectKey: {}, position: {}, statusCode: {}, errorResponse: {}", objectKey, fullObjectKey, position, statusCode, errorBody);
                throw new RuntimeException("Upload failed with status code: " + statusCode + ", response: " + errorBody);
            }
            
            String nextPositionStr = httpResponse.headers().firstValue(OSS_NEXT_POSITION_HEADER)
                    .orElse(httpResponse.headers().firstValue("X-Oss-Next-Append-Position").orElse(null));
            if (StringUtils.isBlank(nextPositionStr)) {
                logger.error("追加上传-失败 objectKey: {}, fullObjectKey: {}, position: {}, statusCode: {}, 响应头中未找到x-oss-next-append-position, responseHeaders: {}", 
                        objectKey, fullObjectKey, position, statusCode, httpResponse.headers().map());
                throw new RuntimeException("Failed to get next position from OSS response");
            }
            long nextPosition = Long.parseLong(nextPositionStr);
            logger.info("追加上传-成功 objectKey: {}, fullObjectKey: {}, position: {}, nextPosition: {}, dataSize: {} bytes, statusCode: {}", 
                    objectKey, fullObjectKey, position, nextPosition, data.length, statusCode);
            return nextPosition;
        } catch (Exception e) {
            logger.error("追加上传-异常 objectKey: {}, fullObjectKey: {}, position: {}, dataSize: {} bytes, error: {}", objectKey, fullObjectKey, position, data.length, e.getMessage(), e);
            throw new RuntimeException("Failed to append to OSS", e);
        }
    }

    public File downloadObject(String objectKey) {
        String fullObjectKey = buildObjectKey(objectKey);
        logger.info("下载-开始 objectKey: {}, fullObjectKey: {}", objectKey, fullObjectKey);
        try {
            Response<PreSignUrlResponse> response = managementServiceClient.getPreSignUrl(fullObjectKey, "", "");
            PreSignUrlResponse preSignResponse = Objects.requireNonNull(response.getResContent(), "PreSign URL response is null");
            String downloadUrl = Objects.requireNonNull(preSignResponse.getGetUrl(), "Download URL is null");
            logger.info("下载-获取下载URL成功 objectKey: {}, fullObjectKey: {}, downloadUrl: {}", objectKey, fullObjectKey, downloadUrl);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).GET().build();
            HttpResponse<InputStream> httpResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int statusCode = httpResponse.statusCode();
            logger.info("下载-请求完成 objectKey: {}, fullObjectKey: {}, statusCode: {}", objectKey, fullObjectKey, statusCode);
            
            if (statusCode < 200 || statusCode >= 300) {
                logger.error("下载-失败 objectKey: {}, fullObjectKey: {}, statusCode: {}, downloadUrl: {}", objectKey, fullObjectKey, statusCode, downloadUrl);
                throw new RuntimeException("Download failed with status code: " + statusCode);
            }

            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            String fileName = "oss_" + System.currentTimeMillis() + "_" + (fullObjectKey.contains("/") ? fullObjectKey.substring(fullObjectKey.lastIndexOf("/") + 1) : fullObjectKey);
            File tempFile = tempDir.resolve(fileName).toFile();

            long startTime = System.currentTimeMillis();
            try (InputStream inputStream = httpResponse.body(); FileOutputStream fos = new FileOutputStream(tempFile)) {
                long bytesTransferred = inputStream.transferTo(fos);
                long downloadTime = System.currentTimeMillis() - startTime;
                logger.info("下载-成功 objectKey: {}, fullObjectKey: {}, localFile: {}, fileSize: {} bytes, downloadTime: {} ms, downloadUrl: {}", 
                        objectKey, fullObjectKey, tempFile.getAbsolutePath(), bytesTransferred, downloadTime, downloadUrl);
            }
            long fileSize = tempFile.exists() ? tempFile.length() : 0;
            if (fileSize == 0) {
                logger.warn("下载-警告 objectKey: {}, fullObjectKey: {}, 下载的文件大小为0, localFile: {}", objectKey, fullObjectKey, tempFile.getAbsolutePath());
            }
            return tempFile;
        } catch (Exception e) {
            logger.error("下载-异常 objectKey: {}, fullObjectKey: {}, error: {}", objectKey, fullObjectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to download from OSS", e);
        }
    }

    /**
     * 获取直传 OSS 的预签名 PUT/GET（用于录音文件导入等整文件上传）。
     *
     * @param objectKey   业务对象键（如 meeting/xxx/import.mp3）
     * @param contentType 与上传时 Content-Type 一致，如 audio/mpeg
     */
    public PreSignUrlResponse getPresignedUpload(String objectKey, String contentType) {
        String fullObjectKey = buildObjectKey(objectKey);
        try {
            Response<PreSignUrlResponse> response = managementServiceClient.getPreSignUrl(fullObjectKey,
                    contentType != null ? contentType : "", "");
            PreSignUrlResponse preSignResponse = Objects.requireNonNull(response.getResContent(), "PreSign URL response is null");
            Objects.requireNonNull(preSignResponse.getPutUrl(), "Upload URL is null");
            logger.info("预签名上传-成功 objectKey: {}, fullObjectKey: {}", objectKey, fullObjectKey);
            return preSignResponse;
        } catch (Exception e) {
            logger.error("预签名上传-失败 objectKey: {}, fullObjectKey: {}", objectKey, fullObjectKey, e);
            throw new RuntimeException("Failed to get presigned upload URL", e);
        }
    }

    public String uploadFile(String objectKey, File file) {
        String fullObjectKey = buildObjectKey(objectKey);
        try {
            Response<PreSignUrlResponse> response = managementServiceClient.getPreSignUrl(fullObjectKey, "audio/wav", "");
            PreSignUrlResponse preSignResponse = Objects.requireNonNull(response.getResContent(), "PreSign URL response is null");
            String uploadUrl = Objects.requireNonNull(preSignResponse.getPutUrl(), "Upload URL is null");

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .header("Content-Type", "audio/wav")
                    .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            logger.info("File uploaded to OSS, objectKey: {}, fullObjectKey: {}", objectKey, fullObjectKey);
            return preSignResponse.getGetUrl();
        } catch (Exception e) {
            logger.error("Error uploading file to OSS, objectKey: {}, fullObjectKey: {}", objectKey, fullObjectKey, e);
            throw new RuntimeException("Failed to upload file to OSS", e);
        }
    }

    /**
     * 将完整 OSS 访问 URL 规范为对象键；若已是对象键则原样返回（经 {@link #extractObjectKeyFromUrl} 规则处理）。
     */
    public String normalizeToObjectKey(String ossUrlOrObjectKey) {
        if (StringUtils.isBlank(ossUrlOrObjectKey)) {
            return null;
        }
        return extractObjectKeyFromUrl(ossUrlOrObjectKey.trim());
    }

    public String getPresignedUrlForObjectKey(String objectKeyOrUrl) {
        if (StringUtils.isBlank(objectKeyOrUrl)) return null;
        String objectKey = extractObjectKeyFromUrl(objectKeyOrUrl);
        if (StringUtils.isBlank(objectKey)) return null;
        String fullObjectKey = buildObjectKey(objectKey);
        try {
            Response<PreSignUrlResponse> response = managementServiceClient.getPreSignUrl(fullObjectKey, "", "");
            PreSignUrlResponse preSignResponse = Objects.requireNonNull(response.getResContent(), "PreSign URL response is null");
            return preSignResponse.getGetUrl();
        } catch (Exception e) {
            logger.error("Error generating presigned URL, objectKey: {}", objectKey, e);
            return null;
        }
    }

    private String extractObjectKeyFromUrl(String ossUrl) {
        if (StringUtils.isBlank(ossUrl)) return null;
        if (!ossUrl.startsWith("http://") && !ossUrl.startsWith("https://")) return ossUrl;
        try {
            java.net.URL url = new java.net.URL(ossUrl);
            String path = url.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            if (path.contains("?")) path = path.substring(0, path.indexOf("?"));
            path = trimToObjectKeyPath(path);
            if (StringUtils.isNotBlank(basePath) && path.startsWith(basePath)) {
                path = path.substring(basePath.length());
                if (path.startsWith("/")) path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            logger.warn("Failed to parse OSS URL: {}", ossUrl, e);
            String path = roughPathFromUrlString(ossUrl);
            return trimToObjectKeyPath(path);
        }
    }

    /**
     * 去掉业务路径前多余前缀（如 omniasst/voice/meeting/... → meeting/...）。
     * 若对象键本身以 {@code meeting/} 开头（如 meeting/meeting-xxx/audio.wav），不可再按 {@code /meeting/} 截断，否则会丢段。
     */
    private static String trimToObjectKeyPath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        if (!path.startsWith("meeting/")) {
            int meetingIdx = path.indexOf("/meeting/");
            if (meetingIdx >= 0) {
                path = path.substring(meetingIdx + 1);
            }
        }
        return path;
    }

    private static String roughPathFromUrlString(String ossUrl) {
        String s = ossUrl;
        int q = s.indexOf('?');
        if (q >= 0) {
            s = s.substring(0, q);
        }
        int scheme = s.indexOf("://");
        if (scheme >= 0) {
            int pathStart = s.indexOf('/', scheme + 3);
            if (pathStart >= 0) {
                s = s.substring(pathStart + 1);
            }
        }
        return s;
    }
}

