package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.classloader.CandidateClassLoaders;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.probe.MinecraftSignature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.xiaozhou233.orangex.runtime.loader.LoaderType.FABRIC;
import static cn.xiaozhou233.orangex.runtime.loader.LoaderType.NEOFORGE;
import static cn.xiaozhou233.orangex.runtime.version.MinecraftVersion.*;

/**
 * Registry of structural version signatures.
 *
 * <p><strong>To add a Minecraft version:</strong> add the enum value to
 * {@link MinecraftVersion} and one signature below. For a brand-new version
 * family (for example a new version.json scheme), also update
 * {@link ModernVersionDetector}.</p>
 *
 * <p>Matching is conservative: if two different versions match, the result is
 * {@link MinecraftVersion#UNKNOWN}. This prevents overlapping mapping sets
 * (such as adjacent NeoForge versions) from silently resolving to the wrong
 * version.</p>
 */
public final class VersionSignatureRegistry {

    private static final List<VersionSignature> SIGNATURES =
            Collections.unmodifiableList(Arrays.<VersionSignature>asList(
            // Vanilla, obfuscated client classes
            VersionSignature.anyLoader(V1_8_9, obf("ave", "A", "S", "bew", "adm")),
            VersionSignature.anyLoader(V1_12_2, obf("bib", "z", "R", "bud", "amu")),
            VersionSignature.anyLoader(V1_16_5_ACTUAL, obf("djz", "C", "F", "dwt", "dzm", "dzj", "brx")),
            VersionSignature.anyLoader(V1_20_1, obf("enn", "N", "F", "few", "fiy", "fiv", "cmm")),
            VersionSignature.anyLoader(V1_20_6, obf("ffh", "Q", "E", "fxx", "gcs", "gcp", "dca")),
            VersionSignature.anyLoader(V1_21_1, obf("fgo", "Q", "E", "fzf", "geb", "gdy", "dcw")),
            VersionSignature.anyLoader(V1_21_11, obf("gfj", "V", "A", "hif", "hnh", "hne", "dwo")),

            // MCP development environment (real 1.8.9 names, no mod loader)
            VersionSignature.anyLoader(V1_8_9, mojang(
                    "net.minecraft.client.Minecraft", "getMinecraft", "theMinecraft",
                    "net.minecraft.client.entity.EntityPlayerSP",
                    "net.minecraft.client.gui.GuiIngame",
                    "net.minecraft.client.renderer.EntityRenderer",
                    "net.minecraft.client.network.NetHandlerPlayClient")),

            // NeoForge uses Mojang mappings
            VersionSignature.forLoader(NEOFORGE, V1_20_1, mojang(
                    "net/minecraft/client/Minecraft", "getInstance", "instance",
                    "net/minecraft/client/multiplayer/ClientLevel",
                    "net/minecraft/client/player/LocalPlayer",
                    "net/minecraft/client/player/AbstractClientPlayer",
                    "net/minecraft/world/level/Level")),
            VersionSignature.forLoader(NEOFORGE, V1_21_1, mojang(
                    "net/minecraft/client/Minecraft", "getInstance", "instance",
                    "net/minecraft/client/multiplayer/ClientLevel",
                    "net/minecraft/client/player/LocalPlayer",
                    "net/minecraft/client/player/AbstractClientPlayer",
                    "net/minecraft/world/level/Level",
                    "net/minecraft/world/item/component/ItemContainerContents")),

            // Fabric uses intermediary names
            VersionSignature.forLoader(FABRIC, V1_21_11, intermediary(
                    "net/minecraft/class_310", "method_1551", "field_1700",
                    "net/minecraft/class_638", "net/minecraft/class_746",
                    "net/minecraft/class_742", "net/minecraft/class_1937",
                    "com/mojang/blaze3d/buffers/GpuBuffer",
                    "com/mojang/blaze3d/systems/CommandEncoder"))
    ));

    public static MinecraftVersion detect(ClassLoader primaryLoader, LoaderType loaderType) {
        MinecraftVersion match = null;

        for (ClassLoader loader : CandidateClassLoaders.of(primaryLoader)) {
            for (VersionSignature signature : SIGNATURES) {
                if (!signature.matches(loader, loaderType)) {
                    continue;
                }

                if (match == null) {
                    match = signature.version();
                } else if (match != signature.version()) {
                    // Two different versions match: refuse to guess.
                    return MinecraftVersion.UNKNOWN;
                }
            }
        }

        return match == null ? MinecraftVersion.UNKNOWN : match;
    }

    private static MinecraftSignature obf(String minecraftClass,
                                          String instanceGetter,
                                          String instanceField,
                                          String... anchors) {
        return MinecraftSignature.of(minecraftClass, instanceGetter, instanceField, anchors);
    }

    private static MinecraftSignature mojang(String minecraftClass,
                                             String instanceGetter,
                                             String instanceField,
                                             String... anchors) {
        return MinecraftSignature.of(minecraftClass, instanceGetter, instanceField, anchors);
    }

    private static MinecraftSignature intermediary(String minecraftClass,
                                                   String instanceGetter,
                                                   String instanceField,
                                                   String... anchors) {
        return MinecraftSignature.of(minecraftClass, instanceGetter, instanceField, anchors);
    }

    private VersionSignatureRegistry() {
    }
}
