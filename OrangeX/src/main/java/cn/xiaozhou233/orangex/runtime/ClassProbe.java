package cn.xiaozhou233.orangex.runtime;

public class ClassProbe {
    public static boolean exists(ClassLoader loader, String... names) {
        if(loader == null)
            return false;

        if (names == null)
            return false;

        for (String name : names) {
            // Check for empty names
            if (name == null || name.isEmpty()) {
                continue;
            }

            // Check for class existence
            try {
                Class.forName(name, false, loader);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static Class<?> find(ClassLoader loader, String... names) {
        if(loader == null)
            return null;
        if (names == null)
            return null;

        for (String name : names) {
            // Check for empty names
            if (name == null || name.isEmpty()) {
                continue;
            }

            try {
                return Class.forName(name, false, loader);
            } catch (Throwable ignored) {
            }
        }

        return null;
    }
}
