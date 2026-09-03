package cn.xiaozhou233.orangex.mapping;

import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

public class MappedClass {
    @Getter
    private final String sourceName;
    private Class<?> targetClass;
    private boolean mapped;

    public MappedClass(String sourceName) {
        this.sourceName = sourceName;
        this.mapped = false;
    }

    public Class<?> getTargetClass(ClassLoader... preferredLoaders) {
        if(mapped) {
            return targetClass;
        }

        targetClass = findClass(sourceName, preferredLoaders);
        mapped = true;

        return targetClass;
    }

    private static Class<?> findClass(String internalName, ClassLoader... preferredLoaders) {
        String binaryName = internalName.replace('/', '.');
        Set<ClassLoader> loaders = candidateLoaders(preferredLoaders);
        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(binaryName, false, loader);
            } catch (ClassNotFoundException | LinkageError ignored) {
            }
        }
        return null;
    }

    private static Set<ClassLoader> candidateLoaders(ClassLoader... preferredLoaders) {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        if (preferredLoaders != null) {
            for (ClassLoader loader : preferredLoaders) {
                if (loader != null) loaders.add(loader);
            }
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) loaders.add(context);
        ClassLoader self = MappedClass.class.getClassLoader();
        if (self != null) loaders.add(self);
        try {
            ClassLoader system = ClassLoader.getSystemClassLoader();
            if (system != null) loaders.add(system);
        } catch (SecurityException ignored) {
        }
        return loaders;
    }
}
