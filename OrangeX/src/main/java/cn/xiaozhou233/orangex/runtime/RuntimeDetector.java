package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.classloader.GameClassLoaderResolver;
import cn.xiaozhou233.orangex.runtime.loader.LoaderDetector;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionDetector;

/**
 * Entry point for Minecraft runtime detection.
 *
 * <p>Each step has a single owner, so the pipeline stays easy to extend:</p>
 * <ol>
 *   <li>{@link GameClassLoaderResolver} - find the game class loader.</li>
 *   <li>{@link LoaderDetector} - identify the mod loader.</li>
 *   <li>{@link VersionDetector} - resolve the Minecraft version.</li>
 * </ol>
 */
public final class RuntimeDetector {

    public static RuntimeInfo detect() {
        ClassLoader classLoader = GameClassLoaderResolver.resolve();
        LoaderType loaderType = LoaderDetector.detect(classLoader);
        int versionId = VersionDetector.detectId(classLoader, loaderType);

        return new RuntimeInfo(
                classLoader,
                loaderType,
                MinecraftVersion.fromId(versionId),
                versionId
        );
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

        System.out.println("Minecraft ClassLoader: " + loader);
        System.out.println("Context ClassLoader (current thread): "
                + Thread.currentThread().getContextClassLoader());
        printHierarchy(loader);

        System.out.println("Loader Type: "
                + (runtime.getLoaderType() == null ? "UNKNOWN" : runtime.getLoaderType().name()));
        System.out.println("Minecraft Version: "
                + (version == null ? "UNKNOWN" : version.name())
                + " (version id " + runtime.getVersionId() + ")");
        System.out.println("========================================");
    }

    private static void printHierarchy(ClassLoader loader) {
        if (loader == null) {
            return;
        }

        StringBuilder hierarchy = new StringBuilder(loader.getClass().getName());
        ClassLoader parent = loader.getParent();
        while (parent != null) {
            hierarchy.append(" -> ").append(parent.getClass().getName());
            parent = parent.getParent();
        }
        System.out.println("ClassLoader Hierarchy: " + hierarchy);
    }

    private RuntimeDetector() {
    }
}
