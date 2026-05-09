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
package com.hojoai.omniasst.sysagent.common;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务发现工具类，用于从 Nacos 获取服务实例
 */
@Component
public class ServiceDiscoveryUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryUtil.class);
    
    @Resource
    private DiscoveryClient discoveryClient;
    
    private final Random random = new Random();
    
    // 轮询策略的计数器，key: serviceName, value: counter
    private final Map<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();
    
    /**
     * 负载均衡策略：在多个 Nacos 实例间选取一个。
     */
    public enum LoadBalanceStrategy {
        /** 随机选取健康实例。 */
        RANDOM,
        /** 轮询，按服务名维护计数器。 */
        ROUND_ROBIN,
        /** 固定取列表第一个。 */
        FIRST
    }

    /**
     * 解析服务基准 URL，默认轮询策略。
     *
     * @param serviceName 注册中心中的服务名
     * @return {@code scheme://host:port}；无实例或异常时为 {@code null}
     */
    public String getServiceBaseUrl(String serviceName) {
        return getServiceBaseUrl(serviceName, LoadBalanceStrategy.ROUND_ROBIN);
    }

    /**
     * 解析服务基准 URL，并指定负载均衡策略。
     *
     * @param serviceName 服务名
     * @param strategy    选取实例的策略
     * @return 基准 URL；失败为 {@code null}
     */
    public String getServiceBaseUrl(String serviceName, LoadBalanceStrategy strategy) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances == null || instances.isEmpty()) {
                logger.error("No instances found for service: {}", serviceName);
                return null;
            }
            
            ServiceInstance instance = selectInstance(instances, serviceName, strategy);
            String baseUrl = instance.getUri().toString();
            logger.debug("Found service [{}] at URL: {} (strategy: {})", serviceName, baseUrl, strategy);
            return baseUrl;
        } catch (Exception e) {
            logger.error("Error getting service [{}] URL from Nacos", serviceName, e);
            return null;
        }
    }
    
    /**
     * 根据负载均衡策略选择服务实例
     */
    private ServiceInstance selectInstance(List<ServiceInstance> instances, String serviceName, LoadBalanceStrategy strategy) {
        if (instances.size() == 1) {
            return instances.get(0);
        }
        
        return switch (strategy) {
            case RANDOM -> {
                int index = random.nextInt(instances.size());
                yield instances.get(index);
            }
            case ROUND_ROBIN -> {
                AtomicInteger counter = roundRobinCounters.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
                int index = Math.abs(counter.getAndIncrement() % instances.size());
                yield instances.get(index);
            }
            case FIRST -> instances.get(0);
        };
    }

    /**
     * 拼接完整请求 URL（默认轮询），{@code path} 无前置 {@code /} 时会自动补上。
     *
     * @param serviceName 服务名
     * @param path        资源路径
     * @return 完整 URL；服务不可达时为 {@code null}
     */
    public String getServiceUrl(String serviceName, String path) {
        return getServiceUrl(serviceName, path, LoadBalanceStrategy.ROUND_ROBIN);
    }

    /**
     * 拼接完整请求 URL，并指定负载均衡策略。
     *
     * @param serviceName 服务名
     * @param path        资源路径
     * @param strategy    负载均衡策略
     * @return 完整 URL；不可达为 {@code null}
     */
    public String getServiceUrl(String serviceName, String path, LoadBalanceStrategy strategy) {
        String baseUrl = getServiceBaseUrl(serviceName, strategy);
        if (baseUrl == null) {
            return null;
        }
        
        // 确保路径以 / 开头
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return baseUrl + path;
    }

    /**
     * 判断服务在注册中心是否至少有一个可用实例。
     *
     * @param serviceName 服务名
     * @return 有实例为 {@code true}
     */
    public boolean isServiceAvailable(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            return instances != null && !instances.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking service [{}] availability", serviceName, e);
            return false;
        }
    }

    /**
     * 返回某服务当前全部实例列表（异常时返回空列表）。
     *
     * @param serviceName 服务名
     * @return 实例列表，永不为 {@code null}
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            return instances != null ? instances : List.of();
        } catch (Exception e) {
            logger.error("Error getting service [{}] instances", serviceName, e);
            return List.of();
        }
    }
}
