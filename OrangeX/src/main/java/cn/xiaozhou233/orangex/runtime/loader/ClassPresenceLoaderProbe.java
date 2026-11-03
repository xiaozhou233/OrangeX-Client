package cn.xiaozhou233.orangex.runtime.loader;

import cn.xiaozhou233.orangex.runtime.probe.ClassProbe;

/**
 * Matches a loader when at least one of its marker classes is present.
 */
public final class ClassPresenceLoaderProbe implements LoaderProbe {

    private final LoaderType type;
    private final String[] markers;

    public ClassPresenceLoaderProbe(LoaderType type, String... markers) {
        this.type = type;
        this.markers = markers.clone();
    }

    @Override
    public LoaderType type() {
        return type;
    }

    @Override
    public boolean matches(ClassLoader loader) {
        return ClassProbe.exists(loader, markers);
    }
}
