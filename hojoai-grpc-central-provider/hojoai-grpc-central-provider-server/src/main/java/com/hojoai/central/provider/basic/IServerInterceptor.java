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
package com.hojoai.central.provider.basic;

import com.google.protobuf.MessageOrBuilder;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * The server interceptor class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class IServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        SimpleForwardingServerCall<ReqT, RespT> simpleForwardingServerCall = new SimpleForwardingServerCall<>(serverCall) {
            public void sendMessage(RespT message) {
                MessageOrBuilder messageOrBuilder = (MessageOrBuilder) message;

                String className = messageOrBuilder.getDescriptorForType().getFile().getName();
                String methodName = serverCall.getMethodDescriptor().getFullMethodName();

                ILoggerHandler.leave(className, methodName, message);
                super.sendMessage(message);
            }
        };

        ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(simpleForwardingServerCall, metadata);

        return new SimpleForwardingServerCallListener<>(listener) {
            public void onMessage(ReqT message) {
                MessageOrBuilder messageOrBuilder = (MessageOrBuilder) message;

                String className = messageOrBuilder.getDescriptorForType().getFile().getName();
                String methodName = serverCall.getMethodDescriptor().getFullMethodName();

                ILoggerHandler.enter(className, methodName, message);
                super.onMessage(message);
            }
        };
    }
}
