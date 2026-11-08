package cn.xiaozhou233.orangex.wrapper.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Reflective lookup helpers shared by the version-specific wrappers.
 *
 * <p>This class intentionally knows nothing about Minecraft: version packages
 * pass the resolved member names in. Lookups walk the class hierarchy because
 * Minecraft frequently declares a member on a base class and stores an
 * instance of a subclass.</p>
 */
public final class ReflectionSupport {

    public static Field findField(Class<?> type, String name) {
        if (type == null || name == null) {
            return null;
        }

        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Keep walking the superclass chain.
            }
        }

        return null;
    }

    public static Field requireField(Class<?> type, String name, String ownerName) {
        Field field = findField(type, name);
        if (field == null) {
            throw new IllegalStateException(
                    "Cannot find field '" + name + "' on " + ownerName);
        }
        return field;
    }

    public static Method findMethod(Class<?> type,
                                    String name,
                                    Class<?> returnType,
                                    Class<?>... parameterTypes) {
        if (type == null || name == null) {
            return null;
        }

        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            Method[] methods = current.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.getName().equals(name)
                        || Modifier.isStatic(method.getModifiers())
                        || !sameParameters(method, parameterTypes)) {
                    continue;
                }
                if (returnType != null && !returnType.equals(method.getReturnType())) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    public static Method requireMethod(Class<?> type,
                                       String name,
                                       Class<?> returnType,
                                       String ownerName,
                                       Class<?>... parameterTypes) {
        Method method = findMethod(type, name, returnType, parameterTypes);
        if (method == null) {
            throw new IllegalStateException(
                    "Cannot find method '" + name + "' on " + ownerName);
        }
        return method;
    }

    public static Object get(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read field '" + field.getName() + "'", e);
        }
    }

    public static double getDouble(Field field, Object target) {
        try {
            return field.getDouble(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read field '" + field.getName() + "'", e);
        }
    }

    public static float getFloat(Field field, Object target) {
        try {
            return field.getFloat(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read field '" + field.getName() + "'", e);
        }
    }

    public static int getInt(Field field, Object target) {
        try {
            return field.getInt(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read field '" + field.getName() + "'", e);
        }
    }

    public static boolean getBoolean(Field field, Object target) {
        try {
            return field.getBoolean(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot read field '" + field.getName() + "'", e);
        }
    }

    public static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot invoke method '" + method.getName() + "'", e);
        }
    }

    private static boolean sameParameters(Method method, Class<?>[] parameterTypes) {
        Class<?>[] actual = method.getParameterTypes();
        if (actual.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].equals(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private ReflectionSupport() {
    }
}
