package cn.xiaozhou233.orangex.loader;


import cn.xiaozhou233.orangex.loader.detector.Detector;
import cn.xiaozhou233.orangex.loader.utils.Utils;

import java.io.File;

public class Loader {
    private static ClassLoader minecraftClassLoader;

    public static void start() {
        System.out.println("======== Loader START ========");
        minecraftClassLoader = Utils.findMinecraftClassLoader();

        Detector.detect(minecraftClassLoader);
        System.out.println("======== Loader END ========");
    }

}