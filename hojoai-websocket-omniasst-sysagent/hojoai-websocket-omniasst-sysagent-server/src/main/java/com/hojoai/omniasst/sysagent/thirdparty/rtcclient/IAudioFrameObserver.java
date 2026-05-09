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
package com.hojoai.omniasst.sysagent.thirdparty.rtcclient;

import com.ding.rtc.DingRtcEngine;
import com.ding.rtc.DingRtcEngineListener;
import com.hojoai.omniasst.sysagent.service.speech.ASRService;
import com.hojoai.omniasst.sysagent.service.speech.TTSService;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRRequest;
import com.hojoai.omniasst.sysagent.service.speech.bean.ASRResponse;
import com.hojoai.omniasst.sysagent.service.speech.bean.TTSRequest;
import com.hojoai.omniasst.sysagent.service.speech.impl.ASRServiceImpl;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The audio frame observer class.
 *
 * @author kevanwong
 * @version 1.0, 2025/10/21
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@Service
public class IAudioFrameObserver extends DingRtcEngine.DingRtcAudioFrameObserver {

    private static final Logger logger = LoggerFactory.getLogger(IAudioFrameObserver.class);

    private static final int CHUNK_SIZE = 320 * 16 * 2;
    private static final String AUDIO_TASK = "all";
    private static final String AUDIO_TYPE = "pcm";
    private static final String LANG = "all";
    private static final int NOT_PACKAGE_END = 0;
    private static final int NOT_DO_LANG_DETECT = 0;
    private static final int VAD_END = 60;

    private static final String TTS_LANG = "chinese";
    private static final String TTS_VOICE_ID = "f9f865f108d383f6d65225b2f59cd9e1";

    private String vid = UUID.randomUUID().toString();
    private final AtomicInteger packageIndex = new AtomicInteger();
    private final ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

    private DingRtcEngine engine;

    @Resource(type = ASRServiceImpl.class)
    private ASRService asrService;

    @Resource
    private TTSService ttsService;

    @Override
    public void onCapturedAudioFrame(DingRtcEngine.DingRtcAudioFrame frame) {
        logger.info("on captured audio frame");
    }

    @Override
    public void onProcessCapturedAudioFrame(DingRtcEngine.DingRtcAudioFrame frame) {
        logger.info("on process captured audio frame");
    }

    @Override
    public void onPublishAudioFrame(DingRtcEngine.DingRtcAudioFrame frame) {
        logger.info("on publish audio frame");
    }

    @Override
    public void onPlaybackAudioFrame(DingRtcEngine.DingRtcAudioFrame frame) {
        logger.info("on playback audio frame");
    }

    @Override
    public void onRemoteUserAudioFrame(String uid, DingRtcEngine.DingRtcAudioFrame frame) {
        try {
            // 1.Process ASR
            String asrText = processASR(frame);
            if (StringUtils.isBlank(asrText)) {
                return;
            }
            logger.info("asr text: {}", asrText);

            // 2.Process LLM
            String llmText = processLLM(asrText);
            if (StringUtils.isBlank(llmText)) {
                return;
            }
            logger.info("llm text: {}", llmText);

            // 3.Process TTS
            processTTS(llmText);
        } catch (Exception e) {
            logger.error("on remote user audio frame error, uid: {}", uid, e);
        }
    }

    private String processASR(DingRtcEngine.DingRtcAudioFrame frame) throws IOException {
        audioStream.write(frame.buffer);

        String asrText = null;
        while (audioStream.size() >= CHUNK_SIZE) {
            byte[] chunkData = new byte[CHUNK_SIZE];
            System.arraycopy(audioStream.toByteArray(), 0, chunkData, 0, CHUNK_SIZE);
            String encodedData = Base64.getEncoder().encodeToString(chunkData);

            ASRRequest asrRequest = makeAsrRequest(encodedData);
            ASRResponse asrResponse = asrService.asr(asrRequest);

            byte[] remainingBuffer = new byte[audioStream.size() - CHUNK_SIZE];
            System.arraycopy(audioStream.toByteArray(), CHUNK_SIZE, remainingBuffer, 0, remainingBuffer.length);
            audioStream.reset();
            audioStream.write(remainingBuffer);

            if (asrResponse == null) {
                vid = UUID.randomUUID().toString();
                packageIndex.set(0);
                continue;
            }
            if (asrResponse.getRtn() != 0) {
                logger.error("asr error, vid: {}, rtn: {}, msg: {}", vid, asrResponse.getRtn(), asrResponse.getMsg());
                vid = UUID.randomUUID().toString();
                packageIndex.set(0);
                continue;
            }

            asrText = asrResponse.getText().getText();

            if (asrResponse.getIsEnd() == 1) {
                vid = UUID.randomUUID().toString();
                packageIndex.set(0);
                break;
            }
        }
        return asrText;
    }

