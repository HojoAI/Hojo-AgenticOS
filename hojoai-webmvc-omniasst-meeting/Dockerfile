# 阶段1：从仓库内的本地压缩包提取静态 ffmpeg，避免构建时访问外网。
# 默认使用 DaoCloud 对 Docker Hub 的代理，避免访问 auth.docker.io 超时；海外或自有镜像可覆盖：
#   docker compose build --build-arg FFMPEG_FETCH_BASE=alpine:3.19 meeting
ARG FFMPEG_FETCH_BASE=docker.m.daocloud.io/library/alpine:3.19
FROM ${FFMPEG_FETCH_BASE} AS ffmpeg-fetch
# buildx 会自动注入；普通 docker build 未注入时默认 amd64
ARG TARGETARCH=amd64
# 约定将压缩包放在 docker/ffmpeg/ 目录下，例如：
# - docker/ffmpeg/ffmpeg-release-amd64-static.tar.xz
# - docker/ffmpeg/ffmpeg-release-arm64-static.tar.xz
COPY docker/ffmpeg/ /opt/ffmpeg-dist/

# 仅安装解压所需工具；ffmpeg 二进制本身来自仓库，不再从外网下载
RUN set -eux; \
    sed -i 's#https://dl-cdn.alpinelinux.org/alpine#https://mirrors.aliyun.com/alpine#g' /etc/apk/repositories; \
    apk add --no-cache xz tar; \
    case "$TARGETARCH" in \
        amd64) \
            src=/opt/ffmpeg-dist/ffmpeg-release-amd64-static.tar.xz \
            ;; \
        arm64) \
            src=/opt/ffmpeg-dist/ffmpeg-release-arm64-static.tar.xz \
            ;; \
        *) echo "unsupported TARGETARCH=$TARGETARCH, use docker buildx --platform linux/amd64 or linux/arm64" >&2; exit 1 ;; \
    esac; \
    test -f "$src" || { echo "missing local ffmpeg package: $src" >&2; exit 1; }; \
    tar -xJf "$src" -C /tmp; \
    d=$(ls -d /tmp/ffmpeg-*-static 2>/dev/null | head -1); \
    test -n "$d" && test -f "$d/ffmpeg"; \
    install -m 0755 "$d/ffmpeg" /ffmpeg; \
    rm -rf /tmp/ffmpeg-*-static /opt/ffmpeg-dist

# Set base image
FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

COPY --from=ffmpeg-fetch /ffmpeg /usr/local/bin/ffmpeg

# Set working directory
WORKDIR /workspace

# Set timezone
ENV TZ=Asia/Shanghai

# Copy application file
COPY ./hojoai-webmvc-omniasst-meeting-server/target/hojoai-webmvc-omniasst-meeting-server-1.0.0-SNAPSHOT.jar ./app.jar

# Expose application port（与 application.properties 中 server.port 一致）
EXPOSE 12007

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
