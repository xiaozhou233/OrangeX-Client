package cn.xiaozhou233.orangex.runtime.utils;

import java.lang.reflect.Field;

public class ReflectUtils {
    public static Object readStaticField(String className, String fieldName) throws Exception {
        Class<?> owner = Class.forName(className);
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
}
