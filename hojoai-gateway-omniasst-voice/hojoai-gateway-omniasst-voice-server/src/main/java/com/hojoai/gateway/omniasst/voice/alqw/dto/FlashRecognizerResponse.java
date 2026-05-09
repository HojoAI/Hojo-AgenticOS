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
package com.hojoai.gateway.omniasst.voice.alqw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 阿里云 FlashRecognizer 接口 JSON 响应体
 * status 为 20000000 表示成功；识别文本在 result 或 flash_result.sentences[].text
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlashRecognizerResponse {

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("result")
    private String result;

    @JsonProperty("status")
    private Long status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("flash_result")
    private FlashResult flashResult;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlashResult {
        @JsonProperty("duration")
        private Long duration;

        @JsonProperty("completed")
        private Boolean completed;

        @JsonProperty("sentences")
        private List<FlashSentence> sentences;

        @JsonProperty("latency")
        private Long latency;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlashSentence {
        @JsonProperty("text")
        private String text;

        @JsonProperty("begin_time")
        private Long beginTime;

        @JsonProperty("end_time")
        private Long endTime;

        @JsonProperty("channel_id")
        private Integer channelId;
    }
}
