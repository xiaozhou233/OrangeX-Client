package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Immutable result of runtime detection.
 */
@Getter
@AllArgsConstructor
public final class RuntimeInfo {

    private final ClassLoader minecraftClassLoader;
    private final LoaderType loaderType;
    private final MinecraftVersion minecraftVersion;

    /**
     * OrangeX internal version id. Kept even when {@link #minecraftVersion} is
     * unknown, so diagnostics can still show what was detected.
     */
    private final int versionId;

    public boolean isKnown() {
        return !minecraftVersion.isUnknown();
    }
}
