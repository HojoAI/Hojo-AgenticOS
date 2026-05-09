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
package com.hojoai.omniasst.sysagent.push.model;

/**
 * Android 通知栏展示字段与提醒开关。
 */
public class PayloadBody {

    /** 通知标题。 */
    private String title;
    /** 通知正文。 */
    private String text;
    /** 状态栏 ticker。 */
    private String ticker;
    /** 是否震动。 */
    private boolean playVibrate = true;
    /** 是否闪灯。 */
    private boolean playLights = true;
    /** 是否响铃。 */
    private boolean playSound = true;
    /** 点击后行为，默认 go_custom。 */
    private String afterOpen = "go_custom";

    public PayloadBody() {}

    public PayloadBody(String title, String text, String ticker,
                       boolean playVibrate, boolean playLights, boolean playSound,
                       String afterOpen) {
        this.title = title;
        this.text = text;
        this.ticker = ticker;
        this.playVibrate = playVibrate;
        this.playLights = playLights;
        this.playSound = playSound;
        this.afterOpen = afterOpen;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getTicker() {
        return ticker;
    }

    public boolean isPlayVibrate() {
        return playVibrate;
    }

    public boolean isPlayLights() {
        return playLights;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public String getAfterOpen() {
        return afterOpen;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPlayVibrate(boolean playVibrate) {
        this.playVibrate = playVibrate;
    }

    public void setPlayLights(boolean playLights) {
        this.playLights = playLights;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public void setAfterOpen(String afterOpen) {
        this.afterOpen = afterOpen;
    }
}