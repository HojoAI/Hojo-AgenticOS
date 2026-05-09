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
package com.hojoai.webmvc.omniasst.meeting.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.webmvc.omniasst.meeting.api.dto.VoiceProfileResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * management 与 profile/删除相关的 {@link Response} 中，{@code resContent} 有时为对象，有时为纯字符串（例如 {@code "删除成功"}），
 * 不能直接反序列化为 {@link VoiceProfileResponse}。
 */
public final class ManagementVoiceProfileResponseParseSupport {

    private ManagementVoiceProfileResponseParseSupport() {}

    public static Response<VoiceProfileResponse> parseFlexibleProfileBody(String responseBody) {
        Response<JsonNode> raw = JacksonUtil.json2Bean(responseBody, new TypeReference<Response<JsonNode>>() {});
        if (raw == null) {
            return null;
        }
        JsonNode contentNode = raw.getResContent();
        VoiceProfileResponse profile = null;
        String textContent = null;
        if (contentNode != null && !contentNode.isNull()) {
            if (contentNode.isTextual()) {
                textContent = contentNode.asText();
            } else {
                profile = JacksonUtil.json2Bean(contentNode.toString(), VoiceProfileResponse.class);
            }
        }
        Response<VoiceProfileResponse> out = Response.newInstance(ResponseStatusBasic.OK, profile);
        String resMessage = raw.getResMessage();
        if (StringUtils.isBlank(resMessage) && StringUtils.isNotBlank(textContent)) {
            resMessage = textContent;
        }
        applyResFields(out, raw.getResCode(), resMessage);
        return out;
    }

    private static void applyResFields(Response<?> target, String resCode, String resMessage) {
        BeanWrapper w = new BeanWrapperImpl(target);
        if (StringUtils.isNotBlank(resCode) && w.isWritableProperty("resCode")) {
            w.setPropertyValue("resCode", resCode.trim());
        }
        if (resMessage != null && w.isWritableProperty("resMessage")) {
            w.setPropertyValue("resMessage", resMessage);
        }
    }
}
