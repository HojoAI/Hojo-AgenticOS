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
package com.hojoai.omniasst.management.service.recommendation.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hojoai.common.date.DateUtil;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.management.common.RecommendationStatusEnum;
import com.hojoai.omniasst.management.common.RecommendationTypeEnum;
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationDAO;
import com.hojoai.omniasst.management.dao.recommendation.PaRecommendationSuppressDAO;
import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendation;
import com.hojoai.omniasst.management.dao.recommendation.po.PaRecommendationSuppress;
import com.hojoai.omniasst.management.service.recommendation.RecommendationService;
import com.hojoai.omniasst.management.service.recommendation.bean.AttachmentItem;
import com.hojoai.omniasst.management.service.recommendation.bean.CreateRecommendationRequest;
import com.hojoai.omniasst.management.service.recommendation.bean.RecommendationItemResponse;
import com.hojoai.omniasst.management.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 「为你推荐」业务：创建推荐、列表查询与重复曝光抑制。
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private static final String REC_ID_PREFIX = "REC";
    private static final int SUPPRESS_THRESHOLD = 3;
    private static final int SUPPRESS_DAYS = 7;

    @Resource
    private PaRecommendationDAO recommendationDAO;

    @Resource
    private PaRecommendationSuppressDAO suppressDAO;

    @Override
    public List<RecommendationItemResponse> listRecommendations(String userId, List<String> types) {
        List<String> suppressedTypes = suppressDAO.selectSuppressedTypes(userId);
        List<PaRecommendation> records = recommendationDAO.selectLatestPerType(userId, suppressedTypes, types);
        return records.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreRecommendation(String userId, String recommendationId) {
        PaRecommendation rec = recommendationDAO.selectByRecommendationId(recommendationId);
        ThrowUtils.ThrowIf(rec == null, ResponseStatusBasic.ILLEGAL_ARGUMENT, "Recommendation not found");
        ThrowUtils.ThrowIf(!RecommendationStatusEnum.ACTIVE.getValue().equals(rec.getStatus()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Recommendation is not active");
        ThrowUtils.ThrowIf(!rec.getUserId().equals(userId), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Recommendation does not belong to user");

        Date now = new Date();
        recommendationDAO.updateStatusToIgnored(recommendationId, now);
        logger.info("Recommendation ignored, id: {}, userId: {}, type: {}", recommendationId, userId, rec.getType());

        // 统计近 7 天该类型忽略次数（含本次），判断是否触发抑制
        Date windowStart = daysAgo(SUPPRESS_DAYS);
        long ignoreCount = recommendationDAO.countIgnoredSince(userId, rec.getType(), windowStart);

        if (ignoreCount >= SUPPRESS_THRESHOLD) {
            Date suppressedUntil = daysLater(SUPPRESS_DAYS);
            upsertSuppress(userId, rec.getType(), (int) ignoreCount, suppressedUntil);
            logger.info("Type suppressed for 7 days, userId: {}, type: {}, until: {}", userId, rec.getType(), suppressedUntil);
        }
    }

    @Override
    public String createRecommendation(CreateRecommendationRequest request) {
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getUserId()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "userId cannot be blank");
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getType()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "type cannot be blank");
        ThrowUtils.ThrowIf(!RecommendationTypeEnum.exists(request.getType()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "Invalid recommendation type: " + request.getType());
        ThrowUtils.ThrowIf(StringUtils.isBlank(request.getTitle()), ResponseStatusBasic.ILLEGAL_ARGUMENT, "title cannot be blank");

        // 去重：同一用户+类型+关联业务只允许一条 active
        if (StringUtils.isNotBlank(request.getRelatedId())) {
            PaRecommendation existing = recommendationDAO.selectActiveByUserTypeRelated(
                    request.getUserId(), request.getType(), request.getRelatedId());
            if (existing != null) {
                logger.info("Duplicate recommendation skipped, userId: {}, type: {}, relatedId: {}",
                        request.getUserId(), request.getType(), request.getRelatedId());
                return existing.getRecommendationId();
            }
        }

        String recId = REC_ID_PREFIX + IdWorker.getIdStr();
        PaRecommendation rec = new PaRecommendation();
        rec.setRecommendationId(recId);
        rec.setUserId(request.getUserId());
        rec.setType(request.getType());
        rec.setTitle(request.getTitle());
        rec.setBody(request.getBody());
        rec.setRelatedId(request.getRelatedId());
        rec.setRelatedType(request.getRelatedType());
        rec.setLandingPage(request.getLandingPage());
        rec.setStatus(RecommendationStatusEnum.ACTIVE.getValue());
        rec.setCreatedAt(new Date());

        if (request.getExtra() != null) {
            rec.setExtra(JacksonUtil.bean2Json(request.getExtra()));
        }
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            rec.setAttachments(JacksonUtil.bean2Json(request.getAttachments()));
        }
        if (request.getExpiresAt() != null) {
            rec.setExpiresAt(new Date(request.getExpiresAt()));
        }

        recommendationDAO.insert(rec);
        logger.info("Recommendation created, id: {}, userId: {}, type: {}", recId, request.getUserId(), request.getType());
        return recId;
    }

    // ==================== 内部方法 ====================

    private void upsertSuppress(String userId, String type, int ignoreCount, Date suppressedUntil) {
        PaRecommendationSuppress existing = suppressDAO.selectByUserIdAndType(userId, type);
        Date now = new Date();
        if (existing != null) {
            existing.setIgnoreCount(ignoreCount);
            existing.setSuppressedUntil(suppressedUntil);
            existing.setUpdatedAt(now);
            suppressDAO.updateById(existing);
        } else {
            PaRecommendationSuppress suppress = new PaRecommendationSuppress();
            suppress.setUserId(userId);
            suppress.setType(type);
            suppress.setIgnoreCount(ignoreCount);
            suppress.setSuppressedUntil(suppressedUntil);
            suppress.setCreatedAt(now);
            suppress.setUpdatedAt(now);
            suppressDAO.insert(suppress);
        }
    }

    private RecommendationItemResponse toResponse(PaRecommendation rec) {
        RecommendationItemResponse resp = new RecommendationItemResponse();
        resp.setRecommendationId(rec.getRecommendationId());
        resp.setType(rec.getType());
        resp.setTitle(rec.getTitle());
        resp.setBody(rec.getBody());
        resp.setRelatedId(rec.getRelatedId());
        resp.setRelatedType(rec.getRelatedType());
        resp.setLandingPage(rec.getLandingPage());
        resp.setCreatedAt(DateUtil.formatAsSecond(rec.getCreatedAt()));

        if (rec.getExpiresAt() != null) {
            resp.setExpiresAt(DateUtil.formatAsSecond(rec.getExpiresAt()));
        }
        if (StringUtils.isNotBlank(rec.getExtra())) {
            resp.setExtra(JacksonUtil.json2Bean(rec.getExtra(), Object.class));
        }
        if (StringUtils.isNotBlank(rec.getAttachments())) {
            resp.setAttachments(JacksonUtil.json2Bean(rec.getAttachments(), new TypeReference<List<AttachmentItem>>() {}));
        }
        return resp;
    }

    private Date daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }

    private Date daysLater(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }
}
