package cn.xiaozhou233.orangex.loader;


import cn.xiaozhou233.juiceloader.JuiceLoader;

import java.io.File;

public class Loader {
    public static String userDir = System.getProperty("user.home");
    public static Thread clientThread;
    public static ClassLoader minecraftClassLoader;

    public static void start() {
        System.out.println("======== Loader START ========");

        // Find client thread
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("Client thread".equals(t.getName())) {
                clientThread = t;
                break;
            }
        }
        if (clientThread == null) {
            throw new IllegalStateException("Cannot find Minecraft client thread");
        }

        // Get Minecraft ClassLoader
        minecraftClassLoader = clientThread.getContextClassLoader();
        System.out.println("Minecraft ClassLoader: " + minecraftClassLoader);

        // Load OrangeX
        File injectionFile = new File(userDir + "/.orangex/OrangeX.jar");
        System.out.println("OrangeX Injection: " + injectionFile.getAbsolutePath());
        JuiceLoader.AddToClassLoader(injectionFile.getAbsolutePath(), minecraftClassLoader);

        // Start OrangeX
        try {
            Class<?> orangeXClass = Class.forName("cn.xiaozhou233.orangex.OrangeX", true, minecraftClassLoader);
            orangeXClass.getMethod("start").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("======== Loader END ========");
    }
}
