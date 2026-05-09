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
package com.hojoai.central.provider.service.common.constants;

/**
 * 友盟推送常量
 *
 * @author My
 * @version 1.0, 2025/12/26
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface UMengConstants {

    // =============================================== 推送类型 ===============================================

    /**
     * 别名推送（单个/多个用户）
     */
    String TYPE_CUSTOMIZED = "customizedcast";

    /**
     * 广播（全部用户）
     */
    String TYPE_BROADCAST = "broadcast";

    /**
     * 单点推送（单个用户）
     */
    String TYPE_UNICAST = "unicast";

    // =============================================== 消息类型 ===============================================

    /**
     * 通知消息
     */
    String MSG_TYPE_NOTIFICATION = "notification";

    /**
     * 自定义消息
     */
    String MSG_TYPE_MESSAGE = "message";

    // =============================================== API 相关 ===============================================

    /**
     * API 路径
     */
    String API_PATH = "/api/send";

    /**
     * HTTP 方法
     */
    String HTTP_METHOD_POST = "POST";

    /**
     * User-Agent
     */
    String USER_AGENT = "Umeng-Push-Client/1.0";

    /**
     * 成功返回码
     */
    String SUCCESS_CODE = "SUCCESS";

    // =============================================== 请求参数 ===============================================

    /**
     * 别名类型（固定为用户ID）
     */
    String ALIAS_TYPE = "uid";

    /**
     * 消息分类
     */
    int MESSAGE_CATEGORY = 1;

    // =============================================== JSON 字段名 ===============================================

    String FIELD_APPKEY = "appkey";
    String FIELD_TIMESTAMP = "timestamp";
    String FIELD_TYPE = "type";
    String FIELD_PRODUCTION_MODE = "production_mode";
    String FIELD_CATEGORY = "category";
    String FIELD_ALIAS_TYPE = "alias_type";
    String FIELD_ALIAS = "alias";
    String FIELD_PAYLOAD = "payload";
    String FIELD_DISPLAY_TYPE = "display_type";
    String FIELD_BODY = "body";
    String FIELD_EXTRA = "extra";
    String FIELD_CHANNEL_PROPERTIES = "channel_properties";
    String FIELD_CHANNEL_ACTIVITY = "channel_activity";
    String FIELD_TITLE = "title";
    String FIELD_TEXT = "text";
    String FIELD_TICKER = "ticker";
    String FIELD_PLAY_VIBRATE = "play_vibrate";
    String FIELD_PLAY_LIGHTS = "play_lights";
    String FIELD_PLAY_SOUND = "play_sound";
    String FIELD_AFTER_OPEN = "after_open";
    String FIELD_CUSTOM = "custom";
    String FIELD_CONTENT = "content";
    String FIELD_RET = "ret";
    String FIELD_SIGN = "sign";
    String FIELD_ACTION = "action";
    String FIELD_PARAMS = "params";
    String FIELD_ROUTE = "route";
    String FIELD_PAGE_NAME = "pageName";
    String FIELD_DEVICE_TOKENS= "device_tokens";
    String FIELD_XIAOMI_CHANNEL_ID = "xiaomi_channel_id";
    String FIELD_CHANNEL_FCM = "channel_fcm";
    String FIELD_MI_ACTIVITY = "mi_activity";
    String FIELD_MI_PUSH = "mipush";

    // =============================================== iOS APNs 字段 ===============================================

    String FIELD_APS = "aps";
    String FIELD_ALERT = "alert";
    String FIELD_BADGE = "badge";
    String FIELD_SOUND = "sound";
    String FIELD_SUBTITLE = "subtitle";
    String FIELD_CONTENT_AVAILABLE = "content-available";
    String FIELD_APS_CATEGORY = "category";
    String FIELD_THREAD_ID = "thread-id";
    String FIELD_INTERRUPTION_LEVEL = "interruption-level";

    String DEFAULT_SOUND = "default";

    // =============================================== 消息内容相关 ===============================================

    /**
     * 默认标题
     */
    String DEFAULT_TITLE = "系统通知";

    /**
     * 默认内容
     */
    String DEFAULT_CONTENT = "您有一条新消息";

    /**
     * 通知栏提示文字
     */
    String DEFAULT_TICKER = "新消息";

    /**
     * 点击后行为：打开应用
     */
    String AFTER_OPEN_GO_APP = "go_app";

    /**
     * 布尔值：true
     */
    String VALUE_TRUE = "true";

    /**
     * 空 JSON 对象
     */
    String EMPTY_JSON_OBJECT = "{}";

    String MEETING_REFRESH = "meetingList";

    String AI_LOADING_FINISH = "AILoadingFinish";

}

