# Vape 4.21 字体与 GUI 渲染（含跨版本）

> 分析对象：`E:\Vape-v4.21`（Vape 4.21 恢复工程）。
> 本文所有相对路径均以该目录为根，例如 `src/main/java/gg/vape/ui/font/FontManager.java`。
> 结论来自源码静态阅读，未做运行时验证。

## 0. 一句话总结

Vape **不依赖某个 Minecraft 版本的 `FontRenderer` / `Screen` 实现**：

- 字体：内嵌 TTF，用移植版 stb_truetype 烘成字形图集，再用自建 shader 画四边形；1.21.10+ 额外提供“直接读取游戏 `FontSet`”的桥。
- GUI：不是 `GuiScreen`，而是每帧在 HUD / PostRender 阶段画的自绘 Overlay，输入由原生层回调。
- 跨版本：`ForgeVersion` 版本谓词 + `MappedClasses`/`Mapping*` 名字映射 + `GuiRenderPrimitives` 双渲染后端（立即模式 / 自建批次）。

---

## 1. 分层架构

```text
注入 DLL → Java payload
        │
        ├─ Runtime.NativeBridge / wrapper.impl.ForgeVersion   版本探测
        ├─ mapping.MappedClasses / Mapper / Mapping*          运行时类与成员解析
        ├─ wrapper.impl.*                                     对 MC 的薄封装
        ├─ utils.render.GuiRenderPrimitives                   绘制门面（双后端）
        └─ ui.font / ui.click                                 字体、组件、布局
```

### 1.1 版本探测

入口：`src/main/java/gg/vape/runtime/NativeBridge.java` 的 `gmv()`。

1. 优先读 Forge / NeoForge 的版本号：
   - `net.minecraftforge.common.ForgeVersion.minorVersion`
   - `net.minecraftforge.fml.loading.FMLLoader.forgeVersion`
   - `FMLLoader.versionInfo().forgeVersion()`
2. 失败则调用 `detectVanillaMappingVersion()`，用锚点类逐个探测：
   - `Vanilla1710Mappings` / `Vanilla189Mappings` / `Badlion189Mappings`
   - `Vanilla1122Mappings` / `Vanilla1165Mappings`
   - `Vanilla1201Mappings` / `Vanilla1206Mappings` / `Vanilla1211Mappings` / `Vanilla12111Mappings`
   - `NeoForge1201Mappings` / `NeoForge1211Mappings`
   - `Vanilla262Mappings` / `Fabric262Mappings`
3. 结果写入 `ForgeVersion.gameVersion`，对应枚举协议号：

```text
13   1.7.10
15   1.8.9
23   1.12.2
35   1.16.4
36   1.16.5
37   1.17
47   1.20.1
50   1.20.6
51   1.21.0
52   1.21.1
54   1.21.4
55   1.21.5
56   1.21.6
60   1.21.10
61   1.21.11
100  26.1
110  26.2
```

之后全代码通过 `ForgeVersion.MC_1_20_1.d()` 这类“大于等于”谓词分支：

- `d()` = `>=`
- `v()` = `>`
- `L()` = `<=`
- `Y()` = `<`
- `B()` = `!=`

### 1.2 跨版本名字映射

#### 类名

`src/main/java/gg/vape/mapping/MappedClasses.java` 里大量静态 `Class` 字段由 `src/main/java/gg/vape/mapping/runtime/` 下的表初始化：

| 表 | 用途 |
| --- | --- |
| `ClassNameRemapTableV23/V35V36/V37/V50/V51/V54/V55/V56/V60/V61/V100/V110` | 各版本“源码类名 → 运行时类名” |
| `MemberNameRemapTableV*` | MCP/mojmap/SRG → 混淆成员名 |
| `NeoForgeClassMap` / `NeoForgeFieldMap` / `NeoForgeObfMap` | NeoForge / 纯原版 1.20.1、1.21.1 的特殊转换 |
| `VanillaSrgMappings` / `NeoForge*Mappings` / `Fabric*Mappings` | classpath 探测 + `joined.srg` 解析 |

`MappedClasses` 还区分运行时形态：

- `Vape.INSTANCE.isForgeAbsent()`：纯原版 / Fabric / NeoForge 无 Forge remap
- `Vape.INSTANCE.isForgeRemapActive()`：Forge 的 SRG remap 路径
- `Vape.INSTANCE.isVanillaMinecraftPresent()`：纯原版