    private String processLLM(String asrText) {
        return asrText;
    }

    private void processTTS(String llmText) {
        TTSRequest request = makeTtsRequest(llmText);
        Flux<DataBuffer> dataBufferFlux = ttsService.tts(request);

        RtcContext context = new RtcContext();
        context.engine = engine;
        context.pcmReadFreq = 25;
        context.refreshAudioFrameBytes();

        dataBufferFlux.subscribe(dataBuffer -> {
                    byte[] chunk = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(chunk);
                    DataBufferUtils.release(dataBuffer);
                    pushPcmToRtc(context, chunk);
                }, // DataBuffer handler
                error -> logger.error("push tts stream to rtc error", error), // Log error
                () -> logger.info("push tts stream to rtc ended")); // Log ended
    }

    private void pushPcmToRtc(RtcContext context, byte[] chunk) {
        if (chunk.length == 0 || context.engine == null) {
            return;
        }

        synchronized (context.audioLock) {
            try {
                context.pcmCache.write(chunk);
            } catch (IOException e) {
                logger.error("write chunk to pcm cache error", e);
                return;
            }

            byte[] cacheBytes = context.pcmCache.toByteArray();
            int processed = 0;
            while (cacheBytes.length - processed >= context.frameBytes) {
                byte[] frameData = new byte[context.frameBytes];
                System.arraycopy(cacheBytes, processed, frameData, 0, context.frameBytes);
                processed += context.frameBytes;
                pushFrame(context, frameData);
            }

            int remaining = cacheBytes.length - processed;
            context.pcmCache.reset();
            if (remaining > 0) {
                context.pcmCache.write(cacheBytes, processed, remaining);
            }
        }
    }

    private void pushFrame(RtcContext context, byte[] frameData) {
        DingRtcEngine.DingRtcAudioFrame frame = new DingRtcEngine.DingRtcAudioFrame();
        frame.bytesPerSample = context.bytesPerSample;
        frame.samplesPerSec = context.pcmSampleRate;
        frame.numChannels = context.pcmChannels;
        // Create a copy of frameData to avoid buffer reuse issues
        byte[] buffer = new byte[frameData.length];
        System.arraycopy(frameData, 0, buffer, 0, frameData.length);
        frame.buffer = buffer;
        frame.numSamples = buffer.length / frame.bytesPerSample / frame.numChannels;
        frame.timestamp = context.pushedSampleCount * 1000 / context.pcmSampleRate;

        while (true) {
            int ret = context.engine.pushExternalAudioFrame(frame);
            if (ret == 0) {
                context.pushedSampleCount += frame.numSamples;
                break;
            }
            if (ret == DingRtcEngine.DingRtcErrorCode.DingRtcErrorAudioBufferFull.getValue()) {
                // SDK buffer full, wait some time, ex: 40ms
                sleep(40);
                continue;
            }
            logger.error("pushExternalAudioFrame failed, ret: {}", ret);
            break;
        }
    }

    private ASRRequest makeAsrRequest(String encodedData) {
        ASRRequest request = new ASRRequest();
        request.setTask(AUDIO_TASK);
        request.setVid(vid);
        request.setPackageIndex(packageIndex.getAndIncrement());
        request.setPackageSize(encodedData.length());
        request.setPackageEnd(NOT_PACKAGE_END);
        request.setType(AUDIO_TYPE);
        request.setLang(LANG);
        request.setPackageData(encodedData);
        request.setDoLangDetect(NOT_DO_LANG_DETECT);
        request.setVadEnd(VAD_END);
        return request;
    }

    private static TTSRequest makeTtsRequest(String llmText) {
        TTSRequest request = new TTSRequest();
        request.setText(llmText);
        request.setLang(TTS_LANG);
        request.setVoiceId(TTS_VOICE_ID);
        request.setReqId(UUID.randomUUID().toString());
        return request;
    }

    public void setEngine(DingRtcEngine engine) {
        this.engine = engine;
    }

