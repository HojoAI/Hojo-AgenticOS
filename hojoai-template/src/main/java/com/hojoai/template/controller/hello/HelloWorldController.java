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
package com.hojoai.template.controller.hello;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.response.Response;
import com.hojoai.template.service.hello.HelloWorldService;
import com.hojoai.template.service.hello.bean.HelloWorldRequest;
import com.hojoai.template.service.hello.bean.HelloWorldResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模板示例 HTTP 接口层。依赖 {@link HelloWorldService} 处理业务，对外提供演示用 REST 入口。
 */
@RestController
@RequestMapping(value = "/template")
public class HelloWorldController {

    @Resource
    private HelloWorldService helloWorldService;

    /**
     * 示例问候接口：校验入参后交由服务层组装响应。
     *
     * @param helloWorldRequest 请求体，需包含非空的 asking
     * @return 统一包装的成功响应，body 为问候结果文案
     * @throws Exception 业务或框架层可能抛出的受检/运行时异常（由全局异常处理器转换响应）
     */
    @PostMapping(value = "/helloWorld")
    public Response<HelloWorldResponse> helloWorld(@RequestBody HelloWorldRequest helloWorldRequest) throws Exception {
        Asserts.notNull(helloWorldRequest, "helloWorldRequest");
        Asserts.notBlank(helloWorldRequest.getAsking(), "asking");

        return helloWorldService.helloWorld(helloWorldRequest);
    }
}
