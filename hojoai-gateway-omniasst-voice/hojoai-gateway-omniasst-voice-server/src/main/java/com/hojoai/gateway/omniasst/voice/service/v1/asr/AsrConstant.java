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
package com.hojoai.gateway.omniasst.voice.service.v1.asr;

/**
 * ASR v1 与下游请求共用的默认参数常量。
 */
public class AsrConstant {
    /** 随 ASR 包上报的默认 VAD 结束参数（与算法侧约定） */
    public static final int ASR_VAD_END_DEFAULT = 55;
    /** 默认识别任务类型（如 all） */
    public static final String ASR_TASK_DEFAULT = "all";
}
