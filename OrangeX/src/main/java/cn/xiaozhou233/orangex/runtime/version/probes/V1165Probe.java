package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.utils.ForgeUtils;
import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;
import cn.xiaozhou233.orangex.runtime.version.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionProbe;

public final class V1165Probe implements VersionProbe {

    @Override
    public MinecraftVersion getVersion() {
        return MinecraftVersion.V1_16_5;
    }

    @Override
    public int score(ClassLoader loader) {
        int score = 0;
        // Obfuscated
        MinecraftProbe.ProbeResult probeObf = new MinecraftProbe(
                "djz",
                "C",
                "F",
                "dwt", "dzm", "dzj", "brx")
                .probe(loader);
        score+= probeObf.getScore();
        probeObf.getMatched().forEach(System.out::println);

        // Forge
        try {
            Object forgeVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.fml.loading.FMLLoader", "forgeVersion");
            int parsedVersion = ForgeUtils.parseForgeVersion(forgeVersion);
            if (parsedVersion == 36) {
                score += 100;
                System.out.println("[DEBUG] Forge Version: " + parsedVersion + ", Version: 1.16.5");
            }
        } catch (Exception ignore) {
            System.out.println("[DEBUG] Forge Version Probe Failed");
        }

        return score;
    }


}
