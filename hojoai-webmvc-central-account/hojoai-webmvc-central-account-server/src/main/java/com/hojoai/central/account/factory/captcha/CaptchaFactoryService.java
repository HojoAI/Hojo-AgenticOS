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
package com.hojoai.central.account.factory.captcha;

public interface CaptchaFactoryService {

    Result createMobileCaptcha(String userMobile, String mobileCaptchaType);

    Boolean verifyMobileCaptcha(String userMobile, String mobileCaptchaType, String mobileCaptcha);

    class Result {

        private ResultStatus resultStatus;
        private String resultContent;

        private Result() {
        }

        private Result(ResultStatus resultStatus) {
            this.resultStatus = resultStatus;
        }

        private Result(ResultStatus resultStatus, String resultContent) {
            this.resultStatus = resultStatus;
            this.resultContent = resultContent;
        }

        public static Result newInstance() {
            return new Result();
        }

        public static Result newInstance(ResultStatus resultStatus) {
            return new Result(resultStatus);
        }

        public static Result newInstance(ResultStatus resultStatus, String resultContent) {
            return new Result(resultStatus, resultContent);
        }

        public ResultStatus getResultStatus() {
            return resultStatus;
        }

        public void setResultStatus(ResultStatus resultStatus) {
            this.resultStatus = resultStatus;
        }

        public String getResultContent() {
            return resultContent;
        }

        public void setResultContent(String resultContent) {
            this.resultContent = resultContent;
        }
    }

    enum ResultStatus {

        OK("OK", "执行成功"),

        MOBILE_CAPTCHA_SENDING_FAILED("MOBILE_CAPTCHA_SENDING_FAILED", "手机验证码发送失败"),

        MOBILE_CAPTCHA_COUNT_LIMIT("MOBILE_CAPTCHA_COUNT_LIMIT", "手机验证码次数上限");

        private String resultCode;
        private String resultMessage;

        ResultStatus(String resultCode, String resultMessage) {
            this.resultCode = resultCode;
            this.resultMessage = resultMessage;
        }

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMessage() {
            return resultMessage;
        }

        public void setResultMessage(String resultMessage) {
            this.resultMessage = resultMessage;
        }
    }

    enum MobileCaptchaType {

        CREATE_MOBILE_USER("CREATE_MOBILE_USER", "创建手机用户"),

        CHANGE_BINDING_USER_MOBILE("CHANGE_BINDING_USER_MOBILE", "换绑用户手机"),

        CREATE_USER_TOKEN("CREATE_USER_TOKEN", "创建用户令牌");

        private String code;
        private String desc;

        MobileCaptchaType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
