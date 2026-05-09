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
package com.hojoai.omniasst.focus.dto.response;

import com.hojoai.omniasst.focus.dto.val.TrainVal;

import java.util.List;

/**
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public class TrainQueryResponseDTO {

    private Integer count;

    private List<TrainVal> trainList;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<TrainVal> getTrainList() {
        return trainList;
    }

    public void setTrainList(List<TrainVal> trainList) {
        this.trainList = trainList;
    }

}
