package cn.xiaozhou233;


import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.orangex.loader.Loader;

import java.io.File;

public enum MCPEntry {

    INSTANCE;

    public void stop() {
        System.out.println("[OrangeX Client] Stopping...");
    }

    public static String userDir = System.getProperty("user.home");
    public static Thread clientThread;
    public static ClassLoader minecraftClassLoader;

    public static void start() {
        System.out.println("======== Loader START ========");

        System.load(new File("../../natives/libjuiceloader.dll").getAbsolutePath());
        JuiceLoader.init();

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
        File injectionFile = new File("../../out/tmp/OrangeX.jar");
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