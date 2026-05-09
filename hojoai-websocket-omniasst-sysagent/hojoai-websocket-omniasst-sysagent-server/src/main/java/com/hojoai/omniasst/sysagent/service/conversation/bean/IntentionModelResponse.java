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
package com.hojoai.omniasst.sysagent.service.conversation.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * 意图模型 HTTP 响应：状态 + {@link Data}（含动作、直接应答或多步 {@link Data.TaskSequence}）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntentionModelResponse {
    /** HTTP/业务状态码，如 200。 */
    private Integer statusCode;
    /** 提示或错误信息。 */
    private String message;
    /** 业务体。 */
    private Data data;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * 意图模型业务数据：直接应答或子任务列表。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        /** 多步子任务序列（与 {@link com.hojoai.omniasst.sysagent.constans.ConversationConstant#INTENTION_ACTION_AGENT} 配合）。 */
        private List<TaskSequence> taskSequence;
        /** 意图动作：reject、callAgent 等。 */
        private String action;
        /** reject 时直接返回给用户的文本。 */
        private String answer;
        /** 会话标题等元信息。 */
        private String title;

        /**
         * 单步子任务：由 {@link com.hojoai.omniasst.sysagent.service.conversation.intention.impl.IntentionServiceImpl} 路由到具体 Agent。
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class TaskSequence {
            /** 任务名称/类型。 */
            private String task;
            /** 任务 ID。 */
            private String taskId;
            /** 任务参数列表。 */
            private List<Map<String, String>> args;
            /** 目标子智能体，对应 {@link com.hojoai.omniasst.sysagent.constans.ConversationConstant} 中 AGENT_*。 */
            private String targetAgent;
            /** 是否耳机场景；意图模型未返回时默认为 false，调度前由会话上下文写入。 */
            private boolean headphone;
            /**
             * 垂直智能体在耳机模式下：下游 {@code subType=bubble} 时保留 bubble，并复制一份相同正文为 {@code speech}（SSE 与落库均两份）；
             * 由 {@link com.hojoai.omniasst.sysagent.service.conversation.intention.impl.IntentionServiceImpl} 按路由写入，不参与意图模型 JSON。
             */
            private boolean mapBubbleSubtypeToSpeech;

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

        public List<TaskSequence> getTaskSequence() {
            return taskSequence;
        }

        public void setTaskSequence(List<TaskSequence> taskSequence) {
            this.taskSequence = taskSequence;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "taskSequence=" + taskSequence +
                    ", action='" + action + '\'' +
                    ", answer='" + answer + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "IntentionModelResponse{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
