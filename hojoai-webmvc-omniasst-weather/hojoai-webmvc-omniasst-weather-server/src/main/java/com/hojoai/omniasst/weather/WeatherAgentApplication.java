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
package com.hojoai.omniasst.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The spring application class.
 *
 * @author kevanwong
 * @version 1.0, 2025/08/19
 * @since hojoai-webmvc-omniasst-weather 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WeatherAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAgentApplication.class, args);
    }
}
