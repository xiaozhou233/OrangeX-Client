package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.classloader.GameClassLoaderResolver;
import cn.xiaozhou233.orangex.runtime.loader.LoaderDetector;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionDetector;

public final class RuntimeDetector {
    public static RuntimeInfo detect() {
        ClassLoader classLoader = GameClassLoaderResolver.resolve();
        LoaderType loaderType = LoaderDetector.detect(classLoader);
        MinecraftVersion version = VersionDetector.detect(classLoader, loaderType);
        return new RuntimeInfo(classLoader, loaderType, version);
    }

    public static void debug() {
        RuntimeInfo runtime = detect();
        System.out.println("======== Minecraft Runtime Info ========");
        System.out.println("Minecraft ClassLoader: " + runtime.getMinecraftClassLoader().getClass().getName());
        System.out.println("Loader Type: " + runtime.getLoaderType().name());
        System.out.println("Minecraft Version: " + runtime.getMinecraftVersion().name());
        System.out.println("========================================");
    }
}