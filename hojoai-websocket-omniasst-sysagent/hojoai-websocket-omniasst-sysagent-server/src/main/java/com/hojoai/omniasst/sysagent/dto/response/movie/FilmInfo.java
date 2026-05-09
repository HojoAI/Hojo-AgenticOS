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
package com.hojoai.omniasst.sysagent.dto.response.movie;

/**
 *
 * @author My
 * @version 1.0, 2025/11/10
 * @since hojoai-websocket-omniasst-sysagent 1.0.0
 */
public class FilmInfo {

    /**
     * 影片ID
     */
    private String filmId;

    /**
     * 影片统一编码11位(暂时用不到)
     */
    private String filmCode;

    /**
     * 影片名称
     */
    private String name;

    /**
     * 海报URL地址
     */
    private String picUrl;

    /**
     * 评分
     */
    private String grade;

    /**
     * 想看人数
     */
    private Integer like;

    /**
     * 导演
     */
    private String director;

    /**
     * 主演
     */
    private String leadingRole;

    /**
     * 上映日期
     */
    private String publishDate;

    /**
     * 影片时长 分钟
     */
    private Integer duration;

    /**
     * 影片类型
     */
    private String filmTypes;

    /**
     * 影片制作地区
     */
    private String movieArea;

    /**
     * 影片语言
     */
    private String language;

    /**
     * 影片简介
     */
    private String intro;

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getFilmCode() {
        return filmCode;
    }

    public void setFilmCode(String filmCode) {
        this.filmCode = filmCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getLeadingRole() {
        return leadingRole;
    }

    public void setLeadingRole(String leadingRole) {
        this.leadingRole = leadingRole;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getFilmTypes() {
        return filmTypes;
    }

    public void setFilmTypes(String filmTypes) {
        this.filmTypes = filmTypes;
    }

    public String getMovieArea() {
        return movieArea;
    }

    public void setMovieArea(String movieArea) {
        this.movieArea = movieArea;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

}
