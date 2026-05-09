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
package com.hojoai.omniasst.sysagent.push.model;

/**
 * 友盟推送调用结果：成功含 msgId/taskId，失败含 error 信息。
 */
public class PushResult {

    /** 是否调用成功。 */
    private boolean success;
    /** 友盟消息 ID。 */
    private String msgId;
    /** 任务 ID。 */
    private String taskId;
    /** 错误码。 */
    private String errorCode;
    /** 错误描述。 */
    private String errorMsg;

    public PushResult() {}

    public PushResult(boolean success, String msgId, String taskId, String errorCode, String errorMsg) {
        this.success = success;
        this.msgId = msgId;
        this.taskId = taskId;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 构造成功结果。
     *
     * @param msgId  消息 ID
     * @param taskId 任务 ID
     */
    public static PushResult success(String msgId, String taskId) {
        PushResult result = new PushResult();
        result.setSuccess(true);
        result.setMsgId(msgId);
        result.setTaskId(taskId);
        return result;
    }

    /** @param errorMsg 错误描述 */
    public static PushResult fail(String errorMsg) {
        return fail(errorMsg, null);
    }

    /**
     * @param errorMsg  错误描述
     * @param errorCode 错误码，可为 null
     */
    public static PushResult fail(String errorMsg, String errorCode) {
        PushResult result = new PushResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        result.setErrorCode(errorCode);
        return result;
    }
}