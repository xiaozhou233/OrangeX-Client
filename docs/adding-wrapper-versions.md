# 添加 Minecraft 版本的 Wrapper

本文说明在版本**已经被检测出来**之后，如何为它增加跨版本 Wrapper。

版本检测本身（`LoaderType`、`MinecraftVersion`、结构签名）见
[adding-minecraft-versions.md](adding-minecraft-versions.md)。本文只处理：

```text
RuntimeInfo (V1_X_Y)
        │
        ▼
WrapperRegistry ──► WrapperProvider ──► V1_X_YMinecraft implements Minecraft
```

## 1. 目录结构

```text
wrapper/
├── MinecraftFactory.java              # 唯一公开入口
├── package-info.java
├── api/
│   ├── Minecraft.java                 # 跨版本契约，只依赖 JDK
│   ├── Player.java
│   └── WorldView.java
├── common/
│   └── Vec3.java                      # 与版本无关的值对象
└── internal/
    ├── WrapperContext.java            # RuntimeInfo + MappingResolver
    ├── WrapperProvider.java           # 单版本 Wrapper 工厂
    ├── WrapperRegistry.java           # 版本 -> Provider 注册表
    └── v1_8_9/
        ├── V1_8_9WrapperProvider.java
        ├── V1_8_9Minecraft.java
        ├── V1_8_9WorldView.java
        └── V1_8_9Player.java
```

各层职责：

| 目录 | 职责 | 允许依赖 |
|---|---|---|
| `api` | 业务代码使用的稳定接口 | 仅 JDK |
| `common` | 跨版本共享的值对象 | 仅 JDK |
| `internal` | 版本选择、Provider、版本实现 | `api`、`common`、`mapping`、`runtime` |
| `internal.vX_Y_Z` | 某一个版本的反射实现 | 可以出现 Minecraft 类名和 MCP 名称 |

规则：

- `api` / `common` 中**不要**出现 `net.minecraft.*`、SRG 名、混淆名或加载器类型。
- 只有 `internal.vX_Y_Z` 可以看到 Minecraft 类名、字段名和方法名。
- Provider 必须是无状态的；所有运行时数据从 `WrapperContext` 获取。
- 实现类**不要**直接调用 `OrangeX.getInstance()` 或 `MappingManager.getResolver()`，依赖由构造器注入。

## 2. 调用链

```java
// OrangeX.start()
runtimeInfo = RuntimeDetector.detect();
MappingManager.init(runtimeInfo);

Minecraft minecraft = MinecraftFactory.create(
        runtimeInfo, MappingManager.getResolver());
```

`MinecraftFactory` 只是组装上下文：

```java
public static Minecraft create(RuntimeInfo runtimeInfo,
                               MappingResolver mappingResolver) {
    return WrapperRegistry.createMinecraft(
            new WrapperContext(runtimeInfo, mappingResolver));
}
```

`WrapperRegistry` 按 `MinecraftVersion` 查表，查不到就抛出
`Unsupported Minecraft version`。

## 3. 添加一个新版本

以 1.12.2 为例。

### 3.1 前置条件

确认版本检测已经能输出正确的 `MinecraftVersion`：

```text
Minecraft Version: V1_12_2 (version id 23)
```

如果没有，先按 [adding-minecraft-versions.md](adding-minecraft-versions.md)
添加内部版本 ID 和结构签名。

### 3.2 新建版本包

```text
wrapper/internal/v1_12_2/
├── V1_12_2WrapperProvider.java
└── V1_12_2Minecraft.java
```

包名使用 `v主版本_次版本_修订号`，类名使用 `V1_12_2Xxx`，与现有
`v1_8_9` 保持一致。

### 3.3 实现 Minecraft Wrapper

