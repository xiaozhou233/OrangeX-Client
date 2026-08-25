package cn.xiaozhou233.orangex.runtime.loader;

import cn.xiaozhou233.orangex.runtime.ClassProbe;

public final class LoaderDetector {

    public static LoaderType detect(ClassLoader loader) {
        if (loader == null) {
            return LoaderType.UNKNOWN;
        }

        // NeoForge has higher priority because it is a Forge fork
        if (isNeoForge(loader)) {
            return LoaderType.NEOFORGE;
        }

        // Forge
        if (isForge(loader)) {
            return LoaderType.FORGE;
        }

        // Fabric
        if (isFabric(loader)) {
            return LoaderType.FABRIC;
        }

        // No mod loader found
        return LoaderType.VANILLA;
    }


    private static boolean isNeoForge(ClassLoader loader) {
        return ClassProbe.exists(loader,
                "net.neoforged.fml.ModList",
                "net.neoforged.fml.loading.FMLLoader");
    }


    private static boolean isForge(ClassLoader loader) {
        return ClassProbe.exists(loader,
                "net.minecraftforge.fml.common.Loader")
                ||
                ClassProbe.exists(loader,
                        "net.minecraftforge.fml.loading.FMLLoader");
    }


    private static boolean isFabric(ClassLoader loader) {
        return ClassProbe.exists(loader,
                "net.fabricmc.loader.api.FabricLoader");
    }

}