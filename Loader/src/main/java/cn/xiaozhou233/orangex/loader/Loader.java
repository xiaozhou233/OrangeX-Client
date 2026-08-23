package cn.xiaozhou233.orangex.loader;


import cn.xiaozhou233.juiceagent.api.JuiceAgent;

import javax.swing.*;
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

        // Check if LaunchWrapper exists
        if (Detector.isLaunchWrapper()){
            System.out.println("LaunchWrapper detected");
        }

        // Get Minecraft ClassLoader
        minecraftClassLoader = clientThread.getContextClassLoader();
        System.out.println("Minecraft ClassLoader: " + minecraftClassLoader);

        switch (Detector.detect()) {
            case MCP:
                break;
            case VANILLA:
                // No more support
                JOptionPane.showMessageDialog(
                        null,
                        "Not support Vanilla Client! \n 不支持原版客户端!",
                        "Vanilla Obfuscation",
                        JOptionPane.ERROR_MESSAGE
                        );
                throw new IllegalStateException("Not support Vanilla Client!");
            case LUNAR:
                break;
            case BADLION:
                // No more support
                JOptionPane.showMessageDialog(
                        null,
                        "Not support Badlion Client! \n 不支持Badlion客户端!",
                        "Badlion Obfuscation",
                        JOptionPane.ERROR_MESSAGE
                        );
                throw new IllegalStateException("Not support Badlion Client!");
            case UNKNOWN:
                JOptionPane.showMessageDialog(
                        null,
                        "Inject Failed! Cause: Unknown Obfuscation \n 注入失败! 原因: 未知的混淆",
                        "Unknown Obfuscation",
                        JOptionPane.ERROR_MESSAGE
                );
                throw new IllegalStateException("Unknown Obfuscation");
            default:
                JOptionPane.showMessageDialog(
                        null,
                        "Inject Failed! Cause: Unknown Obfuscation",
                        "Unknown Obfuscation",
                        JOptionPane.ERROR_MESSAGE
                        );
                throw new IllegalStateException("Unexpected value: " + Detector.detect());
        }

        // Load OrangeX
        File injectionFile = new File(userDir + "/.orangex/OrangeX.jar");
        System.out.println("OrangeX Injection: " + injectionFile.getAbsolutePath());
        JuiceAgent.addToClassLoader(injectionFile.getAbsolutePath(), minecraftClassLoader);

        // Start OrangeX
        try {
            Class<?> orangeXClass = Class.forName("cn.xiaozhou233.orangex.OrangeX", true, minecraftClassLoader);
            Object orangeXInstance = orangeXClass.getMethod("getInstance").invoke(null);
            orangeXClass.getMethod("start").invoke(orangeXInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("======== Loader END ========");
    }
}