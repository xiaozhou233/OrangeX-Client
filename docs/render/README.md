# 渲染相关文档

本目录记录客户端字体、GUI 与跨版本渲染实现的分析与设计参考。

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [vape-font-gui-rendering.md](vape-font-gui-rendering.md) | Vape 4.21 的字体光栅化、文本排版、GUI 组件树、双渲染后端与 1.7.10–26.2 跨版本路径分析 |
| [font-port.md](font-port.md) | OrangeX 的字体移植实现：TTF 烘焙、Render2D API、1.8.9 / 26.2 接入、调试参数与限制 |

## 快速导航

- 字体资源与语言选择 → 主文档 §2.1
- stb_truetype 字形图集 → 主文档 §2.2
- Buffered / Legacy 字体渲染器 → 主文档 §2.3
- Minecraft 原生 `FontSet` 字体桥 → 主文档 §2.4
- § 颜色码、阴影、宽度缓存、GUI Scale → 主文档 §2.5
- 中文覆盖与构建期校验 → 主文档 §2.6
- ClickGUI 生命周期与输入 → 主文档 §3.1
- 坐标系统与缩放 → 主文档 §3.2
- 组件树与布局 → 主文档 §3.3
- `GuiRenderPrimitives` 双后端 → 主文档 §3.4
- Universal Shader 批次渲染 → 主文档 §3.5
- 1.21.6+ / 26.x 兼容 → 主文档 §3.7
- 版本矩阵 → 主文档 §4

## 阅读前提

主文档以 `E:\Vape-v4.21` 为源码根，涉及的文件路径均相对于该目录。
文中结论来自静态源码阅读，运行时行为仍需结合实际客户端验证。
