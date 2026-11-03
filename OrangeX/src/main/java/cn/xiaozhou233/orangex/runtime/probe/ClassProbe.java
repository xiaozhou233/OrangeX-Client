package cn.xiaozhou233.orangex.runtime.probe;

/**
 * Small reflection helper for checking whether classes are visible to a loader.
 *
 * <p>Accepts both binary names ({@code net.minecraft.client.Minecraft}) and
 * internal names ({@code net/minecraft/client/Minecraft}).</p>
 */
public final class ClassProbe {

    public static boolean exists(ClassLoader loader, String... names) {
        return find(loader, names) != null;
    }

    public static Class<?> find(ClassLoader loader, String... names) {
        if (loader == null || names == null) {
            return null;
        }

        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }

            try {
                return Class.forName(toBinaryName(name), false, loader);
            } catch (Throwable ignored) {
                // Missing classes are expected while probing.
            }
        }

        return null;
    }

    private static String toBinaryName(String name) {
        return name.indexOf('/') >= 0 ? name.replace('/', '.') : name;
    }

    private ClassProbe() {
    }
}
