package cn.xiaozhou233.orangex.runtime.classloader;

import cn.xiaozhou233.orangex.runtime.ClassProbe;

public final class GameClassLoaderResolver {

    public static ClassLoader resolve() {
        ClassLoader loader = findGameThreadLoader();
        if (loader != null) {
            return loader;
        }

        loader = findMinecraftLoader(Thread.currentThread().getContextClassLoader());
        if (loader != null) {
            return loader;
        }

        return Thread.currentThread().getContextClassLoader();
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

    private static ClassLoader findMinecraftLoader(ClassLoader candidate) {
        if (candidate == null) {
            return null;
        }

        String[] minecraftClasses = {
                "net.minecraft.client.Minecraft",
                "net.minecraft.class_310"
        };

        Class<?> minecraftClass = ClassProbe.find(candidate, minecraftClasses);
        if (minecraftClass != null) {
            ClassLoader minecraftLoader = minecraftClass.getClassLoader();
            return minecraftLoader == null ? candidate : minecraftLoader;
        }

        return null;
    }

    private GameClassLoaderResolver() {
    }
}
