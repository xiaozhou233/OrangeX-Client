# 添加模块（Module）

本文说明如何新增一个功能模块，包括注册、生命周期、事件订阅、设置项和配置持久化。示例直接使用仓库里的 `NoJumpDelay` 模块。

## 1. 模块在架构中的位置

```text
Module（功能逻辑）
 ├── Value              设置项，自动进入 ClickGUI 和配置文件
 ├── EventBus 订阅       模块启用时自动注册，禁用时自动反注册
 └── wrapper/api        读取游戏数据（Player / WorldView / Minecraft）
        └── wrapper/internal/vX_Y_Z   真正的反射实现
```

模块中**不允许**出现 `net.minecraft.*`、MCP 名、SRG 名或混淆名。需要新的游戏数据时，先按 [adding-wrapper-capabilities.md](adding-wrapper-capabilities.md) 增加 wrapper 能力。

## 2. 最小模块

模块统一放在：

```text
modules/impl/<category>/<ModuleName>.java
```

`NoJumpDelay` 的完整实现：

```java
package cn.xiaozhou233.orangex.modules.impl.movement;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventTickPre;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import org.greenrobot.eventbus.Subscribe;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", ModuleCategory.MOVEMENT);
    }

    @Subscribe
    public void onTickPre(EventTickPre event) {
        Player player = OrangeX.getInstance().getPlayer();
        if (player == null) {
            return;
        }
        player.clearJumpDelay();
    }
}
```

要点：

- 构造函数只调用 `super(name, category[, defaultKey])`。此时游戏可能还没初始化，**不要**在构造函数里读世界、玩家或做反射。
- `@Subscribe` 方法必须是 `public`。
- 模块启用时 `Module#setEnabled` 会自动把模块注册到 EventBus，禁用时自动反注册，所以不需要手动注册/注销。

## 3. 注册模块

在 `ModuleManager#registerAll()` 中加一行：

```java
public void registerAll() {
    register(new HUD());
    register(new ClickGUI());
    register(new ESP());
    register(new NoJumpDelay());
}
```

`register` 只负责把模块放进列表。模块默认关闭，除非构造函数里显式 `setEnabled(true)`（例如 `HUD`）。

## 4. 生命周期

| 阶段 | 发生什么 |
| --- | --- |
| 构造 | `ModuleManager` 创建实例，只初始化名称、分类、keybind 和 Value |
| 启用 | 注册到 EventBus → 调用 `onEnable()` |
| 禁用 | 从 EventBus 反注册 → 调用 `onDisable()` |
| 按键切换 | `ModuleManager#onKey` 比较 `EventKey#getKey()` 和模块 keybind，然后 `toggle()` |
| 加载配置 | 先恢复 Value，再按 `enabled` 字段调用 `setEnabled(...)` |
| 关闭客户端 | 关闭钩子里 `saveIfDirty()` |

重写 `onEnable()` / `onDisable()` 时必须调用 `super`：

```java
@Override
protected void onEnable() {
    super.onEnable();
    // 打开界面、发请求、重置状态……
}

@Override
protected void onDisable() {
    // 关闭界面、恢复游戏状态……
    super.onDisable();
}
```

`onEnable()` 里要假设“玩家可能还没进入世界”。例如 `ClickGUI` 会检查 `mc.isInWorld()` 和 `mc.isScreenOpen()`，条件不满足时立即 `setEnabled(false)`。

## 5. 设置项（Value）

在模块字段上声明即可，`Module` 会自动收集：

```java
public class Example extends Module {

    private final BooleanValue showBox = addBoolean("Box", true);
    private final BooleanValue fill = addBoolean("Fill", false);
    private final ModeValue style = addMode("Style", "Box", "Box", "Corners");
    private final ModeValue color = addMode("Color", "Orange", "Orange", "Health", "Rainbow");

    public Example() {
        super("Example", ModuleCategory.RENDER);
    }
}
```

可用类型：

| 类型 | 创建方法 | 常用 API |
| --- | --- | --- |
| `BooleanValue` | `addBoolean(name, default)` | `isEnabled()`、`setEnabled()`、`toggle()` |
| `ModeValue` | `addMode(name, default, modes...)` | `is("Box")`、`getMode()`、`next()`、`previous()`、`getModes()` |
| `KeybindValue` | `addKeybind(name, key)` | `getKey()`、`matches(key)`、`isBound()` |

规则：

- 名称在同一个模块内必须唯一，重复会抛 `IllegalArgumentException`。
- `ModeValue` 的默认值必须出现在模式列表中。
- `KeybindValue` 使用 1.8.9 / LWJGL 2 键码（见 `input/KeyCodes`），26.2 的 GLFW 键码会先转换。
- 模块自身的 keybind 由 `Module` 自动创建，名字固定为 `keybind`。

### 5.1 条件显示

```java
private final ModeValue style = addMode("Style", "Box", "Box", "Corners");
private final BooleanValue corners = addBoolean("Corners", true)
        .visibleWhen(() -> style.is("Corners"));
```

`visibleWhen` 只影响 ClickGUI 是否显示，不影响配置读写。

### 5.2 值变化回调

```java
private final BooleanValue enabled = addBoolean("Enabled", true)
        .onChange(value -> rebuildCache());
```

`onChange` 只在值真正变化时触发。任何 Value 变化都会自动把配置标记为 dirty，不需要手动保存。

### 5.3 自定义 Value 类型

ClickGUI 目前直接识别 `BooleanValue` 和 `ModeValue`：

- `BooleanValue` 显示开关。
- `ModeValue` 显示 `< 当前值 >`，左键下一个、右键上一个。
- 其他类型在设置页里只读显示 `String.valueOf(value)`。

