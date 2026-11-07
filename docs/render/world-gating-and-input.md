# 世界内渲染与鼠标输入

本文记录两件事：

1. 为什么 1.8.9 的字体 / GUI 只在进入世界后绘制，而 26.2 在主界面也会绘制，以及 OrangeX 现在如何统一。
2. 客户端 GUI 打开时，如何让鼠标在 GUI 上移动，而不是继续控制游戏视角。

> 状态：第 1 部分和第 2 部分的空 GUI / 鼠标输入已落地。M 键打开或关闭输入界面。

---

## 1. 世界内渲染

### 1.1 两个版本的差异

| 版本 | Hook 点 | 调用时机 |
| --- | --- | --- |
| 1.8.9 | `GuiIngame#renderGameOverlay(float)` | `EntityRenderer#updateCameraAndRender` 中，只有 `theWorld != null` 才会调用 |
| 26.2 | `Gui#extractRenderState(DeltaTracker, boolean, boolean)` | 每帧都调用，主界面 / 选世界 / 加载界面同样会执行 |

26.2 的第二个参数就是“当前是否渲染世界”：

```java
// GameRenderer#extract
boolean readyForLevelRendering =
        resourcesLoaded && advanceGameTime && minecraft.level != null;
minecraft.gui.extractRenderState(deltaTracker, readyForLevelRendering, resourcesLoaded);
```

所以 26.2 并不是渲染层出了问题，而是 Hook 拿到的回调本身不区分“菜单”和“世界”。

### 1.2 OrangeX 的处理

26.2 的注入代码把 `shouldRenderLevel` 一起传给门面：

```java
// Render2DHook，26.2 分支
// $0 = this, $1 = DeltaTracker, $2 = shouldRenderLevel
method.insertAfter("Render2DEvents.fire($0, $2);");
```

> 注意：Javassist 的 `$0` 是 `this`，`$1` 才是第一个参数。26.2 的 `shouldRenderLevel` 是第二个参数，因此是 `$2`。

1.8.9 的 HUD 方法本来就只在世界内执行，显式传 `true`：

```java
method.insertAfter("Render2DEvents.fire(null, true);");
```

事件链：

```text
注入代码
  └─ Render2DEvents.fire(context, inWorld)
        └─ Render2DManager.fire(context, inWorld)
              ├─ inWorld == false 且未开启菜单渲染 → 直接 return
              └─ post EventRender2D(context, inWorld)
```

相关文件：

```text
OrangeX/src/main/java/cn/xiaozhou233/orangex/
├── hook/impl/Render2DHook.java
├── event/facade/Render2DEvents.java
├── event/impl/EventRender2D.java
└── render/Render2DManager.java
```

### 1.3 监听器侧

`EventRender2D` 新增了：

```java
public boolean isInWorld();
```

默认情况下主界面不会收到事件；如果以后需要让某个功能在菜单里也绘制，可以临时打开：

```java
OrangeX.getInstance().getRender2DManager().setRenderInMenus(true);
```

或启动参数：

```text
-Dorangex.renderInMenus=true
```

打开后事件仍然会带 `inWorld=false`，监听器可以自行区分。

---

## 2. 鼠标输入

渲染和输入是两套独立机制。字体 / GUI 画出来不代表鼠标就能用，真正决定鼠标行为的是游戏的鼠标捕获状态：

- 1.8.9：`Minecraft#inGameHasFocus` + `MouseHelper`
- 26.2：`MouseHandler#mouseGrabbed`

只要还在捕获状态，鼠标移动就会转视角。

### 2.1 为什么“只放开鼠标”不够

#### 1.8.9

`Minecraft#setIngameNotInFocus()` 会调用 `mouseHelper.ungrabMouseCursor()`，鼠标可以出来。但 `runTick()` 里还有：

```java
if (this.currentScreen == null) {
    if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
        this.setIngameFocus();   // 下一次点击重新捕获
    }
}
```

而且 `currentScreen == null` 时，鼠标事件仍会走 `KeyBinding.setKeyBindState(...)` / `KeyBinding.onTick(...)`，攻击、使用物品等逻辑依然会触发。

#### 26.2

`MouseHandler#releaseMouse()` 只是修改 `mouseGrabbed` 并恢复 GLFW 光标：

