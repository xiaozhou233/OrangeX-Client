package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.utils.ForgeUtils;
import cn.xiaozhou233.orangex.runtime.utils.MinecraftUtils;
import cn.xiaozhou233.orangex.runtime.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

public final class VersionDetector {

    private static int detectedVersion;

    public static MinecraftVersion detect(ClassLoader loader, LoaderType loaderType) {
        int protocol = detectProtocol(loader);
        MinecraftVersion.setCurrentProtocolVersion(protocol);
        return MinecraftVersion.fromProtocolId(protocol);
    }

    private static int detectProtocol(ClassLoader loader) {
        Throwable lastFailure = null;

        // Layer 1: Forge minorVersion (legacy Forge)
        try {
            Object minorVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.common.ForgeVersion", "minorVersion");
            if (minorVersion instanceof Number) {
                return ((Number) minorVersion).intValue();
            }
            lastFailure = new IllegalStateException("ForgeVersion.minorVersion is not numeric");
        } catch (Throwable t) {
            lastFailure = t;
        }

        // Layer 2: FMLLoader.forgeVersion (modern Forge/NeoForge)
        try {
            Object forgeVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.fml.loading.FMLLoader", "forgeVersion");
            int parsed = ForgeUtils.parseForgeVersion(forgeVersion);
            if (parsed >= 0) {
                return parsed;
            }
            lastFailure = new IllegalStateException("FMLLoader.forgeVersion not supported: " + forgeVersion);
        } catch (Throwable t) {
            lastFailure = t;
        }

        // Layer 3: ForgeVersion.forgeVersion (newer Forge)
        try {
            Object forgeVersion = ReflectUtils.readStaticField(
                    "net.minecraftforge.common.ForgeVersion", "forgeVersion");
            int parsed = ForgeUtils.parseForgeVersion(forgeVersion);
            if (parsed >= 0) {
                return parsed;
            }
            lastFailure = new IllegalStateException("ForgeVersion.forgeVersion not supported: " + forgeVersion);
        } catch (Throwable t) {
            lastFailure = t;
        }

        // Layer 4: FMLLoader.versionInfo().forgeVersion() (newest Forge)
        try {
            Class<?> fmlLoader = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            Object versionInfo = fmlLoader.getMethod("versionInfo").invoke(null);
            Object forgeVersion = versionInfo.getClass().getMethod("forgeVersion").invoke(versionInfo);
            int parsed = ForgeUtils.parseForgeVersion(forgeVersion);
            if (parsed >= 0) {
                return parsed;
            }
            lastFailure = new IllegalStateException("FMLLoader.versionInfo().forgeVersion() not supported: " + forgeVersion);
        } catch (Throwable t) {
            lastFailure = t;
        }

        // Layer 5: Fabric/NeoForge marker detection
        boolean isNeoForge = ClassProbe.exists(loader,
                "net.neoforged.fml.ModList",
                "net.neoforged.fml.loading.FMLLoader");
        boolean isFabric = ClassProbe.exists(loader,
                "net.fabricmc.loader.api.FabricLoader");

        // Layer 6: Structural detection via obfuscated/mojmap class probing
        int structuralVersion = detectByStructuralProbe(loader, isNeoForge, isFabric);
        if (structuralVersion != 0) {
            return structuralVersion;
        }

        // Layer 7: version.json detection for 26.x
        int modernVersion = detectModernVersion(loader);
        if (modernVersion != 0) {
            return modernVersion;
        }

        if (lastFailure != null) {
            lastFailure.printStackTrace();
        }
        return 0;
    }

