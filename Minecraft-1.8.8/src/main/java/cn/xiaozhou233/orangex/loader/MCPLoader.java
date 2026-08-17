package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;

import java.io.File;

public enum MCPLoader {
    INSTANCE;

    public static String userDir = System.getProperty("user.home");
    public static Thread clientThread;
    public static ClassLoader minecraftClassLoader;

    public void start() {
        System.out.println("======== OrangeX MCP Loader ========");
        System.load(new File("../natives/libagent.dll").getAbsolutePath());
        JuiceAgent.init("");

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
        JuiceAgent.addToClassLoader(injectionFile.getAbsolutePath(), minecraftClassLoader);

        try {
            Class<?> orangeXClass = Class.forName("cn.xiaozhou233.orangex.OrangeX", true, minecraftClassLoader);
            Object instance = orangeXClass.getMethod("getInstance").invoke(null);
            orangeXClass.getMethod("start").invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        System.out.println("OrangeX MCP Loader Stopping...");
    }
}