#### 成员访问

`src/main/java/gg/vape/mapping/Mapping.java` + `MappingMethod` / `MappingField`：

```java
// 声明式注册（MFontRenderer 片段）
this.P = this.Y("drawInBatch", true, Void.TYPE,
        String.class, Float.TYPE, Float.TYPE, Integer.TYPE, Boolean.TYPE,
        MappedClasses.ZA, MappedClasses.ZK, MappedClasses.Y8,
        Integer.TYPE, Integer.TYPE);
```

运行时解析链：

```text
MappingMethod.invoke*
  ├─ generatedInvoker   （GeneratedAccessorFactory 生成的字节码访问器）
  ├─ reflectedMethod    （Forge remap 未激活时直接反射）
  └─ NativeMappedMemberInvoker
        └─ Reflections
              └─ MappingRegistry.matches()
                    ├─ resources/mappings/forgeXXXX/methods.csv
                    ├─ resources/mappings/forgeXXXX/fields.csv
                    └─ Vanilla*Mappings 的 SRG 表兜底
```

所以同一份 Java 代码可以匹配：

- 1.8.9 的 `func_78276_b`
- 1.16.5 的 SRG 名
- 1.17+ 的 `m_xxxxx_`
- 1.20.1+ 的 mojmap 名
- 26.x 的 mojmap 名

---

## 2. 字体系统

### 2.1 字体资源与语言选择

`src/main/java/gg/vape/ui/font/FontManager.java`：

| `FontFamily` | 资源 | 说明 |
| --- | --- | --- |
| `PROXIMA`（默认） | `resources/proxima.ttf`、`proximabd.ttf` | `FontManager.e()` 读入 |
| `NOTO`（中文） | `resources/noto.ttf` | 约 1.1 MB 的子集字体 |
| `POPPINS` | `resources/poppins_rg.ttf` | |
| `ARIAL` | 系统 `arial.ttf` / `arialbd.ttf` | 扫描 `%LOCALAPPDATA%\Microsoft\Windows\Fonts` 与 `%SystemRoot%\Fonts` |
| `MINECRAFT` | 游戏自身 `FontSet` | 见 2.4 |

语言选择在 `src/main/java/gg/vape/ui/font/FontSelector.java`：

- English → `IdentityFontOption`（Proxima，不翻译）
- Chinese → `NotoFontOption`（`FontFamily.NOTO`，`noto.ttf`）
- Spanish / French / Portuguese → `FontOptionVariant*`（Proxima + 翻译表）

翻译表为 `resources/{english,chinese,french,spanish,portuguese}.properties.txt`，由 `FontOption` 用 `Properties` 载入。`FontOption.s(String)` 做整串查表，未命中返回原文。

### 2.2 字形光栅化：移植版 stb_truetype

`src/main/java/gg/vape/ui/font/stb/` 是一份完整的 stb_truetype Java 移植：

- `StbTrueTypeFontInfo`
- `StbTrueTypePackContext` / `StbTrueTypePackRange` / `StbTrueTypePackedChar`
- `StbTrueTypeGlyphPackNode` / `StbTrueTypeBakeContext`（在 `SmoothFontRasterState` 内）
- `StbGlyphCache`（MC 字体桥专用，见 2.4）

核心入口在 `src/main/java/gg/vape/ui/font/SmoothFontRasterState.java`（约 3400 行）。

`BufferedSmoothFontRenderer.w(byte[], int, String)` 的流程：

1. **收集字符集**
   - ASCII `32..126`
   - Latin-1 `160..255`
   - `chinese.properties.txt` 中出现的所有字符
   - 额外用 GBK 双字节区间（`0xA1..0xF7` × `0xA1..0xFE`）解出全部 CJK 汉字，保证游戏内中文物品/实体名不空白
2. **初始化字体**
   - `SmoothFontRasterState.z(info, bytes, 0)`
   - 取全局 ascent/descent，计算 `f5 = size * (0.6 / aspect)`
3. **打包图集**
   - `4096 × 4096` 单通道位图
   - `stbtt_PackBegin/PackFontRange`
   - 得到每个字的 `x0,y0,x1,y1,xadvance` 与 UV
   - 存入 `Map<Integer, SmoothFontGlyph>`
4. **创建 GL 纹理**
   ```java
   GlImageTexture.create(4096, 4096, buffer, GL_RED /*6403*/,
                         GL_LINEAR /*9729*/, GL_REPEAT /*10497*/);
   ```
   通过 `RenderThreadTaskQueue.enqueue(...)` 回到渲染线程执行。

