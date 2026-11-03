package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Ordered readers for Forge/NeoForge version metadata.
 *
 * <p>The first source that returns a known id wins. Every source is fail-safe:
 * missing classes, fields or methods simply fall through to the next one.</p>
 */
public final class VersionIdSources {

    private static final List<VersionIdSource> SOURCES =
            Collections.unmodifiableList(Arrays.<VersionIdSource>asList(
                    VersionIdSources::legacyForgeMinorVersion,
                    VersionIdSources::fmlLoaderForgeVersion,
                    VersionIdSources::forgeVersionField,
                    VersionIdSources::fmlVersionInfoForgeVersion
            ));

    public static int resolve(ClassLoader loader) {
        for (VersionIdSource source : SOURCES) {
            int versionId = source.resolve(loader);
            if (versionId != MinecraftVersion.UNKNOWN_ID) {
                return versionId;
            }
        }
        return MinecraftVersion.UNKNOWN_ID;
    }

    /**
     * Legacy Forge already exposes an internal-style version id.
     */
    private static int legacyForgeMinorVersion(ClassLoader loader) {
        try {
            Object minorVersion = ReflectUtils.readStaticField(loader,
                    "net.minecraftforge.common.ForgeVersion", "minorVersion");
            if (minorVersion instanceof Number) {
                return ((Number) minorVersion).intValue();
            }
            return MinecraftVersion.UNKNOWN_ID;
        } catch (Throwable ignored) {
            return MinecraftVersion.UNKNOWN_ID;
        }
    }

    /**
     * Modern Forge / NeoForge: {@code FMLLoader.forgeVersion}.
     */
    private static int fmlLoaderForgeVersion(ClassLoader loader) {
        try {
            Object forgeVersion = ReflectUtils.readStaticField(loader,
                    "net.minecraftforge.fml.loading.FMLLoader", "forgeVersion");
            return parse(forgeVersion);
        } catch (Throwable ignored) {
            return MinecraftVersion.UNKNOWN_ID;
        }
    }

    /**
     * Newer Forge: {@code ForgeVersion.forgeVersion}.
     */
    private static int forgeVersionField(ClassLoader loader) {
        try {
            Object forgeVersion = ReflectUtils.readStaticField(loader,
                    "net.minecraftforge.common.ForgeVersion", "forgeVersion");
            return parse(forgeVersion);
        } catch (Throwable ignored) {
            return MinecraftVersion.UNKNOWN_ID;
        }
    }

    /**
     * Newest Forge: {@code FMLLoader.versionInfo().forgeVersion()}.
     */
    private static int fmlVersionInfoForgeVersion(ClassLoader loader) {
        try {
            Class<?> fmlLoader = ReflectUtils.loadClass(loader,
                    "net.minecraftforge.fml.loading.FMLLoader");
            Object versionInfo = fmlLoader.getMethod("versionInfo").invoke(null);
            Object forgeVersion = versionInfo.getClass().getMethod("forgeVersion").invoke(versionInfo);
            return parse(forgeVersion);
        } catch (Throwable ignored) {
            return MinecraftVersion.UNKNOWN_ID;
        }
    }

    private static int parse(Object forgeVersion) {
        int parsed = ForgeVersionParser.parse(forgeVersion);
        return parsed >= 0 ? parsed : MinecraftVersion.UNKNOWN_ID;
    }

    private VersionIdSources() {
    }
}
