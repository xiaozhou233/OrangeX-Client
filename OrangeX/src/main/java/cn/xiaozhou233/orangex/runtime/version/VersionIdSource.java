package cn.xiaozhou233.orangex.runtime.version;

/**
 * Reads an internal Minecraft version id from loader-provided metadata.
 *
 * <p>Sources are intentionally small and independent so new loader versions can
 * be supported by adding one entry to {@link VersionIdSources}.</p>
 */
@FunctionalInterface
public interface VersionIdSource {

    /**
     * @return an internal version id, or {@link MinecraftVersion#UNKNOWN_ID}
     *         when this source has no answer
     */
    int resolve(ClassLoader loader);
}
