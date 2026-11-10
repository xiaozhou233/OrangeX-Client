package cn.xiaozhou233.orangex.config;

import lombok.Getter;

import java.io.File;

public class Config {

    private final ConfigManager manager;
    @Getter
    private final String name;
    @Getter
    private final File file;

    public Config(ConfigManager manager, String name, File file) {
        this.manager = manager;
        this.name = name;
        this.file = file;
    }

    public boolean load() {
        return manager.load(name);
    }

    public boolean save() {
        return manager.save(name);
    }

    public boolean exists() {
        return file.isFile();
    }

    @Override
    public String toString() {
        return name;
    }
}