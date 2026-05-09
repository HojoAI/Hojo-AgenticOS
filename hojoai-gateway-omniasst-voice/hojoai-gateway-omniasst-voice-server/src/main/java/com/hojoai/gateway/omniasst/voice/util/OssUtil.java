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
package com.hojoai.gateway.omniasst.voice.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * 阿里云 OSS 上传、下载与追加上传工具。
 * 配置项来自 {@code oss.aliyun.*}，支持返回预签名 URL 或拼接公网访问地址。
 */
@Component
public class OssUtil {
    private static final Logger logger = LoggerFactory.getLogger(OssUtil.class);

    /** OSS Endpoint，如 oss-cn-xxx.aliyuncs.com */
    @Value("${oss.aliyun.endpoint:}")
    private String endpoint;

    /** AccessKey Id */
    @Value("${oss.aliyun.access-key-id:}")
    private String accessKeyId;

    /** AccessKey Secret */
    @Value("${oss.aliyun.access-key-secret:}")
    private String accessKeySecret;

    /** 存储空间名称 */
    @Value("${oss.aliyun.bucket-name:}")
    private String bucketName;

    /** 对象键统一前缀（可选），不以 / 结尾时会自动补全 */
    @Value("${oss.aliyun.base-path:}")
    private String basePath;

    /** 上传成功后是否生成 HTTPS 预签名 URL */
    @Value("${oss.aliyun.generate-presigned-url:true}")
    private boolean generatePresignedUrl;

    /** 预签名 URL 有效期（小时） */
    @Value("${oss.aliyun.presigned-url-expiration-hours:24}")
    private int presignedUrlExpirationHours;

