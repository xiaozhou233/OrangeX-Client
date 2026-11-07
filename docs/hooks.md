# Hook 系统

本文说明 OrangeX 的字节码 Hook 架构、如何新增一个 Hook，以及向 Minecraft 方法里注入 Javassist 代码时必须注意的坑。

> 只讲当前实现，不讨论历史方案。最小示例见 [hook-on-tick-example.md](hook-on-tick-example.md)。

## 1. 执行流程

```text
游戏启动
  └── Loader 把 OrangeX.jar 加入游戏 ClassLoader，反射调用 OrangeX.start()
        ├── RuntimeDetector.detect()       运行时 / 版本 / ClassLoader
        ├── MappingManager.init()          混淆名映射
        ├── eventBus.register(...)         注册事件监听器
        └── HookManager.applyAll()
              └── 对每个 ClassHook：
                    ├── hook.getTargetClass()                 目标类（点分名）
                    ├── JuiceAgent.getClassBytesByName(...)   读取原始字节码
                    ├── hook.transform(loader, bytes)
                    │     ├── ClassPoolUtils.createPool(loader)   新建 Javassist 环境
                    │     ├── transform(ctClass)              插入调用
                    │     └── ctClass.toBytecode()            生成新字节码
                    └── JuiceAgent.retransformClassByName(...) 写回 JVM
```

注入只发生在类重转换阶段。之后每次执行被改写的方法，都会经过注入进去的调用，把事件 post 到 `EventBus`。

## 2. 核心类

| 类 | 位置 | 职责 |
| --- | --- | --- |
| `HookManager` | `hook/HookManager` | 注册所有 Hook，遍历、读取字节码、触发重转换 |
| `ClassHook` | `hook/ClassHook` | Hook 基类：目标类、注入逻辑、注入代码引用的类 |
| `ClassPoolUtils` | `hook/ClassPoolUtils` | 创建 Javassist `ClassPool`（OrangeX.jar + agent 字节 + loader） |
| `AgentClassPath` | `hook/AgentClassPath` | 用 agent 读取已加载类的运行时字节，兼容 Lunar 等启动器 |
| `orangexbridge.Hooks` | `orangexbridge/Hooks` | 注入代码统一入口，避免 `cn` 默认包冲突 |
| `JuiceAgentBridge` | `hook/JuiceAgentBridge` | 通过 system ClassLoader 反射调用 JuiceAgent API（Fabric 必需） |
| `TickHook` | `hook/impl/TickHook` | 在 `Minecraft.runTick()` 前后插入事件调用 |
| `TickEvents` | `event/facade/TickEvents` | 注入代码唯一引用的门面，负责 post 事件 |
| `EventTickPre` / `EventTickPost` | `event/impl/*` | 事件对象 |

## 3. 新增一个 Hook

### 3.1 继承 `ClassHook`

```java
public final class TickHook extends ClassHook {

    private static final RuntimeInfo runtimeInfo = OrangeX.getInstance().getRuntimeInfo();
    private static final MappingResolver resolver = MappingManager.getResolver();

    /** 注入代码里引用到的类，用于 import 包。 */
    @Override
    protected String[] getInjectedClassNames() {
        return new String[]{"cn.xiaozhou233.orangex.event.facade.TickEvents"};
    }

    /** 目标类，点分名；返回 null 表示当前版本不支持。 */
    @Override
    protected String getTargetClass() {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return resolver.mapClass("net/minecraft/client/Minecraft")
                        .getTargetName().replace("/", ".");
            case V26_2:
                return "net.minecraft.client.Minecraft";
            default:
                return null;
        }
    }

    @Override
    protected void transform(CtClass ctClass) throws Exception {
        String methodName = null;
        String methodDesc = null;

        // 只在这里做“版本 → 方法名/描述符”的映射，下面注入逻辑共用。
        // 新增版本时只需要补一个 case。
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9: {
                MappingMethod runTick = resolver.mapMethod(
                        "net/minecraft/client/Minecraft", "runTick", "()V");
                methodName = runTick.getTargetName();
                methodDesc = runTick.getTargetDescriptor();
                break;
            }
            // 26.2 无混淆：runTick(Z)V，private
            case V26_2: {
                MappingMethod runTick = resolver.mapMethod(
                        "net/minecraft/client/Minecraft", "runTick", "(Z)V");
                methodName = runTick.getTargetName();
                methodDesc = runTick.getTargetDescriptor();
                break;
            }
            default:
                break;
        }

        if (methodName == null || methodDesc == null) {
            return;
        }

        CtMethod method = ctClass.getMethod(methodName, methodDesc);

        // 必须使用简单类名，原因见 4.1。
        method.insertBefore("TickEvents.firePre();");
        method.insertAfter("TickEvents.firePost();");
    }
}
```

