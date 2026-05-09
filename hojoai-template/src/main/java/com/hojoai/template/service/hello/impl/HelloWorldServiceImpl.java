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
package com.hojoai.template.service.hello.impl;

import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.template.service.hello.HelloWorldService;
import com.hojoai.template.service.hello.bean.HelloWorldRequest;
import com.hojoai.template.service.hello.bean.HelloWorldResponse;
import org.springframework.stereotype.Service;

/**
 * {@link com.hojoai.template.service.hello.HelloWorldService} 的默认实现，演示无外部依赖的纯内存组装逻辑。
 */
@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<HelloWorldResponse> helloWorld(HelloWorldRequest helloWorldReq) throws Exception {
        HelloWorldResponse helloWorldResponse = new HelloWorldResponse();

        // 1.execute service
        String answer = String.format("your asking message is: %s", helloWorldReq.getAsking());

        // 2.return response
        helloWorldResponse.setAnswer(answer);

        return Response.newInstance(ResponseStatusBasic.OK, helloWorldResponse);
    }
}
