package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.utils.ForgeUtils;
import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;
import cn.xiaozhou233.orangex.runtime.version.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionProbe;

public final class V12111Probe implements VersionProbe {

    @Override
    public MinecraftVersion getVersion() {
        return MinecraftVersion.V1_21_11;
    }

    @Override
    public int score(ClassLoader loader) {
        int score = 0;
        // Obfuscated
        MinecraftProbe.ProbeResult probeObf = new MinecraftProbe(
                "gfj",
                "V",
                "A",
                "hif", "hnh", "hne", "dwo")
                .probe(loader);
        score+= probeObf.getScore();
        probeObf.getMatched().forEach(System.out::println);

        // Forge
        try {
            Class<?> fmlLoader = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            Object versionInfo = fmlLoader.getMethod("versionInfo").invoke(null);
            Object forgeVersion = versionInfo.getClass().getMethod("forgeVersion").invoke(versionInfo);
            int parsedVersion = ForgeUtils.parseForgeVersion(forgeVersion);
            if (parsedVersion == 61) {
                score += 100;
                System.out.println("[DEBUG] Forge Version: " + parsedVersion + ", Version: 1.21.11");
            }
        } catch (Exception ignore) {
            System.out.println("[DEBUG] Forge Version Probe Failed");
        }

        return score;
    }


}
