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
package com.hojoai.omniasst.focus.domain.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.omniasst.focus.dto.val.AttachmentItemVal;
import com.hojoai.omniasst.focus.repository.mapper.ScheduleMapper;
import com.hojoai.omniasst.focus.repository.po.SchedulePO;
import com.hojoai.omniasst.focus.domain.ScheduleAttachmentDomainService;
import com.hojoai.omniasst.focus.service.api.ManagementAPI;
import com.hojoai.omniasst.focus.service.api.bean.PreSignUrlResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleAttachmentDomainServiceImpl implements ScheduleAttachmentDomainService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleAttachmentDomainServiceImpl.class);
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 90L * 1024 * 1024;
    private static final String PATH_PREFIX = "schedule";

    @Resource
    private ManagementAPI managementAPI;
    @Resource
    private ScheduleMapper scheduleMapper;

    @Async
    @Override
    public void saveAttachmentsToOssAsync(Long scheduleId, String userId, List<MultipartFile> files) {
        if (scheduleId == null || StringUtils.isBlank(userId) || CollectionUtils.isEmpty(files)) {
            return;
        }
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_BYTES) {
                log.warn("schedule attachment exceeds 10MB, skipped scheduleId={} fileName={} size={}", scheduleId, file.getOriginalFilename(), file.getSize());
                return;
            }
            totalSize += file.getSize();
        }
        if (totalSize > MAX_TOTAL_BYTES) {
            log.warn("schedule attachments total exceeds 90MB, scheduleId={} total={}", scheduleId, totalSize);
            return;
        }
        List<AttachmentItemVal> successList = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String fileName = StringUtils.isNotBlank(file.getOriginalFilename()) ? FilenameUtils.getName(file.getOriginalFilename()) : "file";
                String contentType = StringUtils.isNotBlank(file.getContentType()) ? file.getContentType() : "application/octet-stream";
                String path = PATH_PREFIX + "/" + userId + "/" + scheduleId + "/" + UUID.randomUUID().toString().replace("-", "") + "_" + fileName;
                Response<PreSignUrlResponse> resp = managementAPI.getPresignUrl(path, contentType, "");
                PreSignUrlResponse data = resp != null ? resp.getResContent() : null;
                if (data == null || StringUtils.isBlank(data.getPutUrl())) {
                    log.warn("get presign url failed scheduleId={} path={}", scheduleId, path);
                    continue;
                }
                String putUrl = data.getPutUrl();
                byte[] bytes = file.getBytes();
                WebClient.create().put().uri(putUrl).contentType(MediaType.parseMediaType(contentType)).bodyValue(bytes).retrieve().toBodilessEntity().block();
                AttachmentItemVal item = new AttachmentItemVal();
                item.setFileName(fileName);
                item.setContentType(contentType);
                item.setPath(path);
                successList.add(item);
            } catch (Exception e) {
                log.warn("upload schedule attachment failed scheduleId={} fileName={}", scheduleId, file.getOriginalFilename(), e);
            }
        }
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        String attachmentListJson = JacksonUtil.bean2Json(successList);
        scheduleMapper.update(null, new LambdaUpdateWrapper<SchedulePO>().eq(SchedulePO::getId, scheduleId).set(SchedulePO::getAttachmentList, attachmentListJson));
    }
}
