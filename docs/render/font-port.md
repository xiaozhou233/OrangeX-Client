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
│   │   ├── FontManager.java           # 按 (family,size,bold,scale) 缓存
│   │   ├── FontFace.java              # 图集 + 纹理 + drawScale
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

    title.drawStringWithShadow("OrangeX", 8, 8, 0xFFFFFFFF);
    body.drawString("中文测试：钻石剑", 8, 30, 0xFFFFAA00);
}
```

约定：

- 坐标和字号都是 **Minecraft GUI 像素**（`ScaledResolution` / `getGuiScaledWidth` 空间）。
- `FontRenderer` 只在当前帧有效，不要缓存到字段里跨帧使用。
- `getFont(size, bold)` 返回的对象可以在一帧内重复使用。
- 英文默认使用 Proxima；遇到 CJK 字符时自动懒加载 Noto 回退。

## 3. 光栅化流程

1. `StbFontBaker` 用 stb_truetype 读取 TTF 的 hhea/ascent/descent。
2. 字符集：
   - ASCII `32..126`
   - Latin-1 `160..255`
   - Noto 额外解码 GBK 双字节区间（默认全 GBK，`noto.ttf` 子集实际约 6900 个汉字）
3. 在 `4096 × 4096` 的 8 位覆盖度位图上打包：
   - Latin 使用 `2×2` oversampling
   - CJK 使用 `1×1` oversampling
4. 每个字形记录 `x0/y0/x1/y1`（相对基线）、`u0/v0/u1/v1`、`advance`。
5. 纹理上传：
   - 1.8.9：`GL_ALPHA` + 固定管线 `GL_MODULATE`
   - 26.2：扩展为 RGBA（白色 RGB + 覆盖度 alpha），包装成 `DynamicTexture`

字号策略：

```text
rasterHeight = round(logicalSize * guiScale)
drawScale    = logicalSize / rasterHeight
```

因此 GUI Scale 只提高烘焙分辨率，不改变逻辑排版宽度。为避免 4096 图集溢出：

| 字体 | 最大 rasterHeight |
| --- | --- |
| 非 CJK（Proxima/Poppins） | 64 |
| CJK（Noto） | 36 |

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
- 图集使用 `GL_RGBA`（白色 RGB + 覆盖度 alpha），避免 `GL_ALPHA` 在固定管线中 RGB 为 0 导致文字发黑。

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

调试 Overlay 默认开启；不需要时加：

```text
-Dorangex.debugFont=false
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
| `-Dorangex.font.fullGbk` | `true` | `false` 时只烘焙 GB2312 一级汉字（约 3755 个） |
| `-Dorangex.font.atlasSize` | `4096` | 图集边长，允许 `512..8192`。26.2 使用 RGBA 纹理，8192 约占用 256 MB 显存 |

启动日志会输出：

```text
[OrangeX] Font backend: 1.8.9/GL11
[OrangeX] Baked proxima 20px (191 glyphs, 33 ms)
[OrangeX] Baked noto 28px (... glyphs, ... ms)
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
- 没有多页图集；CJK 在超大字号下会被限制到 36px 烘焙高度。
- 只支持 `§0-9/a-f/r`，没有 italic / underline / obfuscated。
- 粗体只对内置了 bold 文件的字体生效（Proxima）；Noto 使用同一文件。
- `FontManager` 目前不会在 GUI Scale 变化时主动释放旧纹理，需要时可调用 `clear()`。
- 26.2 使用 RGBA 图集，4096² 单张约 67 MB 显存；不同字号会各自持有一张。若要做大量字号，建议后续改成 R8 + 自定义画字管线或做字号分桶。

## 8. 已验证环境

| 环境 | 结果 |
| --- | --- |
| 1.8.9 MCP | 正常渲染 |
| 1.8.9 Vanilla（`avo#a(F)V`） | 正常渲染 |
| 1.8.9 Forge（`GuiIngameForge`） | 走同一注入逻辑，待实机验证 |
| 26.2 Vanilla（`Gui#extractRenderState`） | 正常渲染 |

在真实 LWJGL2 窗口上下文中的离屏冒烟测试也通过：绘制字母 `A` 得到
`nonBlack=160, max=255, glError=0`。
