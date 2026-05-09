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
package com.hojoai.omniasst.sysagent.thirdparty.rtctoken;

import io.netty.buffer.ByteBuf;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class AppTokenOptions {

    private Map<String, String> engineOptions;

    public void addEngineOptions(@NonNull Map<String, String> engineOptions) {
        this.engineOptions = engineOptions;
    }

    public void pack(@NonNull ByteBuf buf) {
        final boolean hasEngineOptions = Objects.nonNull(this.engineOptions);
        buf.writeBoolean(hasEngineOptions);
        if (!hasEngineOptions) {
            return;
        }
        buf.writeInt(this.engineOptions.size());
        for (Map.Entry<String, String> entry : this.engineOptions.entrySet()) {
            if (Objects.isNull(entry) || Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue())) {
                throw new IllegalArgumentException("illegal engineOptions entry");
            }
            buf.writeInt(entry.getKey().length());
            buf.writeBytes(entry.getKey().getBytes());
            buf.writeInt(entry.getValue().length());
            buf.writeBytes(entry.getValue().getBytes());
        }
    }

    public static AppTokenOptions unpack(@NonNull ByteBuf buf) {
        final boolean hasEngineOptions = buf.readBoolean();
        if (!hasEngineOptions) {
            return new AppTokenOptions();
        }

        final Map<String, String> engineOptions = new TreeMap<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            final byte[] key = new byte[buf.readInt()];
            final byte[] value = new byte[buf.readInt()];
            engineOptions.put(new String(key), new String(value));
        }
        return new AppTokenOptions(engineOptions);
    }

    private AppTokenOptions(Map<String, String> engineOptions) {
        this.engineOptions = engineOptions == null ? new TreeMap<>() : engineOptions;
    }

    public AppTokenOptions() {
        this.engineOptions = new TreeMap<>();
    }
}
