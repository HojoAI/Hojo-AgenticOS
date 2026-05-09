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
package com.hojoai.omniasst.management.utils;

import jakarta.servlet.http.Part;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将 Servlet {@link Part} 适配为 {@link MultipartFile}，用于 multipart 中未被 Spring 映射到 getFile 的表单项。
 */
public class ServletPartMultipartFile implements MultipartFile {

    private final Part part;

    public ServletPartMultipartFile(Part part) {
        this.part = part;
    }

    @Override
    @NonNull
    public String getName() {
        return part.getName();
    }

    @Override
    public String getOriginalFilename() {
        String fn = part.getSubmittedFileName();
        return fn != null && !fn.isBlank() ? fn : "upload.bin";
    }

    @Override
    public String getContentType() {
        String ct = part.getContentType();
        return ct != null ? ct : "application/octet-stream";
    }

    @Override
    public boolean isEmpty() {
        return part.getSize() == 0;
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        try (InputStream in = part.getInputStream()) {
            return in.readAllBytes();
        }
    }

    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        part.write(dest.getAbsolutePath());
    }
}