    private static int detectByStructuralProbe(ClassLoader loader, boolean isNeoForge, boolean isFabric) {
        Set<ClassLoader> loaders = candidateLoaders(loader);

        boolean vanilla189 = isRuntimePresent(loaders, "ave", "A", "S", "bew", "adm");
        boolean vanilla1122 = isRuntimePresent(loaders, "bib", "z", "R", "bud", "amu");
        boolean vanilla1165 = isRuntimePresent(loaders, "djz", "C", "F", "dwt", "dzm", "dzj", "brx");
        boolean vanilla1201 = isRuntimePresent(loaders, "enn", "N", "F", "few", "fiy", "fiv", "cmm");
        boolean vanilla1206 = isRuntimePresent(loaders, "ffh", "Q", "E", "fxx", "gcs", "gcp", "dca");
        boolean vanilla1211 = isRuntimePresent(loaders, "fgo", "Q", "E", "fzf", "geb", "gdy", "dcw");
        boolean vanilla12111 = isRuntimePresent(loaders, "gfj", "V", "A", "hif", "hnh", "hne", "dwo");

        boolean vanilla262 = isVanilla262Present(loaders);
        boolean fabric262 = vanilla262 && isFabric;

        boolean excludesModern = vanilla262 || fabric262;

        boolean neoForge1201 = isNeoForge && isMojmapPresent(loaders,
                "net/minecraft/client/Minecraft", "getInstance", "instance",
                "net/minecraft/client/multiplayer/ClientLevel",
                "net/minecraft/client/player/LocalPlayer",
                "net/minecraft/client/player/AbstractClientPlayer",
                "net/minecraft/world/level/Level")
                && !excludesModern && !vanilla12111;

        boolean neoForge1211 = isNeoForge && isMojmapPresent(loaders,
                "net/minecraft/client/Minecraft", "getInstance", "instance",
                "net/minecraft/client/multiplayer/ClientLevel",
                "net/minecraft/client/player/LocalPlayer",
                "net/minecraft/client/player/AbstractClientPlayer",
                "net/minecraft/world/level/Level",
                "net/minecraft/world/item/component/ItemContainerContents")
                && !excludesModern && !vanilla12111;

        boolean fabric12111 = isFabric && isRuntimePresent(loaders,
                "net/minecraft/class_310", "method_1551", "field_1700",
                "net/minecraft/class_638", "net/minecraft/class_746",
                "net/minecraft/class_742", "net/minecraft/class_1937",
                "com/mojang/blaze3d/buffers/GpuBuffer",
                "com/mojang/blaze3d/systems/CommandEncoder");

        int matchingVersions = (vanilla189 ? 1 : 0)
                + (vanilla1122 ? 1 : 0)
                + (vanilla1165 ? 1 : 0)
                + (vanilla1201 || neoForge1201 ? 1 : 0)
                + (vanilla1211 || neoForge1211 ? 1 : 0)
                + (vanilla1206 ? 1 : 0)
                + (vanilla12111 || fabric12111 ? 1 : 0)
                + (vanilla262 || fabric262 ? 1 : 0);

        if (matchingVersions == 1) {
            if (vanilla189) return 15;
            if (vanilla1122) return 23;
            if (vanilla1165) return 36;
            if (vanilla1201 || neoForge1201) return 47;
            if (vanilla1211 || neoForge1211) return 52;
            if (vanilla1206) return 50;
            if (vanilla12111 || fabric12111) return 61;
            if (vanilla262) {
                int modernProtocol = getModernProtocol(loaders);
                return modernProtocol != 0 ? modernProtocol : 110;
            }
        }

        return 0;
    }

