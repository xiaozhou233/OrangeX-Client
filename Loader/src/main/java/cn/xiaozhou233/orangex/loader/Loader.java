package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;
import cn.xiaozhou233.orangex.loader.classloader.GameClassLoaderResolver;

import java.io.File;

public class Loader {
    public static String userHome = System.getProperty("user.home");
    public static ClassLoader minecraftClassLoader;

    public static void entry() {
        System.out.println("======== Loader START ========");
        // Find Minecraft ClassLoader
        minecraftClassLoader = GameClassLoaderResolver.resolve();
        System.out.println("Minecraft ClassLoader: " + minecraftClassLoader);

        // Load OrangeX
        File injectionFile = new File(userHome + "/.orangex/OrangeX.jar");
        System.out.println("OrangeX Injection: " + injectionFile.getAbsolutePath());
        JuiceAgent.addToClassLoader(injectionFile.getAbsolutePath(), minecraftClassLoader);

        try {
            Class<?> orangeXClass = Class.forName("cn.xiaozhou233.orangex.entry.Entry", true, minecraftClassLoader);
            orangeXClass.getMethod("entry").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("======== Loader END ========");
    }
}