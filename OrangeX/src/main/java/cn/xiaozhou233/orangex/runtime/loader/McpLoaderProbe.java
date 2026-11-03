package cn.xiaozhou233.orangex.runtime.loader;

import cn.xiaozhou233.orangex.runtime.probe.ClassProbe;

/**
 * MCP is a deobfuscated development environment: it exposes real class names
 * but no mod loader.
 */
public final class McpLoaderProbe implements LoaderProbe {

    @Override
    public LoaderType type() {
        return LoaderType.MCP;
    }

    @Override
    public boolean matches(ClassLoader loader) {
        Class<?> minecraftClass = ClassProbe.find(loader, "net.minecraft.client.Minecraft");
        if (minecraftClass == null) {
            return false;
        }

        try {
            minecraftClass.getDeclaredMethod("getMinecraft");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
