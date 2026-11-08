package cn.xiaozhou233.orangex.wrapper.internal.v26_2;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import cn.xiaozhou233.orangex.wrapper.api.WorldView;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperContext;
import cn.xiaozhou233.orangex.wrapper.internal.WrapperProvider;

public final class V26_2WrapperProvider implements WrapperProvider {

    @Override
    public Minecraft createMinecraft(WrapperContext context) {
        return new V26_2Minecraft(
                context.getRuntimeInfo(),
                context.getMappingResolver());
    }

    @Override
    public WorldView createWorld(WrapperContext context, Minecraft minecraft) {
        return new V26_2WorldView(minecraft);
    }
}