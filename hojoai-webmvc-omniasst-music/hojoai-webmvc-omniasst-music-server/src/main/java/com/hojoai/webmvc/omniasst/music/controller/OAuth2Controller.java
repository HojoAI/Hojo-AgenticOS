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
package com.hojoai.webmvc.omniasst.music.controller;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.webmvc.omniasst.music.service.netease.OAuth2Service;
import com.hojoai.webmvc.omniasst.music.service.netease.dto.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2授权控制器
 * 
 * @author shengxianfei
 * @since 2026-01-17
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);
    
    @Resource
    private OAuth2Service oauth2Service;
    
    /**
     * 接口1：生成授权URL
     * 前端传入userId，后端生成加密的state，返回网易云授权页URL
     *
     * @param request 须含 userId
     * @return 授权 URL 与 state
     */
    @PostMapping("/generate-auth-url")
    public Response<GenerateAuthUrlResponse> generateAuthUrl(@RequestBody GenerateAuthUrlRequest request) {
        logger.info("收到生成授权URL请求 - userId: {}", request != null ? request.getUserId() : null);
        
        Asserts.notNull(request, "request");
        Asserts.notNull(request.getUserId(), "userId");
        
        return oauth2Service.generateAuthUrl(request);
    }
    
    /**
     * 接口2：OAuth回调
     * 接收网易云返回的code和state，验证state后用code兑换token，存入Redis
     * 如果有hojoRedirectUrl，则返回HTML页面进行前端跳转
     *
     * @param code            授权码
     * @param state           本服务下发的加密 state
     * @param hojoRedirectUrl 可选，若存在则响应为 HTML 跳转页，否则为 JSON
     * @return {@link ResponseEntity} 包裹的 HTML，或 {@link Response} JSON
     */
    @GetMapping("/callback")
    public Object callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "hojoRedirectUrl", required = false) String hojoRedirectUrl) {
        logger.info("收到OAuth回调 - code: {}, state: {}, hojoRedirectUrl: {}", 
                code != null ? code.substring(0, Math.min(10, code.length())) + "..." : null,
                state != null ? state.substring(0, Math.min(10, state.length())) + "..." : null,
                hojoRedirectUrl);
        
        Asserts.notNull(code, "code");
        Asserts.notNull(state, "state");
        
        OAuth2CallbackRequest request = new OAuth2CallbackRequest();
        request.setCode(code);
        request.setState(state);
        request.setHojoRedirectUrl(hojoRedirectUrl);
        
        Response<UserTokenData> response = oauth2Service.handleCallback(request);
        
        // 如果有hojoRedirectUrl，返回HTML页面进行前端跳转
        if (StringUtils.isNotBlank(hojoRedirectUrl)) {
            String html = oauth2Service.buildRedirectHtml(hojoRedirectUrl, response);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(html, headers, HttpStatus.OK);
        }
        
        // 否则返回JSON响应
        return response;
    }
    
    /**
     * 刷新token接口（预留，后续优化歌曲搜索接口时使用）
     *
     * @param userId 业务用户 ID
     * @return 刷新后的 token 信息
     */
    @PostMapping("/refresh-token")
    public Response<UserTokenData> refreshToken(@RequestParam("userId") String userId) {
        logger.info("收到刷新token请求 - userId: {}", userId);
        
        Asserts.notNull(userId, "userId");
        
        return oauth2Service.refreshUserToken(userId);
    }
    
    /**
     * 查询授权状态
     *
     * @param userId 业务用户 ID
     * @return 是否已绑定网易云及 openId 等
     */
    @GetMapping("/auth-status")
    public Response<AuthStatusResponse> getAuthStatus(@RequestParam("userId") String userId) {
        logger.info("查询授权状态 - userId: {}", userId);
        
        Asserts.notNull(userId, "userId");
        
        return oauth2Service.getAuthStatus(userId);
    }
    
    /**
     * 取消授权
     *
     * @param userId 业务用户 ID
     * @return 操作结果，body 可为空
     */
    @GetMapping("/revoke")
    public Response<Void> revokeAuth(@RequestParam("userId") String userId) {
        logger.info("取消授权 - userId: {}", userId);
        
        Asserts.notNull(userId, "userId");
        
        return oauth2Service.revokeAuth(userId);
    }
}

