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
package com.hojoai.omniasst.management.controller.push;

import com.hojoai.central.provider.service.common.enums.AgentActionEnum;
import com.hojoai.central.provider.service.common.enums.AgentTypeEnum;
import com.hojoai.central.provider.service.dto.request.push.PushRequestDTO;
import com.hojoai.central.provider.service.utils.EnumUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.dto.request.PAPushRequestDTO;
import com.hojoai.omniasst.management.dto.request.PushUserDeviceTokenRequestDTO;
import com.hojoai.omniasst.management.service.notification.PushService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * The PushController class for umeng push.
 *
 * @author My
 * @version 1.0, 2026/01/13
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@RestController
@RequestMapping("/v0")
public class PushController {
    private static final Logger logger = LoggerFactory.getLogger(PushController.class);

    @Resource
    private PushService pushService;

    @PostMapping("/push")
    public Response<String> push(@RequestBody PAPushRequestDTO request) {
        logger.info("PushController.push method begin, request is -> {}", JacksonUtil.bean2Json(request));

        PushRequestDTO requestDTO = new PushRequestDTO();

        requestDTO.setUserIdList(request.getUserIdList());
        requestDTO.setTitle(request.getTitle());
        requestDTO.setContent(request.getContent());
        requestDTO.setAction(EnumUtil.getEnumByValue(request.getAction(), AgentActionEnum.class));
        requestDTO.setType(EnumUtil.getEnumByCode(request.getType(), AgentTypeEnum.class));
        requestDTO.setBroadcast(request.getBroadcast());
        requestDTO.setConversationInfo(request.getConversationInfo());
        requestDTO.setRoute(request.getRoute());
        requestDTO.setMessageType(request.getMessageType());
        requestDTO.setDeviceToken(request.getDeviceToken());
        requestDTO.setPushChannel(request.getPushChannel());
        requestDTO.setSilent(request.getSilent());
        requestDTO.setRelatedId(request.getRelatedId());
        requestDTO.setRelatedTitle(request.getRelatedTitle());
        if (request.getLandingPage() != null) {
            requestDTO.setRoute(request.getLandingPage());
        }
        requestDTO.setFirebaseData(request.getFirebaseData());

        return pushService.push(requestDTO);
    }

    @PostMapping("/device-token")
    public Response<String> userDeviceToken(@RequestBody PushUserDeviceTokenRequestDTO request) {
        logger.info("PushController.userDeviceToken method begin, request is -> {}", JacksonUtil.bean2Json(request));

        if (Objects.isNull(request) || StringUtils.isBlank(request.getUserId()) || StringUtils.isBlank(request.getDeviceToken())) {
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }

        return pushService.userDeviceToken(request);
    }
}