> 注意：**每个字号、每个字体族都各自持有一张 4096² 图集**。
> 默认还会烘入全部 GBK 汉字，单张 R8 约 16 MB，加 mipmap 约 22 MB，多字号叠加后显存开销明显。

### 2.3 两个字体渲染器实现

`FontManager` 的缓存值是抽象类 `SmoothFontRenderer`，具体类型由后端决定：

```java
GuiRenderPrimitives.d()
        ? new BufferedSmoothFontRenderer(...)
        : new LegacySmoothFontRenderer(...);
```

| 实现 | 版本 | 绘制方式 |
| --- | --- | --- |
| `BufferedSmoothFontRenderer` | 1.16.5+ | 每字 4 顶点 → `RenderBatchBuilder` → universal shader |
| `LegacySmoothFontRenderer` | 1.7.10 / 1.8.9 / 1.12.2 | `glBegin(GL_QUADS)` 立即模式 + 独立 GLSL |

`BufferedSmoothFontRenderer` 的字体 shader 在 `resources/shader/universal_frag.frag`（`shaderMode = 11`）：

```glsl
void fontShader() {
    float alpha = texture(imgTexture, f_TexCoords).x;
    alpha = max(0.0, alpha + 0.25 * (1.0 - alpha));
    float smoothenAlpha = smoothstep(0.2, 0.8, alpha);
    alpha = mix(alpha, 0.0, 1.0 - smoothenAlpha);
    FragColor = vec4(f_VertexColor.rgb, alpha * f_VertexColor.a);
}
```

“先加 0.25 再 smoothstep”是为了让小字号不发虚。

`LegacySmoothFontRenderer` 同样用 stb 建图集，但绘制走：

```java
GL11.glBegin(GL_QUADS);
GL11.glTexCoord2f(...);
GL11.glVertex2d(...);
GL11.glEnd();
```

并用 `glPushMatrix/glScalef` 做缩放；必要时回退 `Minecraft.getFontRenderer()` 画字。

### 2.4 Minecraft 原生字体桥：`StbSmoothFontRenderer`

当用户选择 `FontFamily.MINECRAFT` 时，`FontManager.x(int)` 返回 `StbSmoothFontRenderer`。它不读 TTF，而是读取游戏自己的字体：

1. `Minecraft.q()` 取 MC 的 `FontManager`
2. `h()` 取默认 `FontSet`
3. `StbGlyphCache` 对每个 code point 调 `FontSet.getGlyphInfo(char)`，取出：
   - 纹理 id
   - UV（`u0/v0/u1/v1`）
   - advance
   - ascent / descent
   - glyph 尺寸
4. 缓存为 `StbGlyphCacheEntry`
5. 绘制时：

```java
// 彩色位图字体
BufferedGuiRenderPrimitives.drawMinecraftColorFontGlyph(...);
// R8 强度字体
BufferedGuiRenderPrimitives.drawMinecraftIntensityFontGlyph(...);
```

对应 universal shader 的两个分支：

| shaderMode | 函数 | 适用纹理 |
| --- | --- | --- |
| 16 | `minecraftFontColorShader()` | RGBA8 彩色位图字体 |
| 17 | `minecraftFontIntensityShader()` | R8 强度字体（`.r` 当 alpha） |

纹理格式通过 `glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT)` 探测：

```java
bl2 = n3 != GL_R8; // 33321
```

桥不可用时回退到 wrapper 的 `Minecraft.getFontRenderer()`：

- `StbGlyphCache.l()` 明确要求 **1.21.10+**
- `StbSmoothFontRenderer.y(...)` 用 `MatrixStack.S(f,f,f)` 后调 `FontRenderer.E/V`
- 1.21.6+ 走 `RenderItemTextBridge`（新的 `Font.drawInBatch` 签名）
- 26.2 对空格单独处理（`StbGlyphCache.H(char)` 调 `getStringWidth(" ")`）

### 2.5 文本排版细节

`src/main/java/gg/vape/ui/font/SmoothFontRenderer.java` 统一了 API：

| 方法 | 含义 |
| --- | --- |
| `g/O` | 普通绘制；`O(..., boolean shadow)` |
| `v/I` | 带阴影；`I()` 先剥掉 `§` |
| `d/R/N/Y` | 宽度 / 高度测量 |
| `t/T` | 双色（渐变）文本 |
| `f` | 清空字符串/宽度缓存 |

