package cn.xiaozhou233.orangex.injector;

import cn.xiaozhou233.juiceagent.injector.Injector;
import cn.xiaozhou233.juiceagent.injector.WindowInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static String orangexPath = new File(System.getProperty("user.home") + "/.orangex").getAbsolutePath();
    private static int pid = -1;

    public static void main(String[] args) throws Exception {
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

        System.out.println("================");
        System.out.println("OrangeX Injector v1.0.0");
        System.out.println("OrangeX Path: " + orangexPath);
        System.out.println("================");

        // Unzip injection
        File zipFile = new File("./injection.zip");
        if (!zipFile.exists()) {
            System.err.println("injection.zip not found!");
        }
        try (InputStream zipStream = new FileInputStream(zipFile)) {
            FileUtils.unzip(zipStream, new File(orangexPath));
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load libinject native library
        System.load(new File(orangexPath + "/libinject.dll").getAbsolutePath());
        System.out.println("Loaded libinject.dll");

        // Find Minecraft
        ArrayList<Integer> pids = new ArrayList<>();
        ArrayList<String> windowTitles = new ArrayList<>();
        try {
            String[] titles = {"Minecraft", "minecraft", "1.8.9", "Badlion", "Lunar"};
            String[] ignoreTitles = {"Badlion Client", "Badlion Chat", "Home - Lunar Client"};
            for (String title : titles) {
                WindowInfo[] list = Injector.findWindowsByTitle(title);
                if (list == null || list.length == 0) {
                } else {
                    for (WindowInfo w : list) {
                        if (pids.contains(w.pid))
                            continue;
                        if (Arrays.stream(ignoreTitles).anyMatch(w.title::contains))
                            continue;
                        pids.add(w.pid);
                        windowTitles.add(w.title);
                    }
                }
            }
        } catch (UnsatisfiedLinkError ule) {
            throw new RuntimeException("Native method findWindowsByTitle not available", ule);
        } catch (Exception ex) {
            System.out.println("[ERROR] Error while calling native findWindowsByTitle: " + ex.getMessage());
            ex.printStackTrace();
        }

        // Print results
        System.out.println("=======================");
        for (int i = 0; i < pids.size(); i++) {
            System.out.printf("[ %d ] %s (PID: %d)%n", i + 1, windowTitles.get(i), pids.get(i));
        }
        System.out.println("=======================");
        if (pids.isEmpty()) {
            System.err.println("[ERROR] No Minecraft process found!");
        }

        // Input the sequence number
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.printf("[INPUT] Select (1-%d): ", pids.size());
            int index = scanner.nextInt();
            if (index < 1 || index > pids.size()) {
                throw new RuntimeException("Invalid selection: " + index);
            }
            pid = pids.get(index - 1);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid input", e);
        }

        String agentPath = orangexPath + "/libloader.dll";
        String configDir = orangexPath;
        Injector.inject(pid, agentPath, configDir);
    }
}