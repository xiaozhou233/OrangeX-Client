package cn.xiaozhou233.orangex.loader.utils;

public class Utils {
    public static ClassLoader findMinecraftClassLoader() {
        Thread thread = null;
        // Find client thread
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if ("Client thread".equals(t.getName()) || "Render thread".equals(t.getName())) {
                thread = t;
                break;
            }
        }
        if (thread == null) {
            throw new IllegalStateException("Cannot find Minecraft client thread");
        }
        ClassLoader minecraftClassLoader = thread.getContextClassLoader();
        System.out.println("Minecraft ClassLoader: " + minecraftClassLoader);

        return minecraftClassLoader;
    }
}
