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
package com.hojoai.omniasst.management.service.message.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 进程任务列表单条响应
 *
 * @author David
 * @version 1.0, 2026/03/17
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskItemResponse {

    private String taskId;

    /** meeting / translation */
    private String type;

    private String title;

    private String status;

    private String progressText;

    private Integer estimatedSeconds;

    private String relatedId;

    private String createdAt;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgressText() {
        return progressText;
    }

    public void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    public Integer getEstimatedSeconds() {
        return estimatedSeconds;
    }

    public void setEstimatedSeconds(Integer estimatedSeconds) {
        this.estimatedSeconds = estimatedSeconds;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
