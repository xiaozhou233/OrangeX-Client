package cn.xiaozhou233.orangex.runtime.classloader;

import cn.xiaozhou233.orangex.runtime.probe.ClassProbe;

public final class GameClassLoaderResolver {

    /**
     * Class names that identify the game class loader across supported
     * runtimes: Mojang/Forge/MCP, Fabric intermediary, and 1.8 vanilla
     * obfuscated clients.
     */
    private static final String[] MINECRAFT_CLASSES = {
            "net.minecraft.client.Minecraft",
            "net.minecraft.class_310",
            "ave"
    };

    public static ClassLoader resolve() {
        ClassLoader threadLoader = findGameThreadLoader();

        ClassLoader resolved = definingLoaderOf(threadLoader);
        if (resolved != null) {
            return resolved;
        }

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        resolved = definingLoaderOf(contextLoader);
        if (resolved != null) {
            return resolved;
        }

        ClassLoader ownLoader = GameClassLoaderResolver.class.getClassLoader();
        resolved = definingLoaderOf(ownLoader);
        if (resolved != null) {
            return resolved;
        }

        try {
            resolved = definingLoaderOf(ClassLoader.getSystemClassLoader());
            if (resolved != null) {
                return resolved;
            }
        } catch (SecurityException ignored) {
            // Security manager may forbid system loader lookup.
        }

        // No Minecraft class is visible yet; keep the previous best effort.
        if (threadLoader != null) {
            return threadLoader;
        }
        if (contextLoader != null) {
            return contextLoader;
        }
        return ownLoader;
    }

    /**
     * Returns the loader that actually defines the Minecraft class, falling
     * back to the candidate itself when the class is defined by the bootstrap
     * loader.
     */
    private static ClassLoader definingLoaderOf(ClassLoader candidate) {
        Class<?> minecraftClass = ClassProbe.find(candidate, MINECRAFT_CLASSES);
        if (minecraftClass == null) {
            return null;
        }

        ClassLoader definingLoader = minecraftClass.getClassLoader();
        return definingLoader != null ? definingLoader : candidate;
    }

    private static ClassLoader findGameThreadLoader() {
        // Client thread wins over render thread, regardless of thread iteration order
        ClassLoader clientLoader = null;
        ClassLoader renderLoader = null;

        try {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                String name = thread.getName();

                if ("Client thread".equals(name)) {
                    if (clientLoader == null) {
                        clientLoader = thread.getContextClassLoader();
                    }
                } else if ("Render thread".equals(name)) {
                    if (renderLoader == null) {
                        renderLoader = thread.getContextClassLoader();
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return clientLoader != null ? clientLoader : renderLoader;
    }

    private GameClassLoaderResolver() {
    }
}