具体行为：

- **§ 颜色码**
  - 逐字符解析 `§x`
  - `o(char, float[3])` 是 16 色表
  - `§r` 恢复基础色
- **阴影**
  - Buffered 非 Stb：`Integer.MIN_VALUE`（ARGB `0x80000000`，半透明黑）偏移 `+0.5,+0.5` 画一遍
  - Stb：颜色乘 `0.25`，偏移 `+1,+1` 画一遍
- **宽度缓存**
  - `SmoothFontRendererWidthCache`：`LinkedHashMap`，最多 5120 项
  - 剥离 `§` 后的字符串也有缓存
- **字号 / GUI Scale 关系**

  ```java
  n = (int)(16 * size * guiScaleFactor);  // 烘焙字号
  advance *= 0.6f / guiScaleFactor;       // 逻辑宽度
  ```

  即：**GUI Scale 只提高字形烘焙分辨率（超采样），不改变逻辑排版宽度**；实际屏幕尺寸由外层矩阵的 `2 * guiScale` 放大。

- **截断 / 自适应**
  - `TextLabelFitScaleCache`：从最大字号往下降，直到宽度放得下
  - `TruncatedTextComponent` / `FadingTruncatedTextComponent`
  - `MarqueeTextRendererComponent`：跑马灯
- **翻译时机**
  - `BufferedSmoothFontRenderer.b()` / `StbSmoothFontRenderer.Z()` 在绘制和测宽前都会执行：

    ```java
    string = Vape.INSTANCE.getFontSelector().W().s(string);
    ```

  - 因此翻译后的文本宽度也正确。
- **限制**
  - 按 UTF-16 `char` 遍历，不处理代理对（emoji / 扩展 B 区汉字）
  - CJK 覆盖到 GBK 范围为止

### 2.6 中文覆盖的构建期校验

`build.gradle` 注册了 `verifyFontCoverage`：

- 用 `tools/FontCoverageCheck.java` 直接解析 `noto.ttf` 的 cmap（format 0/4/6/12）
- 对照 `chinese.properties.txt` 中出现的每个字符
- 有缺字则构建失败

所以中文翻译新增字符时，必须同步裁剪或替换 `noto.ttf`。

---

## 3. GUI 系统

### 3.1 生命周期：不是一个 Minecraft `Screen`

Vape 的 ClickGUI 不继承 `GuiScreen`，而是画在游戏画面上的自绘 Overlay：

```text
HUD 打开（inputEnabled = true）
  └─ 游戏 HUD 钩子 EventRender2D.create()
        ├─ GuiRenderPrimitives.o(displayWidth, displayHeight)
        ├─ ClientSettings.renderHudOverlay()/renderHudFrames()
        ├─ EventRender2D.fire()
        └─ GuiRenderPrimitives.L(...)

ClickGUI 打开（inputEnabled = false）
  └─ EventPostRenderTick.fire()
        └─ GuiScreenNativeCallbackBridge.drawScreen(null, 0, 0, 0.0f)
              └─ ClientSettings.renderGui()
        └─ RenderBatchManager.flushGuiBatches(...)
```

- 输入不走 MC 的 `mouseClicked`：
  - 原生层回调 `GuiScreenNativeCallbackBridge.mouseClicked / handleMouseInput`
  - 再进入 `InputEventDispatcher` / `MouseInputState`
- 打开 / 关闭：`ClientSettings.toggle()`
  - `Minecraft.R()` 释放鼠标
  - `gameSettings.P(false)`
  - `inputEnabled = false`
- 26.x 专门注入：
  - `GuiRendererRenderTickMappingTask` 在 `GuiRenderer.render()` 前后插：
    - `EventRender2DHudCallback` → 只画 HUD（在游戏 HUD 下）
    - `EventRender2DGuiCallback` → 画通知和 ClickGUI（在游戏 HUD 上）

这样做最大的好处：**完全绕开各版本 `Screen` / `GuiScreen` / `AbstractGui` 的 API 差异**。

### 3.2 坐标系与缩放

每帧开始：

```java
GuiRenderPrimitives.o(displayWidth, displayHeight);
// legacy: NativeBridge.rs(0, w, h) → glOrtho(0, w, h, 0, 1000, 3000)
// 然后 backend.scale(2, 2, 2)

OpenGlBackendHolder.backend.scale(guiScale, guiScale, guiScale);
```

