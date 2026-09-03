package cn.xiaozhou233.orangex.mapping;

import lombok.Getter;

import java.lang.reflect.Method;

public class MappedMethod {
    private final MappedClass owner;
    @Getter
    private final String sourceName;
    private final Class<?> returnType;
    private final Class<?>[] paramTypes;
    private Method targetMethod;
    private boolean mapped;

    public MappedMethod(MappedClass owner, String sourceName, Class<?> returnType, Class<?>... paramTypes) {
        this.owner = owner;
        this.sourceName = sourceName;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    public Method getTargetMethod(MappingRegistry registry) {
        if(mapped) {
            return targetMethod;
        }

        Class<?> ownerClass = owner.getTargetClass();
        if (ownerClass == null) return null;

        String runtimeName = registry.lookupMethod(owner.getSourceName(), sourceName, paramTypes);
        if (runtimeName == null) runtimeName = sourceName;

        try {
            targetMethod = findMethod(ownerClass, runtimeName, paramTypes);
            if (targetMethod != null) {
                targetMethod.setAccessible(true);
            }
        } catch (Exception ignored) {
        }
        return targetMethod;
    }

    public Object invoke(Object instance, Object... args) {
        if (targetMethod == null) return null;
        try {
            return targetMethod.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method " + sourceName, e);
        }
    }

    public void invokeVoid(Object instance, Object... args) {
        if (targetMethod == null) return;
        try {
            targetMethod.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke void method " + sourceName, e);
        }
    }

    public boolean invokeBoolean(Object instance, Object... args) {
        if (targetMethod == null) return false;
        try {
            return (boolean) targetMethod.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke boolean method " + sourceName, e);
        }
    }

    public int invokeInt(Object instance, Object... args) {
        if (targetMethod == null) return 0;
        try {
            return (int) targetMethod.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke int method " + sourceName, e);
        }
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, paramTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null) {
                return findMethod(clazz.getSuperclass(), name, paramTypes);
            }
            return null;
        }
    }
}
