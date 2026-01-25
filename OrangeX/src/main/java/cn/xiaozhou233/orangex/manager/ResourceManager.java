package cn.xiaozhou233.orangex.manager;

import lombok.Getter;

import java.io.File;

@Getter
public class ResourceManager {
    private final File orangeXDir = new File(System.getProperty("user.home") + "/.orangex");

    // Alt
    private File altConfig = new File(orangeXDir, "alts.json");

    // Panel Config
    private File panelConfig = new File(orangeXDir, "panel.json");
    // Modules Config
    private File modulesConfig = new File(orangeXDir, "modules.json");
}
