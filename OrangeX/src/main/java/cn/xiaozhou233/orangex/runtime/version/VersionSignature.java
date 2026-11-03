package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.probe.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.probe.MinecraftSignature;

/**
 * One recognizable form of a Minecraft version.
 *
 * <p>A version can have several signatures (for example vanilla obfuscated
 * names, Fabric intermediary names and NeoForge Mojang mappings). Matching is
 * intentionally conservative: the registry only accepts a version when exactly
 * one version matches, so overlapping class names cannot produce a guess.</p>
 */
public final class VersionSignature {

    private final MinecraftVersion version;
    private final LoaderType requiredLoader;
    private final MinecraftProbe probe;

    public VersionSignature(MinecraftVersion version,
                            LoaderType requiredLoader,
                            MinecraftSignature signature) {
        this.version = version;
        this.requiredLoader = requiredLoader;
        this.probe = new MinecraftProbe(signature);
    }

    /**
     * A signature that is valid for any mod loader.
     */
    public static VersionSignature anyLoader(MinecraftVersion version,
                                             MinecraftSignature signature) {
        return new VersionSignature(version, null, signature);
    }

    /**
     * A signature that only applies to one mod loader (for example NeoForge or
     * Fabric mapping sets).
     */
    public static VersionSignature forLoader(LoaderType loader,
                                             MinecraftVersion version,
                                             MinecraftSignature signature) {
        return new VersionSignature(version, loader, signature);
    }

    public MinecraftVersion version() {
        return version;
    }

    public boolean matches(ClassLoader loader, LoaderType loaderType) {
        return (requiredLoader == null || requiredLoader == loaderType)
                && probe.matches(loader);
    }
}
