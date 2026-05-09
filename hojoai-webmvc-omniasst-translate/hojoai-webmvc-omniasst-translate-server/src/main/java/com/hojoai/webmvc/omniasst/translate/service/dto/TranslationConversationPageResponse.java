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
package com.hojoai.webmvc.omniasst.translate.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
 * 会话分页包装（若与 {@link com.hojoai.common.page.IPage} 组合使用时的备用结构）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationConversationPageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页 */
    private Integer pageId;
    /** 每页条数 */
    private Integer pageSize;
    /** 总条数 */
    private Long total;
    /** 当前页数据 */
    private List<TranslationConversationListResponse> records;

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<TranslationConversationListResponse> getRecords() {
        return records;
    }

    public void setRecords(List<TranslationConversationListResponse> records) {
        this.records = records;
    }
}