- `Minecraft.J()/h()` 返回 **framebuffer 物理像素**
- 正交投影覆盖 `0..framebufferWidth`
- 先固定 `scale(2,2,2)`，再乘 Vape 自己的 GUI Scale：
  - `0.5 / 0.8 / 1.0 / 1.2 / 1.5`
  - Auto 按高度选：`>= 2000 → 1.5`，`>= 1000 → 1.2`，否则 `1.0`
- 因此 **1 个 UI 逻辑单位 ≈ `2 * guiScale` 物理像素**
- Scissor 按同一比例换算：

  ```java
  double d5 = 2.0 * Vape.INSTANCE.getClientSettings().getGuiScaleFactor();
  ```

- 字体烘焙尺寸同样乘 `guiScale`，但逻辑宽度除回去，保证改 GUI Scale 时布局不跳、只是更清晰

### 3.3 组件树与布局

核心目录 `src/main/java/gg/vape/ui/click/`：

| 类 / 目录 | 职责 |
| --- | --- |
| `GuiComponent` | 位置/尺寸、可见性、父子关系、鼠标/键盘监听、Tooltip、绑定 `Value` |
| `FrameComponent extends GuiComponent` | 可滚动 Frame 的基类 |
| `Frame` | 可拖动/可缩放的窗口 |
| `FrameStackManager` / `ClickGuiFrameManager` | 窗口栈、置顶、Popup |
| `PopupFrame` / `AnchoredPopupFrame` / `DimmedCenteredPopupFrame` | 弹窗 |
| `layout/ComponentLayout` | 布局约束与排版 |
| `layout/WrappingFlowLayout` / `BottomUpFlowLayout` | 流式布局 |
| `module.none.ClientSettings` | 每帧调度、GUI Scale、Frame 注册 |

`FrameComponent` 的两个核心方法：

- `c()`：**渲染**
  - 背景 / 圆角 / 阴影
  - 滚动条
  - `scissor` 裁剪
  - 递归子组件 `child.c()`
- `J()`：**更新 / 交互**
  - hover
  - 点击、滚动
  - tooltip
  - 递归子组件

文本组件最终都落到：

```java
SmoothFontRenderer fr = getFontRenderer(scale);
// 或 getAlternateFontRenderer(scale)  （bold）

fr.d(text, x, y, color);  // 无阴影
fr.v(text, x, y, color);  // 有阴影
```

`GuiComponent.getFontRenderer(double scale)`：

```java
return this.fontOption != null
        ? this.fontOption.k((float) scale, false)
        : Vape.INSTANCE.getFontManager().Y(scale);
```

### 3.4 绘制门面：`GuiRenderPrimitives` + 双后端

`src/main/java/gg/vape/utils/render/GuiRenderPrimitives.java` 是所有 UI 绘制的统一入口。每个方法内部都做同一个判断：

```java
if (GuiRenderPrimitives.d()) {          // 1.16.5+
    // BufferedGuiRenderPrimitives / RenderBatchBuilder
} else {
    // GL11 立即模式 + 自带 GLSL
}
```

`d()` 的实现：

```java
public static boolean d() {
    return ForgeVersion.MC_1_16_5.d();
}
```

后端由 `OpenGlBackendHolder` 选择：

```java
public static OpenGlBackend backend = GuiRenderPrimitives.d()
        ? new BufferedOpenGlBackend()
        : new LegacyOpenGlBackend();
```

| 后端 | 矩阵 | 顶点 |
| --- | --- | --- |
| `LegacyOpenGlBackend` | `glPushMatrix/glTranslatef/glScalef` | `glBegin/glVertex` |
| `BufferedOpenGlBackend` | 软件 `RenderMatrixStack` | 只记录 capability，不直接画 |

### 3.5 现代路径：自建批次 + Universal Shader

这是跨版本 GUI 的核心设计。

1. **每个绘制调用产出一个 `RenderBatchBuilder`**
   - `addSolidRect` / `addRoundedRect` / `addCircle` / `addFontGlyph`
   - `addTexturedRect` / `addBlurPass` / `addCompositeRoundedRect` …
2. **固定 15 个顶点属性**（与 `universal_vert.vert` 的 `layout(location=0..14)` 对应）

   ```text
   shaderMode, position, uv, color,
   inner, feather, center, middleAngle, sweepAngle,
   screenDimensions, origin, innerRect, radius3, spread, corners
   ```

   `shaderMode` 就是“这个顶点走哪段片元逻辑”的编号。

