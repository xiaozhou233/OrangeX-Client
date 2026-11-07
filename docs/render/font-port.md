# 字体移植（Vape 4.21 → OrangeX）

本文说明从 `E:\Vape-v4.21` 移植过来的 TTF 字体子系统，以及它在 1.8.9 / 26.2 两个版本上的接入方式。

> 设计目标：字体光栅化、排版、缓存全部与 Minecraft 版本无关；只有“把图集交给 GPU”和“每帧从哪拿绘制上下文”是版本相关的。

## 1. 目录结构

```text
OrangeX/src/main/java/cn/xiaozhou233/orangex/
├── render/
│   ├── api/
│   │   ├── FontRenderer.java          # 业务代码使用的字体接口
│   │   └── Render2D.java              # 每帧 2D 上下文
│   ├── font/
│   │   ├── FontFamily.java            # 内置字体族
│   │   ├── FontManager.java           # 图集/字号缓存、显存预算与回收
│   │   ├── FontAtlasEntry.java        # 共享图集 + GPU 纹理
│   │   ├── FontFace.java              # 图集视图 + drawScale + 宽度缓存
│   │   ├── FontAtlas.java             # 烘焙结果
│   │   ├── Glyph.java                 # 单字形度量 / UV
│   │   ├── StbFontBaker.java          # stb 打包适配
│   │   ├── TrueTypeFontRenderer.java  # 排版、§ 颜色、阴影、CJK 回退
│   │   └── stb/                       # 移植的 stb_truetype
│   ├── internal/
│   │   ├── FontBackend.java           # 版本相关 GPU 操作
│   │   ├── FontTexture.java
│   │   ├── GlyphDrawer.java
│   │   ├── FontBackendFactory.java
│   │   ├── v1_8_9/                    # GL11 立即模式
│   │   └── v26_2/                     # GuiRenderState 延迟绘制
│   ├── Render2DManager.java
│   └── FontDebugOverlay.java
├── event/
│   ├── facade/Render2DEvents.java     # 注入代码唯一引用
│   └── impl/EventRender2D.java
└── hook/impl/Render2DHook.java        # 每帧 2D 回调
```

## 2. 使用方式

```java
@Subscribe(threadMode = ThreadMode.POSTING)
public void onRender2D(EventRender2D event) {
    Render2D render2D = event.getRender2D();

    FontRenderer title = render2D.getFont(14.0f, true);
    FontRenderer body = render2D.getFont(10.0f, false);
    FontRenderer mono = render2D.getFont(FontFamily.MONTSERRAT, 10.0f, true);

    title.drawStringWithShadow("OrangeX", 8, 8, 0xFFFFFFFF);
    body.drawString("中文测试：钻石剑", 8, 30, 0xFFFFAA00);
}
```

约定：

- 坐标和字号都是 **Minecraft GUI 像素**（`ScaledResolution` / `getGuiScaledWidth` 空间）。
- `FontRenderer` 只在当前帧有效，不要缓存到字段里跨帧使用。
- `getFont(size, bold)` 返回的对象可以在一帧内重复使用；文本宽度会缓存，但不要在
  绘制循环里对每个字符重复 `getFont`。
- 英文默认使用 Proxima；遇到 CJK 字符时自动懒加载 Noto 回退。
- `getFont(FontFamily, size, bold)` 可以显式选择内置字体族；默认族可用
  `-Dorangex.font.family` 切换。

## 3. 光栅化流程

1. `StbFontBaker` 用 stb_truetype 读取 TTF 的 hhea/ascent/descent。
2. 字符集：
   - ASCII `32..126`
   - Latin-1 `160..255`
   - Noto 额外解码 GBK 双字节区间（默认全 GBK，`noto.ttf` 子集实际约 6900 个汉字）
3. 图集边长按字符集自动估算（`512..8192`），打包溢出时自动翻倍重试；只有显式设置
   `-Dorangex.font.atlasSize` 时才固定尺寸。Latin 面通常只有 `512/1024/2048`，
   不再固定 4096。
4. 打包分两遍进行（stb 的 oversampling 是上下文级设置）：
   - Latin 使用 `2×2` oversampling
   - CJK 使用 `1×1` oversampling（按 2×2 打包会让 CJK 图集大 4 倍）
