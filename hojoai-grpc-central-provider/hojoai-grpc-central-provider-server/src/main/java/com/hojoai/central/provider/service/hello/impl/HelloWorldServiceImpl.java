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
package com.hojoai.central.provider.service.hello.impl;

import com.hojoai.central.provider.basic.GrpcService;
import com.hojoai.central.provider.service.hello.HelloWorldDTO;
import com.hojoai.central.provider.service.hello.HelloWorldSayHelloRequest;
import com.hojoai.central.provider.service.hello.HelloWorldServiceGrpc;
import com.hojoai.common.asserts.Asserts;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The service implements class for hello.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
@GrpcService
public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldServiceImpl.class);

    @Override
    public void sayHello(HelloWorldSayHelloRequest request, StreamObserver<HelloWorldDTO> responseObserver) {
        try {
            HelloWorldDTO.Builder builder = HelloWorldDTO.newBuilder();

            // 1.check argument
            String asking = request.getAsking();
            Asserts.notBlank(asking, "asking");

            // 2.execute service
            String answer = String.format("your asking message is: %s", asking);

            // 3.return response
            builder.setAnswer(answer);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("say hello exception", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
