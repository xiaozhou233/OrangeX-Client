# OrangeX 开发文档

本文档目录面向继续开发 OrangeX 的开发者，说明各层职责、代码放在哪里，以及新增功能时的完整流程。

## 文档索引

### 版本与运行时

| 文档 | 内容 |
| --- | --- |
| [adding-minecraft-versions.md](adding-minecraft-versions.md) | 如何让 OrangeX 识别新的 Minecraft 版本：版本 ID、结构签名、加载器探测 |
| [adding-wrapper-versions.md](adding-wrapper-versions.md) | 如何为一个已识别的版本实现跨版本 Wrapper |
| [adding-wrapper-capabilities.md](adding-wrapper-capabilities.md) | 如何新增一种跨版本能力（以 `Player` + `NoJumpDelay` 为例） |
| [reflection-and-mappings.md](reflection-and-mappings.md) | 反射、MCP/SRG/混淆名与 `MappingResolver` 的使用规范 |

### 功能开发

| 文档 | 内容 |
| --- | --- |
| [adding-modules.md](adding-modules.md) | 新增模块：注册、事件、Value 设置项、配置持久化 |
| [hooks.md](hooks.md) | 字节码 Hook 架构与新增 Hook 的规则 |
| [hook-on-tick-example.md](hook-on-tick-example.md) | 从零理解 tick 注入与 EventBus 的示例 |

### 渲染

| 文档 | 内容 |
| --- | --- |
| [render/README.md](render/README.md) | 渲染模块总览 |
| [render/gui-rendering.md](render/gui-rendering.md) | GUI 绘制 API |
| [render/font-port.md](render/font-port.md) | 字体后端 |
| [render/world-gating-and-input.md](render/world-gating-and-input.md) | 世界渲染开关与输入处理 |
| [render/vape-font-gui-rendering.md](render/vape-font-gui-rendering.md) | 字体/GUI 渲染实现细节 |

## 推荐阅读顺序

1. [adding-modules.md](adding-modules.md) —— 先能写一个能开关、能保存配置的模块。
2. [reflection-and-mappings.md](reflection-and-mappings.md) —— 理解为什么不能直接写 Minecraft 类名。
3. [adding-wrapper-capabilities.md](adding-wrapper-capabilities.md) —— 当模块需要新的游戏数据时，如何把它做成跨版本能力。
4. [adding-wrapper-versions.md](adding-wrapper-versions.md) —— 需要支持新版本时再读。
5. [hooks.md](hooks.md) —— 只有需要新的游戏内触发点时再读。

## 分层结构

```text
modules/                     功能模块（只依赖 wrapper/api）
    └── impl/<category>/     按分类放置模块
wrapper/api/                 跨版本契约（只依赖 JDK）
wrapper/common/              与版本无关的值对象
wrapper/internal/            版本选择逻辑
    └── vX_Y_Z/              唯一允许出现 Minecraft 类名/字段名的地方
event/                       EventBus 事件
hook/                        字节码注入
mapping/                     混淆名 -> 运行时名解析
config/                      模块配置持久化
```

## 改动应该放在哪一层

| 需求 | 应该改哪里 |
| --- | --- |
| 新增一个功能（飞行、ESP、自动跳跃……） | `modules/impl/...` + `ModuleManager` 注册 |
| 模块需要读一个新的游戏字段/方法 | 先在 `wrapper/api` 加能力，再在 `wrapper/internal/vX_Y_Z` 实现 |
| 支持新的 Minecraft 版本 | `runtime/version` + `wrapper/internal/vX_Y_Z` |
| 需要新的游戏内触发时机（例如攻击、受伤） | `hook/` 新增 `ClassHook` |
| 新增配置项 | 模块里的 `Value`，会自动进入配置和 ClickGUI |

核心约束：**模块和 `wrapper/api` 中不允许出现 `net.minecraft.*`、SRG 名或混淆名**，版本差异全部收敛在 `wrapper/internal/vX_Y_Z`。

## 构建

```bash
./gradlew :OrangeX:shadowJar
# 或完整打包
./gradlew packageAll
```

`build` 任务被有意禁用，请使用 `shadowJar` / `packageAll`。
