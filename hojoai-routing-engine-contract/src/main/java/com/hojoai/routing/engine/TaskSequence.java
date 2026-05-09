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
package com.hojoai.routing.engine;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * 单步子任务：由路由引擎根据 {@code targetAgent} 分发到具体 Agent。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSequence {

    /** 任务名称/类型。 */
    private String task;

    /** 任务 ID。 */
    private String taskId;

    /** 任务参数列表。 */
    private List<Map<String, String>> args;

    /** 目标子智能体，对应 {@link AgentRoutingConstants} 中 AGENT_*。 */
    private String targetAgent;

    /** 是否耳机场景；由路由引擎根据会话上下文写入。 */
    private boolean headphone;

    /**
     * 垂直智能体在耳机模式下：下游 {@code subType=bubble} 时保留 bubble，
     * 并复制一份相同正文为 {@code speech}（SSE 与落库均两份）；
     * 由路由引擎按路由策略写入，不参与意图模型 JSON。
     */
    private boolean mapBubbleSubtypeToSpeech;

    public TaskSequence() {
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<Map<String, String>> getArgs() {
        return args;
    }

    public void setArgs(List<Map<String, String>> args) {
        this.args = args;
    }

    public String getTargetAgent() {
        return targetAgent;
    }

    public void setTargetAgent(String targetAgent) {
        this.targetAgent = targetAgent;
    }

    public boolean isHeadphone() {
        return headphone;
    }

    public void setHeadphone(boolean headphone) {
        this.headphone = headphone;
    }

    public boolean isMapBubbleSubtypeToSpeech() {
        return mapBubbleSubtypeToSpeech;
    }

    public void setMapBubbleSubtypeToSpeech(boolean mapBubbleSubtypeToSpeech) {
        this.mapBubbleSubtypeToSpeech = mapBubbleSubtypeToSpeech;
    }

    @Override
    public String toString() {
        return "TaskSequence{" +
                "task='" + task + '\'' +
                ", taskId='" + taskId + '\'' +
                ", args=" + args +
                ", targetAgent='" + targetAgent + '\'' +
                ", headphone=" + headphone +
                ", mapBubbleSubtypeToSpeech=" + mapBubbleSubtypeToSpeech +
                '}';
    }
}
