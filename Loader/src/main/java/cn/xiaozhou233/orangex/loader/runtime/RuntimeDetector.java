package cn.xiaozhou233.orangex.loader.runtime;

import cn.xiaozhou233.orangex.loader.runtime.classloader.GameClassLoaderResolver;
import cn.xiaozhou233.orangex.loader.runtime.loader.LoaderDetector;
import cn.xiaozhou233.orangex.loader.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.loader.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.loader.runtime.version.VersionDetector;

public final class RuntimeDetector {
    public static RuntimeInfo detect() {
        ClassLoader classLoader = GameClassLoaderResolver.resolve();
        LoaderType loaderType = LoaderDetector.detect(classLoader);
        MinecraftVersion version = VersionDetector.detect(classLoader, loaderType);
        return new RuntimeInfo(classLoader, loaderType, version);
    }

    public static void debug(RuntimeInfo runtime) {
        System.out.println("======== Minecraft Runtime Info ========");

        if (runtime == null) {
            System.out.println("RuntimeInfo: null");
            System.out.println("========================================");
            return;
        }

        ClassLoader loader = runtime.getMinecraftClassLoader();
        MinecraftVersion version = runtime.getMinecraftVersion();
        int protocol = MinecraftVersion.getCurrentProtocolVersion();

        System.out.println("Minecraft ClassLoader: " + loader);
        System.out.println("Context ClassLoader (current thread): " + Thread.currentThread().getContextClassLoader());
        if (loader != null) {
            StringBuilder hierarchy = new StringBuilder(loader.getClass().getName());
            ClassLoader parent = loader.getParent();
            while (parent != null) {
                hierarchy.append(" -> ").append(parent.getClass().getName());
                parent = parent.getParent();
            }
            System.out.println("ClassLoader Hierarchy: " + hierarchy);
        }
        System.out.println("Loader Type: " + (runtime.getLoaderType() == null ? "UNKNOWN" : runtime.getLoaderType().name()));
        System.out.println("Minecraft Version: " + (version == null ? "UNKNOWN" : version.name())
                + " (version id " + protocol + ")");
        System.out.println("========================================");
    }
}
