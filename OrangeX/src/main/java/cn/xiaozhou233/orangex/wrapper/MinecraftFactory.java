package cn.xiaozhou233.orangex.wrapper;

import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
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

    private MinecraftFactory() {
    }
}
