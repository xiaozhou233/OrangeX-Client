# OrangeX Client

## 支持注入的客户端

| 客户端          | 说明             |
| ------------ | -------------- |
| MCP 1.8.8    | 基于 MCP 的本地开发环境 |
| Lunar Client | 已适配的第三方客户端     |

---

## 如何构建

### 方式一：直接构建

> 项目中已包含构建完成的依赖库，可直接进行构建

* 使用 `./gradlew buildInjector` 构建项目
* 构建完成后，`out/` 目录下会生成 `Injector.jar`
* 使用 `java -jar Injector.jar` 启动注入器

---

### 方式二：重新构建依赖

> 适用于需要自行编译底层依赖库的情况

#### 第一步：下载或构建 JuiceAgent

* 在 [JuiceAgent](https://github.com/xiaozhou233/JuiceAgent) 项目中根据文档自行构建，或直接下载已编译版本
* 获得以下文件：

    * `libagent.dll`
    * `libinject.dll`
    * `libjuiceloader.dll`
* 替换 `natives` 目录中的对应 DLL 文件

#### 第二步：下载或构建 JuiceLoader

* 在 [JuiceLoader](https://github.com/xiaozhou233/JuiceLoader) 项目中根据文档自行构建，或直接下载已编译版本
* 获得 `JuiceLoader.jar`
* 替换 `libraries` 目录中的 `JuiceLoader.jar`

#### 第三步：构建 OrangeX

* 使用 `./gradlew buildInjector` 构建项目
* 构建完成后，`out/` 目录下会生成 `Injector.jar`
* 使用 `java -jar Injector.jar` 启动注入器
