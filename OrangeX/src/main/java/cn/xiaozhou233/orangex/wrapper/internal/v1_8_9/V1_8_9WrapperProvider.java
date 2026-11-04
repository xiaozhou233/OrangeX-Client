package cn.xiaozhou233.orangex.wrapper.internal.v1_8_9;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperContext;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperProvider;

/**
 * Wrapper provider for Minecraft 1.8.9 (vanilla, MCP and Forge runtimes).
 */
public final class V1_8_9WrapperProvider implements WrapperProvider {

    @Override
    public Minecraft createMinecraft(WrapperContext context) {
        return new V1_8_9Minecraft(
                context.getRuntimeInfo(),
                context.getMappingResolver());
    }
}
