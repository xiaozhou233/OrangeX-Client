package cn.xiaozhou233.orangex.wrapper;

import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.Player;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperContext;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperRegistry;

/**
 * Public entry point of the cross-version wrapper.
 *
 * <p>Feature code should only depend on this class and the {@code api} package.
 * Version selection is delegated to {@link WrapperRegistry}.</p>
 */
public final class MinecraftFactory {

    public static Minecraft create(RuntimeInfo runtimeInfo, MappingResolver mappingResolver) {
        return WrapperRegistry.createMinecraft(
                new WrapperContext(runtimeInfo, mappingResolver));
    }

    /**
     * Creates the world/camera view that goes with an already created
     * {@link Minecraft} wrapper.
     */
    public static WorldView createWorld(RuntimeInfo runtimeInfo,
                                        MappingResolver mappingResolver,
                                        Minecraft minecraft) {
        return WrapperRegistry.createWorld(
                new WrapperContext(runtimeInfo, mappingResolver), minecraft);
    }

    /**
     * Creates the local-player view that goes with an already created
     * {@link Minecraft} wrapper.
     */
    public static Player createPlayer(RuntimeInfo runtimeInfo,
                                      MappingResolver mappingResolver,
                                      Minecraft minecraft) {
        return WrapperRegistry.createPlayer(
                new WrapperContext(runtimeInfo, mappingResolver), minecraft);
    }

    private MinecraftFactory() {
    }
}
