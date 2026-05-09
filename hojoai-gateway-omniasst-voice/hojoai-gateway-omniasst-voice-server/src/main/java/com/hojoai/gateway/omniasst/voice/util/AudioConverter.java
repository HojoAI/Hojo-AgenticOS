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
package com.hojoai.gateway.omniasst.voice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于 Java Sound 的 WAV/PCM 互转与时长计算，固定 16kHz、16bit、单声道目标格式。
 */
@Component
public class AudioConverter {
    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);
    /** 目标采样率 Hz */
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    /** 为 false 时跳过实际转码（仅返回路径占位，用于排障） */
    @Value("${audio.converter.enabled:true}")
    private boolean converterEnabled;

    /**
     * 将 WAV 转为原始 PCM 文件（目标格式见类说明）。
     *
     * @param wavFile    源 WAV
     * @param outputPath 输出 PCM 绝对或相对路径
     * @return 生成的 PCM 文件
     * @throws Exception 读取、转码或写出失败；禁用时行为见配置说明
     */
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
     * 将原始 PCM 文件封装为标准 WAV。
     *
     * @param pcmFile    源 PCM
     * @param outputPath 输出 WAV 路径
     * @return WAV 文件
     * @throws Exception 写出失败或输出为空
     */
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

    /**
     * 将内存中的 PCM 字节写入临时文件再转 WAV。
     *
     * @param pcmData    PCM 原始数据
     * @param outputPath 输出 WAV 路径
     * @return WAV 文件
     * @throws Exception 临时文件或转码异常
     */
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
     * 获取PCM音频的时长（秒）
     * @param pcmData PCM音频数据
     * @return 音频时长（秒）
     */
    public double getPcmDuration(byte[] pcmData) {
        // PCM格式：16kHz, 16bit, 单声道
        // 每秒钟字节数 = 采样率 * 位深度/8 * 声道数 = 16000 * 2 * 1 = 32000
        int bytesPerSecond = SAMPLE_RATE * (SAMPLE_SIZE_BITS / 8) * CHANNELS;
        return (double) pcmData.length / bytesPerSecond;
    }

    /**
     * 获取WAV音频的时长（秒）
     * @param wavData WAV音频数据
     * @return 音频时长（秒）
     */
    public double getWavDuration(byte[] wavData) throws Exception {
        Path tempWavFile = Files.createTempFile("temp_wav_", ".wav");
        try {
            Files.write(tempWavFile, wavData);
            return getWavDuration(tempWavFile.toFile());
        } finally {
            try {
                Files.deleteIfExists(tempWavFile);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary WAV file: {}", tempWavFile, e);
            }
        }
    }

    /**
     * 获取WAV音频文件的时长（秒）
     * @param wavFile WAV音频文件
     * @return 音频时长（秒）
     */
    public double getWavDuration(File wavFile) throws Exception {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            double durationInSeconds = (frames / format.getFrameRate());
            return durationInSeconds;
        }
    }

    /**
     * 拼接PCM音频数据，将音频重复拼接以达到目标时长
     * @param pcmData 原始PCM音频数据
     * @param targetDurationSeconds 目标时长（秒）
     * @return 拼接后的PCM音频数据
     */
    public byte[] concatenatePcmAudio(byte[] pcmData, double targetDurationSeconds) {
        double currentDuration = getPcmDuration(pcmData);
        if (currentDuration >= targetDurationSeconds) {
            return pcmData;
        }

        int bytesPerSecond = SAMPLE_RATE * (SAMPLE_SIZE_BITS / 8) * CHANNELS;
        int targetBytes = (int) (targetDurationSeconds * bytesPerSecond);
        int currentBytes = pcmData.length;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(targetBytes);
        
        // 先写入完整的原始音频
        outputStream.write(pcmData, 0, currentBytes);
        
        // 计算还需要多少字节
        int remainingBytes = targetBytes - currentBytes;
        
        if (remainingBytes > 0) {
            // 如果需要，取前面的一部分来补齐
            int segmentBytes = Math.min(currentBytes, remainingBytes);
            outputStream.write(pcmData, 0, segmentBytes);
            remainingBytes -= segmentBytes;
            
            // 如果还需要更多，继续重复
            while (remainingBytes > 0) {
                int copyBytes = Math.min(currentBytes, remainingBytes);
                outputStream.write(pcmData, 0, copyBytes);
                remainingBytes -= copyBytes;
            }
        }
        
        return outputStream.toByteArray();
    }

    /**
     * 拼接WAV音频数据，将音频重复拼接以达到目标时长
     * @param wavData 原始WAV音频数据
     * @param targetDurationSeconds 目标时长（秒）
     * @return 拼接后的WAV音频数据
     */
    public byte[] concatenateWavAudio(byte[] wavData, double targetDurationSeconds) throws Exception {
        // 先转换为PCM进行拼接，再转回WAV
        Path tempWavFile = Files.createTempFile("temp_wav_input_", ".wav");
        Path tempPcmFile = Files.createTempFile("temp_pcm_", ".pcm");
        Path tempWavOutputFile = Files.createTempFile("temp_wav_output_", ".wav");
        
        try {
            // 写入临时WAV文件
            Files.write(tempWavFile, wavData);
            
            // WAV转PCM
            File pcmFile = convertWavToPcm(tempWavFile.toFile(), tempPcmFile.toString());
            byte[] pcmData = Files.readAllBytes(pcmFile.toPath());
            
            // 拼接PCM
            byte[] concatenatedPcm = concatenatePcmAudio(pcmData, targetDurationSeconds);
            
            // PCM转WAV（直接使用byte[]方法）
            File outputWavFile = convertPcmToWav(concatenatedPcm, tempWavOutputFile.toString());
            
            return Files.readAllBytes(outputWavFile.toPath());
        } finally {
            try {
                Files.deleteIfExists(tempWavFile);
                Files.deleteIfExists(tempPcmFile);
                Files.deleteIfExists(tempWavOutputFile);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary files", e);
            }
        }
    }

    /**
     * 将WAV字节数组转换为PCM字节数组
     * @param wavData WAV音频数据
     * @return PCM音频数据
     */
    public byte[] convertWavToPcm(byte[] wavData) throws Exception {
        Path tempWavFile = Files.createTempFile("temp_wav_", ".wav");
        Path tempPcmFile = Files.createTempFile("temp_pcm_", ".pcm");
        
        try {
            // 写入临时WAV文件
            Files.write(tempWavFile, wavData);
            
            // WAV转PCM
            File pcmFile = convertWavToPcm(tempWavFile.toFile(), tempPcmFile.toString());
            
            return Files.readAllBytes(pcmFile.toPath());
        } finally {
            try {
                Files.deleteIfExists(tempWavFile);
                Files.deleteIfExists(tempPcmFile);
            } catch (IOException e) {
                logger.warn("Failed to delete temporary files", e);
            }
        }
    }
}
