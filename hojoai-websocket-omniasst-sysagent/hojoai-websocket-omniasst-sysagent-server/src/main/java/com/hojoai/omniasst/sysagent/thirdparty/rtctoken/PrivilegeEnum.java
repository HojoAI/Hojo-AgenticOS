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
package com.hojoai.omniasst.sysagent.thirdparty.rtctoken;

public enum PrivilegeEnum {

    ENABLE_PRIVILEGE(0x1), ENABLE_AUDIO_PUBLISH(0x2), ENABLE_VIDEO_PUBLISH(0x4), ENABLE_SCREEN_PUBLISH(0x8);

    private final int privilege;

    PrivilegeEnum(int privilege) {
        this.privilege = privilege;
    }

    public int getPrivilege() {
        return privilege;
    }
}
