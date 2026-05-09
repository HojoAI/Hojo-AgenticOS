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
package com.hojoai.webmvc.omniasst.meeting.service.bean;

/**
 * 个人声纹注册与前端约定的界面分支码（见产品弹窗：保存成功/失败、录制合格性检测）。
 */
public final class VoiceprintRegisterUiCodes {

    /** commit 成功 */
    public static final String SUCCESS = "SUCCESS";
    /** commit 保存失败（昵称落库/声纹 register/音色 save 等） */
    public static final String SAVE_FAILED = "SAVE_FAILED";
    /** prepare 录音检测未通过（通用） */
    public static final String GENERIC_ERROR = "GENERIC_ERROR";
    /** prepare 录音检测未通过（环境嘈杂等，可单独文案） */
    public static final String NOISY_ENVIRONMENT = "NOISY_ENVIRONMENT";

    private VoiceprintRegisterUiCodes() {}
}
