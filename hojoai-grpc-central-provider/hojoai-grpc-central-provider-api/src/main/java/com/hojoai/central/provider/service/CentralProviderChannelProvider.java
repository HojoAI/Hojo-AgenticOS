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
package com.hojoai.central.provider.service;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 基于服务发现的 gRPC {@link ManagedChannel} 池：按 Nacos 实例与 metadata 中的 {@code grpc-port} 建连，并对调用方提供轮询与同步刷新。
 */
public class CentralProviderChannelProvider {

    private static final long CHANNEL_SYNC_INTERVAL_TIME = 20000;
    private static final String ADDRESS_SEPARATOR = ":";
    private static final String METADATA_KEY_GRPC_PORT = "grpc-port";

    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger();
    private final AtomicLong lastSyncTime = new AtomicLong();

    private final DiscoveryClient discoveryClient;
    private final String serviceId;

    /**
     * @param discoveryClient Spring Cloud 服务发现客户端
     * @param serviceId       Nacos 等服务注册中的服务名（须与 provider 的 {@code spring.application.name} 一致）
     */
    public CentralProviderChannelProvider(DiscoveryClient discoveryClient, String serviceId) {
        if (discoveryClient == null) {
            throw new IllegalArgumentException("has an illegal argument, [discoveryClient] should not be blank");
        }
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("has an illegal argument, [serviceId] should not be blank");
        }

        this.discoveryClient = discoveryClient;
        this.serviceId = serviceId;
    }

    /**
     * 轮询选用一条可用 Channel 执行阻塞式 gRPC 调用。
     *
     * @param operation 使用 {@link ManagedChannel} 的回调
     * @param <T>       返回类型
     * @return 调用结果
     * @throws IllegalStateException 未发现任何可用实例时
     */
    public <T> T executeWithChannel(Function<ManagedChannel, T> operation) {
        syncChannels();

        if (channels.isEmpty()) {
            throw new IllegalStateException("no available channels for service: " + serviceId);
        }

        List<String> addresses = new ArrayList<>(channels.keySet());
        int index = Math.floorMod(roundRobinIndex.getAndIncrement(), addresses.size());

        return operation.apply(channels.get(addresses.get(index)));
    }

    private void syncChannels() {
        long currentTime = System.currentTimeMillis();

        if (!channels.isEmpty() && (currentTime - lastSyncTime.get()) < CHANNEL_SYNC_INTERVAL_TIME) {
            return;
        }

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceId);
        if (serviceInstances == null || serviceInstances.isEmpty()) {
            cleanupExpiredChannels();
            return;
        }

        Set<String> discoveredAddresses = new HashSet<>();
        for (ServiceInstance serviceInstance : serviceInstances) {
            try {
                String host = serviceInstance.getHost();
                if (host == null || host.trim().isEmpty()) {
                    continue;
                }

                Map<String, String> metadata = serviceInstance.getMetadata();
                if (metadata == null || metadata.isEmpty()) {
                    continue;
                }

                String portStr = metadata.get(METADATA_KEY_GRPC_PORT);
                if (portStr == null || portStr.trim().isEmpty()) {
                    continue;
                }

                int port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    continue;
                }

                String address = host + ADDRESS_SEPARATOR + port;
                channels.computeIfAbsent(address, key -> NettyChannelBuilder.forAddress(host, port).usePlaintext().build());

                discoveredAddresses.add(address);
            } catch (Exception e) {
                continue;
            }

            cleanupExpiredChannels(discoveredAddresses);
            lastSyncTime.set(currentTime);
        }
    }

    private void cleanupExpiredChannels() {
        for (String address : new HashSet<>(channels.keySet())) {
            ManagedChannel managedChannel = channels.remove(address);
            if (managedChannel != null) {
                managedChannel.shutdown();
            }
        }
    }

    private void cleanupExpiredChannels(Set<String> discoveredAddresses) {
        for (String address : new HashSet<>(channels.keySet())) {
            if (!discoveredAddresses.contains(address)) {
                ManagedChannel managedChannel = channels.remove(address);
                if (managedChannel != null) {
                    managedChannel.shutdown();
                }
            }
        }
    }
}