3. **`RenderBatchManager.queueGuiBatch(builder)`**
   - 按纹理、拓扑、模型矩阵、GL capability、scissor 是否相同尝试合并
   - 每个 batch 最多合并 4500 个 builder
4. **`flushGuiBatches()`**
   - 保存并接管 GL 状态（VAO / VBO / program / texture / framebuffer）
   - `RenderBatchBuffer.stageBatch()` → 上传顶点/索引
   - `glDrawElements`
   - 恢复状态
5. **Universal Shader**
   - `resources/shader/universal_vert.vert`
   - `resources/shader/universal_frag.frag`

   顶点着色器：

   ```glsl
   gl_Position = u_Projection * u_View * u_Model * vec4(aPos, 1.0);
   ```

   片元着色器按 `shaderMode` 分发：

   | mode | 功能 |
   | --- | --- |
   | 0 | `arcStroke` |
   | 1 | `blur` |
   | 2 / 3 | 圆 / 圆环描边 |
   | 4 | 纹理圆 |
   | 5 / 6 | 圆角矩形 / 圆角描边 |
   | 7 | 圆角纹理矩形 |
   | 8 | 阴影 |
   | 9 | 纯色 |
   | 10 | 纹理 |
   | 11 | 自定义字体 |
   | 12 | 反向圆角 |
   | 13 / 14 | 胶囊 / 渐变 |
   | 16 / 17 | MC 彩色字体 / MC 强度字体 |

   所有圆角、描边、圆、弧、模糊、字体、阴影、渐变都在一个 shader 里完成。
   **GL 侧只依赖最基础的 VAO / VBO / 纹理 / 混合**，与 MC 的 `RenderType` / `BufferBuilder` / `VertexConsumer` API 解耦，因此 1.17 / 1.20 / 1.21.6 / 26.x 的渲染管线重写基本影响不到它。

### 3.6 旧版路径：立即模式 + 独立 shader

1.8.9 / 1.12.2 等版本：

- `NativeBridge.rs()` 用 `glOrtho` 设置 2D 投影
- `GL11.glBegin/glVertex/glTexCoord/glColor` 立即模式
- 圆角 / 圆 / 阴影等用 `src/main/java/gg/vape/utils/render/shader/*ShaderProgram` 单独编译的 GLSL
  - 如 `LegacyFontShaderProgram`：`#version 430 compatibility` + `gl_ModelViewProjectionMatrix`
- 每个绘制函数显式 `glPushMatrix/glPopMatrix`、`glEnable/glDisable`
- `GlStateManager` 包装层负责不同版本的混合、纹理、alpha test 状态

### 3.7 1.21.6+ / 26.x 的特殊处理

- **1.21.6+**
  - MC 的 `Font.drawInBatch` 改成新签名（`Matrix4f` / `MultiBufferSource` / `DisplayMode`）
  - `MFontRenderer` 针对 `MC_26_1` 与 `MC_26_2` 分别注册不同的 `drawInBatch` / `text` / `drawString` 映射
- **1.21.10+**
  - `Minecraft.getMainRenderTarget()` 可取主 RT
  - `RenderBatchManager.refreshTargetFramebuffer()` 会解析颜色/深度纹理，并包一个自己的 FBO，保证 GUI 画到参与最终合成的目标上
- **26.2**
  - `GuiRenderer.render()` 前/后分别插 HUD / GUI 回调
  - 通知从 HUD pass 移到 GUI pass：

    ```java
    if (!ForgeVersion.MC_26_2.d()) {
        notificationManager.renderNotifications();
    }
    ```

  - 字体桥对空格、纹理格式、`FontSet` 初始化都有专门分支

### 3.8 模糊、物品与实体预览

- **背景模糊**
  - 旧版：`ShaderGroupRenderStateManager` 使用 MC 自己的 post-processing `ShaderGroup`
  - 1.16.5+：`BlurRegionRenderer` + `RenderBatchBuilder.addBlurPass()`，走 universal shader 的 `blur()`
- **物品图标**
  - `ItemIconRenderer` / `ItemStackRenderUtils`
  - 1.21.6+ 用 `RenderItemTextBridge` / `Matrix4fHandle`
- **实体预览**
  - `EntityModelFramebufferRenderer`
  - `Post117EntityModelFramebufferRenderer`
  - 按版本选择“直接画”或“渲染到 FBO 再当纹理贴”
