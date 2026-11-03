package cn.xiaozhou233.orangex.runtime.probe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Matches a {@link MinecraftSignature} against a class loader.
 */
public final class MinecraftProbe {

    private final MinecraftSignature signature;

    public MinecraftProbe(MinecraftSignature signature) {
        this.signature = signature;
    }

    /**
     * The game class and every anchor must be defined by the same class loader,
     * otherwise classes inherited from a parent loader can produce false
     * positives.
     */
    public boolean matches(ClassLoader loader) {
        Class<?> minecraftClass = ClassProbe.find(loader, signature.minecraftClass());
        if (minecraftClass == null) {
            return false;
        }

        ClassLoader definingLoader = minecraftClass.getClassLoader();
        if (definingLoader == null) {
            return false;
        }

        for (String anchor : signature.anchors()) {
            Class<?> anchorClass = ClassProbe.find(definingLoader, anchor);
            if (anchorClass == null || anchorClass.getClassLoader() != definingLoader) {
                return false;
            }
        }

        return matchesStructure(minecraftClass);
    }

    private boolean matchesStructure(Class<?> minecraftClass) {
        try {
            if (!matchesGetter(minecraftClass)) {
                return false;
            }
            return matchesInstanceField(minecraftClass);
        } catch (ReflectiveOperationException | SecurityException | LinkageError e) {
            return false;
        }
    }

    private boolean matchesGetter(Class<?> minecraftClass) throws NoSuchMethodException {
        String getterName = signature.instanceGetter();
        if (getterName == null || getterName.isEmpty()) {
            return true;
        }

        Method getter = minecraftClass.getDeclaredMethod(getterName);
        return Modifier.isStatic(getter.getModifiers())
                && getter.getReturnType() == minecraftClass;
    }

    private boolean matchesInstanceField(Class<?> minecraftClass) throws NoSuchFieldException {
        String fieldName = signature.instanceField();
        if (fieldName == null || fieldName.isEmpty()) {
            // Some Minecraft versions do not expose a singleton field.
            return true;
        }

        Field instance = minecraftClass.getDeclaredField(fieldName);
        return Modifier.isStatic(instance.getModifiers())
                && instance.getType() == minecraftClass;
    }
}
