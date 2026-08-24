package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.classloader.GameClassLoaderResolver;

public final class RuntimeDetector {
    public static RuntimeInfo detect() {
        // detect classloader
        ClassLoader loader = GameClassLoaderResolver.resolve();

        return new RuntimeInfo(loader);
    }

    public static void debug() {
        RuntimeInfo runtime = detect();

        System.out.println("======== Minecraft Runtime Info ========");
        System.out.println("Minecraft ClassLoader: " + runtime.getMinecraftClassLoader().getClass().getName());
        System.out.println("========================================");

    }
}
