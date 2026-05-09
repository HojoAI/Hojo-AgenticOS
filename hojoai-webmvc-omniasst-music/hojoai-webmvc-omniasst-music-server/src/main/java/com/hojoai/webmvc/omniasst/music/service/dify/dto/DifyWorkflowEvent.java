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
package com.hojoai.webmvc.omniasst.music.service.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dify Workflow事件DTO
 * 
 * @author shengxianfei
 * @since 2026-01-21
 */
public class DifyWorkflowEvent {
    
    /**
     * 事件类型：text_chunk, workflow_finished等
     */
    private String event;
    
    /**
     * workflow运行ID
     */
    @JsonProperty("workflow_run_id")
    private String workflowRunId;
    
    /**
     * 任务ID
     */
    @JsonProperty("task_id")
    private String taskId;
    
    /**
     * 事件数据
     */
    private WorkflowEventData data;
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getWorkflowRunId() {
        return workflowRunId;
    }
    
    public void setWorkflowRunId(String workflowRunId) {
        this.workflowRunId = workflowRunId;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public WorkflowEventData getData() {
        return data;
    }
    
    public void setData(WorkflowEventData data) {
        this.data = data;
    }
    
    /**
     * Workflow事件数据
     */
    public static class WorkflowEventData {
        /**
         * 文本内容（text_chunk事件）
         */
        private String text;
        
        /**
         * workflow ID（workflow_finished事件）
         */
        private String id;
        
        /**
         * 状态（workflow_finished事件）
         */
        private String status;
        
        /**
         * 输出内容（workflow_finished事件）
         */
        private WorkflowOutputs outputs;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public WorkflowOutputs getOutputs() {
            return outputs;
        }
        
        public void setOutputs(WorkflowOutputs outputs) {
            this.outputs = outputs;
        }
    }
    
    /**
     * Workflow输出
     */
    public static class WorkflowOutputs {
        /**
         * 输出文本
         */
        private String text;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
}

