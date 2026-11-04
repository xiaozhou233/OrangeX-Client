# 添加 Minecraft 版本支持

本文说明如何为 OrangeX 增加一个新的 Minecraft 版本。大多数版本只需要两步：注册内部版本 ID、注册结构签名。

## 检测流程

`cn.xiaozhou233.orangex.runtime` 按以下顺序检测版本，前一步成功就返回：

1. **加载器元数据** — `VersionIdSources` 读取 Forge/NeoForge 暴露的版本号。
2. **26.x 版本族** — `ModernVersionDetector` 读取客户端自带的 `version.json`。
3. **结构签名** — `VersionSignatureRegistry` 通过类名、单例 getter/field 和锚点类识别版本。

因此，普通版本只需要修改结构签名；只有新的版本族或新的加载器版本号规则才需要动前两步。

## 1. 添加内部版本 ID

在 `runtime/version/MinecraftVersion.java` 中添加枚举值：

```java
V1_21_10(60),
```

注意：

- 这里的数字是 **OrangeX 内部版本 ID**，不是 Minecraft 网络协议号。
- ID 一旦发布就不要再修改或复用，否则会破坏已有的版本判断和存档/配置兼容性。
- `ForgeVersionParser` 会用这个枚举校验 Forge 版本号，所以新版本 ID 也会自动生效。

## 2. 添加结构签名

在 `runtime/version/VersionSignatureRegistry.java` 的 `SIGNATURES` 列表中注册签名。

### 2.1 Vanilla（混淆类名）

```java
VersionSignature.anyLoader(V1_21_10, obf(
        "obfuscatedMinecraftClass", "getterName", "instanceField",
        "anchorClass1", "anchorClass2", "anchorClass3")),
```

### 2.2 MCP / 开发环境（真实类名）

```java
VersionSignature.anyLoader(V1_8_9, mojang(
        "net.minecraft.client.Minecraft", "getMinecraft", "theMinecraft",
        "net.minecraft.client.entity.EntityPlayerSP")),
```

### 2.3 Fabric（intermediary 类名）

```java
VersionSignature.forLoader(FABRIC, V1_21_11, intermediary(
        "net/minecraft/class_310", "method_1551", "field_1700",
        "net/minecraft/class_638", "net/minecraft/class_746")),
```

### 2.4 NeoForge（Mojang 映射）

```java
VersionSignature.forLoader(NEOFORGE, V1_21_1, mojang(
        "net/minecraft/client/Minecraft", "getInstance", "instance",
        "net/minecraft/client/multiplayer/ClientLevel",
        "net/minecraft/client/player/LocalPlayer")),
```

### 参数说明

| 参数 | 说明 |
|---|---|
| `minecraftClass` | Minecraft 主类。支持 `a.b.C` 和 `a/b/C` 两种写法。 |
| `instanceGetter` | 返回单例的静态方法名；没有就传 `null`。 |
| `instanceField` | 保存单例的静态字段名；没有就传 `null`。 |
| `anchors` | 锚点类。必须和 Minecraft 主类由**同一个 ClassLoader** 定义。 |

### 锚点选择规则

- 选目标版本**独有**的类，优先选该版本新增内容对应的类。
- 锚点必须存在于运行时，不能只存在于开发环境。
- 不要选父 ClassLoader 提供的类，否则 `MinecraftProbe` 会判定不匹配。
- 签名之间不能重叠：**如果两个不同版本同时匹配，检测结果会变成 `UNKNOWN`**，这是有意的防误判策略。

## 3. 新版本族（`version.json`）

如果新版本使用 `id == name` 的版本族命名（例如 26.x），并且没有稳定的混淆类名可探测：

1. 在 `MinecraftVersion` 中添加对应 ID。
2. 在 `utils/MinecraftUtils.java` 的 `readModernVersionId` 中识别新的版本族前缀：

```java
if (isFamily(id, "26.3")) {
    return MinecraftVersion.V26_3.getId();
}
```

3. 如有需要，在 `ModernVersionDetector` 中补充该类版本族的类结构检查。

## 4. 新的 Forge 版本号规则

如果 Forge/NeoForge 提供了新的版本信息来源：

1. 在 `VersionIdSources` 中添加一个 `VersionIdSource`，并加入 `SOURCES` 列表。
2. 如果版本号字符串有新的映射规则，修改 `ForgeVersionParser.parse`。

来源方法必须保持 fail-safe：找不到类、字段或方法时返回 `MinecraftVersion.UNKNOWN_ID`，让后续来源继续尝试。

## 5. 验证

构建：

```bash
./gradlew :OrangeX:shadowJar
```

启动带 OrangeX 的客户端，检查日志：

```text
Loader Type: FORGE
Minecraft Version: V1_21_10 (version id 60)
```

也可以直接调用：

```java
RuntimeInfo info = RuntimeDetector.detect();
RuntimeDetector.debug(info);
```

排查建议：

| 现象 | 可能原因 |
|---|---|
| `UNKNOWN (version id 0)` | 签名没匹配上：锚点类名错误、类不存在，或 ClassLoader 不一致。 |
| 检测成旧版本 | 旧版本签名也匹配了目标版本：给新版本签名增加独有锚点，避免重叠。 |
| 检测成新版本但实际是旧版本 | 锚点在旧版本中已经存在：换一个真正新增的锚点。 |
| Fabric/NeoForge 下不匹配 | 缺少对应的 `forLoader(...)` 签名，或 `LoaderType` 判断错误。 |

建议分别验证 Vanilla、Fabric、NeoForge 三种环境，并确认检测结果唯一。

## 6. Checklist

- [ ] `MinecraftVersion` 中的内部 ID 唯一。
- [ ] 至少注册了一条结构签名。
- [ ] 锚点类与 Minecraft 主类由同一个 ClassLoader 定义。
- [ ] 目标版本与相邻版本的签名不会同时匹配。
- [ ] Fabric/NeoForge 使用了正确的 `forLoader(...)`。
- [ ] 新版本族已更新 `MinecraftUtils` / `ModernVersionDetector`。
- [ ] 已在真实客户端中验证 `Loader Type` 和 `Minecraft Version` 输出。

## 附：添加新的 Mod 加载器

版本检测之外，加载器识别也使用注册表：

1. 在 `loader/LoaderType.java` 中添加枚举值。
2. 在 `loader/LoaderDetector.java` 的 `PROBES` 中注册 `LoaderProbe`：
   - 如果只需检查 marker 类，使用 `ClassPresenceLoaderProbe`。
   - 如果需要结构判断，实现 `LoaderProbe`。
3. 如果新加载器需要专属版本签名，在 `VersionSignatureRegistry` 中使用 `VersionSignature.forLoader(...)`。

版本检测完成后，如果需要为该版本提供跨版本 API，见
[adding-wrapper-versions.md](adding-wrapper-versions.md)。