- **纹理图集**
  - `TextureAtlasRegistry` / `TextureAtlas` / `TextureAtlasRegion`
  - `ImageRenderer` 负责 PNG 解码与图集区域查找

---

## 4. 跨版本路径一览

| 版本 | GUI 后端 | 字体渲染 | 备注 |
| --- | --- | --- | --- |
| 1.7.10 | Legacy | `LegacySmoothFontRenderer` + MC 字体回退 | `glOrtho` + 立即模式 |
| 1.8.9（含 Badlion） | Legacy | 同上 | Badlion 有独立按键队列 / 渲染回调 |
| 1.12.2 | Legacy | 同上 | |
| 1.16.5 | **Buffered** | `BufferedSmoothFontRenderer` | 第一批切到自建 VAO/VBO + shader |
| 1.17–1.20.5 | Buffered | Buffered | `drawInBatch(MatrixStack...)` 系 |
| 1.20.6–1.21.5 | Buffered | Buffered / MC 字体桥 | `drawInBatch(Matrix4f, MultiBufferSource, DisplayMode...)` |
| 1.21.6–1.21.11 | Buffered | Buffered / MC 字体桥 | 新 RenderPass / GPU 管线，Vape 仍用自建 shader |
| 26.1 / 26.2 | Buffered | Buffered / MC 字体桥 | `GuiRenderer.render` 前后双回调；自建 FBO 包装主 RT |

---

## 5. 关键结论与坑

1. **字体不是“用 MC 的 FontRenderer 改个 TTF”**
   - 内嵌 stb_truetype 把 TTF 烘成图集
   - 自建 shader 画四边形
   - 中文完整覆盖 GBK 汉字
   - 构建期还有 cmap 覆盖校验

2. **GUI 不是 `GuiScreen`**
   - 每帧在 HUD / PostRender 阶段画自绘 Overlay
   - 输入靠原生回调
   - 天然规避各版本 `Screen` API 差异

3. **跨版本的核心是“两条渲染路径 + 一套映射层”**
   - 1.16.5+：自建批次 + universal shader（GL 基础 API）
   - 旧版：立即模式 + 独立 shader + `glOrtho`
   - 所有 MC 类/字段/方法通过 `MappedClasses` / `MappingMethod` / `Reflections` 按版本解析

4. **缩放策略**
   - 逻辑坐标固定
   - `size * guiScale` 只影响烘焙分辨率
   - 屏幕尺寸由 `2 * guiScale` 统一缩放决定
   - scissor 同步换算

5. **主要代价 / 坑**
   - 每个字号一张 4096² R8 图集（含全 GBK），显存占用高
   - 系统字体依赖 Windows 路径（Arial / Bahnschrift）
   - `StbSmoothFontRenderer` 的 MC 字体桥只有 1.21.10+ 可用，低版本回退 MC 原版字体
   - UTF-16 `char` 遍历不支持代理对（emoji / 扩展汉字）
   - 旧版 shader 依赖 `#version 430 compatibility`，对驱动/上下文有隐含要求
   - 1.21.6+ / 26.x 的 framebuffer 与 GUI pass 需要单独的兼容处理

---

## 6. 建议阅读顺序

如果要继续修改字体或 GUI，建议从这几个入口按顺序看：

1. `src/main/java/gg/vape/ui/font/FontManager.java`
   - 字体选择、字号缓存、GUI Scale 参与方式
2. `src/main/java/gg/vape/ui/font/BufferedSmoothFontRenderer.java`
   - TTF → 字形图集 → 顶点
3. `src/main/java/gg/vape/ui/font/StbSmoothFontRenderer.java`
   - MC 原生 `FontSet` 桥
4. `src/main/java/gg/vape/utils/render/RenderBatchBuilder.java`
   - 图形/字体的顶点语义
5. `src/main/resources/resources/shader/universal_frag.frag`
   - 所有 shaderMode 的实际像素逻辑
6. `src/main/java/gg/vape/utils/render/GuiRenderPrimitives.java`
   - 双后端分界与所有绘制原语
7. `src/main/java/gg/vape/module/none/ClientSettings.java` 的 `renderGui()` / `renderHudFrames()`
   - 每帧顺序、缩放、Frame 调度
8. `src/main/java/gg/vape/ui/click/frame/FrameComponent.java`
   - 组件树渲染 `c()` 与交互 `J()`
