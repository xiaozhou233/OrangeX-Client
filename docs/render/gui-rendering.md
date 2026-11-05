# GUI 2D 渲染

本文说明 OrangeX 的基础 2D GUI 绘制层。它建立在字体移植的 `Render2D` 上下文之上，供后续 ClickGUI / HUD / 面板组件直接使用。

## 1. API

`cn.xiaozhou233.orangex.render.api.Render2D`：

| 方法 | 说明 |
| --- | --- |
| `fillRect(x, y, w, h, argb)` | 实心矩形 |
| `fillGradient(x, y, w, h, top, bottom)` | 垂直渐变矩形 |
| `drawRect(x, y, w, h, thickness, argb)` | 矩形描边 |
| `fillRoundedRect(x, y, w, h, radius, argb)` | 实心圆角矩形 |
| `fillRoundedRect(x, y, w, h, radius, borderWidth, fill, border)` | 带边框圆角矩形 |
| `drawLine(x1, y1, x2, y2, thickness, argb)` | 直线 |
| `enableScissor(x, y, w, h)` | 开启裁剪 |
| `disableScissor()` | 关闭当前裁剪 |
| `getFont(size[, bold])` | 获取字体 |
| `getWidth()` / `getHeight()` / `getGuiScale()` | 当前 GUI 尺寸 |

颜色统一使用 **ARGB int**，例如：

```java
0xFFFFFFFF  // 不透明白
0xCC101010  // 80% 黑
0xFF55FFFF  // 青色
```

坐标是 Minecraft GUI 像素（`ScaledResolution` / `getGuiScaledWidth` 空间）。

## 2. 示例

```java
@Subscribe(threadMode = ThreadMode.POSTING)
public void onRender2D(EventRender2D event) {
    Render2D g = event.getRender2D();

    g.fillRoundedRect(10, 10, 220, 120, 8,
            1.0, 0xCC151515, 0xFF353535);

    g.fillGradient(18, 100, 204, 6,
            0xFF55FFFF, 0xFF55FF55);

    g.drawLine(18, 112, 222, 112, 1.0, 0x80FFFFFF);

    FontRenderer font = g.getFont(10.0f);
    font.drawString("OrangeX", 18, 18, 0xFFFFFFFF);

    g.enableScissor(18, 40, 120, 18);
    font.drawString("只在裁剪区域内显示的一段文字", 18, 40, 0xFFAAAAAA);
    g.disableScissor();
}
```

带边框圆角面板的常见写法：

```java
g.fillRoundedRect(x, y, width, height, 6.0,
        1.0, 0xCC181818, 0xFF3A3A3A);
```

## 3. 版本实现

### 3.1 1.8.9

`V1_8_9Render2D` 使用固定管线立即模式：

- 矩形 / 描边 / 渐变：`GL_QUADS`，渐变使用 `GL_SMOOTH`
- 圆角矩形：按圆角半径生成外轮廓采样点，`GL_TRIANGLE_FAN` 填充
- 边框：先画外层圆角矩形（边框色），再画内层圆角矩形（填充色）
- 裁剪：`GL_SCISSOR_TEST` + `glScissor`，坐标按 GUI Scale 转成 framebuffer 像素
- 直线：水平/垂直用矩形，斜线用 `GL_LINES`

所有 GL 状态由 `V1_8_9RenderState` 统一保存/恢复：

- blend / texture / alpha test / depth / cull / lighting / fog / scissor
- texture binding / active texture / texture env
- shade model / line width / current color

`Render2D.flush()` 在一帧结束时统一恢复，形状和字体可以任意交错绘制。

### 3.2 26.2

`V26_2Render2D` 不直接调用 OpenGL，而是把绘制提交给 `GuiRenderState`：

- 矩形：`GuiGraphicsExtractor#fill`
- 渐变：`GuiGraphicsExtractor#fillGradient`
- 裁剪：`GuiGraphicsExtractor#enableScissor/disableScissor`
- 圆角矩形：
  - 直边用 `fill` 拼出 5 个矩形区域
  - 4 个圆角使用缓存的白色四分之一圆纹理，通过 `innerBlit` 翻转 UV 绘制
  - 纹理按 `radius * guiScale` 生成，保证一个纹素对应一个物理像素
- 边框：外层圆角矩形 + 内层圆角矩形

圆角纹理由 `V26_2ShapeCache` 缓存，按半径像素复用，不会每帧创建。

## 4. 裁剪

裁剪支持嵌套：

```java
g.enableScissor(0, 0, 200, 100);
g.enableScissor(20, 20, 100, 50);
// ...
g.disableScissor(); // 回到第一层
g.disableScissor(); // 关闭
```

- 1.8.9 在 `V1_8_9Render2D` 内维护 scissor 栈
- 26.2 交给 `GuiGraphicsExtractor` 的 scissor 栈

## 5. 当前限制

- 没有旋转、缩放矩阵 API（坐标就是 GUI 像素）
- 26.2 的斜线用 1px 方块近似，复杂斜线建议用贴图
- 圆角矩形在 26.2 依赖生成的角纹理，半径种类很多时会增加纹理数量
- 还没有图片/纹理绘制 API，下一步可以补 `drawTexture` / `drawImage`
- 还没有组件树、布局和输入系统

## 6. 下一步

建议顺序：

1. `Texture` / `drawTexture` 图片绘制
2. `Component` / `Frame` 组件树与布局
3. 鼠标/键盘输入与 ClickGUI 打开逻辑
4. 主题、动画和滚动容器