    private OSS createOssClient() {
        if (StringUtils.isAnyBlank(endpoint, accessKeyId, accessKeySecret)) {
            throw new IllegalStateException("OSS configuration is incomplete");
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 上传本地文件到 OSS。
     *
     * @param objectKey 逻辑对象键（不含 basePath 时由配置拼接）
     * @param file      本地文件
     * @return 预签名 URL 或公网 URL
     * @throws RuntimeException 上传失败
     */
    public String uploadFile(String objectKey, File file) {
        OSS ossClient = createOssClient();
        try {
            String fullObjectKey = buildObjectKey(objectKey);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fullObjectKey, file);
            ossClient.putObject(putObjectRequest);
            logger.info("File uploaded to OSS, objectKey: {}", fullObjectKey);
            if (generatePresignedUrl) {
                return generatePresignedUrl(fullObjectKey);
            } else {
                return buildPublicUrl(fullObjectKey);
            }
        } catch (Exception e) {
            logger.error("Error uploading file to OSS, objectKey: {}", objectKey, e);
            throw new RuntimeException("Failed to upload file to OSS", e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 以流形式上传到 OSS，需指定内容长度。
     *
     * @param objectKey      逻辑对象键
     * @param inputStream    对象数据流
     * @param contentLength  字节长度
     * @return 预签名 URL 或公网 URL
     * @throws RuntimeException 上传失败
     */
    public String uploadFile(String objectKey, InputStream inputStream, long contentLength) {
        OSS ossClient = createOssClient();
        try {
            String fullObjectKey = buildObjectKey(objectKey);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fullObjectKey, inputStream);
            putObjectRequest.getMetadata().setContentLength(contentLength);
            ossClient.putObject(putObjectRequest);
            logger.info("File uploaded to OSS, objectKey: {}", fullObjectKey);
            if (generatePresignedUrl) {
                return generatePresignedUrl(fullObjectKey);
            } else {
                return buildPublicUrl(fullObjectKey);
            }
        } catch (Exception e) {
            logger.error("Error uploading file to OSS, objectKey: {}", objectKey, e);
            throw new RuntimeException("Failed to upload file to OSS", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private String buildObjectKey(String objectKey) {
        if (StringUtils.isNotBlank(basePath)) {
            return basePath.endsWith("/") ? basePath + objectKey : basePath + "/" + objectKey;
        }
        return objectKey;
    }

    private String generatePresignedUrl(String objectKey) {
        OSS ossClient = createOssClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + presignedUrlExpirationHours * 3600 * 1000L);
            URL url = ossClient.generatePresignedUrl(bucketName, objectKey, expiration);
            String urlString = url.toString();
            if (urlString.startsWith("http://")) {
                urlString = urlString.replaceFirst("http://", "https://");
                logger.debug("Converted presigned URL from HTTP to HTTPS, objectKey: {}", objectKey);
            }
            return urlString;
        } catch (Exception e) {
            logger.error("Error generating presigned URL, objectKey: {}", objectKey, e);
            return buildPublicUrl(objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    private String buildPublicUrl(String objectKey) {
        return "https://" + bucketName + "." + endpoint + "/" + objectKey;
    }

    /**
     * 根据完整 OSS URL 下载对象到指定本地文件。
     *
     * @param ossUrl     可访问的 OSS 地址
     * @param outputFile 写入目标
     * @return outputFile
     * @throws IllegalArgumentException URL 无法解析出 objectKey
     * @throws Exception                下载或 IO 异常
     */
    public File downloadFile(String ossUrl, File outputFile) throws Exception {
        OSS ossClient = createOssClient();
        try {
            String objectKey = extractObjectKeyFromUrl(ossUrl);
            if (StringUtils.isBlank(objectKey)) {
                throw new IllegalArgumentException("Invalid OSS URL: " + ossUrl);
            }
            ossClient.getObject(new GetObjectRequest(bucketName, objectKey), outputFile);
            logger.info("File downloaded from OSS, objectKey: {}, output: {}", objectKey, outputFile.getAbsolutePath());
            return outputFile;
        } catch (Exception e) {
            logger.error("Error downloading file from OSS, ossUrl: {}", ossUrl, e);
            throw new RuntimeException("Failed to download file from OSS", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private String extractObjectKeyFromUrl(String ossUrl) {
        if (StringUtils.isBlank(ossUrl)) return null;
        try {
            URL url = new URL(ossUrl);
            String path = url.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            if (path.startsWith(bucketName + "/")) path = path.substring(bucketName.length() + 1);
            return path;
        } catch (Exception e) {
            logger.warn("Failed to parse OSS URL: {}", ossUrl, e);
            if (ossUrl.contains(bucketName)) {
                int idx = ossUrl.indexOf(bucketName) + bucketName.length() + 1;
                if (idx < ossUrl.length()) return ossUrl.substring(idx);
            }
            return ossUrl;
        }
    }

    /**
     * 追加数据到OSS对象
     * 
     * @param objectKey OSS对象key
     * @param data 要追加的数据
     * @param position 追加位置（首次追加时使用0，后续追加使用上次返回的nextPosition）
     * @return 下次追加的位置（nextPosition），如果失败返回-1
     */
    public long appendObject(String objectKey, byte[] data, long position) {
        OSS ossClient = createOssClient();
        try {
            String fullObjectKey = buildObjectKey(objectKey);
            AppendObjectRequest appendRequest = new AppendObjectRequest(
                    bucketName,
                    fullObjectKey,
                    new ByteArrayInputStream(data)
            );
            appendRequest.setPosition(position);
            
            AppendObjectResult result = ossClient.appendObject(appendRequest);
            long nextPosition = result.getNextPosition();
            logger.debug("Appended to OSS, objectKey: {}, position: {}, nextPosition: {}, dataSize: {}", 
                    fullObjectKey, position, nextPosition, data.length);
            return nextPosition;
        } catch (Exception e) {
            logger.error("Error appending to OSS, objectKey: {}, position: {}", objectKey, position, e);
            throw new RuntimeException("Failed to append to OSS", e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从OSS下载对象到本地文件
     * 
     * @param objectKey OSS对象key
     * @return 下载的本地文件
     */
    public File downloadObject(String objectKey) {
        OSS ossClient = createOssClient();
        try {
            String fullObjectKey = buildObjectKey(objectKey);
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            String fileName = "oss_" + System.currentTimeMillis() + "_" + 
                    (objectKey.contains("/") ? objectKey.substring(objectKey.lastIndexOf("/") + 1) : objectKey);
            File tempFile = tempDir.resolve(fileName).toFile();
            
            GetObjectRequest getRequest = new GetObjectRequest(bucketName, fullObjectKey);
            ossClient.getObject(getRequest, tempFile);
            logger.info("Downloaded from OSS, objectKey: {}, localFile: {}", fullObjectKey, tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            logger.error("Error downloading from OSS, objectKey: {}", objectKey, e);
            throw new RuntimeException("Failed to download from OSS", e);
        } finally {
            ossClient.shutdown();
        }
    }
}


