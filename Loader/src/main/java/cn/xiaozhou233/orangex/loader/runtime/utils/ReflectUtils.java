package cn.xiaozhou233.orangex.loader.runtime.utils;

import java.lang.reflect.Field;

public class ReflectUtils {
    public static Object readStaticField(String className, String fieldName) throws Exception {
        return readStaticField(null, className, fieldName);
    }

    /**
     * @param loader the class loader used to resolve the owner class; when
     *               {@code null} the caller's class loader is used
     */
    public static Object readStaticField(ClassLoader loader, String className, String fieldName) throws Exception {
        Class<?> owner = loadClass(loader, className);
        Field field;
        try {
            field = owner.getField(fieldName);
        }
        catch (NoSuchFieldException ignored) {
            field = owner.getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        return field.get(null);
    }

    /**
     * @param loader the class loader used to resolve the class; when
     *               {@code null} the caller's class loader is used
     */
    public static Class<?> loadClass(ClassLoader loader, String className) throws ClassNotFoundException {
        return loader == null
                ? Class.forName(className)
                : Class.forName(className, false, loader);
    }
}
