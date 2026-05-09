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
package com.hojoai.webmvc.omniasst.music.service.netease.dto;

/**
 * 播放数据回传请求
 * 
 * @author shengxianfei
 * @since 2026-01-23
 */
public class PlayDataRecordRequest {
    
    /**
     * 用户ID（用于获取token）
     * 必选
     */
    private String userId;
    
    /**
     * 行为类型
     * startplay: 开始播放
     * play: 结束播放
     * 必选
     */
    private String action;
    
    /**
     * 码率，单位/kbps
     * 128：标准，320：极高，999：无损，1999：hires
     * 高清臻音：3999，超清母带：4999，沉浸环绕声：4099
     * 杜比全景声：2999，臻音全景声（vivid）：5999
     * 播客固定传128
     * 必选
     */
    private Integer bitrate;
    
    /**
     * 文件类型回传枚举值
     * 0：下载文件
     * 1：本地文件
     * 2：缓存文件
     * 3：云盘文件
     * 4：线上文件（不清楚填4）
     * 必选
     */
    private Integer file;
    
    /**
     * 类型
     * song：歌曲
     * dj：播客
     * 必选
     */
    private String type;
    
    /**
     * 开始播放时的时间戳，单位：毫秒
     * 必选
     */
    private Long startLogTime;
    
    /**
     * 当前资源id
     * 例如：歌曲的id，声音的id或者encVoiceId
     * 必选
     */
    private String id;
    
    /**
     * 算法使用
     * 私人漫游、每日推荐和场景音乐已返回该字段，产生播放时需透传alg
     * 其余场景可不传
     * 可选
     */
    private String alg;
    
    /**
     * 播放时长，单位：秒
     * 播放完成或者中止播放时的时长，按照实际播放时长统计，不包含暂停
     * 仅结束播放（action=play）时必选
     */
    private Double time;
    
    /**
     * 结束方式回传枚举值
     * playend：正常结束
     * interrupt：第三方APP打断
     * exception：错误
     * ui: 用户切歌
     * 不清楚填playend
     * 仅结束播放（action=play）时必选
     */
    private String end;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getFile() {
        return file;
    }

    public void setFile(Integer file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStartLogTime() {
        return startLogTime;
    }

    public void setStartLogTime(Long startLogTime) {
        this.startLogTime = startLogTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}

