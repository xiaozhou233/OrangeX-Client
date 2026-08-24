package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.orangex.loader.utils.Utils;

public class Loader {
    private static ClassLoader minecraftClassLoader;
    public static void start() {
        System.out.println("======== Loader START ========");
        minecraftClassLoader = Utils.findMinecraftClassLoader();

        System.out.println("======== Loader END ========");
    }
}