    private static boolean isRuntimePresent(Set<ClassLoader> loaders,
                                            String obfuscatedClass,
                                            String getterName,
                                            String instanceFieldName,
                                            String... anchorClasses) {
        for (ClassLoader loader : loaders) {
            Class<?> mcClass = resolveClass(obfuscatedClass, loader);
            if (mcClass == null) continue;

            ClassLoader definingLoader = mcClass.getClassLoader();
            if (definingLoader == null) continue;

            boolean anchorsMatch = true;
            for (String anchor : anchorClasses) {
                Class<?> anchorClass = resolveClass(anchor, loader);
                if (anchorClass == null || anchorClass.getClassLoader() != definingLoader) {
                    anchorsMatch = false;
                    break;
                }
            }
            if (!anchorsMatch) continue;

            if (matchesMinecraftStructure(mcClass, getterName, instanceFieldName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMojmapPresent(Set<ClassLoader> loaders,
                                           String minecraftClass,
                                           String getterName,
                                           String instanceFieldName,
                                           String... anchorClasses) {
        for (ClassLoader loader : loaders) {
            Class<?> mcClass = resolveClass(minecraftClass, loader);
            if (mcClass == null) continue;

            ClassLoader definingLoader = mcClass.getClassLoader();
            if (definingLoader == null) continue;

            boolean anchorsMatch = true;
            for (String anchor : anchorClasses) {
                Class<?> anchorClass = resolveClass(anchor, definingLoader);
                if (anchorClass == null) {
                    anchorsMatch = false;
                    break;
                }
            }
            if (!anchorsMatch) continue;

            if (matchesMinecraftStructure(mcClass, getterName, instanceFieldName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMinecraftStructure(Class<?> minecraftClass,
                                                     String getterName,
                                                     String instanceFieldName) {
        try {
            Method getter = minecraftClass.getDeclaredMethod(getterName);
            boolean getterOk = Modifier.isStatic(getter.getModifiers())
                    && getter.getReturnType() == minecraftClass;
            if (!getterOk) return false;

            if (instanceFieldName == null || instanceFieldName.isEmpty()) {
                return true;
            }

            Field instance = minecraftClass.getDeclaredField(instanceFieldName);
            return Modifier.isStatic(instance.getModifiers())
                    && instance.getType() == minecraftClass;
        } catch (ReflectiveOperationException | SecurityException | LinkageError e) {
            return false;
        }
    }

    private static boolean isVanilla262Present(Set<ClassLoader> loaders) {
        return getModernProtocol(loaders) != 0;
    }

    private static int getModernProtocol(Set<ClassLoader> loaders) {
        for (ClassLoader loader : loaders) {
            Class<?> mcClass = resolveClass("net/minecraft/client/Minecraft", loader);
            if (mcClass == null) continue;

            ClassLoader definingLoader = mcClass.getClassLoader();
            if (definingLoader == null) continue;

            Class<?> clientLevel = resolveClass("net/minecraft/client/multiplayer/ClientLevel", definingLoader);
            Class<?> localPlayer = resolveClass("net/minecraft/client/player/LocalPlayer", definingLoader);
            Class<?> abstractPlayer = resolveClass("net/minecraft/client/player/AbstractClientPlayer", definingLoader);
            Class<?> level = resolveClass("net/minecraft/world/level/Level", definingLoader);

            if (clientLevel == null || localPlayer == null
                    || abstractPlayer == null || level == null
                    || clientLevel.getSuperclass() != level
                    || localPlayer.getSuperclass() != abstractPlayer) {
                continue;
            }

            if (matchesMinecraftStructure(mcClass, "getInstance", "instance")) {
                int protocol = MinecraftUtils.matchesVersionMetadata(mcClass);
                if (protocol != 0) {
                    return protocol;
                }
            }
        }
        return 0;
    }

    private static int detectModernVersion(ClassLoader loader) {
        Set<ClassLoader> loaders = candidateLoaders(loader);
        return getModernProtocol(loaders);
    }

    private static Class<?> resolveClass(String internalName, ClassLoader loader) {
        String binaryName = internalName.replace('/', '.');
        try {
            return Class.forName(binaryName, false, loader);
        } catch (ClassNotFoundException | LinkageError | SecurityException e) {
            return null;
        }
    }

    private static Set<ClassLoader> candidateLoaders(ClassLoader primary) {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        if (primary != null) {
            loaders.add(primary);
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            loaders.add(context);
        }
        ClassLoader self = VersionDetector.class.getClassLoader();
        if (self != null) {
            loaders.add(self);
        }
        try {
            ClassLoader system = ClassLoader.getSystemClassLoader();
            if (system != null) {
                loaders.add(system);
            }
        } catch (SecurityException ignored) {
        }
        return loaders;
    }

    private VersionDetector() {
    }
}