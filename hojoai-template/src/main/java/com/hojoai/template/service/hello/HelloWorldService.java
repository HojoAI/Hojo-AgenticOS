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
package com.hojoai.template.service.hello;

import com.hojoai.common.response.Response;
import com.hojoai.template.service.hello.bean.HelloWorldRequest;
import com.hojoai.template.service.hello.bean.HelloWorldResponse;

/**
 * 示例问候业务契约。由 Controller 调用，负责组装 {@link HelloWorldResponse}。
 */
public interface HelloWorldService {

    /**
     * 根据用户输入生成问候类返回内容。
     *
     * @param helloWorldRequest 请求对象，调用方需保证 asking 已校验
     * @return 统一响应，携带格式化后的 answer
     * @throws Exception 业务处理过程中可能抛出的异常
     */
    Response<HelloWorldResponse> helloWorld(HelloWorldRequest helloWorldRequest) throws Exception;
}
