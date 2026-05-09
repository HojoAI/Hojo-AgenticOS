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
package com.hojoai.central.provider.dto.response.train;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainListItem {
    @JsonProperty("trainnum") private String trainnum;
    @JsonProperty("depstan") private String depstan;
    @JsonProperty("arrstan") private String arrstan;
    @JsonProperty("depstancode") private String depstancode;
    @JsonProperty("arrstancode") private String arrstancode;
    @JsonProperty("plandeptime") private String plandeptime;
    @JsonProperty("planarrtime") private String planarrtime;
    @JsonProperty("duration") private Integer duration;

    public String getTrainnum() { return trainnum; }
    public void setTrainnum(String trainnum) { this.trainnum = trainnum; }
    public String getDepstan() { return depstan; }
    public void setDepstan(String depstan) { this.depstan = depstan; }
    public String getArrstan() { return arrstan; }
    public void setArrstan(String arrstan) { this.arrstan = arrstan; }
    public String getDepstancode() { return depstancode; }
    public void setDepstancode(String depstancode) { this.depstancode = depstancode; }
    public String getArrstancode() { return arrstancode; }
    public void setArrstancode(String arrstancode) { this.arrstancode = arrstancode; }
    public String getPlandeptime() { return plandeptime; }
    public void setPlandeptime(String plandeptime) { this.plandeptime = plandeptime; }
    public String getPlanarrtime() { return planarrtime; }
    public void setPlanarrtime(String planarrtime) { this.planarrtime = planarrtime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
