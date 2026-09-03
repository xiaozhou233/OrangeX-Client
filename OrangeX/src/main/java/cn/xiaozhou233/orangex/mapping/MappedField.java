package cn.xiaozhou233.orangex.mapping;

import lombok.Getter;

import java.lang.reflect.Field;

public class MappedField {
    private final MappedClass owner;
    @Getter
    private final String sourceName;
    private final Class<?> fieldType;
    private Field targetField;
    private boolean mapped;

    public MappedField(MappedClass owner, String sourceName, Class<?> fieldType) {
        this.owner = owner;
        this.sourceName = sourceName;
        this.fieldType = fieldType;
    }

    public Field getTargetField(MappingRegistry registry) {
        if (mapped) {
            return targetField;
        }

        Class<?> ownerClass = owner.getTargetClass();
        if (ownerClass == null) return null;

        String runtimeName = registry.lookupField(owner.getSourceName(), sourceName);
        if (runtimeName == null) runtimeName = sourceName;

        try {
            targetField = findField(ownerClass, runtimeName);
        } catch (Exception ignored) {
        }
        return targetField;
    }

    public Object get(Object instance) {
        if (targetField == null) return null;
        try {
            return targetField.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field " + sourceName, e);
        }
    }

    public void set(Object instance, Object value) {
        if (targetField == null) return;
        try {
            targetField.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + sourceName, e);
        }
    }

    public int getInt(Object instance) {
        if (targetField == null) return 0;
        try {
            return targetField.getInt(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get int field " + sourceName, e);
        }
    }

    public void setInt(Object instance, int value) {
        if (targetField == null) return;
        try {
            targetField.setInt(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set int field " + sourceName, e);
        }
    }

    public boolean getBoolean(Object instance) {
        if (targetField == null) return false;
        try {
            return targetField.getBoolean(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get boolean field " + sourceName, e);
        }
    }

    public void setBoolean(Object instance, boolean value) {
        if (targetField == null) return;
        try {
            targetField.setBoolean(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set boolean field " + sourceName, e);
        }
    }

    public double getDouble(Object instance) {
        if (targetField == null) return 0;
        try {
            return targetField.getDouble(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get double field " + sourceName, e);
        }
    }

    public float getFloat(Object instance) {
        if (targetField == null) return 0;
        try {
            return targetField.getFloat(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get float field " + sourceName, e);
        }
    }

    public long getLong(Object instance) {
        if (targetField == null) return 0;
        try {
            return targetField.getLong(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get long field " + sourceName, e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            throw e;
        }
    }
}