    private void startPushAudio(RtcContext context) {
        if (context.isPushAudioStarted) {
            return;
        }
        context.isPushAudioStarted = true;
        if (context.engine != null) {
            // 开启推流
            context.engine.publishLocalAudioStream(true);
            // 设置音频PCM推流
            context.engine.setExternalAudioSource(true, context.pcmSampleRate, context.pcmChannels);
        }
        context.externalAudioThread = new Thread(() -> {
            DingRtcEngine.DingRtcAudioFrame frame = new DingRtcEngine.DingRtcAudioFrame();
            try (RandomAccessFile file = new RandomAccessFile(context.pcmFilePath, "r")) {
                long sampleCount = 0;
                // 16-bit pcm, 2byte
                int bytesPerSample = 2;
                int samplesToRead = context.pcmSampleRate / (1000 / context.pcmReadFreq);
                int bufferSize = samplesToRead * context.pcmChannels * bytesPerSample;
                byte[] buffer = new byte[bufferSize];
                long delay = 0;
                long startClock = System.currentTimeMillis();
                long lastStatsClock = startClock;
                while (!context.quit) {
                    long lastPos = file.getFilePointer();
                    int readBytes = file.read(buffer);
                    if (readBytes != bufferSize) {
                        // 未读满缓冲区则从头循环读（读完文件后也会 seek(0)）
                        file.seek(0);
                        continue;
                    }
                    frame.bytesPerSample = bytesPerSample;
                    frame.samplesPerSec = context.pcmSampleRate;
                    frame.numChannels = context.pcmChannels;
                    frame.buffer = buffer;
                    frame.numSamples = readBytes / frame.bytesPerSample / frame.numChannels;
                    frame.timestamp = sampleCount * 1000 / context.pcmSampleRate;

                    delay = frame.timestamp;
                    long elapsed = System.currentTimeMillis() - startClock;
                    if (delay - elapsed > 5) {
                        sleep(delay - elapsed);
                    }

                    if (frame.numSamples > 0) {
                        int ret = context.engine.pushExternalAudioFrame(frame);
                        if (ret == 0) {
                            sampleCount += frame.numSamples;
                        } else {
                            file.seek(lastPos);
                            // sdk buffer full, wait some time ,ex: 40ms
                            if (ret == DingRtcEngine.DingRtcErrorCode.DingRtcErrorAudioBufferFull.getValue()) {
                                sleep(40);
                            }
                        }
                    }

                    long elpasedStats = System.currentTimeMillis() - lastStatsClock;
                    if (elpasedStats >= 2000) {
                        log("[Demo] audioPushThread pushExternalAudioFrame sampleCount:", sampleCount);
                        lastStatsClock = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                log("[Demo] pushAudio error:", e.toString());
            } finally {
                context.externalAudioFinished = true;
            }
        }, "pub-audio");
        context.externalAudioThread.start();
    }

    private static void log(Object... args) {
        if (args != null && args.length > 0) {
            for (Object o : args) {
                if (o != null) {
                    System.out.print(o);
                }
            }
            System.out.println();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class RtcContext {

        DingRtcEngine engine = null;
        DingRtcEngineListener listener = null;

        volatile boolean joinFinished = false;
        int joinResult = -1;

        volatile boolean quit = false;

        // audio config
        String pcmFilePath;
        int pcmSampleRate = 16000;
        int pcmChannels = 1;
        int pcmReadFreq = 40;
        final int bytesPerSample = 2;
        int frameBytes = pcmSampleRate / pcmReadFreq * pcmChannels * bytesPerSample;
        ByteArrayOutputStream pcmCache = new ByteArrayOutputStream();
        volatile boolean isPushAudioStarted = false;
        volatile boolean externalAudioFinished = false;
        Thread externalAudioThread;
        final Object audioLock = new Object();
        long pushedSampleCount = 0;

        // video config
        String videoYUVFilePath;
        DingRtcEngine.DingRtcVideoFormat videoPixelFormat = DingRtcEngine.DingRtcVideoFormat.DingRtcVideoI420;
        int videoWidth = 0;
        int videoHeight = 0;
        int videoRotation = 0;
        int videoFps = 25;
        volatile boolean isPushVideoStarted = false;
        volatile boolean externalVideoFinished = false;
        Thread externalVideoThread;

        void refreshAudioFrameBytes() {
            int freq = pcmReadFreq <= 0 ? 25 : pcmReadFreq;
            this.frameBytes = pcmSampleRate / freq * pcmChannels * bytesPerSample;
        }

        void reset() {
            if (engine != null) {
                engine.destroy();
            }
            engine = null;
            listener = null;
            if (externalAudioThread != null) {
                try {
                    externalAudioThread.join();
                } catch (InterruptedException e) {
                    logger.error("[Demo] reset audio thread error:", e.toString());
                }
            }
            if (externalVideoThread != null) {
                try {
                    externalVideoThread.join();
                } catch (InterruptedException e) {
                    logger.error("[Demo] reset video thread error:", e.toString());
                }
            }
        }
    }
}
