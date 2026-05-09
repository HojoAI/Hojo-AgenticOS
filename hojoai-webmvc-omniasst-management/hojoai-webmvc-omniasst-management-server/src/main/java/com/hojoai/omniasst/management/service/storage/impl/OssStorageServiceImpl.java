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
package com.hojoai.omniasst.management.service.storage.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.configure.OssConfiguration;
import com.hojoai.omniasst.management.service.storage.StorageService;
import com.hojoai.omniasst.management.service.storage.bean.AppendUploadResponse;
import com.hojoai.omniasst.management.service.storage.bean.ExtendSignResponse;
import com.hojoai.omniasst.management.service.storage.bean.PreSignUrlResponse;
import com.hojoai.omniasst.management.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于阿里云 OSS 的预签名、追加上传等存储能力实现。
 */
@Service
public class OssStorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(OssStorageServiceImpl.class);

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final long EXPIRATION_TIME_MILLIS = 7L * 24 * 60 * 60 * 1000;

    @Resource
    private OssConfiguration ossConfiguration;

    @Override
    public PreSignUrlResponse generatePreSignUrl(String path, String contentType, String style) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(path), ResponseStatusBasic.ILLEGAL_ARGUMENT, "path cannot be blank");

        contentType = StringUtils.trimToEmpty(contentType);

        logger.info("Generate pre-sign URL, path: {}, keyName: {}", path, path);

        OSS ossClient = createOssClient(ossConfiguration.getAccessKeyId(), ossConfiguration.getAccessKeySecret(), ossConfiguration.getEndpoint());

        try {
            Date expiration = new Date(System.currentTimeMillis() + EXPIRATION_TIME_MILLIS);

            String putUrl = generatePutPreSignUrl(ossClient, ossConfiguration.getBucketName(), path, expiration, contentType);

            String getUrl = generateGetPreSignUrl(ossClient, ossConfiguration.getBucketName(), path, expiration, null);

            String resizeUrl = null;
            if (StringUtils.isNotBlank(style)) {
                resizeUrl = generateGetPreSignUrl(ossClient, ossConfiguration.getBucketName(), path, expiration, style);
            }

            String pureUrl = getUrl;
            int questionMarkIndex = getUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                pureUrl = getUrl.substring(0, questionMarkIndex);
            }

            PreSignUrlResponse response = new PreSignUrlResponse();
            response.setPath(path);
            response.setPutUrl(putUrl);
            response.setGetUrl(getUrl);
            response.setPureUrl(pureUrl);
            response.setResizeUrl(resizeUrl);

            logger.info("Generate pre-sign URL success, keyName: {}, putUrl: {}", path, putUrl);
            return response;
        } catch (Exception e) {
            logger.error("Generate pre-sign URL failed, path: {}, error: {}", path, e.getMessage(), e);
            throw new RuntimeException("Generate pre-sign URL failed: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public ExtendSignResponse extendSign(String path, String style) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(path), ResponseStatusBasic.ILLEGAL_ARGUMENT, "path cannot be blank");

        path = RegExUtils.removeFirst(StringUtils.trimToEmpty(path), "^/+");

        logger.info("Extend sign, path: {}", path);

        OSS ossClient = createOssClient(ossConfiguration.getAccessKeyId(), ossConfiguration.getAccessKeySecret(), ossConfiguration.getEndpoint());

        try {
            Date expiration = new Date(System.currentTimeMillis() + EXPIRATION_TIME_MILLIS);

            String getUrl = generateGetPreSignUrl(ossClient, ossConfiguration.getBucketName(), path, expiration, null);

            String resizeUrl = null;
            if (StringUtils.isNotBlank(style)) {
                resizeUrl = generateGetPreSignUrl(ossClient, ossConfiguration.getBucketName(), path, expiration, style);
            }

            ExtendSignResponse response = new ExtendSignResponse();
            response.setGetUrl(getUrl);
            response.setResizeUrl(resizeUrl);

            logger.info("Extend sign success, path: {}, getUrl: {}", path, getUrl);
            return response;
        } catch (Exception e) {
            logger.error("Extend sign failed, path: {}, error: {}", path, e.getMessage(), e);
            throw new RuntimeException("Extend sign failed: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

    private OSS createOssClient(String accessKeyId, String accessKeySecret, String endpoint) {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setProtocol(endpoint.startsWith("https://") ? Protocol.HTTPS : Protocol.HTTP);
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        String cleanEndpoint = endpoint.replace("https://", "").replace("http://", "");
        String region = extractRegionFromEndpoint(cleanEndpoint);

        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();
    }

    private String extractRegionFromEndpoint(String endpoint) {
        if (endpoint.contains("oss-cn-hangzhou")) return "cn-hangzhou";
        if (endpoint.contains("oss-cn-shanghai")) return "cn-shanghai";
        if (endpoint.contains("oss-cn-beijing")) return "cn-beijing";
        if (endpoint.contains("oss-cn-shenzhen")) return "cn-shenzhen";
        if (endpoint.contains("oss-cn-guangzhou")) return "cn-guangzhou";
        if (endpoint.contains("oss-cn-chengdu")) return "cn-chengdu";
        if (endpoint.contains("oss-cn-hongkong")) return "cn-hongkong";
        if (endpoint.contains("oss-us-west-1")) return "us-west-1";
        if (endpoint.contains("oss-us-east-1")) return "us-east-1";
        if (endpoint.contains("oss-ap-southeast-1")) return "ap-southeast-1";
        return "cn-hangzhou";
    }

    private String generatePutPreSignUrl(OSS ossClient, String bucketName, String keyName, Date expiration, String contentType) {
        GeneratePresignedUrlRequest putRequest = new GeneratePresignedUrlRequest(bucketName, keyName, HttpMethod.PUT);
        putRequest.setExpiration(expiration);

        if (StringUtils.isNotBlank(contentType)) {
            Map<String, String> headers = new HashMap<>();
            headers.put(CONTENT_TYPE_HEADER, contentType);
            putRequest.setHeaders(headers);
        }

        URL signedUrl = ossClient.generatePresignedUrl(putRequest);
        return signedUrl.toExternalForm();
    }

    private String generateGetPreSignUrl(OSS ossClient, String bucketName, String keyName, Date expiration, String style) {
        GeneratePresignedUrlRequest getRequest = new GeneratePresignedUrlRequest(bucketName, keyName, HttpMethod.GET);
        getRequest.setExpiration(expiration);
        if (StringUtils.isNotEmpty(style)) {
            getRequest.setProcess(style);
        }
        URL signedUrl = ossClient.generatePresignedUrl(getRequest);
        return signedUrl.toExternalForm();
    }

    @Override
    public AppendUploadResponse generateAppendUploadUrl(String path, Long position, String contentType) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(path), ResponseStatusBasic.ILLEGAL_ARGUMENT, "path cannot be blank");
        ThrowUtils.ThrowIf(position == null || position < 0, ResponseStatusBasic.ILLEGAL_ARGUMENT, "position must be >= 0");

        path = RegExUtils.removeFirst(StringUtils.trimToEmpty(path), "^/+");
        contentType = StringUtils.trimToEmpty(contentType);

        logger.info("追加上传-开始生成预签名URL path: {}, position: {}, contentType: {}", path, position, contentType);

        OSS ossClient = createOssClient(ossConfiguration.getAccessKeyId(), ossConfiguration.getAccessKeySecret(), ossConfiguration.getEndpoint());

        try {
            Date expiration = new Date(System.currentTimeMillis() + EXPIRATION_TIME_MILLIS);

            // 生成AppendObject的预签名URL
            // AppendObject使用POST方法，需要position参数
            // 注意：查询参数顺序很重要，append必须在position之前
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(ossConfiguration.getBucketName(), path, HttpMethod.POST);
            request.setExpiration(expiration);
            request.addQueryParameter("append", ""); // AppendObject的标识，必须在position之前
            request.addQueryParameter("position", String.valueOf(position)); // 追加位置
            // 注意：OSS AppendObject的预签名URL不支持在签名中包含Content-Type头
            // 如果设置Content-Type头会导致签名验证失败，所以不设置Content-Type

            URL signedUrl = ossClient.generatePresignedUrl(request);
            String appendUrl = signedUrl.toExternalForm();
            logger.info("追加上传-生成预签名URL成功 path: {}, position: {}, appendUrl: {}", path, position, appendUrl);

            AppendUploadResponse response = new AppendUploadResponse();
            response.setPath(path);
            response.setAppendUrl(appendUrl);
            // 注意：nextPosition需要在上传后从响应头中获取，这里无法预知
            // 客户端上传后需要从响应头中读取 x-oss-next-append-position 来获取下次上传位置
            response.setNextPosition(position); // 临时值，实际需要从上传响应中获取
            response.setContentLength(position); // 当前文件长度（如果position=0表示首次上传）

            logger.info("追加上传-生成预签名URL完成 path: {}, position: {}, bucketName: {}", path, position, ossConfiguration.getBucketName());
            return response;
        } catch (Exception e) {
            logger.error("追加上传-生成预签名URL失败 path: {}, position: {}, error: {}", path, position, e.getMessage(), e);
            throw new RuntimeException("Generate append upload URL failed: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

}

