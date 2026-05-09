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
package com.hojoai.omniasst.sysagent.controller.movie;

import com.hojoai.common.asserts.Asserts;
import com.hojoai.common.jackson.JacksonUtil;
import com.hojoai.common.response.Response;
import com.hojoai.common.response.ResponseStatusBasic;
import com.hojoai.omniasst.sysagent.dto.request.movie.*;
import com.hojoai.omniasst.sysagent.dto.response.movie.*;
import com.hojoai.omniasst.sysagent.service.movie.MovieService;
import com.hojoai.omniasst.sysagent.service.movie.OrderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * The MovieController class for movie.
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
@RestController
@RequestMapping(value = "/omniasst-sysagent/panhe/movie")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @Resource
    private MovieService movieService;

    @Resource
    private OrderService orderService;

    /**
     * 查询影片列表。
     *
     * @param request 列表查询条件
     * @return 影片列表或错误时 {@link ResponseStatusBasic#GENERAL_ERROR}
     */
    @PostMapping(value = "/films")
    public Response<GetFilmListResponse> getFilmList(@RequestBody GetFilmListRequest request) {
        try {
            logger.info("MovieController.getFilmList request -> {}", JacksonUtil.bean2Json(request));

            Response<GetFilmListResponse> response = movieService.getFilmList(request);

            logger.info("MovieController.getFilmList response -> {}", JacksonUtil.bean2Json(response));
            return response;
        } catch (Exception e) {
            logger.error("MovieController.getFilmList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 按条件查询有排期的影院列表。
     *
     * @param request 已校验的排期影院查询请求
     * @return 影院列表封装响应
     */
    @PostMapping(value = "/schedule-cinemas")
    public Response<GetScheduleCinemaListResponse> getScheduleCinemaList(@RequestBody GetScheduleCinemaListRequest request) {
        try {
            logger.info("MovieController.getScheduleCinemaList request -> {}", JacksonUtil.bean2Json(request));

            Asserts.notNull(request, "getScheduleCinemaList request is null!");
            request.validate(request);

            Response<GetScheduleCinemaListResponse> response = movieService.getScheduleCinemaList(request);

            logger.info("MovieController.getScheduleCinemaList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.getScheduleCinemaList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 查询影片场次列表。
     *
     * @param request 场次查询条件
     * @return 场次列表响应
     */
    @PostMapping(value = "/film-shows")
    public Response<GetFilmShowListResponse> getFilmShowList(@RequestBody GetFilmShowListRequest request) {
        try {
            logger.info("MovieController.getFilmShowList request -> {}", JacksonUtil.bean2Json(request));

            Asserts.notNull(request, "getFilmShowList request is null!");
            request.validate(request);

            Response<GetFilmShowListResponse> response = movieService.getFilmShowList(request);

            logger.info("MovieController.getFilmShowList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.getFilmShowList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 查询指定场次的座位图/选座信息。
     *
     * @param request 场次与座位查询参数
     * @return 座位列表响应
     */
    @PostMapping(value = "/show-seats")
    public Response<GetShowSeatListResponse> getShowSeatList(@RequestBody GetShowSeatListRequest request) {
        try {
            logger.info("MovieController.getShowSeatList request -> {}", JacksonUtil.bean2Json(request));

            Asserts.notNull(request, "getShowSeatList request is null!");
            request.validate(request);

            Response<GetShowSeatListResponse> response = movieService.getShowSeatList(request);

            logger.info("MovieController.getShowSeatList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.getShowSeatList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 查询影院列表。
     *
     * @param request 影院筛选条件
     * @return 影院列表响应
     */
    @PostMapping(value = "/cinemas")
    public Response<GetCinemaListResponse> getCinemaList(@RequestBody GetCinemaListRequest request) {
        try {
            logger.info("MovieController.getCinemaList request -> {}", JacksonUtil.bean2Json(request));

            Asserts.notNull(request, "getCinemaList request is null!");
            request.validate(request);

            Response<GetCinemaListResponse> response = movieService.getCinemaList(request);

            logger.info("MovieController.getCinemaList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.getCinemaList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 按影院 ID 查询该影院上映影片列表。
     *
     * @param cinemaId 影院唯一标识
     * @return 影片列表响应
     */
    @GetMapping(value = "/cinema-film")
    public Response<GetCinemaFilmListResponse> getCinemaFilmList(@RequestParam(value = "cinemaId") String cinemaId) {
        try {
            logger.info("MovieController.getCinemaFilmList request -> {}", cinemaId);

            Asserts.notEmpty(cinemaId, "getCinemaFilmList request is null!");

            Response<GetCinemaFilmListResponse> response = movieService.getCinemaFilmList(cinemaId);

            logger.info("MovieController.getCinemaFilmList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.getShowSeatList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 查询影片排期列表（按影片维度）。
     *
     * @param request 排期查询请求
     * @return 排期列表响应
     */
    @PostMapping(value = "/film-schedule")
    public Response<GetFilmScheduleListResponse> getShowSeatList(@RequestBody GetFilmScheduleListRequest request) {
        try {
            Asserts.notNull(request, "getShowSeatList request is null!");
            request.validate(request);
            return movieService.getFilmScheduleList(request);
        } catch (Exception e) {
            logger.error("MovieController.getShowSeatList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 创建影票订单并初始化支付流程。
     *
     * @param request 下单参数（场次、座位等）
     * @return 订单与支付相关信息
     */
    @PostMapping(value = "/payment/init")
    public Response<CreateOrderResponse> initPaymentList(@RequestBody CreateOrderRequest request) {
        try {
            logger.info("MovieController.initPaymentList request -> {}", JacksonUtil.bean2Json(request));

            Asserts.notNull(request, "initPaymentList request is null!");
            request.validate(request);

            Response<CreateOrderResponse> response = orderService.createOrder(request);

            logger.info("MovieController.initPaymentList response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.initPaymentList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

    /**
     * 查询订单详情。
     *
     * @param userId  用户 ID
     * @param orderNo 订单号
     * @return 订单详情或错误响应
     */
    @GetMapping(value = "/order/detail")
    public Response<GetOrderDetailResponse> getOrderDetail(@RequestParam(value = "userId") String userId, @RequestParam(value = "orderNo") String orderNo) {
        try {
            logger.info("MovieController.getOrderDetail request userId is -> {}, orderNo is -> {}", userId, orderNo);

            Asserts.notEmpty(userId, "getOrderDetail request userId is null!");
            Asserts.notEmpty(orderNo, "getOrderDetail request orderNo is null!");

            Response<GetOrderDetailResponse> response = orderService.getOrderDetail(userId, orderNo);

            logger.info("MovieController.getOrderDetail response -> {}", JacksonUtil.bean2Json(response));

            return response;
        } catch (Exception e) {
            logger.error("MovieController.initPaymentList error: ", e);
            return Response.newInstance(ResponseStatusBasic.GENERAL_ERROR);
        }
    }

}