```java
public void releaseMouse() {
    if (this.mouseGrabbed) {
        this.mouseGrabbed = false;
        ...
        InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), 212993, this.xpos, this.ypos);
    }
}
```

但 `MouseHandler#onButton()` 中：

```java
if (this.minecraft.gui.screen() == null) {
    if (!this.mouseGrabbed && pressed) {
        this.grabMouse();   // 下一次点击重新捕获
    }
}
```

`grabMouse()` 内部还会调用 `minecraft.gui.setScreen(null)`。

结论：**单纯 ungrab / releaseMouse 不能作为“打开 GUI”的实现**。

### 2.2 推荐方案：用一个只负责输入的 Screen

Screen 会同时完成：

- 释放鼠标捕获
- `KeyMapping.releaseAll()`，WASD / 攻击 / 物品栏不再控制游戏
- 把 `mouseMoved` / `mouseClicked` / `mouseScrolled` / `keyPressed` 路由给 GUI
- 关闭时自动重新捕获鼠标

因此推荐“HUD 继续负责绘制，Screen 只负责输入”的组合，而不是把 HUD 直接改造成 Screen。

#### 1.8.9

`GuiScreen` 是**抽象类**（1.8.8 / 1.8.9 都是），不能直接 `new`。实际实现是运行时用 Javassist 生成一个空的 concret 子类，再通过 `JuiceAgent#defineClass` 定义到游戏 ClassLoader：

```text
GuiScreen（抽象）
  └─ GeneratedInputScreen（运行时生成，public 无参构造 → super()）
```

生成后用 `MappingResolver` 解析出的 `displayGuiScreen` 打开：

```java
Object screen = inputScreenClass.getDeclaredConstructor().newInstance();
mcClass.getMethod(displayGuiScreenName, guiScreenClass).invoke(mc, screen);
```

`displayGuiScreen(screen)` 内部会 `setIngameNotInFocus()`；`displayGuiScreen(null)` 内部会 `setIngameFocus()`。

需要 Hook `GuiScreen` 接收输入，并且只处理自己的空壳实例：

```text
GuiScreen#drawScreen(int mouseX, int mouseY, float partialTicks)   // 每帧鼠标坐标
GuiScreen#mouseClicked(int mouseX, int mouseY, int mouseButton)
GuiScreen#mouseReleased(int mouseX, int mouseY, int state)
GuiScreen#mouseClickMove(int mouseX, int mouseY, int button, long time)
GuiScreen#keyTyped(char typedChar, int keyCode)                    // ESC 默认关闭界面
```

这里拿到的坐标已经是 GUI 缩放后的坐标，可以直接给 `Render2D` 使用。

两点注意：

- 原版 `GuiScreen#drawScreen()` 默认只画 `buttonList` / `labelList`，空壳不会覆盖 HUD。
- `GuiScreen#doesGuiPauseGame()` 默认返回 `true`，单人会暂停；`GuiInputHook` 会只对生成的空壳返回 `false`。

#### 26.2

`Screen` 是抽象类，实际实现是 `V26_3InputScreen`（输入状态记录在 `ClientInput`）：

```java
public final class V26_2InputScreen extends Screen {

    public V26_2InputScreen() {
        super(Component.empty());
    }

    // 不要原版的模糊 / 菜单背景
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
    }

    // 不想暂停世界就返回 false
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // GUI 仍由 HUD Hook 绘制，这里留空
    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
    }

    @Override
    public void mouseMoved(double x, double y) {
        ClientInput.onMouseMove(x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ClientInput.onMouseButton(event.x(), event.y(), event.button(), true);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        ClientInput.onMouseButton(event.x(), event.y(), event.button(), false);
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        ClientInput.onMouseMove(x, y);
        return true;
    }
}
```

ESC 不需要自己处理：`Screen#keyPressed` 默认会调用 `onClose()`，进而 `gui.setScreen(null)`，`ClientInput` 在下一个 tick 检测到界面关闭后自动重置状态。

打开 / 关闭（`V26_3Input` 内部）：

```java
minecraft.gui.setScreen(new V26_2InputScreen()); // releaseMouse + releaseAll + init
minecraft.gui.setScreen(null);                   // grabMouse
```

26.2 的 `mouseMoved(x, y)`、`MouseButtonEvent.x()/y()` 已经是 `MouseHandler#getScaledXPos()` 换算后的 GUI 坐标，不要再乘 `guiScale`。

