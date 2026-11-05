# 示例：Hook onTick

本文用一个最小示例演示 **“把事件调用注入到 Minecraft tick 方法”** 的完整流程，包括：

- hook 和 EventBus 分别负责什么；
- 用自研事件跑通注入；
- 改用 greenrobot EventBus；
- hook 没触发时怎么排查。

> 仅为示例，省略了版本差异、异常隔离、线程安全、监听器快路径等生产细节。
> 项目里的实际实现（HookManager / ClassHook / ClassPoolUtils）见 [hooks.md](hooks.md)。

## 1. 先理解“触发”

EventBus 只负责分发，它不会自己产生事件。真正让事件发生的，是你在游戏方法里插入的那行 `post`：

```text
Minecraft.runTick()            ← 游戏每 tick 自己调用
    │
    ├── TickEvents.firePre()   ← 你插进去的一行
    │       └── EventBus.post(new TickEvent(true))
    │               └── 回调 @Subscribe onTick(...)
    │
    ├── 原来的 runTick 方法体
    │
    └── TickEvents.firePost()
```

可以这样理解：

- **EventBus** = 广播站，负责把事件发给监听器。
- **hook / 注入** = 在游戏代码里装一个按钮，游戏走到那里就按下按钮。
- 没有注入，就没有 `post`；没有 `post`，EventBus 就没有东西可分发。

所以完整链路永远是：

```text
字节码注入 → 游戏调用被改写的方法 → post → EventBus 分发 → 监听器
```

## 2. 方案 A：最小自研事件

### 2.1 事件

```java
package cn.xiaozhou233.orangex.hook;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TickEvent {

    public interface Listener {
        void onTick(TickEvent event);
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private final boolean pre;

    private TickEvent(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }

    public static void register(Listener listener) {
        LISTENERS.add(listener);
    }

    public static void firePre() {
        fire(new TickEvent(true));
    }

    public static void firePost() {
        fire(new TickEvent(false));
    }

    private static void fire(TickEvent event) {
        for (Listener listener : LISTENERS) {
            try {
                listener.onTick(event);
            } catch (Throwable ignored) {
                // 示例：真实实现应记录日志
            }
        }
    }
}
```

### 2.2 注入器（Javassist）

把 `runTick()` 改写成：

```java
public void runTick() {
    TickEvent.firePre();
    // ... 原方法体 ...
    TickEvent.firePost();
}
```

```java
package cn.xiaozhou233.orangex.hook;

import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.mapping.MappingMethod;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;

public final class TickHook {

    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String RUN_TICK = "runTick";
    private static final String RUN_TICK_DESC = "()V";

    /**
     * @return 改写后的字节码；返回 null 表示不处理这个类
     */
    public static byte[] transform(ClassLoader loader, String className, byte[] classBytes) {
        if (className == null || !MINECRAFT.replace('.', '/').equals(className)) {
            return null;
        }

        try {
            // 用 MappingResolver 拿到当前运行时的真实类名/方法名，
            // 不要硬编码混淆名。
            MappingClass mc = MappingManager.getResolver().mapClass(MINECRAFT);
            MappingMethod runTick = mc.getMethod(RUN_TICK, RUN_TICK_DESC);
            if (runTick == null) {
                return null;
            }

            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(loader));

            CtClass cc = pool.makeClass(new ByteArrayInputStream(classBytes), false);
            CtMethod method = cc.getMethod(
                    runTick.getTargetName(),
                    runTick.getTargetDescriptor()
            );

            // 不要写全限定名，原因见 3.7
            pool.importPackage("cn.xiaozhou233.orangex.hook");
            method.insertBefore("TickEvent.firePre();");
            method.insertAfter("TickEvent.firePost();");

            byte[] result = cc.toBytecode();
            cc.detach(); // 避免 ClassPool 一直持有这个类
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
```

### 2.3 注册到类加载流程

