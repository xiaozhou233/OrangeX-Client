package cn.xiaozhou233;


import cn.xiaozhou233.juiceloader.JuiceLoader;
import cn.xiaozhou233.orangex.loader.Loader;

import java.io.File;

public enum MCPEntry {

    INSTANCE;

    public void stop() {
        System.out.println("[OrangeX Client] Stopping...");
    }

    public void start() {
        System.out.println("[OrangeX Client] Starting...");

        // JuiceLoader
        System.load(new File("../../natives/libjuiceloader.dll").getAbsolutePath());
        JuiceLoader.init();

        Loader.start();
        System.out.println("[OrangeX Client] Done.");

    }
}