### 3.2 注册

在 `HookManager` 构造器里注册：

```java
private HookManager() {
    register(new TickHook());
}
```

构造函数里只做注册，不要在此时访问尚未初始化的运行时信息。

### 3.3 使用映射，不要硬编码

运行时的类名/方法名可能是混淆名，统一通过 `MappingResolver` 获取：

```java
MappingClass mc = resolver.mapClass("net/minecraft/client/Minecraft");
MappingMethod m = resolver.mapMethod("net/minecraft/client/Minecraft", "runTick", "()V");
```

各版本的命名来源：

| 版本 | 解析器 | 运行时名字 |
| --- | --- | --- |
| 1.8.9 MCP | `MCPMappingResolver` | MCP 名（`runTick`） |
| 1.8.9 Vanilla/Forge | `SrgMappingResolver` | 混淆名（`s` 等） |
| 26.2 | `IdentityFallbackMappingResolver` | Mojang 名（`net.minecraft.client.Minecraft#runTick(Z)V`，private） |

26.2 本身没有混淆，identity 解析器会把 `net/minecraft/client/Minecraft` 和 `runTick()V` 原样返回。只要目标类/方法相同，一个 Hook 就可以同时覆盖多个版本（`TickHook` 的 `V1_8_9` 和 `V26_2` 共用同一段注入逻辑）。

`ClassHook` 的基类逻辑会负责剩下的步骤：读取字节码 → 注入 → `toBytecode()` → 交给 `JuiceAgent` 重转换。

## 4. 注入代码的硬性规则

### 4.1 注入代码统一走 `orangexbridge`

Vanilla 1.8.8 的客户端 jar 里有一个**默认包**下的混淆类 `cn`（`1.8.8.jar!/cn.class`）。Javassist 解析限定名是从左往右的，写：

```java
method.insertBefore("cn.xiaozhou233.orangex.event.facade.TickEvents.firePre();");
```

会把 `cn` 解析成那个类，然后把后面的 `xiaozhou233...` 当成字段/内部类，最终报出极具误导性的错误：

```text
no such class: cn$xiaozhou233.orangex.event.facade.TickEvents
```

这**不是** classpath 问题，加多少类路径都没用。现在所有注入代码都通过一个不会和默认包冲突的顶层包：

```java
method.insertBefore("orangexbridge.Hooks.tickPre();");
```

`orangexbridge.Hooks` 只是转发到真正的 facade；1.8.9 专用的辅助方法在 `orangexbridge.Legacy`。这样不需要 `importPackage`，也不会触发 `cn` 冲突。

### 4.2 被引用的类必须对 Javassist 可见

`Loader` 会设置系统属性：

```java
System.setProperty("orangex.jar", orangexFile.getAbsolutePath());
```

`ClassPoolUtils.createPool(loader)` 会读取该属性，把 OrangeX.jar 放在 classpath 第一位，因此 `orangexbridge.*` 和 `cn.xiaozhou233.*` 都能从 jar 直接解析。

游戏类则由 `AgentClassPath` 通过 `JuiceAgent#getClassBytesByName` 提供**运行时字节码**。原因是一些启动器（例如 Lunar 的 GenesisClassLoader）按 MCP 名字提供资源，但资源里的字节仍然是混淆名，Javassist 会报：

```text
cannot find net.minecraft.util.IThreadListener: od found in net/minecraft/util/IThreadListener.class
```

从 agent 拿到已加载类的字节可以保证类名和目标类一致。

### 4.3 `ClassPool` 不再用全局单例

`ClassPoolUtils.createPool(loader)` 每次创建一个新的 `ClassPool(false)`，包含：

```text
OrangeX.jar
AgentClassPath（已加载游戏类的运行时字节）
LoaderClassPath(game loader)   兜底
LoaderClassPath(system loader) JDK 类
```

不再使用 `ClassPool.getDefault()`：某些启动器的注入线程 `Thread.currentThread().getContextClassLoader()` 为 `null`，`getDefault()` 的系统路径会在解析类时抛异常。

`ClassHook.transform()` 最后会 `ctClass.detach()`，避免 `ClassPool` 长期持有 `CtClass`。

### 4.4 异常不要外泄

`HookManager` 对每个 Hook 单独 `try/catch(Throwable)`：某个 Hook 失败只打印堆栈，不影响其他 Hook，也不阻断游戏启动。

