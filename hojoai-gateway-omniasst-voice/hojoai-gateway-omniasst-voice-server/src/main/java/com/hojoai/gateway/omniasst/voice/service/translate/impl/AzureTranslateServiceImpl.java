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
package com.hojoai.gateway.omniasst.voice.service.translate.impl;

import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransRequest;
import com.hojoai.gateway.omniasst.voice.api.dto.AsrTransResponse;
import com.hojoai.gateway.omniasst.voice.config.AzureSpeechConfig;
import com.hojoai.gateway.omniasst.voice.service.azure.BiDirectionalTranslationSession;
import com.hojoai.gateway.omniasst.voice.service.azure.AzureTranslationSessionManager;
import com.hojoai.gateway.omniasst.voice.service.translate.TranslateService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Azure翻译服务实现
 * 基于Microsoft Azure Speech Service实现音频流式翻译
 */
@Service("azureTranslateService")
public class AzureTranslateServiceImpl implements TranslateService {

    private static final Logger logger = LoggerFactory.getLogger(AzureTranslateServiceImpl.class);

    @Resource
    private AzureTranslationSessionManager sessionManager;

    @Resource
    private AzureSpeechConfig azureConfig;

    /**
     * 处理音频翻译请求
     * 核心流程:
     * 1. 第一个音频包(packageIndex=0)创建Azure会话
     * 2. 持续推送音频到Azure流
     * 3. 返回Azure事件更新的识别和翻译结果
     * 4. 当Azure触发recognized事件时返回isEnd=1
     * 5. 清理会话
     * 
     * @param request 翻译请求
     * @return 翻译响应
     */
    @Override
    public AsrTransResponse translate(AsrTransRequest request) {
        String vid = request.getVid();
        int packageIndex = request.getPackageIndex();
        String sourceLang = request.getSourceLang();
        String targetLang = request.getLang();

        logger.info(">>>>>> [语音服务收到请求] vid: {}, packageIndex: {}, 双向翻译: {} <-> {}, packageSize: {} <<<<<<", 
            vid, packageIndex, sourceLang, targetLang, request.getPackageSize());

        try {
            // 参数校验
            if (StringUtils.isBlank(vid) || StringUtils.isBlank(sourceLang) || StringUtils.isBlank(targetLang) || StringUtils.isBlank(request.getPackageData())) {
                logger.warn("[参数校验失败] vid: {}, sourceLang: {}, targetLang: {}, packageData: {}", 
                    vid, sourceLang, targetLang, request.getPackageData() != null ? "存在" : "null");
                return buildErrorResponse(vid, 1, "参数不完整");
            }

            // 解析音频数据
            byte[] audioData;
            try {
                audioData = Base64.getDecoder().decode(request.getPackageData());
                logger.debug("[音频数据解析] vid: {}, Base64长度: {}, 解码后大小: {} bytes", 
                    vid, request.getPackageData().length(), audioData.length);
            } catch (IllegalArgumentException e) {
                logger.error("[音频解码失败] vid: {}", vid, e);
                return buildErrorResponse(vid, 1, "音频数据格式错误");
            }

            // 获取或创建Azure双向翻译会话
            // 支持双向翻译：sourceLang <-> targetLang
            BiDirectionalTranslationSession session = sessionManager.getOrCreateSession(vid, sourceLang, targetLang);

            // 推送音频到Azure流
            session.pushAudio(audioData);
            logger.debug("[音频已推送] vid: {}, packageIndex: {}, 音频大小: {} bytes", vid, packageIndex, audioData.length);

            // 构造响应
            AsrTransResponse response = new AsrTransResponse();
            response.setRtn(0);
            response.setVid(vid);

            // 创建文本对象
            AsrTransResponse.AsrTransText text = new AsrTransResponse.AsrTransText();

            // 检查Azure是否触发了recognized事件(句子结束)
            if (session.isSentenceDone()) {
                // Azure认为句子说完了
                response.setIsEnd(1);
                response.setLangDetect(session.getDetectedLanguage());  // 返回检测到的语言
                text.setFixedText(session.getFinalText());
                text.setTmpText(session.getFinalText());
                text.setText(session.getFinalTranslation());
                
                logger.info(">>>>>> [返回最终结果 - 句子结束] vid: {}, packageIndex: {} <<<<<<", vid, packageIndex);
                logger.info("isEnd: 1 (句子结束)");
                logger.info("最终识别文本(fixedText): [{}]", session.getFinalText());
                logger.info("最终翻译文本(text): [{}]", session.getFinalTranslation());
                logger.info("检测到的源语言(langDetect): {}", session.getDetectedLanguage());
                logger.info("===================================================================");

                // 清理会话，准备下一轮
                sessionManager.removeSession(vid);

            } else {
                // 句子还在继续，返回临时结果
                response.setIsEnd(0);
                // 如果已经检测到语言，也返回
                if (StringUtils.isNotBlank(session.getDetectedLanguage())) {
                    response.setLangDetect(session.getDetectedLanguage());
                }
                text.setTmpText(session.getTmpText());
                text.setText(session.getTmpTranslation());
                text.setFixedText("");

                logger.info(">>>>>> [返回临时结果 - 句子进行中] vid: {}, packageIndex: {} <<<<<<", vid, packageIndex);
                logger.info("isEnd: 0 (句子进行中)");
                logger.info("临时识别文本(tmpText): [{}]", session.getTmpText());
                logger.info("临时翻译文本(text): [{}]", session.getTmpTranslation());
                if (StringUtils.isNotBlank(session.getDetectedLanguage())) {
                    logger.info("检测到的源语言(langDetect): {}", session.getDetectedLanguage());
                }
                logger.info("===================================================================");
            }

            response.setText(text);
            return response;

        } catch (Exception e) {
            logger.error("[翻译处理异常] vid: {}, packageIndex: {}", vid, packageIndex, e);
            // 异常时清理会话
            sessionManager.removeSession(vid);
            return buildErrorResponse(vid, 1, "翻译处理失败: " + e.getMessage());
        }
    }

    /**
     * 构造错误响应
     * 
     * @param vid 会话ID
     * @param rtn 错误码
     * @param msg 错误消息
     * @return 错误响应对象
     */
    private AsrTransResponse buildErrorResponse(String vid, int rtn, String msg) {
        AsrTransResponse response = new AsrTransResponse();
        response.setRtn(rtn);
        response.setVid(vid);
        response.setMsg(msg);
        response.setIsEnd(0);
        
        AsrTransResponse.AsrTransText text = new AsrTransResponse.AsrTransText();
        text.setText("");
        text.setTmpText("");
        text.setFixedText("");
        response.setText(text);
        
        return response;
    }
}
