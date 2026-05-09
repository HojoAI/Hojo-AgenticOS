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
package com.hojoai.webmvc.omniasst.meeting.common;

public class GlobalConstants {

    /** 列表排序方向：升序 */
    public static final String SORT_ORDER_ASC = "asc";

    /** 列表排序方向：降序 */
    public static final String SORT_ORDER_DESC = "desc";

    /** 推送卡片业务类型：会议场景 */
    public static final String PUSH_CARD_TYPE = "meeting";

    /** 推送卡片 action，与消息中心 / PA 推送协议一致 */
    public static final String PUSH_CARD_ACTION = "action.pa.message";

    /** 海外版消息中心 / FCM 静默推送通道 */
    public static final String PUSH_CHANNEL_FCM = "fcm";

    /** management 侧 PushServiceImpl 组装 Firebase task payload */
    public static final String MESSAGE_CENTER_TYPE_TASK = "task";

    /** management 侧 PushServiceImpl 组装 Firebase message payload（落库 + totalUnread） */
    public static final String MESSAGE_CENTER_TYPE_MESSAGE = "message";

    /** 与 AgentActionEnum.WEB_MESSAGE 的 value 一致，供 PushController 解析 */
    public static final String MESSAGE_CENTER_WEB_MESSAGE_ACTION = "action.web.message";

    /** 进程任务卡片标题（标题模型尚未产出时的占位） */
    public static final String MESSAGE_CENTER_TASK_TITLE = "会议纪要生成中";

    /** 会议详情落地页前缀，后接 meetingId */
    public static final String MEETING_DETAIL_LANDING_PREFIX = "/meeting/detail/";

    /** FCM 页面刷新推送的 action 值 */
    public static final String REFRESH_PAGE_ACTION = "action.web.refreshPage";

    /** FCM 页面刷新推送的 pageName 值：通知前端刷新会议列表 */
    public static final String PAGE_NAME_MEETING_LIST = "meetingList";

    /** 会议打标（WebSocket / 详情 REST）全局最小时间间隔，毫秒 */
    public static final long MEETING_MARK_MIN_INTERVAL_MS = 5000L;

}
