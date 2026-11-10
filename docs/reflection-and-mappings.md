# 反射与混淆映射

本文说明 OrangeX 如何安全地访问 Minecraft 的类、字段和方法，以及多版本/多加载器下必须遵守的规则。

## 1. 为什么不能直接写死

同一个字段在不同运行环境里的名字完全不同：

| 环境 | 本地玩家字段 | 跳跃冷却字段 |
| --- | --- | --- |
| 1.8.9 MCP（开发环境） | `thePlayer` | `jumpTicks` |
| 1.8.9 Vanilla / Forge（混淆） | `h` | `bn` |
| 26.2（Mojang 名） | `player` | `noJumpDelay` |

而且混淆名会随客户端版本变化。直接写 `getDeclaredField("jumpTicks")` 在 MCP 开发环境能跑，到了线上混淆客户端会立刻 `NoSuchFieldException`。

因此项目统一遵守：

```text
业务代码写“命名名”（MCP / Mojang）
        │
        ▼
MappingResolver 翻译成运行时名（混淆名或原样）
        │
        ▼
ReflectionSupport 按运行时名查找并缓存 Field / Method
```

**只有 `wrapper/internal/vX_Y_Z` 允许出现 Minecraft 类名、字段名和方法名。**
模块、`wrapper/api`、`wrapper/common` 中都不要出现 `net.minecraft.*`。

## 2. 命名来源

项目里会出现三种名字，注意区分：

| 名字 | 示例 | 用在哪里 |
| --- | --- | --- |
| 命名名（MCP / Mojang） | `net/minecraft/entity/EntityLivingBase#jumpTicks` | 源码常量、`MappingResolver` 的输入 |
| 运行时名（混淆 / 原样） | `pr#bn` / `noJumpDelay` | `Class#getDeclaredField`、`Class#getDeclaredMethod` |
| 内部名（internal） | `pr/bn` | 映射文件、`Class.forName` 前需要 `.replace('/', '.')` |

写映射时统一使用**内部名 + 命名成员名**，例如：

```java
resolver.mapClass("net/minecraft/entity/EntityLivingBase");
resolver.mapField("net/minecraft/entity/EntityLivingBase", "jumpTicks");
resolver.mapMethod("net/minecraft/client/Minecraft", "runTick", "()V");
```

## 3. MappingResolver

入口：

```java
MappingResolver resolver = MappingManager.getResolver();
```

`MappingManager.init(runtimeInfo)` 按版本和加载器选择实现：

| 版本 | 加载器 | Resolver | 行为 |
| --- | --- | --- | --- |
| 1.8.9 | MCP | `MCPMappingResolver` | 全部按原名返回 |
| 1.8.9 | Vanilla | `SrgMappingResolver("mappings/vanilla189/joined.srg")` | 命名名 → 混淆名 |
| 1.8.9 | Forge | `SrgMappingResolver("mappings/forge189/joined.srg")` | 命名名 → 混淆名 |
| 26.2 | 任意（Mojang 名） | `IdentityFallbackMappingResolver` | 全部按原名返回 |

映射文件位于：

```text
OrangeX/src/main/resources/mappings/vanilla189/joined.srg
OrangeX/src/main/resources/mappings/forge189/joined.srg
```

SRG 行格式（节选）：

```text
CL: pr net/minecraft/entity/EntityLivingBase
FD: pr/bn net/minecraft/entity/EntityLivingBase/jumpTicks
```

`SrgMappingResolver` 会在内部把 `FD` 的右侧（命名）作为 key，左侧（混淆）作为目标名，所以：

```java
resolver.mapField("net/minecraft/entity/EntityLivingBase", "jumpTicks")
        .getTargetName();   // "bn"
```

### 3.1 API

```java
MappingClass mapClass(String className);

MappingField mapField(String owner, String fieldName);

MappingMethod mapMethod(String owner, String methodName, String descriptor);
```

`MappingClass` 还提供：

```java
mappingClass.getTargetName();                        // 运行时类名（内部名）
mappingClass.getField("jumpTicks");                  // 可能为 null
mappingClass.getMethod("runTick", "()V");            // 可能为 null
```

两种取值方式要注意：

- `resolver.mapField(...)` / `resolver.mapMethod(...)`：找不到映射时返回**同名映射**，不会为 null，适合“名字一定存在，只是可能被混淆”的场景。
- `MappingClass#getField` / `#getMethod`：非 identity 映射找不到时返回 `null`，适合需要显式判断“这个成员在当前版本不存在”的场景。

当前 `MappingClass` 有 identity 模式（MCP / 26.2），该模式下未登记的成员也返回同名映射。

### 3.2 类名与 `Class.forName`

映射出来的类名是内部名（`pr`、`net/minecraft/client/Minecraft`），加载时要转成二进制名，并始终使用游戏的 ClassLoader：

```java
String binaryName = resolver.mapClass("net/minecraft/client/Minecraft")
        .getTargetName()
        .replace('/', '.');

Class<?> minecraftClass = Class.forName(
        binaryName, false, runtimeInfo.getMinecraftClassLoader());
```

不要用 `Class.forName(name)` 的单参版本：Fabric / Forge / 启动器的类可能只在游戏 ClassLoader 中可见。

## 4. ReflectionSupport

`wrapper/internal/ReflectionSupport` 是版本实现共用的反射工具，所有查找都会沿父类链向上找：

