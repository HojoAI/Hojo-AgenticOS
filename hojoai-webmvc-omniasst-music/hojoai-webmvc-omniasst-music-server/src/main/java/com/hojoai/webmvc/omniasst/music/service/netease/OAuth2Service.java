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
package com.hojoai.webmvc.omniasst.music.service.netease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.*;
import com.hojoai.webmvc.omniasst.music.util.NetEaseCloudSignUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2授权服务
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
@Service
public class OAuth2Service {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);
    
    @Resource
    private NeteaseCloudMusicAPI neteaseCloudMusicAPI;
    
    @Resource
    private NetEaseCloudSignUtil signUtil;
    
    @Resource
    private RedissonClient redissonClient;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Value("${netease.cloud.app-id}")
    private String appId;
    
    @Value("${netease.cloud.app-secret}")
    private String appSecret;
    
    @Value("${netease.cloud.oauth.callback-url:http://localhost:12009/omniasst-music/oauth2/callback}")
    private String callbackUrl;
    
    @Value("${netease.cloud.oauth.auth-page-url:https://music.163.com/st/platform/oauth/authorize}")
    private String authPageUrl;
    
    @Value("${netease.cloud.oauth.state-encrypt-key}")
    private String stateEncryptKey;
    
    /** 网易云开放请求签名类型 */
    private static final String SIGN_TYPE = "RSA_SHA256";
    
    /** Redis 中用户 OAuth Token 的 key 前缀 */
    private static final String REDIS_KEY_USER_TOKEN_PREFIX = "netease:oauth2:user:token:";
    
    /** OAuth state 明文有效期（毫秒），默认 10 分钟 */
    private static final long STATE_VALID_DURATION = 10 * 60 * 1000; // state有效期10分钟
    
    @PostConstruct
    void validateStateEncryptKey() {
        if (StringUtils.isBlank(stateEncryptKey)) {
            throw new IllegalStateException("netease.cloud.oauth.state-encrypt-key must be configured");
        }
    }
    
    /** 本地记录的 accessToken 过期时间（秒）：7 天减 1 小时，略短于平台实际有效期 */
    private static final long ACCESS_TOKEN_EXPIRE_SECONDS = 6 * 24 * 60 * 60 + 23 * 60 * 60;
    
    /** 本地记录的 refreshToken 过期时间（秒）：20 天，与 Redis TTL 一致 */
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 20 * 24 * 60 * 60;
    
    /**
     * 生成网易云 OAuth 授权页 URL（可 H5 或 orpheus 唤端），并生成加密 state。
     *
     * @param request 含 userId、clientType，可选 schemaUrl、hojoRedirectUrl
     * @return 成功时返回授权链接与 state；失败时 resCode 非 200
     */
    public Response<GenerateAuthUrlResponse> generateAuthUrl(GenerateAuthUrlRequest request) {
        logger.info("生成授权URL请求: userId={}, clientType={}, schemaUrl={}", 
                request.getUserId(), request.getClientType(), request.getSchemaUrl());
        
        try {
            // 生成state（包含userId + 随机串 + 过期时间）
            String state = generateState(request.getUserId());
            
            // 构建回调URL，如果有hojoRedirectUrl则添加到参数中
            StringBuilder callbackUrlBuilder = new StringBuilder(callbackUrl);
            if (StringUtils.isNotBlank(request.getHojoRedirectUrl())) {
                callbackUrlBuilder.append("?hojoRedirectUrl=").append(URLEncoder.encode(request.getHojoRedirectUrl(), StandardCharsets.UTF_8.name()));
            }
            String finalCallbackUrl = callbackUrlBuilder.toString();
            
            // 构建H5授权页URL
            StringBuilder h5UrlBuilder = new StringBuilder(authPageUrl);
            h5UrlBuilder.append("?clientId=").append(appId);
            h5UrlBuilder.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.name()));
            h5UrlBuilder.append("&clientType=").append(request.getClientType());
            h5UrlBuilder.append("&redirectUrl=").append(URLEncoder.encode(finalCallbackUrl, StandardCharsets.UTF_8.name()));
            
            // 如果提供了schemaUrl，添加到URL中
            if (StringUtils.isNotBlank(request.getSchemaUrl())) {
                h5UrlBuilder.append("&schemaUrl=").append(URLEncoder.encode(request.getSchemaUrl(), StandardCharsets.UTF_8.name()));
            }
            
            String h5AuthUrl = h5UrlBuilder.toString();
            
            // 如果提供了schemaUrl，需要包装成 orpheus:// 协议（唤端方式）
            String authUrl;
            if (StringUtils.isNotBlank(request.getSchemaUrl())) {
                // 唤端授权页格式：orpheus://openurl?url=encode(授权页URL)
                authUrl = "orpheus://openurl?url=" + URLEncoder.encode(h5AuthUrl, StandardCharsets.UTF_8.name());
                logger.debug("构建唤端授权URL: {}", authUrl);
            } else {
                // H5授权方式
                authUrl = h5AuthUrl;
            }
            
            GenerateAuthUrlResponse response = new GenerateAuthUrlResponse();
            response.setAuthUrl(authUrl);
            response.setState(state);
            
            logger.info("授权URL生成成功: userId={}, authUrl={}", request.getUserId(), authUrl);
            return Response.newInstance("200", "成功", response);
            
        } catch (Exception e) {
            logger.error("生成授权URL失败", e);
            return Response.newInstance("500", "生成授权URL失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 处理 OAuth 回调：校验 state、用 code 换 token、写入 Redis。
     *
     * @param request 含 code、state，可选 hojoRedirectUrl（由 Controller 决定返回 HTML 或 JSON）
     * @return 成功时返回用户 token 摘要；state 无效或换票失败时 resCode 非 200
     */
    public Response<UserTokenData> handleCallback(OAuth2CallbackRequest request) {
        logger.info("处理OAuth回调: code={}, state={}", 
                request.getCode() != null ? request.getCode().substring(0, Math.min(10, request.getCode().length())) + "..." : null, 
                request.getState() != null ? request.getState().substring(0, Math.min(10, request.getState().length())) + "..." : null);
        
        try {
            // 验证state并提取userId
            String userId = validateAndExtractUserId(request.getState());
            if (StringUtils.isBlank(userId)) {
                return Response.newInstance("400", "state无效或已过期", null);
            }
            
            logger.info("state验证成功，提取到userId: {}", userId);
            
            // 用code兑换token
            OAuth2TokenData tokenData = exchangeToken(request.getCode());
            
            // 存储token到Redis
            UserTokenData userTokenData = saveUserToken(userId, tokenData);
            
            logger.info("OAuth回调处理成功: userId={}, openId={}", userId, tokenData.getOpenId());
            return Response.newInstance("200", "授权成功", userTokenData);
            
        } catch (Exception e) {
            logger.error("处理OAuth回调失败", e);
            return Response.newInstance("500", "授权失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 从 Redis 读取用户 OAuth Token（不做刷新）。
     *
     * @param userId 业务用户 ID
     * @return 已存储的 token，未授权时为 null
     */
    public UserTokenData getUserToken(String userId) {
        String redisKey = REDIS_KEY_USER_TOKEN_PREFIX + userId;
        RBucket<UserTokenData> bucket = redissonClient.getBucket(redisKey);
        UserTokenData tokenData = bucket.get();
        
        if (tokenData != null) {
            logger.debug("从Redis获取用户token: userId={}", userId);
        } else {
            logger.debug("Redis中不存在用户token: userId={}", userId);
        }
        
        return tokenData;
    }
    
    /**
     * 使用 refreshToken 刷新 accessToken 并更新 Redis。
     *
     * @param userId 业务用户 ID
     * @return 成功返回新 token；不存在或 refresh 过期时返回 404/401 等
     */
    public Response<UserTokenData> refreshUserToken(String userId) {
        logger.info("刷新用户token: userId={}", userId);
        
        try {
            // 从Redis获取现有token
            UserTokenData existingToken = getUserToken(userId);
            if (existingToken == null) {
                return Response.newInstance("404", "用户token不存在，请重新登录", null);
            }
            
            // 检查refreshToken是否过期
            long currentTime = System.currentTimeMillis();
            if (existingToken.getRefreshTokenExpireTime() != null && 
                currentTime > existingToken.getRefreshTokenExpireTime()) {
                logger.warn("refreshToken已过期，需要重新登录: userId={}", userId);
                return Response.newInstance("401", "refreshToken已过期，请重新登录", null);
            }
            
            // 调用刷新token接口
            OAuth2TokenData newTokenData = callRefreshTokenAPI(existingToken.getRefreshToken());
            
            // 更新Redis中的token
            UserTokenData updatedTokenData = saveUserToken(userId, newTokenData);
            
            logger.info("刷新token成功: userId={}", userId);
            return Response.newInstance("200", "刷新成功", updatedTokenData);
            
        } catch (Exception e) {
            logger.error("刷新token失败: userId={}", userId, e);
            return Response.newInstance("500", "刷新token失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 查询用户是否已完成网易云授权且 refreshToken 未过期。
     *
     * @param userId 业务用户 ID
     * @return authorized 及 openId、unionId（已授权时）
     */
    public Response<AuthStatusResponse> getAuthStatus(String userId) {
        logger.info("查询授权状态: userId={}", userId);
        
        try {
            UserTokenData tokenData = getUserToken(userId);
            AuthStatusResponse response = new AuthStatusResponse();
            
            if (tokenData == null || StringUtils.isBlank(tokenData.getRefreshToken())) {
                response.setAuthorized(false);
                logger.info("用户未授权: userId={}", userId);
            } else {
                // 检查refreshToken是否过期
                long currentTime = System.currentTimeMillis();
                if (tokenData.getRefreshTokenExpireTime() != null && 
                    currentTime > tokenData.getRefreshTokenExpireTime()) {
                    response.setAuthorized(false);
                    logger.info("用户授权已过期: userId={}", userId);
                } else {
                    response.setAuthorized(true);
                    response.setOpenId(tokenData.getOpenId());
                    response.setUnionId(tokenData.getUnionId());
                    logger.info("用户已授权: userId={}, openId={}", userId, tokenData.getOpenId());
                }
            }
            
            return Response.newInstance("200", "成功", response);
            
        } catch (Exception e) {
            logger.error("查询授权状态失败: userId={}", userId, e);
            return Response.newInstance("500", "查询失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 删除 Redis 中的用户 token，即解除网易云绑定。
     *
     * @param userId 业务用户 ID
     * @return 操作结果，成功 resCode 为 200
     */
    public Response<Void> revokeAuth(String userId) {
        logger.info("取消授权: userId={}", userId);
        
        try {
            String redisKey = REDIS_KEY_USER_TOKEN_PREFIX + userId;
            RBucket<UserTokenData> bucket = redissonClient.getBucket(redisKey);
            
            if (bucket.isExists()) {
                bucket.delete();
                logger.info("授权已取消，token已删除: userId={}", userId);
            } else {
                logger.info("用户token不存在，无需删除: userId={}", userId);
            }
            
            return Response.newInstance("200", "取消授权成功", null);
            
        } catch (Exception e) {
            logger.error("取消授权失败: userId={}", userId, e);
            return Response.newInstance("500", "取消授权失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 生成state（包含userId + 随机串 + 过期时间，AES加密后Base64编码）
     */
    private String generateState(String userId) throws Exception {
        long expireTime = System.currentTimeMillis() + STATE_VALID_DURATION;
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        // 格式：userId|randomStr|expireTime
        String plainState = userId + "|" + randomStr + "|" + expireTime;
        
        // AES加密
        SecretKeySpec keySpec = new SecretKeySpec(stateEncryptKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainState.getBytes(StandardCharsets.UTF_8));
        
        // Base64编码
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * 验证state并提取userId
     */
    private String validateAndExtractUserId(String state) {
        try {
            // 处理URL传输过程中可能出现的空格问题
            // Base64字符串中的'+'在URL中可能被解释为空格，需要还原
            // 同时处理可能的换行符和其他空白字符
            String cleanedState = state.replace(' ', '+').replaceAll("\\s+", "");
            
            // Base64解码
            byte[] encrypted = Base64.getDecoder().decode(cleanedState);
            
            // AES解密
            SecretKeySpec keySpec = new SecretKeySpec(stateEncryptKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            String plainState = new String(decrypted, StandardCharsets.UTF_8);
            String[] parts = plainState.split("\\|");
            
            if (parts.length != 3) {
                logger.error("state格式错误: {}", plainState);
                return null;
            }
            
            String userId = parts[0];
            long expireTime = Long.parseLong(parts[2]);
            
            // 检查是否过期
            if (System.currentTimeMillis() > expireTime) {
                logger.error("state已过期: userId={}, expireTime={}", userId, expireTime);
                return null;
            }
            
            return userId;
            
        } catch (Exception e) {
            logger.error("state验证失败", e);
            return null;
        }
    }
    
    /**
     * 用code兑换token
     */
    private OAuth2TokenData exchangeToken(String code) throws Exception {
        logger.info("用code兑换token");
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        
        Map<String, String> bizContentMap = new HashMap<>();
        bizContentMap.put("grantCode", code);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        // 生成签名
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<OAuth2TokenData> response = neteaseCloudMusicAPI.getTokenByCode(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("兑换token失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        logger.info("兑换token成功: openId={}", response.getData().getOpenId());
        return response.getData();
    }
    
    /**
     * 调用刷新token API
     */
    private OAuth2TokenData callRefreshTokenAPI(String refreshToken) throws Exception {
        logger.info("调用刷新token API");
        
        NeteaseCloudCommonRequest commonRequest = buildCommonRequest();
        
        Map<String, String> bizContentMap = new HashMap<>();
        bizContentMap.put("clientId", appId);
        bizContentMap.put("clientSecret", appSecret);
        bizContentMap.put("refreshToken", refreshToken);
        commonRequest.setBizContent(objectMapper.writeValueAsString(bizContentMap));
        
        // 生成签名
        String sign = signUtil.rsa256Sign(commonRequest);
        commonRequest.setSign(sign);
        
        NeteaseApiResponse<OAuth2TokenData> response = neteaseCloudMusicAPI.refreshToken(
                commonRequest.getAppId(),
                commonRequest.getSignType(),
                commonRequest.getSign(),
                commonRequest.getTimestamp(),
                commonRequest.getBizContent(),
                commonRequest.getEncryptType(),
                commonRequest.getDevice());
        
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            throw new RuntimeException("刷新token失败: " + (response != null ? response.getMessage() : "响应为空"));
        }
        
        logger.info("刷新token成功");
        return response.getData();
    }
    
    /**
     * 保存用户token到Redis
     */
    private UserTokenData saveUserToken(String userId, OAuth2TokenData tokenData) {
        long currentTime = System.currentTimeMillis();
        
        UserTokenData userTokenData = new UserTokenData();
        userTokenData.setUserId(userId);
        userTokenData.setAccessToken(tokenData.getAccessToken());
        userTokenData.setRefreshToken(tokenData.getRefreshToken());
        userTokenData.setOpenId(tokenData.getOpenId());
        userTokenData.setUnionId(tokenData.getUnionId());
        userTokenData.setCreateTime(currentTime);
        
        // 计算过期时间
        // accessToken: 6天23小时后过期
        userTokenData.setAccessTokenExpireTime(currentTime + ACCESS_TOKEN_EXPIRE_SECONDS * 1000);
        // refreshToken: 20天后过期
        userTokenData.setRefreshTokenExpireTime(currentTime + REFRESH_TOKEN_EXPIRE_SECONDS * 1000);
        
        // 存储到Redis，设置过期时间为20天（以refreshToken的过期时间为准）
        String redisKey = REDIS_KEY_USER_TOKEN_PREFIX + userId;
        RBucket<UserTokenData> bucket = redissonClient.getBucket(redisKey);
        bucket.set(userTokenData, REFRESH_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        logger.info("用户token已存储到Redis: userId={}, accessTokenExpire={}, refreshTokenExpire={}", 
                userId, 
                new Date(userTokenData.getAccessTokenExpireTime()), 
                new Date(userTokenData.getRefreshTokenExpireTime()));
        
        return userTokenData;
    }
    
    /**
     * 构建带 JS 跳转的 HTML 页面，将授权结果拼入 hojoRedirectUrl 的 query。
     *
     * @param hojoRedirectUrl 前端回跳地址
     * @param response        换票结果封装
     * @return HTML 文档字符串
     */
    public String buildRedirectHtml(String hojoRedirectUrl, Response<UserTokenData> response) {
        try {
            boolean success = "200".equals(response.getResCode());
            String status = success ? "true" : "false";
            String errorMsg = response.getResMessage() != null ? response.getResMessage() : "";
            
            // URL编码错误消息
            String encodedErrorMsg = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
            
            // 构建跳转URL
            StringBuilder redirectUrlBuilder = new StringBuilder(hojoRedirectUrl);
            redirectUrlBuilder.append(hojoRedirectUrl.contains("?") ? "&" : "?");
            redirectUrlBuilder.append("status=").append(status);
            redirectUrlBuilder.append("&errorMsg=").append(encodedErrorMsg);
            redirectUrlBuilder.append("&bindType=wangyiyunBind");
            
            String finalRedirectUrl = redirectUrlBuilder.toString();
            
            // 生成HTML页面，使用JavaScript进行前端跳转
            return "<!DOCTYPE html>\n" +
                   "<html>\n" +
                   "<head>\n" +
                   "    <meta charset=\"UTF-8\">\n" +
                   "    <title>授权跳转中...</title>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <script type=\"text/javascript\">\n" +
                   "        window.location.href = \"" + finalRedirectUrl.replace("\"", "\\\"") + "\";\n" +
                   "    </script>\n" +
                   "    <p>正在跳转...</p>\n" +
                   "</body>\n" +
                   "</html>";
        } catch (Exception e) {
            logger.error("构建跳转HTML失败", e);
            // 如果构建失败，返回错误页面
            return "<!DOCTYPE html>\n" +
                   "<html>\n" +
                   "<head>\n" +
                   "    <meta charset=\"UTF-8\">\n" +
                   "    <title>跳转失败</title>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <p>跳转失败，请重试</p>\n" +
                   "</body>\n" +
                   "</html>";
        }
    }
    
    /**
     * 构建公共请求参数
     */
    private NeteaseCloudCommonRequest buildCommonRequest() throws Exception {
        NeteaseCloudCommonRequest request = new NeteaseCloudCommonRequest();
        request.setAppId(appId);
        request.setSignType(SIGN_TYPE);
        request.setTimestamp(System.currentTimeMillis());
        request.setEncryptType("server");
        
        // 构建设备信息
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("channel", "hojo");
        deviceMap.put("deviceId", "321");
        deviceMap.put("deviceType", "andrcar");
        deviceMap.put("appVer", "0.1");
        deviceMap.put("os", "andrcar");
        deviceMap.put("osVer", "8.1.0");
        deviceMap.put("brand", "hojo");
        deviceMap.put("model", "kys");
        deviceMap.put("clientIp", "192.168.0.1");
        request.setDevice(objectMapper.writeValueAsString(deviceMap));
        
        return request;
    }
}

