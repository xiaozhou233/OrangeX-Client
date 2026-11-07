package cn.xiaozhou233.orangex.injector;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static final String orangexPath =
            new File(System.getProperty("user.home"), ".orangex").getAbsolutePath();
    public static final String agentPath =
            new File(orangexPath, "libloader.dll").getAbsolutePath();
    public static final String configDir = orangexPath;

    public static void main(String[] args) {
        setupConsole();

        System.out.println("================");
        System.out.println("OrangeX Injector v1.0.0");
        System.out.println("OrangeX Path: " + orangexPath);
        System.out.println("================");

        extractInjection();
        if (!loadNativeLibrary()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            InjectorWindow window = new InjectorWindow(agentPath, configDir);
            window.setVisible(true);
        });
    }

    private static void setupConsole() {
        try {
            System.setOut(new PrintStream(
                    new FileOutputStream(FileDescriptor.out),
                    true,
                    StandardCharsets.UTF_8.name()
            ));
            System.setErr(new PrintStream(
                    new FileOutputStream(FileDescriptor.err),
                    true,
                    StandardCharsets.UTF_8.name()
            ));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void extractInjection() {
        File zipFile = new File("./injection.zip");
        if (!zipFile.exists()) {
            System.err.println("injection.zip not found!");
            return;
        }

        try (InputStream zipStream = new FileInputStream(zipFile)) {
            FileUtils.unzip(zipStream, new File(orangexPath));
            System.out.println("Extracted injection.zip to " + orangexPath);
        } catch (IOException exception) {
            System.err.println("[ERROR] Failed to extract injection.zip: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static boolean loadNativeLibrary() {
        try {
            System.load(new File(orangexPath, "libinject.dll").getAbsolutePath());
            System.out.println("Loaded libinject.dll");
            return true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to load libinject.dll:\n" + throwable,
                    "OrangeX Injector",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static void installLookAndFeel() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Component.accentColor", new Color(0xFF8A00));
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 12);
        } catch (Throwable throwable) {
            System.out.println("[WARN] FlatLaf is not available; using the default look and feel");
            return;
        }
        applyWindowScale();
    }

    /**
     * Large monitors are often driven at 100% OS scaling, which makes Swing
     * UIs look tiny. Compute the scale the JRE already applies and add a zoom
     * factor on top until the UI reaches a comfortable physical size.
     */
    private static void applyWindowScale() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (screen.height < 1120) {
            return;
        }

        float target = screen.height >= 1800 ? 2.0f : 1.5f;
        GraphicsConfiguration configuration = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        double effective = UIScale.getSystemScaleFactor(configuration)
                * UIScale.getUserScaleFactor();
        if (effective >= target) {
            return;
        }

        float zoom = (float) (target / effective);
        if (UIScale.setZoomFactor(zoom)) {
            FlatLaf.updateUI();
            System.out.println("[OrangeX] UI scale " + target + "x (zoom " + zoom + ")");
        }
    }
}
