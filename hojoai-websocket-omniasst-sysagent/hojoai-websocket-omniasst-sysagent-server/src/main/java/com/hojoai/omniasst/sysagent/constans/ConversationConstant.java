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
package com.hojoai.omniasst.sysagent.constans;

/**
 * 对话与意图流水线中的字符串常量：响应模式、内容类型、角色、智能体标识、SSE 事件名、加载文案与记忆分组等。
 * <p>与客户端、management、模型侧约定保持一致，避免魔法字串散落。</p>
 */
public class ConversationConstant {

    /** 响应模式：流式输出。 */
    public static final String RESPONSE_MODE = "streaming";

    /** 内容类型：纯文本。 */
    public static final String CONTENT_TYPE_TEXT = "text";
    /** 内容类型：图片。 */
    public static final String CONTENT_TYPE_IMAGE = "image";
    /** 内容类型：文件。 */
    public static final String CONTENT_TYPE_FILE = "file";
    /** 内容类型：音频。 */
    public static final String CONTENT_TYPE_AUDIO = "audio";
    /** 内容类型：视频。 */
    public static final String CONTENT_TYPE_VIDEO = "video";
    /** 文本子类型：气泡区展示。 */
    public static final String CONTENT_SUB_TYPE_BUBBLE = "bubble";
    /** 文本子类型：耳机场景下由 bubble 映射的语音区展示。 */
    public static final String CONTENT_SUB_TYPE_SPEECH = "speech";
    /** 文本子类型：正文区。 */
    public static final String CONTENT_SUB_TYPE_BODY = "body";
    /** 文本子类型：卡片形态。 */
    public static final String CONTENT_SUB_TYPE_CARD = "card";

    /** 消息角色：用户。 */
    public static final String ROLE_USER = "user";
    /** 消息角色：系统。 */
    public static final String ROLE_SYSTEM = "system";
    /** 消息角色：助手。 */
    public static final String ROLE_ASSISTANT = "assistant";
    /** 消息角色：上下文补充。 */
    public static final String ROLE_CONTEXT = "context";

    /** 模型侧角色：用户。 */
    public static final String MODEL_ROLE_USER = "user";
    /** 模型侧角色：系统。 */
    public static final String MODEL_ROLE_SYSTEM = "system";
    /** 模型侧角色：智能体。 */
    public static final String MODEL_ROLE_AGENT = "agent";

    /** 存储范围：库表与上下文均包含。 */
    public static final String STORAGE_TYPE_ALL = "all";
    /** 存储范围：仅数据库。 */
    public static final String STORAGE_TYPE_DB = "db";
    /** 存储范围：仅上下文。 */
    public static final String STORAGE_TYPE_CONTEXT = "context";

    /** 协议中与智能体相关的字面量 {@code "agent"}。 */
    public static final String AGENT = "agent";
    /** 智能体：通用聊天。 */
    public static final String AGENT_CHAT = "chat";
    /** 智能体：联网搜索聊天。 */
    public static final String AGENT_CHAT_WEBSEARCH = "chat_websearch";
    /** 智能体：文档/图文。 */
    public static final String AGENT_DOC = "doc";
    /** 智能体：翻译。 */
    public static final String AGENT_TRANSLATOR = "translator";
    /** 智能体：会议。 */
    public static final String AGENT_MEETING = "meeting";
    /** 智能体：日程/专注（focus 服务）。 */
    public static final String AGENT_FOCUS = "event";
    /** 智能体：音乐。 */
    public static final String AGENT_MUSIC = "music";
    /** 智能体：天气。 */
    public static final String AGENT_WEATHER = "weather";
    /** 智能体：播客/资讯推荐（协议常量名为 PODCAST）。 */
    public static final String AGENT_PODCAST = "podcast";

    /** 智能体中文展示名：聊天。 */
    public static final String AGENT_CHAT_CN = "聊天";
    /** 智能体中文展示名：图文。 */
    public static final String AGENT_DOC_CN = "图文";
    /** 智能体中文展示名：翻译。 */
    public static final String AGENT_TRANSLATOR_CN = "翻译";
    /** 智能体中文展示名：会议。 */
    public static final String AGENT_MEETING_CN = "会议";
    /** 智能体中文展示名：联网搜。 */
    public static final String AGENT_CHAT_WEBSEARCH_CN = "联网搜";
    /** 智能体中文展示名：日程。 */
    public static final String AGENT_FOCUS_CN = "日程";
    /** 智能体中文展示名：音乐。 */
    public static final String AGENT_MUSIC_CN = "音乐";
    /** 智能体中文展示名：天气。 */
    public static final String AGENT_WEATHER_CN = "天气";
    /** 智能体中文展示名：播客（与 {@link #AGENT_PODCAST} 对应）。 */
    public static final String AGENT_NEWS_CN = "播客";

    /** 意图动作：拒绝继续多智能体，直接返回应答文本。 */
    public static final String INTENTION_ACTION_REJECT = "reject";
    /** 意图动作：调用子智能体链路。 */
    public static final String INTENTION_ACTION_AGENT = "callAgent";

    /** SSE 事件名：业务消息内容。 */
    public static final String EVENT_NAME_MESSAGE = "message";
    /** SSE 事件名：心跳 ping。 */
    public static final String EVENT_NAME_PING = "ping";
    /** SSE 事件名：错误。 */
    public static final String EVENT_NAME_ERROR = "error";
    /** SSE 事件名：加载中提示。 */
    public static final String EVENT_NAME_LOADING = "loading";
    /** SSE 事件名：即将调用的子智能体类型（值为 {@link #AGENT_CHAT} 等标识）。 */
    public static final String EVENT_NAME_AGENT_TYPE = "agentType";

    /** 通用加载中文案。 */
    public static final String LOADING_MESSAGE = "正在思考...";
    /** 聊天场景加载中文案。 */
    public static final String CHAT_LOADING_MESSAGE = "请稍候，我正在回答...";
    /** 联网搜索场景加载中文案。 */
    public static final String CHAT_WEBSEARCH_LOADING_MESSAGE = "正在进行联网搜索...";
    /** 启用某智能体时的加载文案模板，{@code %s} 为中文名。 */
    public static final String AGENT_LOADING_MESSAGE_TEMPLATE = "正在启用：%s智能体";

    /** 个人助理记忆分组 ID，写入外部记忆服务时使用。 */
    public static final String PA_MEMORY_GROUP_ID = "PA_MAIN_DIALOGUE";
}