```java
package cn.xiaozhou233.orangex.wrapper.internal.v1_12_2;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingField;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

public final class V1_12_2Minecraft implements Minecraft {

    // 这里写 MCP（named）名称，混淆名由 MappingResolver 负责转换
    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String INSTANCE_FIELD = "theMinecraft";

    private final RuntimeInfo runtimeInfo;
    private final MappingClass mappingMinecraft;

    private Object instance;

    public V1_12_2Minecraft(RuntimeInfo runtimeInfo,
                            MappingResolver mappingResolver) {
        this.runtimeInfo = runtimeInfo;
        this.mappingMinecraft = mappingResolver.mapClass(MINECRAFT_CLASS);
    }

    @Override
    public synchronized Object getMinecraft() {
        if (instance == null) {
            instance = resolveInstance();
        }
        return instance;
    }

    private Object resolveInstance() {
        MappingField mappingField = mappingMinecraft.getField(INSTANCE_FIELD);
        if (mappingField == null) {
            throw new IllegalStateException(
                    "No mapping for field '" + INSTANCE_FIELD + "' on "
                            + MINECRAFT_CLASS);
        }

        Class<?> minecraftClass;
        try {
            minecraftClass = Class.forName(
                    mappingMinecraft.getTargetName().replace('/', '.'),
                    false,
                    runtimeInfo.getMinecraftClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot load Minecraft class "
                            + mappingMinecraft.getTargetName(), e);
        }

        // 具体反射细节可参考 V1_8_9Minecraft#resolveInstance
        // 重点：优先 getDeclaredField + setAccessible，字段通常不是 public
        // 静态字段时传 null：field.get(null)
        throw new UnsupportedOperationException("TODO");
    }
}
```

实现约定：

- 字段名、方法名、参数描述符统一使用 MCP 名称。
- 通过 `MappingClass` / `MappingResolver` 转换成当前运行时名称。
- 映射文件里的类名是 internal name（`a/b/C`），传给 `Class.forName`
  前必须转成 binary name（`a.b.C`）；`getTargetName().replace('/', '.')`。
- 私有字段用 `getDeclaredField` + `setAccessible(true)`，不要用
  `getField`（它只能找到 public 字段）。
- 需要跨线程访问的单例缓存加 `synchronized` 或 `volatile`。

### 3.4 实现 Provider

```java
package cn.xiaozhou233.orangex.wrapper.internal.v1_12_2;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperContext;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperProvider;

public final class V1_12_2WrapperProvider implements WrapperProvider {

    @Override
    public Minecraft createMinecraft(WrapperContext context) {
        return new V1_12_2Minecraft(
                context.getRuntimeInfo(),
                context.getMappingResolver());
    }
}
```

### 3.5 注册 Provider

在 `wrapper/internal/WrapperRegistry.java` 的静态块中加一行：

```java
static {
    register(MinecraftVersion.V1_8_9, new V1_8_9WrapperProvider());
    register(MinecraftVersion.V1_12_2, new V1_12_2WrapperProvider());
}
```

不需要改 `MinecraftFactory`、`OrangeX` 或任何业务代码。

### 3.6 支持该版本的映射

`MappingManager.init(runtimeInfo)` 目前会按版本和加载器选择
`MappingResolver`。新版本通常需要：

1. 在 `MappingManager` 中增加版本分支：

   ```java
   case V1_12_2: {
       if (loaderType == LoaderType.MCP) {
           resolver = new MCPMappingResolver();
       } else if (loaderType == LoaderType.VANILLA) {
           resolver = new SrgMappingResolver("mappings/vanilla1122/joined.srg");
       } else if (loaderType == LoaderType.FORGE) {
           resolver = new SrgMappingResolver("mappings/forge1122/joined.srg");
       }
       break;
   }
   ```

2. 把对应的 `joined.srg` 放到
   `OrangeX/src/main/resources/mappings/<loader>/joined.srg`。

3. 如果该版本使用 Mojang 映射（例如 NeoForge / 现代版本），不要复用 MCP
   名称直接当运行时名称，需要单独实现一个 `MappingResolver`，或者让
   `mapClass/mapField/mapMethod` 返回正确的运行时名称。

MCP 开发环境使用 `MappingClass.identity(...)`：没有显式注册的成员会按
同名返回，因此 `theMinecraft` 这类 named 字段可以直接使用。

## 4. 添加新的 Wrapper 能力

当需要暴露 Player、World、Chat 等能力时，不要往 `Minecraft` 接口里堆方法，
按能力单独建接口。完整实战（`Player` + `NoJumpDelay`）见
[adding-wrapper-capabilities.md](adding-wrapper-capabilities.md)。

以 `Player` 为例：

```java
// api/Player.java
public interface Player {
    void clearJumpDelay();
}
```

`WrapperProvider` 为每种能力增加一个工厂方法。依赖 `Minecraft` 实例的能力
要把实例一起传进来：

