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
package com.hojoai.webmvc.omniasst.meeting.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 音频格式转换（如 WAV 解析、ffmpeg 转 PCM），供会议实时链路与导入流水线使用。
 */
@Component
public class AudioConverter {
    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    @Value("${audio.converter.enabled:true}")
    private boolean converterEnabled;

    /**
     * 导入直处理：用系统 ffmpeg 将 mp3/m4a 等解码为与 {@link #convertWavToPcm} 一致的原始 PCM（16kHz / 16bit LE / mono）。
     * 运行环境需在 PATH 中可用 ffmpeg，或通过 {@code meeting.import.ffmpeg.path} 指定可执行文件路径。
     */
    @Value("${meeting.import.ffmpeg.enabled:true}")
    private boolean importFfmpegEnabled;

    @Value("${meeting.import.ffmpeg.path:ffmpeg}")
    private String importFfmpegExecutable;

    @Value("${meeting.import.ffmpeg.timeout-seconds:3600}")
    private long importFfmpegTimeoutSeconds;

    public File convertWavToPcm(File wavFile, String outputPath) throws Exception {
        if (!converterEnabled) {
            logger.warn("Audio converter is disabled, skipping conversion");
            return new File(outputPath);
        }
        File pcmFile = new File(outputPath);
        if (pcmFile.exists()) {
            Files.delete(pcmFile.toPath());
        }
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat sourceFormat = audioInputStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    SAMPLE_SIZE_BITS,
                    CHANNELS,
                    CHANNELS * 2,
                    SAMPLE_RATE,
                    BIG_ENDIAN
            );
            AudioFormat convertedFormat = sourceFormat.matches(targetFormat) ? sourceFormat : targetFormat;
            try (AudioInputStream convertedStream = AudioSystem.getAudioInputStream(convertedFormat, audioInputStream);
                 FileOutputStream fos = new FileOutputStream(pcmFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = convertedStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
        if (!pcmFile.exists() || pcmFile.length() == 0) {
            throw new RuntimeException("PCM file was not created or is empty");
        }
        logger.info("WAV to PCM conversion completed, input: {}, output: {}, size: {} bytes",
                wavFile.getAbsolutePath(), pcmFile.getAbsolutePath(), pcmFile.length());
        return pcmFile;
    }

    /**
     * 使用 ffmpeg 将压缩音频转为 ASR 所需的 s16le PCM（16kHz 单声道），输出为裸 PCM 文件。
     */
    public File convertCompressedAudioToPcm(File inputFile, String outputPcmPath) throws Exception {
        if (!importFfmpegEnabled) {
            throw new IllegalStateException("已关闭 meeting.import.ffmpeg.enabled，无法解码 mp3/m4a");
        }
        if (inputFile == null || !inputFile.isFile()) {
            throw new IllegalArgumentException("输入文件无效");
        }
        File pcmFile = new File(outputPcmPath);
        if (pcmFile.exists()) {
            Files.delete(pcmFile.toPath());
        }

        List<String> candidates = buildFfmpegExecutableCandidates();
        IOException firstStartFailure = null;
        Process process = null;
        int candidateCount = candidates.size();
        for (int idx = 0; idx < candidateCount; idx++) {
            String ffmpegExe = candidates.get(idx);
            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegExe);
            cmd.add("-nostdin");
            cmd.add("-y");
            cmd.add("-i");
            cmd.add(inputFile.getAbsolutePath());
            cmd.add("-f");
            cmd.add("s16le");
            cmd.add("-acodec");
            cmd.add("pcm_s16le");
            cmd.add("-ar");
            cmd.add(String.valueOf(SAMPLE_RATE));
            cmd.add("-ac");
            cmd.add(String.valueOf(CHANNELS));
            cmd.add(pcmFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            logger.info("ffmpeg 尝试 {}/{}: {}", idx + 1, candidateCount, ffmpegExe);
            logger.debug("ffmpeg 完整命令: {}", String.join(" ", cmd));
            try {
                process = pb.start();
                if (idx > 0) {
                    logger.info("ffmpeg 已用备用路径启动成功: {}", ffmpegExe);
                }
                break;
            } catch (IOException e) {
                if (firstStartFailure == null) {
                    firstStartFailure = e;
                }
                if (idx == 0) {
                    logger.warn("ffmpeg 路径不可用，将尝试下一候选: {}", ffmpegExe, e);
                } else {
                    logger.debug("ffmpeg 路径不可用: {}", ffmpegExe, e);
                }
            }
        }
        if (process == null) {
            throw new IllegalStateException(
                    "无法启动 ffmpeg。已依次尝试: " + candidates
                            + "。请在主机安装 ffmpeg（如 apt install ffmpeg），或设置 meeting.import.ffmpeg.path 为绝对路径。配置项当前为: "
                            + importFfmpegExecutable,
                    firstStartFailure);
        }
        StringBuilder logOut = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (logOut.length() < 32000) {
                    logOut.append(line).append('\n');
                }
            }
        }
        boolean finished = process.waitFor(importFfmpegTimeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("ffmpeg 执行超时（" + importFfmpegTimeoutSeconds + "s）");
        }
        int exit = process.exitValue();
        if (exit != 0) {
            throw new RuntimeException("ffmpeg 退出码=" + exit + "，输出:\n" + logOut);
        }
        if (!pcmFile.exists() || pcmFile.length() == 0) {
            throw new RuntimeException("ffmpeg 未生成有效 PCM 文件");
        }
        logger.info("ffmpeg 解码完成, input: {}, output: {}, size: {} bytes",
                inputFile.getAbsolutePath(), pcmFile.getAbsolutePath(), pcmFile.length());
        return pcmFile;
    }

    public File convertPcmToWav(File pcmFile, String outputPath) throws Exception {
        if (!converterEnabled) {
            logger.warn("Audio converter is disabled, skipping conversion");
            return new File(outputPath);
        }
        File wavFile = new File(outputPath);
        if (wavFile.exists()) {
            Files.delete(wavFile.toPath());
        }
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                CHANNELS * 2,
                SAMPLE_RATE,
                BIG_ENDIAN
        );
        long frameLength = pcmFile.length() / audioFormat.getFrameSize();
        try (FileInputStream fis = new FileInputStream(pcmFile);
             AudioInputStream audioInputStream = new AudioInputStream(fis, audioFormat, frameLength)) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
        }
        if (!wavFile.exists() || wavFile.length() == 0) {
            throw new RuntimeException("WAV file was not created or is empty");
        }
        logger.info("PCM to WAV conversion completed, input: {}, output: {}, size: {} bytes",
                pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(), wavFile.length());
        return wavFile;
    }

    public File convertPcmToWav(byte[] pcmData, String outputPath) throws Exception {
        Path tempPcmFile = Files.createTempFile("tts_pcm_", ".pcm");
        try {
            Files.write(tempPcmFile, pcmData);
            return convertPcmToWav(tempPcmFile.toFile(), outputPath);
        } finally {
            try {
                Files.deleteIfExists(tempPcmFile);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary PCM file: {}", tempPcmFile, e);
            }
        }
    }

    /**
     * 解析 ffmpeg 可执行文件候选路径：优先配置项；若为裸命令名则追加常见绝对路径（Linux/mac 上服务进程常无完整 PATH）。
     */
    private List<String> buildFfmpegExecutableCandidates() {
        List<String> out = new ArrayList<>();
        String configured = importFfmpegExecutable == null ? "" : importFfmpegExecutable.trim();
        if (configured.isEmpty()) {
            configured = "ffmpeg";
        }
        File configuredFile = new File(configured);
        if (configuredFile.isAbsolute() && configuredFile.isFile()) {
            out.add(configuredFile.getAbsolutePath());
            return out;
        }
        out.add(configured);
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("windows")) {
            // 不依赖 isFile()：容器/最小镜像可能尚未安装 ffmpeg，但仍需把标准路径纳入尝试列表，便于安装后生效；
            // 若未安装，由 ProcessBuilder 失败与最终异常说明即可。
            String[] fallbacks = {"/usr/bin/ffmpeg", "/usr/local/bin/ffmpeg", "/opt/homebrew/bin/ffmpeg"};
            for (String p : fallbacks) {
                if (!out.contains(p)) {
                    out.add(p);
                }
            }
        }
        return out;
    }
}

