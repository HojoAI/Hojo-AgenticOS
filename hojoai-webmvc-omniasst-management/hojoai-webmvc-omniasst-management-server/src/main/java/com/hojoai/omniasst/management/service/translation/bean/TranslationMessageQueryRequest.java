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
package com.hojoai.omniasst.management.service.translation.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hojoai.omniasst.management.common.PageRequest;

import java.io.Serializable;
import java.util.Objects;

/**
 * The request bean for querying translation messages.
 *
 * @author zhangxuepeng
 * @version 1.0, 2025/12/16
 * @since hojoai-webmvc-omniasst-management 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationMessageQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The translation conversation id.
     */
    private String translationConversationId;

    public TranslationMessageQueryRequest() {
    }

    public String getTranslationConversationId() {
        return translationConversationId;
    }

    public void setTranslationConversationId(String translationConversationId) {
        this.translationConversationId = translationConversationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TranslationMessageQueryRequest that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(translationConversationId, that.translationConversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), translationConversationId);
    }
}
