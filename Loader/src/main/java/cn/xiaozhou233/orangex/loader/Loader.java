package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;
import cn.xiaozhou233.orangex.loader.classloader.GameClassLoaderResolver;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;

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
        addToGameClassLoader(orangexFile, minecraftClassLoader);

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

    /**
     * Fabric's KnotClassLoader keeps its own code-source list and rejects jars
     * that were added with a plain URLClassLoader-style addURL. Expose the jar
     * through KnotClassDelegate#addCodeSource, which updates both the code
     * source list and the internal URL loader.
     */
    private static void addToGameClassLoader(File jar, ClassLoader loader) {
        if (isKnotClassLoader(loader)) {
            try {
                addToKnotClassLoader(loader, jar.toPath());
                return;
            } catch (ReflectiveOperationException | RuntimeException e) {
                System.err.println(
                        "Failed to expose " + jar.getName()
                                + " to Fabric KnotClassLoader: " + e);
                e.printStackTrace();
            }
        }

        JuiceAgent.addToClassLoader(jar.getAbsolutePath(), loader);
    }

    private static boolean isKnotClassLoader(ClassLoader loader) {
        return loader != null
                && "net.fabricmc.loader.impl.launch.knot.KnotClassLoader"
                .equals(loader.getClass().getName());
    }

    private static void addToKnotClassLoader(ClassLoader loader, Path jar)
            throws ReflectiveOperationException {
        Method getDelegate = loader.getClass().getDeclaredMethod("getDelegate");
        getDelegate.setAccessible(true);
        Object delegate = getDelegate.invoke(loader);

        Method addCodeSource = delegate.getClass()
                .getMethod("addCodeSource", Path.class);
        addCodeSource.setAccessible(true);
        addCodeSource.invoke(delegate, jar);
    }
}
