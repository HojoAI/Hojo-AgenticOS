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
package com.hojoai.central.provider.basic;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logger handler class.
 *
 * @author kevanwong
 * @version 1.0, 2017/10/21
 * @since hojoai-grpc-central-provider 1.0.0
 */
public class ILoggerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ILoggerHandler.class);

    private static final int LOG_THRESHOLD = 1000;

    public static void enter(String className, String methodName, Object object) {
        try {
            String jsonMessage = JsonFormat.printer().omittingInsignificantWhitespace().print((Message) object);
            String message = jsonMessage.length() > LOG_THRESHOLD ? jsonMessage.substring(0, LOG_THRESHOLD) : jsonMessage;

            logger.info("service enter on class: {}, method: {}, message: {}", className, methodName, message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("failed to convert protobuf message to json", e);
        }
    }

    public static void leave(String className, String methodName, Object object) {
        try {
            String jsonMessage = JsonFormat.printer().omittingInsignificantWhitespace().print((Message) object);
            String message = jsonMessage.length() > LOG_THRESHOLD ? jsonMessage.substring(0, LOG_THRESHOLD) : jsonMessage;

            logger.info("service leave on class: {}, method: {}, message: {}", className, methodName, message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("failed to convert protobuf message to json", e);
        }
    }
}
