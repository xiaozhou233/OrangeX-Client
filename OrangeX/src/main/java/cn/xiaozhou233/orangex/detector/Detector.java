package cn.xiaozhou233.orangex.detector;

public class Detector {

    private static boolean classExists(String className) {
        String resourceName = className.replace('.', '/') + ".class";

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = Detector.class.getClassLoader();
        }

        return classLoader.getResource(resourceName) != null;
    }

    public static DetectorType detect() {

        // Lunar Client Detection
        if (classExists("com.moonsworth.lunar.genesis.Genesis")) {
            System.out.println("Lunar class found!");
            return DetectorType.LUNAR;
        }

        System.out.println("Lunar class not found");

        // Badlion Detection
        if (classExists("net.badlion.client.Wrapper")) {
            System.out.println("Badlion class found!");
            return DetectorType.BADLION;
        }

        System.out.println("Badlion class not found");

        // MCP Detection
        if (classExists("net.minecraft.client.Minecraft")) {
            System.out.println("MCP class found!");
            return DetectorType.MCP;
        }
        System.out.println("MCP class not found");

        // Vanilla Detection
        if (classExists("ave")) {
            System.out.println("Vanilla class found!");
            return DetectorType.VANILLA;
        }

        System.out.println("Vanilla class not found");

        return DetectorType.UNKNOWN;
    }

    public static boolean isLaunchWrapper() {
        return classExists("net.minecraft.launchwrapper.Launch");
    }
}