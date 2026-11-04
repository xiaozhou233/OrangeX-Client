package cn.xiaozhou233.orangex.wrapper.api;

/**
 * Cross-version view of {@code net.minecraft.client.Minecraft}.
 *
 * <p>Only the native client instance is exposed for now. More operations will be
 * added incrementally as they are needed.</p>
 */
public interface Minecraft {

    /**
     * Returns the native {@code net.minecraft.client.Minecraft} instance.
     *
     * <p>Feature code should treat the result as opaque; version-specific
     * handling belongs to the implementation.</p>
     */
    Object getMinecraft();
}
