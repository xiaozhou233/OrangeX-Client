package cn.xiaozhou233.orangex.wrapper.internal;

import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;

import java.util.Objects;

/**
 * Shared services handed to every version provider.
 *
 * <p>Cross-version helpers belong here instead of being looked up through
 * singletons inside implementations. When a new service is needed (logger,
 * event bus, ...), add it here and expose a getter; provider signatures stay
 * stable.</p>
 */
public final class WrapperContext {

    private final RuntimeInfo runtimeInfo;
    private final MappingResolver mappingResolver;

    public WrapperContext(RuntimeInfo runtimeInfo, MappingResolver mappingResolver) {
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo, "runtimeInfo");
        this.mappingResolver = Objects.requireNonNull(mappingResolver, "mappingResolver");
    }

    public RuntimeInfo getRuntimeInfo() {
        return runtimeInfo;
    }

    public MappingResolver getMappingResolver() {
        return mappingResolver;
    }
}
