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
package com.hojoai.omniasst.sysagent.controller.umeng;

import com.hojoai.omniasst.sysagent.configure.UmengPushProperties;
import com.hojoai.omniasst.sysagent.push.constants.PushConstants;
import com.hojoai.omniasst.sysagent.push.model.AndroidPayload;
import com.hojoai.omniasst.sysagent.push.model.PayloadBody;
import com.hojoai.omniasst.sysagent.push.model.PushRequest;
import com.hojoai.omniasst.sysagent.push.model.PushResult;
import com.hojoai.omniasst.sysagent.push.model.dto.PushRequestDTO;
import com.hojoai.omniasst.sysagent.push.util.UmengPushUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 友盟推送 HTTP 入口：接收业务侧 DTO，组装 {@link PushRequest} 后由 {@link UmengPushUtil} 下发。
 * <p>配置项来自 {@link UmengPushProperties}（如生产/测试模式）。</p>
 */
@RestController
@RequestMapping("/omniasst-sysagent/v1/push")
public class PushController {

    private static final Logger logger = LoggerFactory.getLogger(PushController.class);

    @Resource
    private UmengPushUtil pushUtil;

    @Resource
    private UmengPushProperties properties;

    /**
     * 按用户别名发送自定义推送（通知或透传消息），并返回统一 JSON 结构。
     *
     * @param requestDTO 用户 ID、展示类型、业务 action/params 等
     * @return {@code code=0} 表示成功；参数非法或异常时 {@code code=-1} 与 {@code message}
     */
    @PostMapping("/send")
    public Map<String, Object> sendNotification(@RequestBody PushRequestDTO requestDTO) {
        try {
            // 1. 验证参数
            if (requestDTO == null) {
                return errorResponse("请求参数不能为空");
            }

            if (requestDTO.getUid() == null || requestDTO.getUid().trim().isEmpty()) {
                return errorResponse("用户ID不能为空");
            }

            if (requestDTO.getDisplayType() == null || requestDTO.getDisplayType().trim().isEmpty()) {
                return errorResponse("消息类型不能为空");
            }

            // 检查消息类型是否合法
            if (!requestDTO.getDisplayType().equals(PushConstants.DISPLAY_TYPE_NOTIFICATION)
                    && !requestDTO.getDisplayType().equals(PushConstants.DISPLAY_TYPE_MESSAGE)) {
                return errorResponse("不支持的消息类型: " + requestDTO.getDisplayType());
            }

            // 2. 记录日志
            logger.info("推送请求: uid={}, type={}", requestDTO.getUid(), requestDTO.getDisplayType());

            // 3. 构建推送请求
            PushRequest pushRequest = new PushRequest();
            pushRequest.setType(PushConstants.PUSH_TYPE_CUSTOMIZED);
            pushRequest.setAliasType(PushConstants.ALIAS_TYPE_USER_ID);
            pushRequest.setAlias(requestDTO.getUid());
            pushRequest.setProductionMode(properties.isProductionMode());
            pushRequest.setDescription("推送_" + System.currentTimeMillis());

            // 4. 构建payload
            AndroidPayload payload = new AndroidPayload();
            payload.setDisplayType(requestDTO.getDisplayType());

            if (PushConstants.DISPLAY_TYPE_NOTIFICATION.equals(requestDTO.getDisplayType())) {
                PayloadBody body = new PayloadBody();
                body.setTitle(PushConstants.DEFAULT_TITLE);
                body.setText(PushConstants.DEFAULT_CONTENT);
                body.setAfterOpen(PushConstants.DEFAULT_AFTER_OPEN);
                body.setPlayVibrate(PushConstants.DEFAULT_PLAY_VIBRATE);
                body.setPlayLights(PushConstants.DEFAULT_PLAY_LIGHTS);
                body.setPlaySound(PushConstants.DEFAULT_PLAY_SOUND);
                payload.setBody(body);
            }

            // 5. 设置extra参数
            if (requestDTO.getAction() != null || requestDTO.getParams() != null) {
                Map<String, Object> extra = new HashMap<>();
                if (requestDTO.getAction() != null) {
                    extra.put("action", requestDTO.getAction());
                }
                if (requestDTO.getParams() != null) {
                    extra.put("params", requestDTO.getParams());
                }
                payload.setExtra(extra);
            }

            pushRequest.setPayload(payload);

            // 6. 发送推送
            PushResult result = pushUtil.sendPush(pushRequest);

            // 7. 返回结果
            return buildResponse(result, requestDTO.getDisplayType());

        } catch (Exception e) {
            logger.error("推送异常", e);
            return errorResponse("系统异常");
        }
    }

    /**
     * 错误响应
     */
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", -1);
        response.put("message", message);
        return response;
    }

    /**
     * 构建成功/失败响应
     */
    private Map<String, Object> buildResponse(PushResult result, String displayType) {
        Map<String, Object> response = new HashMap<>();
        response.put("displayType", displayType);

        if (result.isSuccess()) {
            response.put("code", 0);
            response.put("message", "推送成功");
            response.put("msgId", result.getMsgId());
            response.put("taskId", result.getTaskId());
        } else {
            response.put("code", -1);
            response.put("message", result.getErrorMsg());
            response.put("errorCode", result.getErrorCode());
        }

        return response;
    }
}