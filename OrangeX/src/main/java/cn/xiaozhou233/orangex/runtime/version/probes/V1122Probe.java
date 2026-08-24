package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;
import cn.xiaozhou233.orangex.runtime.version.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionProbe;

public final class V1122Probe implements VersionProbe {

    @Override
    public MinecraftVersion getVersion() {
        return MinecraftVersion.V1_12_2;
    }

    @Override
    public int score(ClassLoader loader) {
        int score = 0;
        // Obfuscated
        MinecraftProbe.ProbeResult probeObf = new MinecraftProbe(
                "bib",
                "z",
                "R",
                "bud", "amu")
                .probe(loader);
        score+= probeObf.getScore();
        probeObf.getMatched().forEach(System.out::println);

        // Forge
        try {
            Object minorVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.common.ForgeVersion", "minorVersion");
            if (minorVersion instanceof Number) {
                int forgeMinorVersion = ((Number) minorVersion).intValue();
                if (forgeMinorVersion == 23) {
                    score += 100;
                    System.out.println("[DEBUG] Forge Version Probe Success! Version: 1.12.2");
                }
            }
        } catch (Exception ignore) {
            System.out.println("[DEBUG] Forge Version Probe Failed");
        }

        return score;
    }
}
