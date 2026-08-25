package cn.xiaozhou233.orangex.runtime.version.probes;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import cn.xiaozhou233.orangex.runtime.utils.ForgeUtils;
import cn.xiaozhou233.orangex.runtime.utils.MinecraftUtils;
import cn.xiaozhou233.orangex.runtime.version.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;
import cn.xiaozhou233.orangex.runtime.version.VersionProbe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class V262Probe implements VersionProbe {

    @Override
    public MinecraftVersion getVersion() {
        return MinecraftVersion.V1_21_11;
    }

    @Override
    public int score(ClassLoader loader) {
        int score = 0;
        // Vanilla (Not obfuscated)
        MinecraftProbe.ProbeResult probeObf = new MinecraftProbe(
                "net.minecraft.client.Minecraft",
                "getInstance",
                "instance",
                "net.minecraft.client.multiplayer.ClientLevel", "net.minecraft.client.player.LocalPlayer", "net.minecraft.client.player.AbstractClientPlayer", "net.minecraft.world.level.Level")
                .probe(loader);
        score+= probeObf.getScore();
        probeObf.getMatched().forEach(System.out::println);

        Class<?> mcClass = ClassProbe.find(loader, "net.minecraft.client.Minecraft");
        if (mcClass != null) {
            int protocol = MinecraftUtils.matchesVersionMetadata(mcClass);
            if (protocol == 110) {
                score += 100;
                System.out.println("[DEBUG] Protocol Version: " + protocol + ", Version: 26.2");
            }
        }
        return score;
    }
}
