# 贡献指南

感谢你对 Hojo-Agentic-OS 项目感兴趣！我们欢迎任何形式的贡献。

本地构建（`make build`）、Maven 顺序与前置依赖说明见 **[docs/BUILD_zh.md](docs/BUILD_zh.md)**（英文：[BUILD.md](docs/BUILD.md)）。安全漏洞请按 **[SECURITY.md](SECURITY.md)** 私下报告，勿发公开 Issue。

## 行为准则

参与本项目即表示你同意遵守 **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)**（贡献者公约）。摘要如下：

- 尊重所有贡献者
- 保持友好和建设性的讨论
- 欢迎新贡献者并耐心回答问题
- 关注于什么对项目最好

## 如何贡献

### 报告 Bug

如果你发现了 Bug，请在 GitHub 上提交 Issue：

1. 搜索现有 Issue，确认问题未被报告
2. 使用 [Bug 报告模板](https://github.com/HojoAI/Hojo-Agentic-OS/issues/new?template=bug_report.yml)创建新 Issue
3. 提供清晰的复现步骤和期望行为
4. 包含相关日志、截图或错误信息

### 提出新功能

1. 搜索现有 Issue，确认功能未被提议
2. 使用 [功能建议模板](https://github.com/HojoAI/Hojo-Agentic-OS/issues/new?template=feature_request.yml)创建新 Issue
3. 清晰描述功能用例和预期效果
4. 等待维护者讨论和批准

### 提交代码

#### 开发环境设置

```bash
# 克隆仓库
git clone https://github.com/HojoAI/Hojo-Agentic-OS.git
cd Hojo-Agentic-OS

# 构建并将各模块安装到本地 ~/.m2（仓库无根 pom.xml，顺序与 Makefile 一致）
make build
```

#### 分支策略

- `main` - 主分支，保持稳定
- `develop` - 开发分支
- `feature/*` - 功能分支
- `bugfix/*` - Bug 修复分支
- `hotfix/*` - 紧急修复分支

#### 提交 Pull Request

1. Fork 仓库到你的 GitHub 账户
2. 从 `develop` 分支创建功能分支：`git checkout -b feature/your-feature-name`
3. 进行修改并提交代码
4. 推送到你的 Fork：`git push origin feature/your-feature-name`
5. 在 GitHub 上提交 Pull Request

#### 代码规范

- 遵循 Google Java Style Guide
- 使用 IDE 格式化工具（如 IntelliJ IDEA 的 Google Java Format 插件）
- 添加必要的单元测试
- 确保所改模块测试通过：`mvn -f <模块路径>/pom.xml test`（见 docs/BUILD_zh.md）
- 为公共 API 添加 Javadoc 注释

#### Commit 消息规范

使用语义化提交消息：

```
<type>(<scope>): <subject>

<body>

<footer>
```

Type 类型：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建或辅助工具

示例：
```
feat(voice): add real-time translation support

Implement WebSocket-based real-time translation service
with automatic language detection and streaming support.

Closes #123
```

### 文档贡献

文档同样重要！你可以通过以下方式贡献：

- 修复拼写错误和错别字
- 改进现有文档的清晰度
- 添加代码示例和教程
- 翻译文档到其他语言

## 代码审查流程

提交 PR 后：

1. 自动 CI 检查将运行（构建、测试、代码质量检查）
2. 维护者会进行人工审查
3. 可能会要求你进行修改
4. 审查通过后，你的代码将被合并

## 获取帮助

- GitHub Issues: 技术问题和功能讨论
- GitHub Discussions: 一般问题和社区交流
- 安全问题：请查看 [SECURITY.md](SECURITY.md)

## 许可证

你向本仓库提交并期望被合并的代码，同意在 **[与本作品相同的许可条款](LICENSE)** 下授权：即 **Hojo AgenticOS License**——在 **Apache License 2.0** 基础上附加 `LICENSE` 中写明的条件（品牌与署名、贡献者条款，包括许可方可调整本协议及可将贡献用于商业用途等）。**这不等同于**未附加条款的「纯」Apache-2.0 单独文本。若你需要其他安排，请在提交大量变更前与维护者书面约定。

## 感谢

感谢每一位贡献者！你的贡献让这个项目变得更好。
