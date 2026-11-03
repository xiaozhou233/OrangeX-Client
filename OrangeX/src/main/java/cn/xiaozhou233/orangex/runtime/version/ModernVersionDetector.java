package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.classloader.CandidateClassLoaders;
import cn.xiaozhou233.orangex.runtime.probe.ClassProbe;
import cn.xiaozhou233.orangex.runtime.probe.MinecraftProbe;
import cn.xiaozhou233.orangex.runtime.probe.MinecraftSignature;
import cn.xiaozhou233.orangex.runtime.utils.MinecraftUtils;

/**
 * Detects the 26.x line from class structure plus the bundled
 * {@code version.json} metadata.
 *
 * <p>This runs before structural signatures: once a 26.x client is confirmed,
 * older NeoForge signatures must not be considered (they share class names).</p>
 */
public final class ModernVersionDetector {

    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";

    private static final MinecraftProbe MINECRAFT_PROBE = new MinecraftProbe(
            MinecraftSignature.of(MINECRAFT_CLASS, "getInstance", "instance"));

    public static int detect(ClassLoader primaryLoader) {
        for (ClassLoader loader : CandidateClassLoaders.of(primaryLoader)) {
            int versionId = detectIn(loader);
            if (versionId != MinecraftVersion.UNKNOWN_ID) {
                return versionId;
            }
        }
        return MinecraftVersion.UNKNOWN_ID;
    }

    private static int detectIn(ClassLoader loader) {
        Class<?> minecraftClass = resolveClass(loader, MINECRAFT_CLASS);
        if (minecraftClass == null) {
            return MinecraftVersion.UNKNOWN_ID;
        }

        ClassLoader definingLoader = minecraftClass.getClassLoader();
        if (definingLoader == null || !MINECRAFT_PROBE.matches(loader)) {
            return MinecraftVersion.UNKNOWN_ID;
        }

        if (!hasExpectedHierarchy(definingLoader)) {
            return MinecraftVersion.UNKNOWN_ID;
        }

        return MinecraftUtils.readModernVersionId(minecraftClass);
    }

    private static boolean hasExpectedHierarchy(ClassLoader loader) {
        Class<?> clientLevel = resolveClass(loader, "net/minecraft/client/multiplayer/ClientLevel");
        Class<?> localPlayer = resolveClass(loader, "net/minecraft/client/player/LocalPlayer");
        Class<?> abstractClientPlayer = resolveClass(loader, "net/minecraft/client/player/AbstractClientPlayer");
        Class<?> level = resolveClass(loader, "net/minecraft/world/level/Level");

        return clientLevel != null
                && localPlayer != null
                && abstractClientPlayer != null
                && level != null
                && clientLevel.getSuperclass() == level
                && localPlayer.getSuperclass() == abstractClientPlayer;
    }

    private static Class<?> resolveClass(ClassLoader loader, String internalName) {
        try {
            return Class.forName(internalName.replace('/', '.'), false, loader);
        } catch (ClassNotFoundException | LinkageError | SecurityException e) {
            return null;
        }
    }

    private ModernVersionDetector() {
    }
}
