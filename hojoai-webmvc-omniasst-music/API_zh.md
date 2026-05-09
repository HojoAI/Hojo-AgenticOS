# hojoai-webmvc-omniasst-music — API 文档

## 基址

- **context-path**：`/omniasst-music`
- 经网关：`{网关}/omniasst-music/...`

---

## 网易云聚合 — `/netease-cloud`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/netease-cloud/songs` | 歌曲检索 |
| GET | `/netease-cloud/songs/bytag` | 按标签 |
| POST | `/netease-cloud/songs/fuzzy` | 模糊搜索 |
| GET | `/netease-cloud/songs/toplist` | 排行榜 |
| GET | `/netease-cloud/song/playurl` | 播放地址 |
| GET | `/netease-cloud/song/lyric` | 歌词 |
| POST | `/netease-cloud/play/data/record` | 播放数据上报 |

## OAuth2 — `/oauth2`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/oauth2/generate-auth-url` | 生成授权 URL |
| GET | `/oauth2/callback` | 授权回调 |
| POST | `/oauth2/refresh-token` | 刷新 Token |
| GET | `/oauth2/auth-status` | 授权状态 |
| GET | `/oauth2/revoke` | 撤销授权 |

## 音乐智能体 — `/music-agent`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/music-agent/query` | 智能体查询，**SSE**（`text/event-stream`） |

---

## 内部 Feign（非本服务对外路径）

`NeteaseCloudMusicAPI` 映射网易云开放平台路径（如 `/openapi/music/basic/...`），由配置指定 `url`，**客户端应使用上文 `/netease-cloud` 与 `/oauth2` 等对外接口**。
