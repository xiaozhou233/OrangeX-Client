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

    public static void debug(RuntimeInfo runtime) {
        ClassLoader loader = runtime.getMinecraftClassLoader();
        MinecraftVersion version = runtime.getMinecraftVersion();
        int protocol = MinecraftVersion.getCurrentProtocolVersion();

        System.out.println("======== Minecraft Runtime Info ========");
        System.out.println("Minecraft ClassLoader: " + loader.getClass().getName());
        System.out.println("Context ClassLoader: " + Thread.currentThread().getContextClassLoader());
        System.out.print("ClassLoader Hierarchy: " + loader.getClass().getName());
        ClassLoader parent = loader.getParent();
        while (parent != null) {
            System.out.print(" -> " + parent.getClass().getName());
            parent = parent.getParent();
        }
        System.out.println();
        System.out.println("Loader Type: " + runtime.getLoaderType().name());
        System.out.println("Minecraft Version: " + version.name() + " (protocol " + protocol + ")");
        System.out.println("========================================");
    }
}