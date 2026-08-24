package cn.xiaozhou233.orangex.runtime.version;

public interface VersionProbe {

    MinecraftVersion getVersion();

    int score(ClassLoader loader);
}