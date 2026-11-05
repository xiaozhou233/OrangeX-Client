package cn.xiaozhou233.orangex.render.internal;

import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.render.internal.v1_8_9.V1_8_9FontBackend;
import cn.xiaozhou233.orangex.render.internal.v26_2.V26_2FontBackend;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;

/**
 * Selects the font backend for the detected Minecraft version.
 */
public final class FontBackendFactory {

    public static FontBackend create(RuntimeInfo runtimeInfo,
                                     MappingResolver mappingResolver,
                                     Object minecraft) {
        switch (runtimeInfo.getMinecraftVersion()) {
            case V1_8_9:
                return new V1_8_9FontBackend(runtimeInfo, mappingResolver, minecraft);
            case V26_2:
                return new V26_2FontBackend(runtimeInfo);
            default:
                throw new IllegalStateException(
                        "No font backend for " + runtimeInfo.getMinecraftVersion());
        }
    }

    private FontBackendFactory() {
    }
}
