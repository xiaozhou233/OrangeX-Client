package cn.xiaozhou233.orangex.runtime;

import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RuntimeInfo {
    private final ClassLoader minecraftClassLoader;
    private final LoaderType loaderType;
}
