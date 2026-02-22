package cn.xiaozhou233.orangex.injector;

import cn.xiaozhou233.juiceagent.injector.InjectorNative;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Injector {
    public static String orangexPath = System.getProperty("user.home") + "/.orangex";
    public static void main(String[] args) {
        System.out.println("OrangeX Injector");

        int pid = -1;

        // Unzip injection.zip to ~/.orangex
        System.out.println("Unzip injection.zip to " + orangexPath);
        try (InputStream zipStream = ClassLoader.getSystemResourceAsStream("injection.zip")) {
            if (zipStream == null) {
                System.err.println("ZIP resource not found!");
                return;
            }
            FileUtils.unzip(zipStream, new File(orangexPath));
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load Native Library
        System.load(orangexPath + "/libinject.dll");
        InjectorNative injectorNative = new InjectorNative();

        // Find Minecraft
        ArrayList<Integer> pids = new ArrayList<>();
        ArrayList<String> windowTitles = new ArrayList<>();
        try {
            String[] titles = {"Minecraft", "minecraft", "1.8.9", "Badlion", "Lunar"};
            String[] ignoreTitles = {"Badlion Client", "Badlion Chat", "Home - Lunar Client"};
            for (String title : titles) {
                InjectorNative.WindowInfo[] list = InjectorNative.findWindowsByTitle(title);
                if (list == null || list.length == 0) {
                } else {
                    for (InjectorNative.WindowInfo w : list) {
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

        // Print the result
        System.out.println("=======================");
        for (int i = 0; i < pids.size(); i++) {
            System.out.printf("[ %d ] %s (PID: %d)%n", i + 1, windowTitles.get(i), pids.get(i));
        }
        System.out.println("=======================");

        if (pids.isEmpty()) {
            System.out.println("[ERROR] No Minecraft process found!");
            return;
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

        // Inject
        String agentPath = orangexPath + "/libagent.dll";
        String configDir = orangexPath;
        injectorNative.inject(pid, agentPath, configDir);
    }

}