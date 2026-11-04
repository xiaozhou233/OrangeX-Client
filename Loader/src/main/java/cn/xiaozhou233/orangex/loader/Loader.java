package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;
import cn.xiaozhou233.orangex.loader.classloader.GameClassLoaderResolver;

import java.io.File;

public class Loader {
    public static final String VERSION = "1.0.0-SNAPSHOT";
    public static final String ORANGEX_CLASS = "cn.xiaozhou233.orangex.OrangeX";

    public static String OrangeXPath = new File(System.getProperty("user.home")+"/.orangex").getAbsolutePath();
    public static Thread clientThread;
    public static ClassLoader minecraftClassLoader;

    public static void entry() {
        System.out.println("OrangeX-Loader " + VERSION + " loaded!");

        // Resolve the Minecraft ClassLoader (runtime detection itself lives in OrangeX)
        ClassLoader gameClassLoader = GameClassLoaderResolver.resolve();

        // Find the game thread. Modern clients use "Render thread"; legacy
        // clients use "Client thread". It is only needed as a class loader
        // fallback, so a miss here must not abort injection.
        clientThread = findGameThread();

        // Get Minecraft ClassLoader (prefer the resolved game class loader)
        minecraftClassLoader = gameClassLoader != null
                ? gameClassLoader
                : clientThread != null ? clientThread.getContextClassLoader() : null;
        if (minecraftClassLoader == null) {
            throw new IllegalStateException("Cannot find Minecraft ClassLoader");
        }
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

    /**
     * Legacy clients call the game thread "Client thread"; 1.13+ calls it
     * "Render thread". Return whichever is available.
     */
    private static Thread findGameThread() {
        Thread renderThread = null;

        try {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                String name = thread.getName();

                if ("Client thread".equals(name)) {
                    return thread;
                }
                if ("Render thread".equals(name) && renderThread == null) {
                    renderThread = thread;
                }
            }
        } catch (Throwable ignored) {
        }

        return renderThread;
    }
}
