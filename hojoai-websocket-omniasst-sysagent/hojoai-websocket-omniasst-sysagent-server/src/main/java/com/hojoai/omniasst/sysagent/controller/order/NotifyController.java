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
package com.hojoai.omniasst.sysagent.controller.order;

import com.hojoai.omniasst.sysagent.service.order.PayNotifyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * The AlipayController class for alipay.
 *
 * @author My
 * @version 1.0, 2025/11/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@RestController
@RequestMapping(value = "/omniasst-sysagent")
public class NotifyController {

    private static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @Resource
    private PayNotifyService payNotifyService;

    /** 支付宝异步通知成功时应返回的纯文本，否则支付宝会重试。 */
    private static final String ALIPAY_SUCCESS = "success";

    /** 支付宝异步通知处理失败时返回，用于告知需重试或记录日志。 */
    private static final String ALIPAY_FAIL = "fail";

    /** 磐河等渠道约定的成功回包文本。 */
    private static final String PANHE_SUCCESS = "SUCCESS";

    /** 磐河等渠道约定的失败回包文本。 */
    private static final String PANHE_FAIL = "FAIL";

    /**
     * 支付宝支付异步通知：解析表单参数并交由业务验签与落单。
     *
     * @param request HTTP 请求，参数在 query/form 中
     * @return {@link #ALIPAY_SUCCESS} 或 {@link #ALIPAY_FAIL}
     */
    @PostMapping(value = "/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> paramMap = this.convertRequestParamMap(request);

            logger.info("alipayNotify paramMap -> {}", paramMap);

            Boolean isSuccess = payNotifyService.alipayNotify(paramMap);

            return isSuccess ? ALIPAY_SUCCESS : ALIPAY_FAIL;
        } catch (Exception e) {
            logger.error("MovieController.getFilmList error: ", e);
            return ALIPAY_FAIL;
        }
    }

    /**
     * 磐河支付异步通知入口。
     *
     * @param request 携带渠道回调参数
     * @return {@link #PANHE_SUCCESS} 或 {@link #PANHE_FAIL}（异常时当前实现返回 {@link #ALIPAY_FAIL} 字符串）
     */
    @PostMapping(value = "/panhe/notify")
    public String panheNotify(HttpServletRequest request) {
        try {
            Map<String, String> paramMap = this.convertRequestParamMap(request);

            logger.info("panheNotify paramMap -> {}", paramMap);

            Boolean isSuccess = payNotifyService.panheNotify(paramMap);

            return isSuccess ? PANHE_SUCCESS : PANHE_FAIL;
        } catch (Exception e) {
            logger.error("MovieController.getFilmList error: ", e);
            return ALIPAY_FAIL;
        }
    }

    private Map<String, String> convertRequestParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();

        Map<String, String[]> requestParamMap = request.getParameterMap();

        for (String name : requestParamMap.keySet()) {
            String[] values = requestParamMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            paramMap.put(name, valueStr);
        }

        return paramMap;
    }

}