### 4.5 Fabric：不要直接引用 JuiceAgent API

JuiceAgent 的 native 方法只注册在 **system ClassLoader** 加载的那份 API 类上。Fabric 的 `KnotClassLoader` 会优先从自己的 code source 加载类，如果 `OrangeX.jar` 里 shade 了 `cn.xiaozhou233.juiceagent.api.*`，Knot 就会加载第二份 `JuiceAgent`，调用时直接抛：

```text
java.lang.UnsatisfiedLinkError: 'byte[] cn.xiaozhou233.juiceagent.api.JuiceAgent.getClassBytesByName(java.lang.String)'
```

因此：

- `OrangeX.jar` **不要** shade JuiceAgent API；
- 需要调用 `getClassBytesByName` / `retransformClassByName` 时，统一走 `JuiceAgentBridge`，它用 `Class.forName(..., ClassLoader.getSystemClassLoader())` 拿到已注册 native 的那份类；
- 不要在 OrangeX 里 `import cn.xiaozhou233.juiceagent.api.JuiceAgent`。

`Loader` 运行在 system ClassLoader 下，不受这个限制。

## 5. 事件总线

底层是 greenrobot EventBus，但注入代码只认门面 `TickEvents`：

```java
public class TickEvents {
    private static final EventBus BUS = OrangeX.getInstance().getEventBus();

    public static void firePre() {
        BUS.post(new EventTickPre());
    }

    public static void firePost() {
        BUS.post(new EventTickPost());
    }
}
```

监听器：

```java
OrangeX.getInstance().getEventBus().register(new Object() {
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTick(EventTickPre event) {
        // 游戏主线程
    }
});
```

约定：

- 只用 `ThreadMode.POSTING`，非 Android 环境不要用 `MAIN` / `ASYNC`；
- 注册和 post 必须是同一个 `EventBus` 实例（统一从 `OrangeX.getInstance().getEventBus()` 获取）；
- 监听器需要卸载时调用 `eventBus.unregister(listener)`。

## 6. 排查

| 现象 | 原因 |
| --- | --- |
| `no such class: cn$xiaozhou233...` | 注入代码写了 `cn.*` 全限定名，撞上 1.8.8 的 `cn` 类。统一改用 `orangexbridge.Hooks` |
| `cannot find net.minecraft...: od found in ...` | Lunar / Genesis 等启动器的类资源仍是混淆名。确认 `AgentClassPath` 生效，并且日志里有 `javassist bridge OK` |
| `[OrangeX] javassist bridge FAILED` | OrangeX.jar 没放进 `ClassPool`，或部署的 jar 是旧版本（缺少 `orangexbridge`） |
| `CannotCompileException`，`getMethod` 抛 `NotFoundException` | 版本不对/方法名或描述符错误，检查 `MappingResolver` |
| 日志有 `[OrangeX] javassist classpath: ...` 但无事件打印 | 注入成功但没触发：确认目标方法真的每帧调用（1.8.x `runTick()V`，26.2 `runTick(Z)V`） |
| 事件 post 了但 `@Subscribe` 不执行 | 没注册 / 不是同一个 `EventBus` / 方法不是 `public` |
| `UnsatisfiedLinkError: JuiceAgent.getClassBytesByName` | Fabric 下加载了第二份 API；不要把 API shade 进 OrangeX.jar，走 `JuiceAgentBridge` |
| `NoClassDefFoundError` | OrangeX.jar 不在游戏 ClassLoader；检查 Loader 的注入路径 |
| 某个 Hook 失败，其他 Hook 正常 | 预期行为，`HookManager` 会隔离异常 |

## 7. 相关文件

```text
OrangeX/src/main/java/cn/xiaozhou233/orangex/
├── hook/
│   ├── HookManager.java
│   ├── ClassHook.java
│   ├── ClassPoolUtils.java
│   ├── AgentClassPath.java
│   ├── SafeClassPath.java
│   ├── JuiceAgentBridge.java
│   └── impl/
│       ├── TickHook.java
│       ├── Render2DHook.java
│       ├── GuiInputHook.java
│       └── KeyPressHook.java
└── event/
    ├── Event.java
    ├── facade/
    │   ├── TickEvents.java
    │   ├── Render2DEvents.java
    │   ├── KeyEvents.java
    │   └── InputEvents.java
    └── impl/EventTickPre.java, EventTickPost.java, EventRender2D.java, EventKey.java

OrangeX/src/main/java/orangexbridge/
├── Hooks.java          注入代码统一入口
└── Legacy.java         1.8.9 专用（LWJGL 2）
```
