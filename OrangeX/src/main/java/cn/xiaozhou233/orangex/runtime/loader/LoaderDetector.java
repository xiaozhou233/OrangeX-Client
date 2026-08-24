package cn.xiaozhou233.orangex.runtime.loader;

import cn.xiaozhou233.orangex.runtime.ClassProbe;

public class LoaderDetector {
    public static LoaderType detect(ClassLoader loader) {
        if(loader == null)
            return LoaderType.UNKNOWN;

        // Loader Class Check
        boolean isNeoForge = ClassProbe.exists(loader, "net.neoforged.fml.ModList", "net.neoforged.fml.loading.FMLLoader");
        boolean isFabric = ClassProbe.exists(loader, "net.fabricmc.loader.api.FabricLoader", "net.fabricmc.loader.impl.launch.knot.KnotClassLoader");
        boolean isForge = ClassProbe.exists(loader, "net.minecraftforge.fml.loading.FMLLoader", "net.minecraftforge.common.ForgeVersion", "net.minecraftforge.fml.common.Loader");

        // number of matches
        int matches = (isNeoForge ? 1 : 0) + (isFabric ? 1 : 0) + (isForge ? 1 : 0);

        // Matches 0: no mod loader detected, Vanilla
        if (matches == 0)
            return LoaderType.VANILLA;

        // Matches > 1 : WTF Why are there multiple Loaders? Unknown
        if (matches > 1)
            return LoaderType.UNKNOWN;

        if (isNeoForge)
            return LoaderType.NEOFORGE;
        if (isFabric)
            return LoaderType.FABRIC;
        if (isForge)
            return LoaderType.FORGE;

        return LoaderType.UNKNOWN;

    }
}
