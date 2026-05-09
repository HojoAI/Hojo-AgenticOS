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

import io.netty.buffer.ByteBuf;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class RTCService {

    @NonNull
    private final String channelId;
    @NonNull
    private final String userId;
    private Integer privilege;

    public RTCService(@NonNull String channelId, @NonNull String userId) {
        this.channelId = channelId;
        this.userId = userId;
    }

    public static RTCService createServiceOnlyWithUserId(@NonNull String userId) {
        return new RTCService(AppTokenConstants.WILDCARD_CHARACTERS, userId);
    }

    public static RTCService createServiceOnlyWithChannelId(@NonNull String channelId) {
        return new RTCService(channelId, AppTokenConstants.WILDCARD_CHARACTERS);
    }

    public void addAudioPublishPrivilege() {
        if (Objects.isNull(this.privilege)) {
            this.privilege = 0;
            this.privilege = this.privilege | PrivilegeEnum.ENABLE_PRIVILEGE.getPrivilege();
        }
        this.privilege = this.privilege | PrivilegeEnum.ENABLE_AUDIO_PUBLISH.getPrivilege();
    }

    public void addVideoPublishPrivilege() {
        if (Objects.isNull(this.privilege)) {
            this.privilege = 0;
            this.privilege = this.privilege | PrivilegeEnum.ENABLE_PRIVILEGE.getPrivilege();
        }
        this.privilege = this.privilege | PrivilegeEnum.ENABLE_VIDEO_PUBLISH.getPrivilege();
    }

    public void addScreenPublishPrivilege() {
        if (Objects.isNull(this.privilege)) {
            this.privilege = 0;
            this.privilege = this.privilege | PrivilegeEnum.ENABLE_PRIVILEGE.getPrivilege();
        }
        this.privilege = this.privilege | PrivilegeEnum.ENABLE_SCREEN_PUBLISH.getPrivilege();
    }

    public void pack(@NonNull ByteBuf buf) {
        final byte[] channelId = this.channelId.getBytes();
        buf.writeInt(channelId.length);
        buf.writeBytes(channelId);
        final byte[] userId = this.userId.getBytes();
        buf.writeInt(userId.length);
        buf.writeBytes(userId);
        final boolean hasPrivilege = Objects.nonNull(this.privilege);
        buf.writeBoolean(hasPrivilege);
        if (hasPrivilege) {
            buf.writeInt(this.privilege);
        }
    }

    public static RTCService unpack(@NonNull ByteBuf buf) {
        final byte[] byteChannelId = new byte[buf.readInt()];
        for (int i = 0; i < byteChannelId.length; i++) {
            byteChannelId[i] = buf.readByte();
        }
        final String channelId = new String(byteChannelId);

        final byte[] byteUserId = new byte[buf.readInt()];
        for (int i = 0; i < byteUserId.length; i++) {
            byteUserId[i] = buf.readByte();
        }
        final String userId = new String(byteUserId);

        final boolean hasPrivilege = buf.readBoolean();
        Integer privilege = null;
        if (hasPrivilege) {
            privilege = buf.readInt();
        }
        return new RTCService(channelId, userId, privilege);
    }

    private RTCService(@NonNull String channelId, @NonNull String userId, Integer privilege) {
        this.channelId = channelId;
        this.userId = userId;
        this.privilege = privilege;
    }
}