5. 每个字形记录 `x0/y0/x1/y1`（相对基线）、`u0/v0/u1/v1`、`advance`。
6. 纹理上传：
   - 1.8.9：`GL_LUMINANCE_ALPHA`（2 字节/像素，RGB=L=alpha=覆盖度）。`GL_ALPHA`
     的 RGB 恒为 0，`GL_MODULATE` 会把文字乘成黑色；`LUMINANCE_ALPHA` 没有这个问题。
   - 26.2：扩展为 RGBA（白色 RGB + 覆盖度 alpha），直接写入 `NativeImage` 缓冲区，
     不再额外分配一份 RGBA 数组。

实际图集尺寸（本仓库字体实测）：

| 场景 | 图集 |
| --- | --- |
| Latin 14px 以下（raster ≤ 22） | `512²` |
| Latin 28~44px（HUD Logo 等） | `1024²` |
| Latin 64px | `2048²` |
| 全 GBK，raster 8~16 | `1024²` |
| 全 GBK，raster 20~36 | `2048²` |
| 全 GBK，raster 44+ | `4096²`（默认最大 36，不会走到） |

字号策略：

```text
rasterHeight = round(logicalSize * guiScale)   # 再按字体族上限裁剪
drawScale    = logicalSize / bakeHeight
```

因此 GUI Scale 只提高烘焙分辨率，不改变逻辑排版宽度。

## 3.1 图集复用与显存

图集按 `(字体资源, bakeHeight)` 缓存，逻辑字号只影响 `drawScale`，所以多个字号共用
一张纹理。为避免 4096 图集溢出，烘焙高度上限为：

| 字体 | 最大 rasterHeight | 烘焙高度 |
| --- | --- | --- |
| 非 CJK（Proxima/Poppins/Jello/Montserrat） | 64 | 精确高度（图集自适应） |
| CJK（Noto） | 36 | 量化到 `12/16/20/24/28/32/36` |

Noto 的每个烘焙高度都是一整张全字符图集，量化可以让相邻字号共享同一张纹理。
例如 guiScale=2 时，11px 和 12px 都会落到 24px 桶。

显存控制（`FontManager`）：

- `beginFrame()` 由 `Render2DManager` 每帧调用，把本帧用到的图集标记为“钉住”。
- 超过 `orangex.font.atlasBudgetMb` 时，从**非本帧使用**的图集里按 LRU 释放，
  因此不会出现每帧重新烘焙的抖动，当前帧的工作集可以暂时超出预算。
- 图集空闲超过 `orangex.font.idleSeconds` 后释放；GUI Scale 切换后的旧图集
  会在下一个 trim 周期回收。
- 每个 `FontFace` 自带一个宽度缓存（跨帧共享，最多 1024 条），`getStringWidth`
  对静态文本只计算一次。

## 4. 版本接入

### 4.1 1.8.9

- Hook：`GuiIngame#renderGameOverlay(F)V` 的末尾。
- Forge：改为 `net.minecraftforge.client.GuiIngameForge#renderGameOverlay(F)V`。
- `V1_8_9Render2D` 通过 `MappingResolver` 反射 `ScaledResolution`，拿到 `scaledWidth/scaledHeight/scaleFactor`。
- `V1_8_9GlyphDrawer` 使用 `GL11` 立即模式绘制四边形；首次绘制时保存并在 `flush()` 中恢复：
  - 纹理绑定 / 激活纹理单元
  - `GL_BLEND` / `GL_TEXTURE_2D` / `GL_ALPHA_TEST` / `GL_DEPTH_TEST`
  - `GL_CULL_FACE` / `GL_LIGHTING` / `GL_FOG` / `GL_SCISSOR_TEST`
  - `GL_TEXTURE_ENV_MODE`
  - 当前 `glColor`
- 图集使用 `GL_LUMINANCE_ALPHA`（RGB=L=覆盖度，A=覆盖度，2 字节/像素）：
  - `GL_ALPHA` 的 RGB 为 0，`GL_MODULATE` 会让文字发黑；
  - `GL_RGBA` 要多占一倍显存；
  - 由于 L 也会被 `GL_MODULATE` 乘一次，字形顶点色需要预乘 alpha，并使用
    `GL_ONE / GL_ONE_MINUS_SRC_ALPHA` 混合；形状绘制仍用普通 alpha 混合，
    切换由 `V1_8_9RenderState` 自动管理并在帧末恢复原混合函数。

> 坑：LWJGL2 的 `glGetFloat(GL_CURRENT_COLOR, buffer)` 有
> `BufferChecks` 限制，缓冲区至少要有 **16** 个元素；只分配 4 个会在首个字形
> 抛出 `IllegalArgumentException`，表现为“字体烘焙成功但屏幕无反应”。

### 4.2 26.2

