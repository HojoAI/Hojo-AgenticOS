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
package com.hojoai.webmvc.omniasst.music.util;

import com.hojoai.webmvc.omniasst.music.service.netease.dto.NeteaseCloudCommonRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 网易云音乐签名工具类
 * 
 * @author shengxianfei
 * @since 2026-01-13
 */
@Component
public class NetEaseCloudSignUtil {
    
    /** 签名算法：RSA + SHA256 */
    private static final String SIGN_ALGORITHM = "SHA256WithRSA";
    
    /** 密钥算法名称 */
    private static final String KEY_ALGORITHM = "RSA";
    
    @Value("${netease.cloud.private.key}")
    private String privateKey;
    
    /**
     * RSA SHA256签名
     * 
     * @param request 请求参数
     * @return 签名字符串
     * @throws Exception 签名失败
     */
    public String rsa256Sign(NeteaseCloudCommonRequest request) throws Exception {
        if (Objects.isNull(request)) {
            throw new IllegalArgumentException("request must not be null");
        }
        
        String content = this.getSignCheckContent(request);
        
        if (Objects.isNull(content) || content.isBlank()) {
            throw new IllegalArgumentException("sign content must not be empty");
        }
        
        if (Objects.isNull(privateKey) || privateKey.isBlank()) {
            throw new IllegalStateException("private key must be configured");
        }
        
        PrivateKey priKey;
        try (InputStream keyStream = new ByteArrayInputStream(privateKey.getBytes(StandardCharsets.UTF_8))) {
            priKey = this.getPrivateKeyFromPKCS8(KEY_ALGORITHM, keyStream);
        }
        
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        
        byte[] signed = signature.sign();
        return Base64.encodeBase64String(signed);
    }
    
    /**
     * 从PKCS8格式获取私钥
     */
    private PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null) {
            throw new IllegalArgumentException("private key input stream is null");
        }
        
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        
        // 读取私钥内容
        byte[] keyBytes = ins.readAllBytes();
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        
        // Base64解码
        byte[] encodedKey = Base64.decodeBase64(key);
        
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }
    
    /**
     * 获取待签名内容
     * 将请求参数按key排序后拼接成字符串
     */
    private String getSignCheckContent(NeteaseCloudCommonRequest request) throws Exception {
        Map<String, String> requestMap = BeanUtils.describe(request);
        
        if (MapUtils.isEmpty(requestMap)) {
            return null;
        }
        
        // 移除sign字段（sign字段本身不参与签名）
        requestMap.remove("sign");
        // 移除class字段（BeanUtils.describe会自动添加）
        requestMap.remove("class");
        
        // 过滤null值和空字符串，并排序
        List<String> keys = requestMap.keySet().stream()
                .filter(k -> requestMap.get(k) != null && !requestMap.get(k).isEmpty())
                .sorted()
                .toList();
        
        StringBuilder content = new StringBuilder();
        
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = requestMap.get(key);
            content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
        }
        
        System.out.println("【签名内容】: " + content.toString());
        return content.toString();
    }
}