新增自定义 `Value` 子类时，除了实现 `toJson()` / `fromJson(JsonElement)`，还要修改 `ui/clickgui/SettingsSection.java` 才能交互。绝大多数需求用现有三种类型就够了。

## 6. 配置持久化

配置文件位于：

```text
~/.orangex/configs/<name>.json
```

结构大致如下：

```json
{
  "version": 1,
  "modules": {
    "cn.xiaozhou233.orangex.modules.impl.movement.NoJumpDelay": {
      "enabled": true,
      "values": {
        "keybind": 0
      }
    }
  }
}
```

关键规则：

- 模块以 `getConfigKey()` 为键，默认返回**类全名**。修改显示名不会丢配置；读取时还会兼容旧版用显示名作键的配置。
- 保存内容 = `enabled` + 模块全部 Value（包括 keybind）。
- 启用/禁用模块、修改任意 Value 都会 `markDirty()`。
- 真正落盘发生在：关闭 ClickGUI、切换配置、JVM 关闭钩子。也可以手动调用 `ConfigManager#saveIfDirty()`。
- 反序列化时未知字段会被忽略，非法值（例如不存在的 mode）不会写入，避免坏配置让模块不可用。
- 新增模块后不需要改 `ConfigManager` 或序列化器，`ModuleManager` 里的模块会被自动序列化。

## 7. 事件

模块通过 `@Subscribe` 接收事件。项目统一使用一个 EventBus：`OrangeX.getInstance().getEventBus()`。

| 事件 | 触发时机 | 适合做什么 |
| --- | --- | --- |
| `EventTickPre` | 每个游戏 tick，`Minecraft.runTick` 之前 | 移动、战斗、状态修改 |
| `EventTickPost` | 每个 tick，`runTick` 之后 | 需要读取本 tick 结果的逻辑、延迟操作 |
| `EventRender2D` | GUI 绘制帧 | HUD、ESP、ClickGUI |
| `EventKey` | 键盘按下 | 快捷键、模块切换 |

约定：

- 使用默认的 `ThreadMode.POSTING`，不要用 `ASYNC` / `MAIN`。所有回调都在游戏主线程。
- 事件方法里不要做耗时操作；反射 `Field` / `Method` 要缓存。
- 模块被禁用后不会再收到事件；如果逻辑依赖“禁用时恢复”，写在 `onDisable()` 里。
- `EventRender2D#isInWorld()` 表示当前帧是否在世界中；现代版本在标题界面也会触发渲染事件，不能假设有玩家。
- 投影世界坐标必须使用 `EventRender2D#getPartialTicks()`，不要自己读计时器。

## 8. 读取游戏数据

入口都在 `OrangeX`：

```java
OrangeX client = OrangeX.getInstance();

client.getMinecraft();   // 原生 Minecraft 实例（不透明对象）
client.getPlayer();      // 跨版本本地玩家能力
client.getWorldView();   // 世界/相机快照
```

所有 wrapper 都可能在以下情况返回 `null` 或不可用：

- 还在标题界面、没有进入世界；
- 该版本没有实现对应能力；
- 启动时反射失败（日志会打印 `World wrapper unavailable` / `Player wrapper unavailable`）。

因此每次使用前都要判空。ESP 的写法可以作为参考：

```java
WorldView world = OrangeX.getInstance().getWorldView();
if (world == null) {
    return;
}
WorldSnapshot snapshot = world.snapshot(event.getPartialTicks());
```

## 9. NoJumpDelay 的完整调用链

```text
ModuleManager 注册 NoJumpDelay
        │
        ▼
用户开启模块 ── EventBus.register(module)
        │
        ▼
Minecraft.runTick() 前（TickHook 注入）
        │  EventTickPre
        ▼
NoJumpDelay.onTickPre()
        │  OrangeX.getInstance().getPlayer()
        ▼
Player wrapper（V1_8_9Player / V26_2Player）
        │  反射 jumpTicks / noJumpDelay
        ▼
把冷却计时器清零 → 落地即可再次起跳
```

涉及文件：

```text
modules/impl/movement/NoJumpDelay.java
wrapper/api/Player.java
wrapper/internal/v1_8_9/V1_8_9Player.java
wrapper/internal/v26_2/V26_2Player.java
```

## 10. Checklist

- [ ] 模块放在 `modules/impl/<category>/`，构造函数不访问游戏状态。
- [ ] 在 `ModuleManager#registerAll()` 注册。
- [ ] 需要配置的字段使用 `addBoolean` / `addMode` / `addKeybind`，名称唯一。
- [ ] 依赖玩家/世界时先判空。
- [ ] 不使用 Minecraft 类名、字段名或混淆名；需要新数据先加 wrapper。
- [ ] `onEnable` / `onDisable` 重写时调用了 `super`。
- [ ] `./gradlew :OrangeX:shadowJar` 通过，并在游戏里验证开关、配置保存和重连后的行为。

## 11. 常见问题

| 现象 | 原因 |
| --- | --- |
| 模块不显示在 ClickGUI | 没在 `ModuleManager#registerAll()` 注册，或分类写错 |
| 开关无效 / 事件不触发 | `@Subscribe` 方法不是 `public`，或模块没有真正启用 |
| 启动时 `NullPointerException` | 在构造函数或字段初始化时访问了玩家/世界 |
| 配置重启后丢失 | Value 名称重复导致构造失败，或配置从未触发落盘（关闭 ClickGUI / 正常退出） |
| 设置页看不到某个值 | `visibleWhen` 条件为 false，或该类型不被 ClickGUI 识别 |
| 每帧卡顿 | 在 tick/render 回调里重复做反射查找，改为在 wrapper 构造时缓存 |
