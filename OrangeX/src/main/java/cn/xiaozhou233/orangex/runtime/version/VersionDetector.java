package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.loader.LoaderType;

public final class VersionDetector {

    public static MinecraftVersion detect(ClassLoader loader, LoaderType loaderType) {
        return MinecraftVersion.UNKNOWN;
    }
}