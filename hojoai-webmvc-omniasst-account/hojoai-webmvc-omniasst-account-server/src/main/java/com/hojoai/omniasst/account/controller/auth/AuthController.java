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
package com.hojoai.omniasst.account.controller.auth;

import com.hojoai.omniasst.account.service.auth.AuthService;
import com.hojoai.omniasst.account.service.auth.bean.*;
import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 海外认证 HTTP 接口：Google/Apple、邮箱 OTP、Magic Link、登出与注销，路径前缀 {@code /omniasst-account/auth}。
 */
@RestController
@RequestMapping(value = "/omniasst-account/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * Google 登录。
     *
     * @param request idToken、platformType
     * @return 登录结果
     * @throws Exception 业务或系统异常
     */
    @PostMapping(value = "/login/google")
    public Response<AuthLoginResponse> loginByGoogle(@RequestBody GoogleLoginRequest request) throws Exception {
        Asserts.notBlank(request.getIdToken(), "idToken");
        Asserts.gtZero(request.getPlatformType(), "platformType");
        return authService.loginByGoogle(request);
    }

    /**
     * Apple 登录。
     */
    @PostMapping(value = "/login/apple")
    public Response<AuthLoginResponse> loginByApple(@RequestBody AppleLoginRequest request) throws Exception {
        Asserts.notBlank(request.getIdentityToken(), "identityToken");
        Asserts.gtZero(request.getPlatformType(), "platformType");
        return authService.loginByApple(request);
    }

    /**
     * 发送邮箱验证码。
     */
    @PostMapping(value = "/email/send-code")
    public Response<?> sendEmailCode(@RequestBody EmailSendCodeRequest request) throws Exception {
        Asserts.notBlank(request.getEmail(), "email");
        return authService.sendEmailCode(request);
    }

    /**
     * 邮箱验证码登录。
     */
    @PostMapping(value = "/email/login")
    public Response<AuthLoginResponse> loginByEmailOtp(@RequestBody EmailLoginRequest request) throws Exception {
        Asserts.notBlank(request.getEmail(), "email");
        Asserts.notBlank(request.getCode(), "code");
        Asserts.gtZero(request.getPlatformType(), "platformType");
        return authService.loginByEmailOtp(request);
    }

    /**
     * 发送 Magic Link 邮件。
     */
    @PostMapping(value = "/email/send-magic-link")
    public Response<MagicLinkSendResponse> sendMagicLink(@RequestBody EmailSendCodeRequest request) throws Exception {
        Asserts.notBlank(request.getEmail(), "email");
        Asserts.gtZero(request.getPlatformType(), "platformType");
        return authService.sendMagicLink(request);
    }

    /**
     * 轮询 Magic Link 登录状态。
     */
    @GetMapping(value = "/magic/status")
    public Response<AuthLoginResponse> queryMagicLinkStatus(@RequestParam String loginToken) throws Exception {
        Asserts.notBlank(loginToken, "loginToken");
        return authService.queryMagicLinkStatus(loginToken);
    }

    /**
     * 邮件内链接使用：用户点击邮件即发起 GET，无需再点 H5 上的按钮。
     * 配置 {@code oversea.auth.magic-link.base-url} 应为对外可访问的前端确认页路径（不含 ?verifyToken=），
     * 例如 {@code https://域名/pa/general/magic-link}。
     * 若希望邮件链接直接调后端，则配置为 {@code https://网关域名/omniasst-account/auth/magic/verify}。
     */
    @GetMapping(value = "/magic/verify", params = "verifyToken")
    public ResponseEntity<String> verifyMagicLinkByGet(@RequestParam String verifyToken) throws Exception {
        Asserts.notBlank(verifyToken, "verifyToken");
        MagicLinkVerifyRequest request = new MagicLinkVerifyRequest();
        request.setToken(verifyToken);
        Response<?> r = authService.verifyMagicLink(request);
        return toMagicLinkVerifyHtmlResponse(r);
    }

    /**
     * Magic Link 验证（JSON 提交）。
     */
    @PostMapping(value = "/magic/verify")
    public Response<?> verifyMagicLink(@RequestBody MagicLinkVerifyRequest request) throws Exception {
        Asserts.notBlank(request.getToken(), "token");
        return authService.verifyMagicLink(request);
    }

    @SuppressWarnings("unchecked")
    private static ResponseEntity<String> toMagicLinkVerifyHtmlResponse(Response<?> r) {
        if (r == null) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_HTML)
                    .body(magicLinkHtmlPage(false, "系统异常", "请稍后重试或返回 App。"));
        }
        if ("OK".equals(r.getResCode())) {
            Object content = r.getResContent();
            if (content instanceof com.hojoai.omniasst.account.service.auth.bean.AuthLoginResponse resp) {
                if ("success".equals(resp.getStatus())) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .body(magicLinkHtmlPage(true, "登录成功", "请返回 App 继续使用。"));
                }
                String reason = resp.getFailureReason();
                String detail = "consumed".equals(reason) ? "该链接已被使用，请重新发送登录邮件。" : "登录链接已过期，请重新发送登录邮件。";
                return ResponseEntity.status(400)
                        .contentType(MediaType.TEXT_HTML)
                        .body(magicLinkHtmlPage(false, "无法完成登录", detail));
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(magicLinkHtmlPage(true, "登录成功", "请返回 App 继续使用。"));
        }
        String msg = r.getResMessage() != null ? r.getResMessage() : r.getResCode();
        return ResponseEntity.status(400)
                .contentType(MediaType.TEXT_HTML)
                .body(magicLinkHtmlPage(false, "无法完成登录", escapeHtml(msg)));
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String magicLinkHtmlPage(boolean success, String title, String detail) {
        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>"
                + "<title>" + escapeHtml(title) + "</title>"
                + "<style>body{font-family:sans-serif;padding:24px;text-align:center;color:#18171c;}p{margin:8px 0;}</style></head><body>"
                + "<p><strong>" + escapeHtml(title) + "</strong></p>"
                + "<p>" + detail + "</p>"
                + (success ? "" : "<p>若问题持续，请重新请求登录邮件。</p>")
                + "</body></html>";
    }

    /**
     * 登出当前会话。
     */
    @PostMapping(value = "/logout")
    public Response<?> logout(@RequestHeader("X-User-Id") String userId,
                              @RequestHeader("X-User-Token") String userToken) throws Exception {
        Asserts.notBlank(userId, "X-User-Id");
        Asserts.notBlank(userToken, "X-User-Token");
        return authService.logout(userId);
    }

    /**
     * 注销账号。
     */
    @PostMapping(value = "/account/delete")
    public Response<?> deleteAccount(@RequestHeader("X-User-Id") String userId,
                                     @RequestHeader("X-User-Token") String userToken) throws Exception {
        Asserts.notBlank(userId, "X-User-Id");
        Asserts.notBlank(userToken, "X-User-Token");
        return authService.deleteAccount(userId);
    }
}
