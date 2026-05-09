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
package com.hojoai.omniasst.focus.configure;

import com.hojoai.central.provider.service.CentralProviderChannelProvider;
import com.hojoai.central.provider.service.flight.FlightClient;
import com.hojoai.central.provider.service.movie.MovieClient;
import com.hojoai.central.provider.service.push.PushClient;
import com.hojoai.central.provider.service.train.TrainClient;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The IConfigurationForGrpc class for gRPC server.
 *
 * @author My
 * @version 1.0, 2025/11/12
 * @since hojoai-websocket-omniasst-focus 1.0.0
 */
@Configuration
public class IConfigurationForGrpc {

    @Resource
    private DiscoveryClient discoveryClient;

    @Value("${grpc.central.provider.service.name}")
    private String grpcCentralProviderServiceName;

    @Bean
    public CentralProviderChannelProvider centralProviderChannelProvider() {
        return new CentralProviderChannelProvider(discoveryClient, grpcCentralProviderServiceName);
    }

    @Bean
    public PushClient pushClient() {
        return new PushClient(centralProviderChannelProvider());
    }

    @Bean
    public MovieClient movieClient() {
        return new MovieClient(centralProviderChannelProvider());
    }

    @Bean
    public FlightClient flightClient() {
        return new FlightClient(centralProviderChannelProvider());
    }

    @Bean
    public TrainClient trainClient() {
        return new TrainClient(centralProviderChannelProvider());
    }

}