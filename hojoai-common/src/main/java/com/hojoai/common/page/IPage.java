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
package com.hojoai.common.page;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * The common class for the page.
 *
 * @author kevanwong
 * @version 1.0, 2018/6/27
 * @since hojoai-common 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IPage<T> {

    /** 当前页码，从 1 或 0 起算由业务约定，本类仅作承载 */
    private Integer pageId;
    /** 每页条数 */
    private Integer pageSize;
    /** 符合条件的总记录数 */
    private Long totalCount;
    /** 当前页数据列表 */
    private List<T> pageContent;

    private IPage() {
    }

    private IPage(List<T> pageContent) {
        this.pageContent = pageContent;
    }

    private IPage(Long totalCount, List<T> pageContent) {
        this.totalCount = totalCount;
        this.pageContent = pageContent;
    }

    private IPage(Integer pageId, Integer pageSize, Long totalCount, List<T> pageContent) {
        this.pageId = pageId;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.pageContent = pageContent;
    }

    /**
     * 创建空分页对象（各字段为 null，由调用方后续填充）。
     *
     * @param <T> 列表元素类型
     * @return 新的 {@link IPage} 实例
     */
    public static <T> IPage<T> newInstance() {
        return new IPage<>();
    }

    /**
     * 创建仅含当前页列表的分页对象。
     *
     * @param pageContent 当前页数据，可为 null 或空列表
     * @param <T>         列表元素类型
     * @return 新的 {@link IPage} 实例
     */
    public static <T> IPage<T> newInstance(List<T> pageContent) {
        return new IPage<>(pageContent);
    }

    /**
     * 创建含总条数与当前页列表的分页对象。
     *
     * @param totalCount  总记录数
     * @param pageContent 当前页数据，可为 null 或空列表
     * @param <T>         列表元素类型
     * @return 新的 {@link IPage} 实例
     */
    public static <T> IPage<T> newInstance(Long totalCount, List<T> pageContent) {
        return new IPage<>(totalCount, pageContent);
    }

    /**
     * 创建完整分页信息（页码、页大小、总条数、当前页数据）。
     *
     * @param pageId      当前页码
     * @param pageSize    每页条数
     * @param totalCount  总记录数
     * @param pageContent 当前页数据，可为 null 或空列表
     * @param <T>         列表元素类型
     * @return 新的 {@link IPage} 实例
     */
    public static <T> IPage<T> newInstance(Integer pageId, Integer pageSize, Long totalCount, List<T> pageContent) {
        return new IPage<>(pageId, pageSize, totalCount, pageContent);
    }

    /**
     * 获取当前页码。
     *
     * @return 页码，可能为 null
     */
    public Integer getPageId() {
        return pageId;
    }

    /**
     * 设置当前页码。
     *
     * @param pageId 页码
     */
    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页大小，可能为 null
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数。
     *
     * @param pageSize 每页大小
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取总记录数。
     *
     * @return 总条数，可能为 null
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * 设置总记录数。
     *
     * @param totalCount 总条数
     */
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 获取当前页数据列表。
     *
     * @return 列表，可能为 null
     */
    public List<T> getPageContent() {
        return pageContent;
    }

    /**
     * 设置当前页数据列表。
     *
     * @param pageContent 列表，可为 null
     */
    public void setPageContent(List<T> pageContent) {
        this.pageContent = pageContent;
    }
}
