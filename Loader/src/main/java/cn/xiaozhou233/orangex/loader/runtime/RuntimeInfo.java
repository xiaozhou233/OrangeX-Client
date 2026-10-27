package cn.xiaozhou233.orangex.loader.runtime;

import cn.xiaozhou233.orangex.loader.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.loader.runtime.version.MinecraftVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RuntimeInfo {
    private final ClassLoader minecraftClassLoader;
    private final LoaderType loaderType;
    private final MinecraftVersion minecraftVersion;
}
