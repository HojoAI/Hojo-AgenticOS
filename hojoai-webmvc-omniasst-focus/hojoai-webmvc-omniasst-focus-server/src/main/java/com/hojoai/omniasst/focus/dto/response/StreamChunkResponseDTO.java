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
package com.hojoai.omniasst.focus.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 流式响应 Chunk DTO
 * 用于解析 OpenAI 流式响应的 JSON 格式
 * 
 * @author hojoai
 * @date 2026-01-25
 */
public class StreamChunkResponseDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("object")
    private String object;
    
    @JsonProperty("created")
    private Long created;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("service_tier")
    private String serviceTier;
    
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
    
    @JsonProperty("choices")
    private List<Choice> choices;
    
    @JsonProperty("usage")
    private Object usage;
    
    @JsonProperty("obfuscation")
    private String obfuscation;
    
    public StreamChunkResponseDTO() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public Long getCreated() {
        return created;
    }
    
    public void setCreated(Long created) {
        this.created = created;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getServiceTier() {
        return serviceTier;
    }
    
    public void setServiceTier(String serviceTier) {
        this.serviceTier = serviceTier;
    }
    
    public String getSystemFingerprint() {
        return systemFingerprint;
    }
    
    public void setSystemFingerprint(String systemFingerprint) {
        this.systemFingerprint = systemFingerprint;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    public Object getUsage() {
        return usage;
    }
    
    public void setUsage(Object usage) {
        this.usage = usage;
    }
    
    public String getObfuscation() {
        return obfuscation;
    }
    
    public void setObfuscation(String obfuscation) {
        this.obfuscation = obfuscation;
    }
    
    /**
     * 获取 content 内容
     * @return content 字符串，如果不存在则返回 null
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            Choice choice = choices.get(0);
            if (choice != null && choice.getDelta() != null) {
                return choice.getDelta().getContent();
            }
        }
        return null;
    }
    
    /**
     * Choice 对象
     */
    public static class Choice {
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("delta")
        private Delta delta;
        
        @JsonProperty("finish_reason")
        private String finishReason;
        
        public Choice() {
        }
        
        public Integer getIndex() {
            return index;
        }
        
        public void setIndex(Integer index) {
            this.index = index;
        }
        
        public Delta getDelta() {
            return delta;
        }
        
        public void setDelta(Delta delta) {
            this.delta = delta;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
    
    /**
     * Delta 对象
     */
    public static class Delta {
        @JsonProperty("content")
        private String content;
        
        public Delta() {
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
