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
package com.hojoai.omniasst.management.service.meeting.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hojoai.omniasst.management.common.PageRequest;

import java.io.Serializable;
import java.util.Objects;

/**
 * The request bean for querying meeting records.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingListRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The user id.
     */
    private String userId;

    /**
     * The keyword for fuzzy search (match title or meeting date).
     */
    private String keyword;

    /**
     * The summary status filter (0: not completed, 1: completed).
     */
    private Integer summaryStatus;

    public MeetingListRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(Integer summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeetingListRequest that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(keyword, that.keyword)
                && Objects.equals(summaryStatus, that.summaryStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, keyword, summaryStatus);
    }
}
