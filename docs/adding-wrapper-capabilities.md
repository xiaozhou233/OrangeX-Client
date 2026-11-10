# 添加跨版本 Wrapper 能力

本文说明当模块需要一份新的游戏数据时，如何把它做成跨版本能力。完整示例直接使用本次新增的 `Player` + `NoJumpDelay`。

## 1. 什么时候需要新能力

出现以下情况时，应该新增 wrapper 能力，而不是在模块里直接反射：

- 模块需要读/写一个 Minecraft 字段或方法；
- 这个字段在不同版本/加载器下名字不同；
- 多个模块可能复用同一份数据（玩家、世界、背包、聊天……）。

判断标准：**只要代码里出现 `net.minecraft.*` 或混淆名，就说明位置不对。**
模块只能依赖 `wrapper/api`，版本差异收敛到 `wrapper/internal/vX_Y_Z`。

## 2. 分层与文件位置

以 `Player` 为例：

```text
wrapper/api/Player.java                             跨版本契约（只依赖 JDK）
wrapper/internal/WrapperProvider.java               每个版本的能力工厂
wrapper/internal/WrapperRegistry.java               版本 -> Provider 分发
wrapper/MinecraftFactory.java                       对外创建入口
wrapper/internal/v1_8_9/V1_8_9Player.java           1.8.9 实现
wrapper/internal/v26_2/V26_2Player.java             26.2 实现
OrangeX.java                                        启动时创建并持有 wrapper
```

规则：

- `api` / `common` 不允许 import `net.minecraft.*`。
- Provider 必须无状态，运行时数据全部来自 `WrapperContext`。
- 实现类不要调用 `OrangeX.getInstance()` 或 `MappingManager.getResolver()`，依赖由构造器注入。
- 新的能力接口按“能力”拆分，不要把 Player、Chat、Inventory 全塞进 `Minecraft` 接口。

## 3. 第一步：定义 API

`wrapper/api/Player.java`：

```java
package cn.xiaozhou233.orangex.wrapper.api;

/**
 * Cross-version view of the local player.
 */
public interface Player {

    /**
     * Clears the client-side jump cooldown.
     * Does nothing while no world/player is loaded.
     */
    void clearJumpDelay();
}
```

接口只描述**能力**，不暴露 Minecraft 类型。宁可多一个 `Object getHandle()` 之类的逃生口，也不要把 `net.minecraft.entity.Entity` 写进 API。

## 4. 第二步：实现各版本

### 4.1 1.8.9

```java
package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

public final class V1_8_9Player implements Player {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String ENTITY_LIVING = "net/minecraft/entity/EntityLivingBase";

    private static final String PLAYER_FIELD = "thePlayer";
    private static final String JUMP_DELAY_FIELD = "jumpTicks";

    private final Object minecraft;
    private final Field playerField;
    private final String jumpDelayFieldName;

    private Field jumpDelayField;

    public V1_8_9Player(MappingResolver resolver, Minecraft minecraft) {
        this.minecraft = minecraft.getMinecraft();

        MappingClass minecraftMapping = resolver.mapClass(MINECRAFT);
        this.playerField = resolveField(
                minecraftMapping, PLAYER_FIELD, this.minecraft.getClass());
        this.jumpDelayFieldName = fieldName(
                resolver.mapClass(ENTITY_LIVING), JUMP_DELAY_FIELD);
    }

    @Override
    public void clearJumpDelay() {
        Object localPlayer = ReflectionSupport.get(playerField, minecraft);
        if (localPlayer == null) {
            return;
        }

        if (jumpDelayField == null) {
            jumpDelayField = ReflectionSupport.requireField(
                    localPlayer.getClass(), jumpDelayFieldName, ENTITY_LIVING);
        }

        ReflectionSupport.setInt(jumpDelayField, localPlayer, 0);
    }
}
```

### 4.2 26.2

```java
package cn.xiaozhou233.orangex.wrapper.internal.v26_2;

public final class V26_2Player implements Player {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String ENTITY_LIVING = "net/minecraft/world/entity/LivingEntity";

    private static final String PLAYER_FIELD = "player";
    private static final String JUMP_DELAY_FIELD = "noJumpDelay";

    // 构造、解析和 clearJumpDelay 与 1.8.9 版本相同
}
```

差异只体现在常量上：

| 含义 | 1.8.9 | 26.2 |
| --- | --- | --- |
| Minecraft 里的玩家字段 | `thePlayer` | `player` |
| 玩家基类 | `net/minecraft/entity/EntityLivingBase` | `net/minecraft/world/entity/LivingEntity` |
| 跳跃冷却字段 | `jumpTicks` | `noJumpDelay` |

名称解析的细节见 [reflection-and-mappings.md](reflection-and-mappings.md)。

## 5. 第三步：扩展 Provider

`wrapper/internal/WrapperProvider.java` 增加一个工厂方法：

```java
public interface WrapperProvider {

    Minecraft createMinecraft(WrapperContext context);

    WorldView createWorld(WrapperContext context, Minecraft minecraft);

    Player createPlayer(WrapperContext context, Minecraft minecraft);
}
```

两个版本的 Provider 分别实现：

```java
// v1_8_9/V1_8_9WrapperProvider
@Override
public Player createPlayer(WrapperContext context, Minecraft minecraft) {
    return new V1_8_9Player(context.getMappingResolver(), minecraft);
}

// v26_2/V26_2WrapperProvider
@Override
public Player createPlayer(WrapperContext context, Minecraft minecraft) {
    return new V26_2Player(context.getMappingResolver(), minecraft);
}
```