```java
package cn.xiaozhou233.orangex.hook;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public final class TickTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        return TickHook.transform(loader, className, classfileBuffer);
    }

    public static void install(Instrumentation instrumentation) {
        // true = 允许 retransform 已经加载过的类
        instrumentation.addTransformer(new TickTransformer(), true);
    }
}
```

> 工程里 `Instrumentation` 由 JuiceAgent/Loader 提供，这里只展示挂载点。
> 如果目标类已经加载，还需要 `instrumentation.retransformClasses(minecraftClass);`。

### 2.4 使用

```java
TickEvent.register(event -> {
    if (event.isPre()) {
        System.out.println("tick 开始");
    } else {
        System.out.println("tick 结束");
    }
});
```

## 3. 方案 B：greenrobot EventBus

EventBus 只替换“分发”这一层，注入部分不变。

### 3.1 依赖

```gradle
dependencies {
    implementation 'org.greenrobot:eventbus:3.3.1'
}
```

和 gson、javassist 一样做 relocate，避免和游戏/其他 Mod 冲突：

```gradle
shadowJar {
    relocate 'com.google.gson', 'cn.xiaozhou233.orangex.shaded.gson'
    relocate 'org.javassist', 'cn.xiaozhou233.orangex.shaded.javassist'
    relocate 'org.greenrobot.eventbus', 'cn.xiaozhou233.orangex.shaded.eventbus'
}
```

### 3.2 事件

```java
package cn.xiaozhou233.orangex.hook;

public final class TickEvent {

    private final boolean pre;

    public TickEvent(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return pre;
    }
}
```

### 3.3 监听器

```java
package cn.xiaozhou233.orangex.hook;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public final class TickListener {

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTick(TickEvent event) {
        if (event.isPre()) {
            System.out.println("pre tick");
        }
    }
}
```

### 3.4 门面类

注入代码只认这个门面，不直接引用 EventBus：

```java
package cn.xiaozhou233.orangex.hook;

import org.greenrobot.eventbus.EventBus;

public final class TickEvents {

    private static final EventBus BUS = EventBus.getDefault();

    public static void register(Object listener) {
        BUS.register(listener);
    }

    public static void unregister(Object listener) {
        BUS.unregister(listener);
    }

    public static void firePre() {
        BUS.post(new TickEvent(true));
    }

    public static void firePost() {
        BUS.post(new TickEvent(false));
    }
}
```

这样做的原因：

- 以后改库或做 relocate，字节码字符串不用跟着改。
- 门面里可以统一加 `hasListeners` 判断、线程判断、异常处理。

### 3.5 注入

和方案 A 完全一样，只是把调用目标换成 `TickEvents`：

```java
pool.importPackage("cn.xiaozhou233.orangex.hook");
method.insertBefore("TickEvents.firePre();");
method.insertAfter ("TickEvents.firePost();");
```

### 3.7 不要写全限定名（1.8.8 的 `cn` 类）

Vanilla 1.8.8 的客户端 jar 里有一个默认包下的混淆类 `cn`（`1.8.8.jar!/cn.class`）。
Javassist 从左往右解析限定名，写：

```java
method.insertBefore("cn.xiaozhou233.orangex.hook.TickEvents.firePre();");
```

会先找到 `cn` 类，再把 `xiaozhou233...` 当成它的字段/内部类，最后报出误导性的：

```text
no such class: cn$xiaozhou233.orangex.hook.TickEvents
```

这不是 classpath 问题。正确做法是 `pool.importPackage(...)` 后只写简单类名，就像上面两段示例那样。
项目里的 `ClassHook#getInjectedClassNames()` 就是为此存在的，详见 [hooks.md](hooks.md)。

### 3.6 注册

```java
TickEvents.register(new TickListener());
```

到此触发链完整：

```text
runTick() → TickEvents.firePre()
          → EventBus.post(TickEvent)
          → TickListener.onTick()
```

## 4. hook 没生效的三个必要条件