绘制顺序方面，26.2 的 `Gui#extractRenderState` 会先提取 Screen，再走到 OrangeX 的 Hook 尾部，所以 HUD Hook 里的内容会盖在 Screen 之上；1.8.9 则是 HUD 先画、空壳 Screen 后画，只要空壳不画背景就不会遮挡。

#### 26.2 的编译依赖

`libraries/Minecraft26.2.jar` 是 Java 25 字节码（class version 69），而当前 Gradle 使用 JDK 17，不能直接作为 `compileOnly` 依赖。因此 OrangeX 增加了一个只参与编译的 stub 模块：

```text
Minecraft262Stubs/
└── src/main/java/
    ├── com/mojang/blaze3d/platform/{Window,InputConstants}.java
    └── net/minecraft/
        ├── client/{Minecraft,MouseHandler}.java
        ├── client/gui/{Gui,GuiGraphicsExtractor}.java
        ├── client/gui/screens/Screen.java
        ├── client/input/MouseButtonEvent.java
        ├── client/multiplayer/ClientLevel.java
        └── network/chat/{Component,MutableComponent}.java
```

`OrangeX/build.gradle` 使用 `compileOnly project(':Minecraft262Stubs')`。stub **不会打进 OrangeX.jar**，运行时仍然加载游戏提供的真正 26.2 类。stub 里的字段类型必须和真实类一致，否则会生成错误的字段描述符（例如 `Minecraft#level` 必须是 `ClientLevel` 而不是 `Object`）。

### 2.3 当前实现

```text
OrangeX/src/main/java/cn/xiaozhou233/orangex/
├── input/
│   ├── ClientInput.java                 跨版本输入状态：开/关、鼠标坐标、按键状态
│   ├── InputController.java             在 EventTickPre 中驱动 ClientInput
│   ├── internal/
│   │   ├── VersionInput.java            版本输入接口
│   │   └── v1_8_9/V1_8_9Input.java      1.8.9：运行时生成 GuiScreen 子类 + LWJGL 2
│   └── v26_2/
│       ├── V26_2Input.java              26.2：gui.setScreen + InputConstants
│       └── V26_2InputScreen.java        26.2 空 Screen
├── event/facade/InputEvents.java        1.8.9 注入代码的门面
└── hook/impl/
    ├── GuiInputHook.java                1.8.9 GuiScreen 输入 / 关闭 Hook
    └── KeyPressHook.java                1.8.9 KeyBinding.onTick 打开 Hook
```

行为：

- M 键在**世界内**打开空 GUI；再次按下关闭。
- 1.8.9 的开关走事件而不是 tick 轮询：
  - 打开：`KeyBinding#onTick(int)` → `InputEvents.onKeyPressed` → `ClientInput.requestOpen()`
  - 关闭：`GuiScreen#keyTyped(char, int)` → `InputEvents.onKeyTyped` → `ClientInput.requestClose()`
  - 两者都只在 tick 末尾（`EventTickPost` → `ClientInput.applyPending`）真正执行。
  - 原因：如果在键盘循环里直接打开 Screen，同一个 M 事件会立刻被 `currentScreen.handleKeyboardInput()` 再处理一次，导致刚打开就关闭。
  - 自动重复事件通过 `V1_8_9Input.isRepeatEvent()` 过滤，按住 M 不会反复开关。
- 26.2 仍然每 tick 用 `InputConstants.isKeyDown(window, 77)` 轮询 M。
- 打开时：
  - 1.8.9：运行时生成 `GuiScreen` 具体子类，再 `Minecraft#displayGuiScreen(...)`；`doesGuiPauseGame()` 对自己的空壳返回 `false`。
  - 26.2：`Gui#setScreen(new V26_2InputScreen())`，`isPauseScreen()` 返回 `false`。
- 打开后鼠标由系统光标接管，`GuiScreen#drawScreen` / `Screen#mouseMoved` 持续把 GUI 坐标写入 `ClientInput`。
- `ClientInput` 可供 `EventRender2D` 读取：

```java
if (ClientInput.isOpen()) {
    double x = ClientInput.getMouseX();
    double y = ClientInput.getMouseY();
    boolean left = ClientInput.isLeftDown();
}
```