- Hook：`net.minecraft.client.gui.Gui#extractRenderState(DeltaTracker, boolean, boolean)` 的末尾。
- 把 `Gui` 实例传给 `Render2DEvents`。
- `V26_2Render2D` 反射：
  - `Gui.minecraft`
  - `Gui.guiRenderState`
  - 构造 `GuiGraphicsExtractor`
  - `Window#getGuiScale/getGuiScaledWidth/getGuiScaledHeight`
- 每个字形调用 `GuiGraphicsExtractor#innerBlit(...)`，把 `BlitRenderState` 加入 `GuiRenderState`，由 `GuiRenderer` 统一排序、合批、绘制。
- 不直接调用任何 OpenGL。

## 5. 调试

构建：

```bash
./gradlew :OrangeX:shadowJar
# 或完整注入包
./gradlew packageAll
```

> 部署注意：MCP 开发环境通常直接读取
> `E:\OrangeX-Client\out\tmp\OrangeX.jar`，而 Vanilla/Forge/NeoForge/Fabric
> 启动器会读取 `%USERPROFILE%\.orangex\OrangeX.jar`。改完代码后必须把新
> jar 同步过去，否则会出现“MCP 正常、其他版本没反应”。

调试 Overlay 默认关闭；需要时加：

```text
-Dorangex.debugFont=true
```

会在屏幕左上角绘制：

```text
OrangeX
Font backend: V1_8_9Render2D / V26_2Render2D
ASCII 0123456789
中文测试：钻石剑 / 迅捷
```

其他属性：

| 属性 | 默认 | 说明 |
| --- | --- | --- |
| `-Dorangex.font.fullGbk` | `true` | `false` 时只烘焙 GB2312 一级汉字（约 3755 个），CJK 图集可缩小到 2048 以内 |
| `-Dorangex.font.atlasSize` | 自动 | 固定图集边长（`512..8192`）。不设置时按字形集自动估算并在溢出时翻倍 |
| `-Dorangex.font.atlasBudgetMb` | `128` | 字体纹理显存预算（MB）。超出后按 LRU 释放非当前帧使用的图集 |
| `-Dorangex.font.idleSeconds` | `30` | 图集空闲多久后释放（秒） |
| `-Dorangex.font.debugCache` | `false` | 打印图集烘焙/释放日志与占用大小 |
| `-Dorangex.font.family` | `Proxima` | 默认字体族，支持显示名/枚举名/文件名，如 `Montserrat Light` |

启动日志会输出：

```text
[OrangeX] Font backend: 1.8.9/GL11
```

## 6. 添加新版本

1. 在 `render/internal/vX_Y_Z/` 新建：
   - `VX_Y_ZFontBackend implements FontBackend`
   - `VX_Y_ZFontTexture implements FontTexture`
   - `VX_Y_ZRender2D implements Render2D`
   - `VX_Y_ZGlyphDrawer implements GlyphDrawer`
2. 在 `FontBackendFactory` 的 `switch` 中注册。
3. 在 `Render2DHook` 的 `getTargetClass/transform` 中增加该版本的每帧 2D 入口。
4. 如果该版本使用新的 GUI 渲染架构，只需要改 `Render2D`/`GlyphDrawer`，字体光栅化与排版代码不用动。

## 7. 当前限制

- 没有移植 Vape 的 `StbSmoothFontRenderer`（直接读游戏 `FontSet`），因此没有“MC 原版字体”选项。
- 没有多页图集；CJK 在超大字号下会被限制到 36px 烘焙高度（对应当前 `2048²`
  全字符图集，26.2 约 16 MB）。
- 只支持 `§0-9/a-f/r`，没有 italic / underline / obfuscated。
- 粗体只对内置了 bold 文件的字体生效（Proxima/Montserrat/Jello）；Noto 使用同一文件。
- 26.2 的 `DynamicTexture` 固定为 RGBA，无法只用 1 字节/像素的 R8；目前靠自适应图集
  尺寸、CJK 字号分桶、1× CJK oversampling 和显存预算控制占用。

## 8. 已验证环境

| 环境 | 结果 |
| --- | --- |
| 1.8.9 MCP | 正常渲染 |
| 1.8.9 Vanilla（`avo#a(F)V`） | 正常渲染 |
| 1.8.9 Forge（`GuiIngameForge`） | 走同一注入逻辑，待实机验证 |
| 26.2 Vanilla（`Gui#extractRenderState`） | 正常渲染 |

在真实 LWJGL2 窗口上下文中的离屏冒烟测试也通过：绘制字母 `A` 得到
`nonBlack=160, max=255, glError=0`。