| 条件 | 说明 |
|---|---|
| 类必须被重新加载/重定义 | 如果 `Minecraft` 已经加载，只加 transformer 不会生效，必须 `retransformClasses` 或 `RedefineClasses`。 |
| 类名/方法名必须正确 | 运行时的名字可能是混淆名，必须通过 `MappingResolver` 拿，不能写死 `runTick`。 |
| 注入类必须对 game ClassLoader 可见 | OrangeX 的 jar 必须加到游戏 ClassLoader，否则 `TickEvents` 会 `NoClassDefFoundError`。 |

三种都满足，`firePre()` 才会被执行。

## 5. 排查

先在 `firePre()` 里加一行日志：

```java
public static void firePre() {
    System.out.println("[OrangeX] tick hook hit");
    BUS.post(new TickEvent(true));
}
```

| 现象 | 原因 |
|---|---|
| `no such class: cn$xiaozhou233...` | 注入代码写了全限定名，撞上 1.8.8 的 `cn` 类，见 3.7 |
| 日志不打印 | 注入没生效：transformer 没挂上 / 没 retransform / 类名方法名不对 |
| 日志打印但 `@Subscribe` 不执行 | 没注册 / 注册和 post 不是同一个 `EventBus` 实例 / 事件类型不匹配 / 方法不是 public |
| 执行了但不在游戏线程 | 用了 `ThreadMode.ASYNC` 等非 POSTING 模式 |
| `NoClassDefFoundError` | 库没进 game ClassLoader，或没有 shade |

记住：**日志不打印属于注入问题，日志打印了才算 EventBus 问题**，不要一上来就怀疑库。

## 6. 另一种触发方式：平台事件桥

如果只是先把流程跑通，可以暂时用 Forge/Fabric 自带的事件，桥接进 EventBus，不改字节码。

Forge 1.8.9：

```java
@SubscribeEvent
public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
        TickEvents.firePre();
    }
}
```

Fabric：

```java
ClientTickEvents.START_CLIENT_TICK.register(client -> TickEvents.firePre());
```

注意：

- Forge 的 `@SubscribeEvent` 和 greenrobot 的 `@Subscribe` 名字很像，注意 import，必要时写全限定名。
- Vanilla 环境没有这类事件。
- 不同加载器写法不同，OrangeX 需要为每个加载器单独处理。

所以平台事件桥适合早期验证，长期方案还是字节码注入。

## 7. greenrobot 在 Minecraft 里的注意点

1. **只用 `ThreadMode.POSTING`**
   非 Android 环境不要用 `MAIN` / `MAIN_ORDERED` / `ASYNC`，否则可能投递到错误线程，读游戏状态时会崩。

2. **父类也会收到事件**
   greenrobot 会把事件派发给父类型的 `@Subscribe` 方法。有 `TickEvent` 和 `PreTickEvent extends TickEvent` 时，注册 `onTick(TickEvent)` 会同时收到两者。

3. **统一使用一个实例**
   注册和 `post` 必须是同一个 `EventBus`。建议像 `TickEvents` 一样用一个静态持有者。

4. **`@Subscribe` 方法保持 public**
   减少反射和兼容性问题。

5. **退出时反注册**
   持有可卸载类的监听器要 `TickEvents.unregister(listener)`。

## 8. 简化点

| 简化 | 说明 |
|---|---|
| 只处理 `Minecraft.runTick()` | 不同版本入口可能不同，需要按版本选择映射。 |
| 没有取消语义 | 真实事件通常带 `cancel`，并在注入代码里 `if (event.fire()) return;`。 |
| 没有 `hasListeners()` 快路径 | 可以在注入前判断监听器数量，避免空事件对象分配。 |
| 没有处理异常与重入 | 监听器异常、递归触发都需要额外保护。 |
| 直接使用 Javassist 字符串注入 | 需要精确指令时改用 ASM（`MethodVisitor`）更可控。 |
