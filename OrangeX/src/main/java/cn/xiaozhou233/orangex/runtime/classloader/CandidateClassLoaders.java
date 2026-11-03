package cn.xiaozhou233.orangex.runtime.classloader;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects every class loader that may define Minecraft classes.
 *
 * <p>The primary loader is tried first, so detection stays deterministic when
 * several loaders can see the same classes.</p>
 */
public final class CandidateClassLoaders {

    public static Set<ClassLoader> of(ClassLoader primary) {
        Set<ClassLoader> loaders = new LinkedHashSet<>();

        add(loaders, primary);
        add(loaders, Thread.currentThread().getContextClassLoader());
        add(loaders, CandidateClassLoaders.class.getClassLoader());

        try {
            add(loaders, ClassLoader.getSystemClassLoader());
        } catch (SecurityException ignored) {
            // Security manager may forbid system loader lookup.
        }

        return loaders;
    }

    private static void add(Set<ClassLoader> loaders, ClassLoader loader) {
        if (loader != null) {
            loaders.add(loader);
        }
    }

    private CandidateClassLoaders() {
    }
}
