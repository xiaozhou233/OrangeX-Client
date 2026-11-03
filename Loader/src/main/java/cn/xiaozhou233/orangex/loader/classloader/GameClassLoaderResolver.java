package cn.xiaozhou233.orangex.loader.classloader;

/**
 * Loader-side bootstrap resolver: only resolves the Minecraft game class loader,
 * which is required before OrangeX can be injected. Full runtime detection
 * (loader type / Minecraft version) lives in OrangeX.
 */
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

        Class<?> minecraftClass = findClass(candidate, minecraftClasses);
        if (minecraftClass != null) {
            ClassLoader minecraftLoader = minecraftClass.getClassLoader();
            return minecraftLoader == null ? candidate : minecraftLoader;
        }

        return null;
    }

    private static Class<?> findClass(ClassLoader loader, String... names) {
        if (loader == null || names == null) {
            return null;
        }

        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }

            String binaryName = name.indexOf('/') >= 0 ? name.replace('/', '.') : name;

            try {
                return Class.forName(binaryName, false, loader);
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private GameClassLoaderResolver() {
    }
}
