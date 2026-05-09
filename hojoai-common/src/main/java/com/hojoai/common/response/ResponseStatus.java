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
package com.hojoai.common.response;

/**
 * The common interface for the inner response status.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-common 1.0.0
 */
public interface ResponseStatus {

    /**
     * 获取响应状态码。
     *
     * @return 状态码字符串
     */
    String getResCode();

    /**
     * 设置响应状态码。
     *
     * @param resCode 状态码
     */
    void setResCode(String resCode);

    /**
     * 获取响应说明信息。
     *
     * @return 说明文案
     */
    String getResMessage();

    /**
     * 设置响应说明信息。
     *
     * @param resMessage 说明文案
     */
    void setResMessage(String resMessage);
}
