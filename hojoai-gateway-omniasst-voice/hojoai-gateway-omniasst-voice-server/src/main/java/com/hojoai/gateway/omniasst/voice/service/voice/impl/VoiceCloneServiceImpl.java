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
package com.hojoai.gateway.omniasst.voice.service.voice.impl;

import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneProvider;
import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneProviderFactory;
import com.hojoai.gateway.omniasst.voice.service.voice.VoiceCloneService;
import com.hojoai.gateway.omniasst.voice.service.voice.dto.VoiceCloneResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 声音克隆门面：按请求中的 provider 名称从 {@link VoiceCloneProviderFactory} 选取 {@link VoiceCloneProvider} 执行。
 */
@Service
public class VoiceCloneServiceImpl implements VoiceCloneService {
    private static final Logger logger = LoggerFactory.getLogger(VoiceCloneServiceImpl.class);

    @Resource
    private VoiceCloneProviderFactory voiceCloneProviderFactory;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 未注册的 {@code provider}
     */
    @Override
    public VoiceCloneResponse cloneVoice(MultipartFile audioFile, String fileType, String provider) {
        logger.info("Delegating voice clone request to provider: {}, fileType: {}", provider, fileType);
        VoiceCloneProvider voiceCloneProvider = voiceCloneProviderFactory.getProvider(provider);
        if (voiceCloneProvider == null) {
            throw new IllegalArgumentException("Unsupported voice clone provider: " + provider);
        }
        return voiceCloneProvider.cloneVoice(audioFile, fileType);
    }
}
