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
package com.hojoai.webmvc.omniasst.translate.websocket.bean;

/**
 * 下行消息中与协议对齐的 pdata 包装。
 *
 * @param <T> pdata 业务体类型
 */
public class BackCustomPayloadMessage<T> {
    /** 业务数据 */
    private T pdata;
    public T getPdata() { return pdata; }
    public void setPdata(T pdata) { this.pdata = pdata; }
}

