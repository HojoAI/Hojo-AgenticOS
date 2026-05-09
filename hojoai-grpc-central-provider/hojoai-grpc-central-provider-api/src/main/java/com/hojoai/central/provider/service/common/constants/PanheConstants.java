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
 * The PanheConstants class for panhe.
 *
 * @author My
 * @version 1.0, 2025/11/24
 * @since hojoai-grpc-central-provider 1.0.0
 */
public interface PanheConstants {

    Long REDIS_EXPIRE_TIME = 365L;

    Long REDIS_EXPIRE_TIME_CINEMA_ID = 10L;

    String CITY_REDIS_KEY = "allCityCode";

    String PARAM_APP_KEY = "appKey";

    String PARAM_TYPE = "type";

    String PARAM_KEYWORD = "keyWord";

    String PARAM_PAGE_INDEX = "pageIndex";

    String PARAM_PAGE_SIZE = "pageSize";

    String PARAM_FILM_ID = "filmId";

    String PARAM_CITY_CODE = "cityCode";

    String PARAM_DATE = "date";

    String PARAM_CINEMA_ID = "cinemaId";

    String PARAM_SHOW_ID = "showId";

    String PARAM_REGION_CODE = "regionCode";

    String PARAM_LONGITUDE = "longitude";

    String PARAM_LATITUDE = "latitude";

    String PARAM_LOW_KEYWORD = "keyword";

    String PARAM_ORDER_NO = "orderNo";

    String PARAM_PAY_AMOUNT = "payAmount";

    String PARAM_SIGN = "sign";

    String PARAM_CUSTOMER_ORDER_NO = "customerOrderNo";

    // 订单总金额（后台会校验，传错会下单失败）
    String PARAM_ORDER_AMOUNT = "orderAmount";

    // 选择的座位ID集合
    String PARAM_SEAT_IDS = "seatIds";

    // 当选择的座位已售时，是否接受换座出票
    String PARAM_ACCEPT_CHANGE_SEAT = "acceptChangeSeat";

    // 订票手机号
    String PARAM_MOBILE = "mobile";

    // 订单状态变更异步回调地址
    String PARAM_CALLBACK_URL = "callBackUrl";

}
