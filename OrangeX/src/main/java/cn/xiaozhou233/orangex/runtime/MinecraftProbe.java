package cn.xiaozhou233.orangex.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MinecraftProbe {
    private final String minecraftClass;
    private final String minecraftGetter;
    private final String minecraftInstanceField;
    private final String[] minecraftAnchors;

    public MinecraftProbe(String minecraftClass, String minecraftGetter, String minecraftInstanceField, String... minecraftAnchors) {
        this.minecraftClass = minecraftClass;
        this.minecraftGetter = minecraftGetter;
        this.minecraftInstanceField = minecraftInstanceField;
        this.minecraftAnchors = minecraftAnchors;
    }

    /**
     * Probes the given loader. The game class and every anchor must be defined by
     * the same class loader, otherwise classes inherited from a parent loader can
     * produce false positives.
     */
    public boolean probe(ClassLoader loader) {
        // Class Check
        Class<?> mcClass = ClassProbe.find(loader, minecraftClass);
        if (mcClass == null) {
            return false;
        }

        ClassLoader definingLoader = mcClass.getClassLoader();
        if (definingLoader == null) {
            return false;
        }

        // Anchor Check
        for (String anchor : minecraftAnchors) {
            if (anchor == null || anchor.isEmpty()) {
                continue;
            }

            Class<?> anchorClass = ClassProbe.find(definingLoader, anchor);
            if (anchorClass == null || anchorClass.getClassLoader() != definingLoader) {
                return false;
            }
        }

        return matchesStructure(mcClass);
    }

    private boolean matchesStructure(Class<?> mcClass) {
        try {
            // Method(Getter) Check
            if (minecraftGetter != null && !minecraftGetter.isEmpty()) {
                Method getter = mcClass.getDeclaredMethod(minecraftGetter);
                boolean getterOk = Modifier.isStatic(getter.getModifiers())
                        && getter.getReturnType() == mcClass;
                if (!getterOk) {
                    return false;
                }
            }

            // Instance Field Check (Tips: Some version of minecraft does not have this field)
            if (minecraftInstanceField == null || minecraftInstanceField.isEmpty()) {
                return true;
            }

            Field instance = mcClass.getDeclaredField(minecraftInstanceField);
            return Modifier.isStatic(instance.getModifiers())
                    && instance.getType() == mcClass;
        } catch (ReflectiveOperationException | SecurityException | LinkageError e) {
            return false;
        }
    }
}
