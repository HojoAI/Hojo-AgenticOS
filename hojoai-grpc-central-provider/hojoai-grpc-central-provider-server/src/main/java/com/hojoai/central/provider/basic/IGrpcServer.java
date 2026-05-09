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

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 内嵌 gRPC Server 生命周期管理：启动时注册所有带 {@link GrpcService} 的 Bean，端口由 {@code grpc.port} 配置。
 */
@Component
public class IGrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(IGrpcServer.class);

    @Resource
    private ApplicationContext applicationContext;

    @Value("${grpc.port}")
    private int port;

    private Server server;

    @PostConstruct
    public void init() {
        start();
        logger.info("grpc server started on port: {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(IGrpcServer.this::stop));
    }

    @PreDestroy
    public void destroy() {
        stop();
        logger.info("grpc server stopped on port: {}", port);
    }

    private void start() {
        try {
            ServerBuilder<?> serverBuilder = NettyServerBuilder.forPort(port);

            // add service
            String[] grpcServiceBeanNameArray = applicationContext.getBeanNamesForAnnotation(GrpcService.class);
            for (String grpcServiceBeanName : grpcServiceBeanNameArray) {
                serverBuilder.addService(ServerInterceptors.intercept((BindableService) applicationContext.getBean(grpcServiceBeanName), new IServerInterceptor()));
                logger.info("grpc server builder on class: {}", grpcServiceBeanName);
            }

            server = serverBuilder.build().start();
        } catch (Exception e) {
            throw new UnsupportedOperationException("start exception", e);
        }
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
