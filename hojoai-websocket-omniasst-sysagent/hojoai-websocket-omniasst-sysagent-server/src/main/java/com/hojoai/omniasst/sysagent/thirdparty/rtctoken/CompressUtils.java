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

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtils {

    public static byte[] compress(byte[] data) {
        byte[] output;
        final Deflater deflater = new Deflater();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

        try {
            deflater.reset();
            deflater.setInput(data);
            deflater.finish();

            byte[] buf = new byte[data.length];
            while (!deflater.finished()) {
                int i = deflater.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Throwable e) {
            output = data;
        } finally {
            deflater.end();
        }

        return output;
    }

    public static byte[] decompress(byte[] data) {
        final Inflater inflater = new Inflater();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

        try {
            inflater.setInput(data);
            byte[] buf = new byte[data.length];
            while (!inflater.finished()) {
                int i = inflater.inflate(buf);
                bos.write(buf, 0, i);
            }
        } catch (Exception e) {
        } finally {
            inflater.end();
        }

        return bos.toByteArray();
    }
}
