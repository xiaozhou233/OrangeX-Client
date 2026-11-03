package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.loader.LoaderType;

/**
 * Resolves the internal Minecraft version id.
 *
 * <p>Detection order, from most to least reliable:</p>
 * <ol>
 *   <li>Loader-provided Forge/NeoForge metadata.</li>
 *   <li>The 26.x {@code version.json} family.</li>
 *   <li>Structural class signatures.</li>
 * </ol>
 */
public final class VersionDetector {

    public static int detectId(ClassLoader loader, LoaderType loaderType) {
        int versionId = VersionIdSources.resolve(loader);
        if (versionId != MinecraftVersion.UNKNOWN_ID) {
            return versionId;
        }

        versionId = ModernVersionDetector.detect(loader);
        if (versionId != MinecraftVersion.UNKNOWN_ID) {
            return versionId;
        }

        return VersionSignatureRegistry.detect(loader, loaderType).getId();
    }

    private VersionDetector() {
    }
}