为什么参数里要传 `Minecraft`：实现需要从 `Minecraft` 实例上读 `thePlayer` / `player` 字段；`WrapperContext` 只包含运行时信息和映射解析器。

## 6. 第四步：Registry 分发

`wrapper/internal/WrapperRegistry.java`：

```java
public static Player createPlayer(WrapperContext context, Minecraft minecraft) {
    return provider(context).createPlayer(context, minecraft);
}
```

`provider(context)` 按 `RuntimeInfo#getMinecraftVersion()` 找到对应 Provider，未注册的版本会抛出 `Unsupported Minecraft version`。

## 7. 第五步：Factory 暴露

`wrapper/MinecraftFactory.java`：

```java
public static Player createPlayer(RuntimeInfo runtimeInfo,
                                  MappingResolver mappingResolver,
                                  Minecraft minecraft) {
    return WrapperRegistry.createPlayer(
            new WrapperContext(runtimeInfo, mappingResolver), minecraft);
}
```

业务代码只应依赖 `MinecraftFactory` 和 `api`，不要直接 import `WrapperRegistry` 或版本实现。

## 8. 第六步：启动时创建

`OrangeX.start()` 中，在 `MinecraftFactory.create(...)` 之后创建：

```java
try {
    this.player = MinecraftFactory.createPlayer(
            runtimeInfo, MappingManager.getResolver(), minecraft);
} catch (Throwable throwable) {
    // 能力不可用时客户端仍要能启动，依赖它的模块自然失效即可。
    System.err.println("[OrangeX] Player wrapper unavailable: "
            + throwable.getMessage());
    throwable.printStackTrace();
}
```

同时加 getter：

```java
@Getter
private Player player;
```

注意：

- 创建顺序必须是 `Minecraft` → `WorldView` / `Player`，因为后两者构造时需要读取 `Minecraft` 实例上的字段。
- 能力构造失败不能中断启动；模块里对 `null` 做处理。
- 模块开启时才订阅事件，所以 wrapper 为 null 时模块只是“没有效果”，不会崩。

## 9. 第七步：模块使用

```java
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

模块不需要知道版本，也不需要知道字段名。完整调用链：

```text
TickHook 注入 Minecraft.runTick
        │
        ▼
EventBus.post(EventTickPre)
        │
        ▼
NoJumpDelay.onTickPre()
        │
        ▼
OrangeX.getPlayer()                 // api.Player
        │
        ▼
V1_8_9Player / V26_2Player          // 版本实现
        │
        ▼
ReflectionSupport.setInt(jumpTicks / noJumpDelay, 0)
```

## 10. 设计约定

### 10.1 对象会变，字段不会

- `Minecraft` 单例：整个进程稳定。
- 本地玩家：换世界、重连、切服务器都会变。
- 玩家类：进程内稳定，可以缓存 `Field` / `Method`。

因此典型模式是：

```text
构造器：解析 Minecraft 字段
首次调用：解析玩家类字段并缓存
每次调用：重新读取玩家对象，判空，再读写字段
```

### 10.2 能力要能安全地“不可用”

- 无世界/无玩家时返回 null 或空操作，不要抛异常。
- 构造失败时只打日志，让依赖模块自然失效。
- 读操作失败不要让整个渲染帧挂掉（参考 ESP 对 `WorldView` 的 try/catch）。

### 10.3 保持 API 纯净

- 不把 `Object` 强转逻辑泄漏到模块。
- 不把版本枚举带进模块（不要写 `if (version == V1_8_9)`）。
- 同一个能力如果有多个数据，放在同一个接口里，例如 `Player#getHealth()`、`Player#clearJumpDelay()`。

### 10.4 命名

- `api` 接口用能力名：`Player`、`WorldView`、`Minecraft`。
- 版本实现用 `V<主>_<次>_<修订><能力>`：`V1_8_9Player`、`V26_2Player`。
- Provider 用 `V<版本>WrapperProvider`。
- 包名 `v1_8_9` / `v26_2`，与现有结构保持一致。

## 11. 支持新版本时要做什么

当 `WrapperRegistry` 新增一个版本时，该版本的 Provider 必须实现**所有**能力方法：

```java
@Override public Minecraft createMinecraft(WrapperContext context) { ... }

@Override public WorldView createWorld(WrapperContext context, Minecraft minecraft) { ... }

@Override public Player createPlayer(WrapperContext context, Minecraft minecraft) { ... }
```

某个版本暂时不支持的能力可以抛 `UnsupportedOperationException`，`OrangeX.start()` 会捕获并记录，其他能力不受影响。

## 12. Checklist

- [ ] `wrapper/api` 新接口只依赖 JDK，没有 Minecraft 类型。
- [ ] 每个已支持版本都实现了新的 Provider 方法。
- [ ] `WrapperRegistry` 和 `MinecraftFactory` 已增加分发方法。
- [ ] `OrangeX` 启动时创建 wrapper，并捕获异常。
- [ ] 模块只依赖 `api`，使用前判空。
- [ ] 没有缓存玩家/世界实例，只缓存 `Field` / `Method`。
- [ ] 所有名字通过 `MappingResolver`。
- [ ] `./gradlew :OrangeX:shadowJar` 通过。
- [ ] 在 1.8.9（Vanilla / Forge / MCP）和 26.2 至少各验证一次。

## 13. 相关文档

- [adding-modules.md](adding-modules.md)：模块注册、Value、配置持久化。
- [reflection-and-mappings.md](reflection-and-mappings.md)：反射工具和映射细节。
- [adding-wrapper-versions.md](adding-wrapper-versions.md)：新增 Minecraft 版本。
