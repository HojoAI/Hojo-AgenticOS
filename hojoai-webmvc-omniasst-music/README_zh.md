# hojoai-webmvc-omniasst-music

**Omniasst 音乐服务**：聚合网易云能力（搜歌、播放地址、歌词、排行榜等），提供 OAuth2 授权流程，以及基于 SSE 的音乐智能体对话能力。

## 能力列表

- 网易云搜索：歌曲搜索、模糊搜索、标签搜索、排行榜
- 播放能力：播放地址获取、歌词获取
- OAuth2 授权：授权 URL 生成、回调处理、Token 刷新、状态查询
- 音乐智能体：AI 驱动的自然语言音乐查询，通过 SSE 流式返回
- 播放数据上报：播放记录上报网易云

## 技术栈

- Java 21、Spring Boot Web
- 继承 [hojoai-parent](../hojoai-parent)；通过 Feign 等调用网易云开放平台（配置项指定 `url`）

## 运行约定

| 项目 | 说明 |
|------|------|
| 可运行模块 | `hojoai-webmvc-omniasst-music-server` |
| 默认 HTTP 端口 | `12009` |
| Servlet context-path | `/omniasst-music` |
| Spring 应用名 | `hojoai-webmvc-omniasst-music` |
| 经网关 | [hojoai-gateway-omniasst](../hojoai-gateway-omniasst) `/omniasst-music/**` |

## 构建与运行

```bash
cd hojoai-webmvc-omniasst-music
mvn clean install
cd hojoai-webmvc-omniasst-music-server && mvn spring-boot:run
```

## 文档

- [API.md](./API.md) — 对外 `/netease-cloud`、`/oauth2`、`/music-agent` 等路径说明。