| 方法 | 说明 |
| --- | --- |
| `findField(Class, name)` | 查找实例字段（跳过 static），找不到返回 `null`，并已 `setAccessible(true)` |
| `requireField(Class, name, ownerName)` | 同上，找不到抛 `IllegalStateException` |
| `findMethod(Class, name, returnType, params...)` | 按名称、参数类型（可选返回类型）查找实例方法 |
| `requireMethod(...)` | 同上，找不到抛异常 |
| `get` / `getInt` / `getFloat` / `getDouble` / `getBoolean` | 读取字段 |
| `setInt(Field, target, value)` | 写入 int 字段 |
| `invoke(Method, target, args...)` | 调用方法，包装反射异常 |

为什么必须沿父类链查找：字段经常声明在基类，而实际对象是子类。例如 `jumpTicks` 声明在 `EntityLivingBase`，拿到的对象却是 `EntityPlayerSP`；玩家字段声明在 `Minecraft`，实现类可能被启动器替换。

## 5. 编写一个版本实现

以 `V1_8_9Player` 为例：

```java
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

规律：

1. **在构造器里解析 `Minecraft` 上的字段**。`Minecraft` 实例在整个进程里是稳定的。
2. **玩家实例不要缓存**。换世界、重连、切服务器后对象会变，每次使用时重新读字段。
3. **字段本身可以缓存**。玩家类在进程内稳定，第一次拿到玩家后再解析 `jumpTicks` / `noJumpDelay` 并缓存。
4. **玩家为 null 时直接返回**。标题界面、加载界面没有本地玩家。
5. **所有名字通过 `MappingResolver` / `MappingClass` 获取**，不要出现 `"bn"`、`"h"` 这类硬编码。

### 5.1 1.8.9 与 26.2 的对照

| 含义 | 1.8.9 | 26.2 |
| --- | --- | --- |
| 本地玩家字段 | `Minecraft#thePlayer` | `Minecraft#player` |
| 玩家基类 | `net/minecraft/entity/EntityLivingBase` | `net/minecraft/world/entity/LivingEntity` |
| 跳跃冷却字段 | `jumpTicks`（int） | `noJumpDelay`（int） |
| 冷却语义 | 跳跃后设为 10，减到 0 才允许再跳 | 每 tick 递减，大于 0 时禁止起跳 |

注意 1.8.9 的 `jumpTicks` 是从基类继承的，因此 `ReflectionSupport.requireField(localPlayer.getClass(), ...)` 会向上找到 `EntityLivingBase`。

## 6. 常见坑

| 坑 | 说明 / 解决 |
| --- | --- |
| 用 `getField` 读私有字段 | `getField` 只能找到 public 字段，改用 `getDeclaredField` + `setAccessible(true)`（`ReflectionSupport` 已封装） |
| 把内部名直接传给 `Class.forName` | 先 `.replace('/', '.')` |
| 用默认 ClassLoader | 使用 `runtimeInfo.getMinecraftClassLoader()` |
| 缓存玩家对象 | 玩家对象会换；缓存字段或方法，不缓存实例 |
| 字段在父类上 | `ReflectionSupport` 会沿父类链查找，不要只查 `getClass().getDeclaredField(...)` |
| 基本类型用 `Field#get` | int 用 `getInt` / `setInt`，float 用 `getFloat`，否则拿到的是包装类型或类型不匹配 |
| 静态字段 | `findField` 会跳过 static；单例字段需要单独按静态字段处理（参考 `V1_8_9Minecraft#findStaticField`） |
| 方法重载 | 提供参数类型；如果方法名相同但签名不同，`MappingResolver` 需要同时给出 descriptor |
| 映射文件过期 | 客户端小版本变化后混淆名可能变，更新对应的 `joined.srg` |
| 每个 tick 重复查找 | 构造器或首次使用时解析并缓存 `Field` / `Method` |
| 反射异常吞掉不报 | wrapper 构造失败会打印日志；模块里也要避免静默失败导致“功能看起来没生效” |

## 7. 排查顺序

1. 打开日志，确认版本和加载器检测正确：

   ```text
   Loader Type: VANILLA
   Minecraft Version: V1_8_9 (version id 15)
   ```

2. 打印解析结果，确认命名名映射到了预期的运行时名：

   ```java
   System.out.println(resolver.mapField(OWNER, "jumpTicks").getTargetName());
   ```

3. 确认反射查找的类来自正确的 ClassLoader。
4. 确认对象非 null：标题界面没有玩家，属于正常情况。
5. 确认字段类型：int 字段不能用 `get()` 后强转 `Integer` 以外的处理方式。
6. 如果只有某个加载器失败，检查 `MappingManager` 是否选中了对应版本的 SRG 文件。

## 8. Checklist

- [ ] 模块 / `wrapper/api` / `wrapper/common` 没有 Minecraft 类名或混淆名。
- [ ] 所有类、字段、方法名都通过 `MappingResolver`。
- [ ] 私有成员使用 `getDeclaredField` + `setAccessible(true)`。
- [ ] `Class.forName` 使用游戏 ClassLoader，并转换内部名。
- [ ] 玩家/世界对象不缓存，字段/方法缓存。
- [ ] 对象可能为 null 的路径已处理。
- [ ] 首次解析失败会打日志，不会每 tick 重试刷屏。
- [ ] 在 Vanilla、Forge、MCP / 26.2 至少各验证一次。
