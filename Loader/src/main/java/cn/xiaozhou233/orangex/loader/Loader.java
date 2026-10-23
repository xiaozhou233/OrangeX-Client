package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;

import java.io.File;

public class Loader {
    public static final String VERSION = "1.0.0-SNAPSHOT";
    public static final String ORANGEX_CLASS = "cn.xiaozhou233.orangex.OrangeX";

    public static String OrangeXPath = new File(System.getProperty("user.home")+"/.orangex").getAbsolutePath();
    public static Thread clientThread;
    public static ClassLoader minecraftClassLoader;

    public static void entry() {
        System.out.println("OrangeX-Loader " + VERSION + " loaded!");

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

        // Dev Only: Load JuiceAgent
        if (isDevEnvironment()) {
            System.load(new File("../../natives/libagent.dll").getAbsolutePath());
            JuiceAgent.init("");
        }

        // Load OrangeX
        File orangexFile;
        if (isDevEnvironment()) {
            orangexFile = new File("../../out/tmp/OrangeX.jar");
        } else {
            orangexFile = new File(OrangeXPath + "/OrangeX.jar");
        }
        System.out.println("OrangeX Injection: " + orangexFile.getAbsolutePath());
        JuiceAgent.addToClassLoader(orangexFile.getAbsolutePath(), minecraftClassLoader);

        // Start OrangeX
        try {
            Class<?> orangeX = Class.forName(ORANGEX_CLASS, true, minecraftClassLoader);
            Object orangeXInstance = orangeX.getMethod("getInstance").invoke(null);
            orangeX.getMethod("start").invoke(orangeXInstance);
        } catch (Exception e) {
            System.err.println("Failed to invoke OrangeX start!");
            throw new RuntimeException(e);
        }
    }

    public static boolean isDevEnvironment() {
        return Boolean.getBoolean("orangex.dev");
    }
}