package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.classloader.GameClassLoaderResolver;
import cn.xiaozhou233.orangex.runtime.loader.LoaderDetector;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;

public final class RuntimeDetector {
    public static RuntimeInfo detect() {
        // detect classloader
        ClassLoader classLoader = GameClassLoaderResolver.resolve();
        LoaderType loaderType = LoaderDetector.detect(classLoader);

        return new RuntimeInfo(classLoader, loaderType);
    }

    public static void debug() {
        RuntimeInfo runtime = detect();

        System.out.println("======== Minecraft Runtime Info ========");
        System.out.println("Minecraft ClassLoader: " + runtime.getMinecraftClassLoader().getClass().getName());
        System.out.println("Loader Type: " + runtime.getLoaderType().name());
        System.out.println("========================================");

    }
}
