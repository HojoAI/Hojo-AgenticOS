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
package com.hojoai.webmvc.omniasst.meeting.controller;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.service.bean.VoiceprintRegisterUiCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Collections;

/**
 * 将个人声纹注册与前端约定的分支码写入 {@link Response#getResMessage()}，不依赖 hojoai-common 中 Response 的具体构造 API。
 */
public final class VoiceprintRegisterResponseSupport {

    private static final Logger logger = LoggerFactory.getLogger(VoiceprintRegisterResponseSupport.class);

    private VoiceprintRegisterResponseSupport() {}

    /**
     * 成功：在标准 OK 响应上覆盖 {@code resMessage} 为 {@link VoiceprintRegisterUiCodes#SUCCESS}。
     */
    public static <T> Response<T> okWithSuccessMessage(Response<T> response) {
        setResMessageIfWritable(response, VoiceprintRegisterUiCodes.SUCCESS);
        return response;
    }

    private static final String DEFAULT_FAIL_DETAIL = "操作失败，请稍后重试或查看服务端日志。";

    /**
     * 业务失败（仍返回 HTTP 200）：{@code resMessage} 为约定码；{@code resContent} 含 {@code detail}（与接口文档一致，便于联调；前端可忽略）。
     */
    public static Response<Object> failWithUiCode(String uiCode, String detailMessage) {
        String detail = StringUtils.isNotBlank(detailMessage) ? detailMessage.trim() : DEFAULT_FAIL_DETAIL;
        Object content = Collections.singletonMap("detail", detail);
        Response<Object> r = Response.newInstance(ResponseStatusBasic.OK, content);
        setResMessageIfWritable(r, uiCode);
        return r;
    }

    /**
     * @deprecated 请使用 {@link #failWithUiCode(String, String)}，传入 {@code null} 作第二参数即可
     */
    @Deprecated
    public static Response<Object> failWithUiCodeOnly(String uiCode) {
        return failWithUiCode(uiCode, null);
    }

    private static void setResMessageIfWritable(Object responseBean, String resMessage) {
        if (responseBean == null || resMessage == null) {
            return;
        }
        BeanWrapper w = new BeanWrapperImpl(responseBean);
        if (w.isWritableProperty("resMessage")) {
            w.setPropertyValue("resMessage", resMessage);
        } else {
            logger.warn("Response 无可写属性 resMessage，无法写入 UI 分支码: {}", resMessage);
        }
    }
}
