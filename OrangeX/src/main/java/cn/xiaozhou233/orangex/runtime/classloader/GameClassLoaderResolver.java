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
        try {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                String name = thread.getName();

                if (!"Client thread".equals(name)  && !"Render thread".equals(name)) {
                    continue;
                }

                ClassLoader loader = thread.getContextClassLoader();
                if (loader != null) {
                    return loader;
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
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
