将本地 ffmpeg 静态压缩包放到本目录，供 `Dockerfile` 在构建时直接 `COPY` 使用。

需要的文件名：

- `ffmpeg-release-amd64-static.tar.xz`
- `ffmpeg-release-arm64-static.tar.xz`

构建逻辑会根据 `TARGETARCH` 自动选择对应文件：

- `amd64` -> `ffmpeg-release-amd64-static.tar.xz`
- `arm64` -> `ffmpeg-release-arm64-static.tar.xz`

注意事项：

- 目录里至少需要存在当前构建目标架构对应的压缩包，否则构建会报 `missing local ffmpeg package`。
- 如果只构建单一架构，只放对应的一个文件也可以。
- 这些文件应纳入构建上下文，不要在 `.dockerignore` 中排除本目录。
