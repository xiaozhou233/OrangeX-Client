package cn.xiaozhou233.orangex.loader;

import cn.xiaozhou233.juiceagent.api.JuiceAgent;
import cn.xiaozhou233.orangex.loader.classloader.GameClassLoaderResolver;

import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

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
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            JTextArea textArea = new JTextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
            textArea.setLineWrap(false);
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(1000, 600));

            JOptionPane.showMessageDialog(
                    null,
                    scrollPane,
                    "Failed to load OrangeX",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        System.out.println("======== Loader END ========");
    }
}