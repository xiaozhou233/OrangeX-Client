package cn.xiaozhou233.orangex.loader.runtime;

public class ClassProbe {
    public static boolean exists(ClassLoader loader, String... names) {
        return find(loader, names) != null;
    }

    public static Class<?> find(ClassLoader loader, String... names) {
        if (loader == null)
            return null;

        if (names == null)
            return null;

        for (String name : names) {
            // Check for empty names
            if (name == null || name.isEmpty()) {
                continue;
            }

            String binaryName = toBinaryName(name);

            // Check for class existence
            try {
                return Class.forName(binaryName, false, loader);
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    /**
     * Accepts both binary names ({@code net.minecraft.client.Minecraft}) and
     * internal names ({@code net/minecraft/client/Minecraft}).
     */
    private static String toBinaryName(String name) {
        return name.indexOf('/') >= 0 ? name.replace('/', '.') : name;
    }
}
