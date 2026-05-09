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
package com.hojoai.gateway.omniasst.voice.alqw;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云相关 HTTP 调用工具（OkHttp），固定连接/读写超时，用于 Flash 识别等场景。
 */
public class HttpUtil {

    private static String getResponseWithTimeout(Request q) {
        String ret = null;

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        OkHttpClient client = httpBuilder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        try {
            Response s = client.newCall(q).execute();
            ret = s.body().string();
            s.close();
        } catch (SocketTimeoutException e) {
            ret = null;
            System.err.println("get result timeout");
        } catch (IOException e) {
            System.err.println("get result error " + e.getMessage());
        }

        return ret;
    }

    /**
     * POST 上传本地文件（二进制流）。
     *
     * @param url      完整 URL
     * @param headers  可选请求头
     * @param fileName 本地文件路径
     * @return 响应体字符串；超时或 IO 失败返回 {@code null}
     */
    public static String sendPostFile(String url, HashMap<String, String> headers, String fileName) {
        RequestBody body;

        File file = new File(fileName);
        if (!file.isFile()) {
            System.err.println("The filePath is not a file: " + fileName);
            return null;
        } else {
            body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        }

        Headers.Builder hb = new Headers.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                hb.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(hb.build())
                .post(body)
                .build();

        return getResponseWithTimeout(request);
    }

    /**
     * POST 原始字节体。
     *
     * @param url     完整 URL
     * @param headers 可选请求头
     * @param data    非空字节数组
     * @return 响应体；失败返回 {@code null}
     */
    public static String sendPostData(String url, HashMap<String, String> headers, byte[] data) {
        RequestBody body;

        if (data.length == 0) {
            System.err.println("The send data is empty.");
            return null;
        } else {
            body = RequestBody.create(MediaType.parse("application/octet-stream"), data);
        }

        Headers.Builder hb = new Headers.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                hb.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(hb.build())
                .post(body)
                .build();

        return getResponseWithTimeout(request);
    }

    /**
     * GET/无 body POST 变体：将音频地址拼到 query {@code audio_address} 后请求。
     *
     * @param url     基础 URL（将追加 {@code &audio_address=}）
     * @param headers 可选请求头
     * @param link    音频链接，不可为空
     * @return 响应体；失败返回 {@code null}
     */
    public static String sendPostLink(String url, HashMap<String, String> headers, String link){
        RequestBody body;

        if (link.isEmpty()) {
            System.err.println("The send link is empty.");
            return null;
        } else {
            url=url+"&audio_address="+link;
        }

        Headers.Builder hb = new Headers.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                hb.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(hb.build())
                .build();

        return getResponseWithTimeout(request);
    }
}
