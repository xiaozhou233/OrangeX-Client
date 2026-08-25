package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;
import cn.xiaozhou233.orangex.runtime.version.MinecraftProbe;
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
        // Vanilla
        MinecraftProbe.ProbeResult probeObf = new MinecraftProbe(
                "ave",
                "A",
                "S",
                "bew", "adm")
                .probe(loader);
        score+= probeObf.getScore();
        probeObf.getMatched().forEach(System.out::println);

        // Forge
        try {
            Object minorVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.common.ForgeVersion", "minorVersion");
            if (minorVersion instanceof Number) {
                int forgeMinorVersion = ((Number) minorVersion).intValue();
                if (forgeMinorVersion == 15) {
                    score += 100;
                    System.out.println("[DEBUG] Forge Version Probe Success! Version: 1.8.9");
                }
            }
        } catch (Exception ignore) {
            System.out.println("[DEBUG] Forge Version Probe Failed");
        }

        // MCP
        MinecraftProbe.ProbeResult probeMCP = new MinecraftProbe("net.minecraft.client.Minecraft", "getMinecraft", "theMinecraft").probe(loader);
        score += probeMCP.getScore();
        probeMCP.getMatched().forEach(System.out::println);

        return score;
    }
}
