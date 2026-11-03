package cn.xiaozhou233.orangex.runtime.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Identifies the mod loader by running an ordered list of {@link LoaderProbe}s.
 *
 * <p>To support a new mod loader, add a probe to {@link #PROBES} and a matching
 * value to {@link LoaderType}.</p>
 */
public final class LoaderDetector {

    /**
     * Order matters:
     * <ol>
     *   <li>NeoForge before Forge, because NeoForge is a Forge fork.</li>
     *   <li>MCP before VANILLA, because MCP is a deobfuscated environment.</li>
     * </ol>
     */
    private static final List<LoaderProbe> PROBES = Collections.unmodifiableList(Arrays.<LoaderProbe>asList(
            new ClassPresenceLoaderProbe(LoaderType.NEOFORGE,
                    "net.neoforged.fml.ModList",
                    "net.neoforged.fml.loading.FMLLoader"),
            new ClassPresenceLoaderProbe(LoaderType.FORGE,
                    "net.minecraftforge.fml.common.Loader",
                    "net.minecraftforge.fml.loading.FMLLoader"),
            new ClassPresenceLoaderProbe(LoaderType.FABRIC,
                    "net.fabricmc.loader.api.FabricLoader"),
            new McpLoaderProbe()
    ));

    public static LoaderType detect(ClassLoader loader) {
        if (loader == null) {
            return LoaderType.UNKNOWN;
        }

        for (LoaderProbe probe : PROBES) {
            if (probe.matches(loader)) {
                return probe.type();
            }
        }

        return LoaderType.VANILLA;
    }

    private LoaderDetector() {
    }
}