```java
public interface WrapperProvider {

    Minecraft createMinecraft(WrapperContext context);

    WorldView createWorld(WrapperContext context, Minecraft minecraft);

    Player createPlayer(WrapperContext context, Minecraft minecraft);
}
```

每个版本的 Provider 实现自己的 `createPlayer`；不支持的版本抛
`UnsupportedOperationException`：

```java
@Override
public Player createPlayer(WrapperContext context, Minecraft minecraft) {
    return new V1_8_9Player(context.getMappingResolver(), minecraft);
}
```

然后在 `WrapperRegistry` 增加分发方法，并在 `MinecraftFactory` 暴露：

```java
// WrapperRegistry
public static Player createPlayer(WrapperContext context, Minecraft minecraft) {
    return provider(context).createPlayer(context, minecraft);
}

// MinecraftFactory
public static Player createPlayer(RuntimeInfo runtimeInfo,
                                  MappingResolver mappingResolver,
                                  Minecraft minecraft) {
    return WrapperRegistry.createPlayer(
            new WrapperContext(runtimeInfo, mappingResolver), minecraft);
}
```

最后在 `OrangeX.start()` 里创建并保存（失败只记录日志）：

```java
try {
    this.player = MinecraftFactory.createPlayer(
            runtimeInfo, MappingManager.getResolver(), minecraft);
} catch (Throwable throwable) {
    System.err.println("[OrangeX] Player wrapper unavailable: "
            + throwable.getMessage());
}
```

这样：

- 业务代码只依赖 `api`。
- 新版本只需要实现同一个 Provider 接口。
- 新能力只需要加一个 API、一个 Provider 方法和一个分发方法。
- 具体字段反射写在 `internal.vX_Y_Z` 中，模块看不到 Minecraft 类名。

## 5. 构建与验证

构建：

```bash
./gradlew :OrangeX:shadowJar
# 或完整打包
./gradlew packageAll
```

启动客户端后检查日志：

```text
Loader Type: VANILLA
Minecraft Version: V1_8_9 (version id 15)
ave@71e820ad
```

最后一行是 `MinecraftFactory.create(...).getMinecraft()` 的输出，能打印出
实例就说明：

1. 版本识别正确；
2. `WrapperRegistry` 找到了对应 Provider；
3. `MappingResolver` 能解析该版本和加载器；
4. 反射字段/方法名正确。

## 6. 常见问题

| 现象 | 原因 |
|---|---|
| `Unsupported Minecraft version: V1_X_Y` | 没有在 `WrapperRegistry` 注册 Provider。 |
| `No mapping for field 'theMinecraft'` | 当前 `MappingResolver` 没有该字段的映射；检查 SRG 文件和 `MappingManager` 分支。 |
| `NoSuchFieldException: S` | 使用了 `getField` 读取 private 字段；改用 `getDeclaredField` + `setAccessible(true)`。 |
| `Cannot load Minecraft class ...` | 运行时类名错误，或 `RuntimeInfo` 的 ClassLoader 不对。 |
| `Wrapper provider already registered for ...` | 同一个版本注册了两次，检查静态块。 |
| 启动时报 `Unsupported version` | `MappingManager.init` 没有该版本的分支。 |

## 7. Checklist

- [ ] `MinecraftVersion` 已有该版本，且版本检测能正确输出。
- [ ] `wrapper/internal/vX_Y_Z` 下有 `VX_Y_ZWrapperProvider`、`VX_Y_ZMinecraft`，
      以及该版本支持的其它能力实现（`WorldView`、`Player`……）。
- [ ] Provider 实现了 `createMinecraft`、`createWorld`、`createPlayer`；
      暂不支持的能力显式抛 `UnsupportedOperationException`。
- [ ] `WrapperRegistry` 已注册该 Provider，并为每种能力增加分发方法。
- [ ] `MappingManager` 已支持该版本 + 加载器组合。
- [ ] SRG 资源已放到 `src/main/resources/mappings/...`。
- [ ] 实现类中没有静态单例查询，依赖全部来自 `WrapperContext`。
- [ ] `api` / `common` 中没有任何 Minecraft 或映射名称。
- [ ] `./gradlew :OrangeX:shadowJar` 通过，并在真实客户端中打印出实例。

新增能力而不是新增版本时，见 [adding-wrapper-capabilities.md](adding-wrapper-capabilities.md)。
