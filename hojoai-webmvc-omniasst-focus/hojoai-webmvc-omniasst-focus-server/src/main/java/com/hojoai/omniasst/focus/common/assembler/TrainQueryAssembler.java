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
package com.hojoai.omniasst.focus.common.assembler;

import com.hojoai.central.provider.grpc.TrainListItem;
import com.hojoai.central.provider.grpc.TrainTableResponse;
import com.hojoai.omniasst.focus.dto.response.TrainQueryResponseDTO;
import com.hojoai.omniasst.focus.dto.val.TrainVal;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 将 gRPC 火车查询响应转换为 TrainQueryResponseDTO
 *
 * @author My
 * @version 1.0, 2026/03/06
 * @since hojoai-webmvc-omniasst-focus 1.0.0
 */
public final class TrainQueryAssembler {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private TrainQueryAssembler() {
    }

    /**
     * 从 gRPC TrainTableResponse 映射到 TrainQueryResponseDTO
     */
    public static TrainQueryResponseDTO fromGrpcResponse(TrainTableResponse resp) {
        TrainQueryResponseDTO dto = new TrainQueryResponseDTO();
        if (resp == null || resp.getData().getTrainlistList().isEmpty()) {
            dto.setCount(0);
            dto.setTrainList(Collections.emptyList());
            return dto;
        }
        List<TrainVal> list = resp.getData().getTrainlistList().stream()
                .map(TrainQueryAssembler::toTrainVal)
                .collect(Collectors.toList());
        dto.setCount(list.size());
        dto.setTrainList(list);
        return dto;
    }

    private static TrainVal toTrainVal(TrainListItem info) {
        if (info == null) {
            return new TrainVal();
        }
        TrainVal val = new TrainVal();
        val.setTrainNo(emptyToNull(info.getTrainnum()));
        // val.setSeatNo(emptyToNull(info.getSeatNo()));
        // val.setTicketGate(emptyToNull(info.getTicketGate()));
        val.setDepStan(emptyToNull(info.getDepstan()));
        val.setArrStan(emptyToNull(info.getArrstan()));
        val.setDepCode(emptyToNull(info.getDepstancode()));
        val.setArrCode(emptyToNull(info.getArrstancode()));
        val.setDepPlanTime(stringToDate(info.getPlandeptime()));
        val.setArrPlanTime(stringToDate(info.getPlanarrtime()));
        val.setDuration(info.getDuration() != 0 ? info.getDuration() : null);
        return val;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static Date stringToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
