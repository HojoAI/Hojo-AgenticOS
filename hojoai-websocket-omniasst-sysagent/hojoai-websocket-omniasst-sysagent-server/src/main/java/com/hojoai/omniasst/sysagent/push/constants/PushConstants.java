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
package com.hojoai.omniasst.sysagent.push.constants;

/**
 * 友盟推送相关的类型、展示形态、默认值与 API 片段常量。
 */
public class PushConstants {

    /** 单播：按 device_token。 */
    public static final String PUSH_TYPE_UNICAST = "unicast";
    /** 列播：多个 device_token。 */
    public static final String PUSH_TYPE_LISTCAST = "listcast";
    /** 别名播：按 alias。 */
    public static final String PUSH_TYPE_CUSTOMIZED = "customizedcast";
    /** 文件播：受众来自文件 ID。 */
    public static final String PUSH_TYPE_FILECAST = "filecast";
    /** 广播。 */
    public static final String PUSH_TYPE_BROADCAST = "broadcast";
    /** 分组播。 */
    public static final String PUSH_TYPE_GROUPCAST = "groupcast";

    /** Android 通知栏通知（带系统通知样式）。 */
    public static final String DISPLAY_TYPE_NOTIFICATION = "notification";
    /** 透传/自定义消息。 */
    public static final String DISPLAY_TYPE_MESSAGE = "message";

    /** 默认通知标题。 */
    public static final String DEFAULT_TITLE = "系统通知";
    /** 默认通知正文。 */
    public static final String DEFAULT_CONTENT = "您有一条新的消息";
    /** 点击通知后的打开行为（友盟约定）。 */
    public static final String DEFAULT_AFTER_OPEN = "go_custom";

    /** 默认是否震动。 */
    public static final boolean DEFAULT_PLAY_VIBRATE = true;
    /** 默认是否闪灯。 */
    public static final boolean DEFAULT_PLAY_LIGHTS = true;
    /** 默认是否响铃。 */
    public static final boolean DEFAULT_PLAY_SOUND = true;

    /** 字符串形式 true，用于部分表单序列化。 */
    public static final String TRUE_STR = "true";
    /** 字符串形式 false。 */
    public static final String FALSE_STR = "false";

    /** 友盟发送接口路径片段。 */
    public static final String API_SEND = "/api/send";

    /** 友盟返回成功标识（与对端文档一致）。 */
    public static final String SUCCESS_CODE = "SUCCESS";
    /** HTTP User-Agent 头。 */
    public static final String USER_AGENT = "Umeng-Push-Client/1.0";

    /** 别名类型：业务用户 ID。 */
    public static final String ALIAS_TYPE_USER_ID = "user_id";
}