`FontDebugOverlay` 现在会显示 `Input: OPEN/closed` 和鼠标坐标，并在 GUI 打开时画一个红色十字光标。

### 2.4 备选方案：不经过 Screen 的 Overlay

如果完全不想引入 Screen，也可以参考 Vape 的纯 Overlay 思路，但需要自己补齐所有输入拦截：

26.2：

```text
mouseHandler.releaseMouse()
Hook MouseHandler#grabMouse()            GUI 打开时直接 return
Hook MouseHandler#onButton(...)          GUI 打开时转发点击并 return
Hook MouseHandler#onScroll(...)          GUI 打开时转发滚轮并 return
Hook KeyboardHandler#keyPress(...)       GUI 打开时转发键盘并 return
```

鼠标坐标：

```java
double x = mouseHandler.getScaledXPos(minecraft.getWindow());
double y = mouseHandler.getScaledYPos(minecraft.getWindow());
```

1.8.9：

```text
mc.setIngameNotInFocus()
Hook Minecraft#setIngameFocus()          GUI 打开时直接 return
处理 runTick() 中 currentScreen == null 的鼠标按键 / KeyBinding 路径
```

鼠标坐标：

```java
int x = Mouse.getX() * scaledWidth / mc.displayWidth;
int y = scaledHeight - Mouse.getY() * scaledHeight / mc.displayHeight - 1;
```

这条路的优点是绕开 Screen API；缺点是键盘、点击、滚轮的拦截全部要自己维护，ClickGUI 场景下不如 Screen 方案稳。

---

## 3. 常见坑

| 现象 | 原因 |
| --- | --- |
| 26.2 主界面也绘制 HUD | `Gui#extractRenderState` 每帧执行，需要看 `shouldRenderLevel` |
| 注入后 `shouldRenderLevel` 取错 | Javassist `$0` 是 `this`，第二个参数是 `$2` |
| 放开鼠标后点一下又转视角 | 1.8.9 `runTick` 会 `setIngameFocus`；26.2 `onButton` 会 `grabMouse` |
| 26.2 `grabMouse` 把界面关掉 | `MouseHandler#grabMouse` 内部会 `gui.setScreen(null)` |
| 点击坐标偏移 | Screen 回调坐标已经过 GUI Scale，不要再乘一次 |
| 1.8.9 注入报 `no such class: cn$...` | 注入代码写了全限定 `cn.*`，应使用简单类名 + `getInjectedClassNames()` |
| 空 GUI 打开后玩家还能动 | 1.8.9 / 26.2 都必须真正打开 Screen；只 ungrab 不够 |
| 1.8.9 空 GUI 导致单人暂停 | 已 Hook `GuiScreen#doesGuiPauseGame`，对自己的空壳返回 false |
| 1.8.9 短按 M 没反应 | 已改为 `KeyBinding#onTick` 事件路径；可用 `-Dorangex.debugInput=true` 查看按键日志 |
| 聊天 / 背包界面里按 M 被劫持 | 打开前检查 `isScreenOpen()`，任何原版 Screen 存在时都不响应 |

---

## 4. 相关文件

```text
OrangeX/src/main/java/cn/xiaozhou233/orangex/
├── hook/
│   ├── HookManager.java
│   └── impl/
│       ├── TickHook.java
│       ├── Render2DHook.java
│       ├── GuiInputHook.java
│       └── KeyPressHook.java
├── input/
│   ├── ClientInput.java
│   ├── InputController.java
│   ├── internal/
│   │   ├── VersionInput.java
│   │   └── v1_8_9/V1_8_9Input.java
│   └── v26_2/
│       ├── V26_2Input.java
│       └── V26_2InputScreen.java
├── event/
│   ├── facade/
│   │   ├── TickEvents.java
│   │   ├── Render2DEvents.java
│   │   └── InputEvents.java
│   └── impl/
│       ├── EventTickPre.java
│       ├── EventTickPost.java
│       └── EventRender2D.java
├── render/
│   ├── Render2DManager.java
│   └── FontDebugOverlay.java
└── wrapper/
    ├── api/Minecraft.java
    └── internal/
        ├── v1_8_9/V1_8_9Minecraft.java
        └── v26_2/V26_2Minecraft.java

Minecraft262Stubs/src/main/java/      26.2 compileOnly stubs（不打包）
```
