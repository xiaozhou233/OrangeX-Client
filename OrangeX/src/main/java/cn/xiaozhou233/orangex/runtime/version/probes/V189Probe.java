package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionProbe;
public final class V189Probe implements VersionProbe {

    @Override
    public MinecraftVersion getVersion() {
        return MinecraftVersion.V1_8_9;
    }

    @Override
    public int score(ClassLoader loader) {
        int score = 0;
        // MCP, Obfuscated, Srg
        if (ClassProbe.exists(loader, "net.minecraft.client.Minecraft", "ave")) {
            score += 10;
        }
        if (ClassProbe.exists(loader, "net.minecraft.client.gui.GuiScreen", "axu")) {
            score += 10;
        }
        if (ClassProbe.exists(loader, "net.minecraft.client.renderer.entity.RenderManager", "bnn")) {
            score += 10;
        }
        if (ClassProbe.exists(loader, "net.minecraft.client.renderer.RenderGlobal", "buy")) {
            score += 10;
        }
        return score;
    }
}
