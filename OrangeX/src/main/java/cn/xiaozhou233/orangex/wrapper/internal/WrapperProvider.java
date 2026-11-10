package cn.xiaozhou233.orangex.wrapper.internal;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;

/**
 * Creates the wrappers for one Minecraft version.
 *
 * <p>A provider is stateless: all runtime data comes from the supplied
 * {@link WrapperContext}, so the same instance can serve the whole process.</p>
 *
 * <p>When new wrapper types are added (player, world, ...), add one factory
 * method per type here and implement it in every provider. Providers that do
 * not support a type should throw {@link UnsupportedOperationException}.</p>
 */
public interface WrapperProvider {

    /**
     * Creates the wrapper for {@code net.minecraft.client.Minecraft}.
     */
    Minecraft createMinecraft(WrapperContext context);

    /**
     * Creates the world/camera view backed by the given client instance.
     */
    WorldView createWorld(WrapperContext context, Minecraft minecraft);

    /**
     * Creates the local-player view backed by the given client instance.
     */
    Player createPlayer(WrapperContext context, Minecraft minecraft);
}
