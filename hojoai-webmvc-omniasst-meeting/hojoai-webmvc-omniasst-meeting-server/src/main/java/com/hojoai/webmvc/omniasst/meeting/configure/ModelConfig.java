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
package com.hojoai.webmvc.omniasst.meeting.configure;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The ModelConfig class for model by spring ai.
 *
 * @author lianghay
 * @version 1.0, 2026/01/04
 * @since hojoai-webmvc-omniasst-meeting 1.0.0
 */

@Configuration
public class ModelConfig {

    @Value("${model.meeting-title.url}")
    private String meetingTitleModelUrl;

    @Value("${model.meeting-title.api-key}")
    private String meetingTitleModelApiKey;

    @Value("${model.meeting-title.model-name}")
    private String meetingTitleModelName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("meetingTitleModel")
    public ChatClient getMeetingTitle(){
        OpenAiApi meetingModelApi = OpenAiApi.builder()
                .baseUrl(meetingTitleModelUrl)
                .apiKey(meetingTitleModelApiKey)
                .build();

        OpenAiChatModel meetingModel = OpenAiChatModel.builder()
                .openAiApi(meetingModelApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(meetingTitleModelName)
                        .build())
                .build();

        return ChatClient.builder(meetingModel).build();
    }

}
