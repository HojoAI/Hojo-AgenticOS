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
package com.hojoai.webmvc.omniasst.meeting.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreSignUrlResponse {
    private String path;
    private String putUrl;
    private String getUrl;
    private String pureUrl;
    private String resizeUrl;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getPutUrl() { return putUrl; }
    public void setPutUrl(String putUrl) { this.putUrl = putUrl; }
    public String getGetUrl() { return getUrl; }
    public void setGetUrl(String getUrl) { this.getUrl = getUrl; }
    public String getPureUrl() { return pureUrl; }
    public void setPureUrl(String pureUrl) { this.pureUrl = pureUrl; }
    public String getResizeUrl() { return resizeUrl; }
    public void setResizeUrl(String resizeUrl) { this.resizeUrl = resizeUrl; }
}

