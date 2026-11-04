package cn.xiaozhou233.orangex.wrapper.internal;

import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.internal.v1_8_9.V1_8_9WrapperProvider;

import java.util.EnumMap;
import java.util.Map;

/**
 * Maps a Minecraft version to its {@link WrapperProvider}.
 *
 * <p><strong>To add a version:</strong> implement {@link WrapperProvider} in a
 * new {@code internal.vX_Y_Z} package and register it in the static block
 * below. No switch statements or other call sites need to change.</p>
 */
public final class WrapperRegistry {

    private static final Map<MinecraftVersion, WrapperProvider> PROVIDERS =
            new EnumMap<MinecraftVersion, WrapperProvider>(MinecraftVersion.class);

    static {
        register(MinecraftVersion.V1_8_9, new V1_8_9WrapperProvider());
    }

    public static void register(MinecraftVersion version, WrapperProvider provider) {
        if (version == null) {
            throw new IllegalArgumentException("version must not be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }

        WrapperProvider previous = PROVIDERS.put(version, provider);
        if (previous != null) {
            throw new IllegalStateException(
                    "Wrapper provider already registered for " + version);
        }
    }

    public static boolean supports(MinecraftVersion version) {
        return version != null && PROVIDERS.containsKey(version);
    }

    public static Minecraft createMinecraft(WrapperContext context) {
        MinecraftVersion version = context.getRuntimeInfo().getMinecraftVersion();
        WrapperProvider provider = PROVIDERS.get(version);

        if (provider == null) {
            throw new IllegalStateException(
                    "Unsupported Minecraft version: " + version);
        }

        return provider.createMinecraft(context);
    }

    private WrapperRegistry() {
    }
}
