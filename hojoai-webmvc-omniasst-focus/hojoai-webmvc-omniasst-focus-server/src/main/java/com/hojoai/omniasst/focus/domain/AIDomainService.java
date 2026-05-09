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
package com.hojoai.omniasst.focus.domain;

import com.hojoai.central.provider.service.common.enums.CommonEnum;
import reactor.core.publisher.Flux;

/**
 * AI Domain Service 接口
 * 使用 Spring AI Alibaba 框架进行大模型调用
 * 
 * @author hojoai
 * @date 2026-01-09
 */
public interface AIDomainService {

    /**
     * 非流式输出 - 调用大模型获取完整响应
     *
     * @param typeEnum prompt类型 (default/schedule-create/creative/technical等)
     * @param requestDTO 用户查询内容
     * @return AI回复内容
     */
    <T> T chat(CommonEnum typeEnum, Object requestDTO, Class<T> responseClass);

    /**
     * 流式输出 - 调用大模型获取流式响应
     * 自动解析流式响应的 JSON 格式，提取 choices[0].delta.content 字段
     *
     * @param typeEnum prompt类型 (default/schedule-create/creative/technical等)
     * @param requestDTO 用户输入
     * @return 流式响应Flux，返回提取的 content 字符串
     */
    Flux<String> chatStream(CommonEnum typeEnum, Object requestDTO);

}

