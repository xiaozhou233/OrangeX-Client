package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.OptionSerializer;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ModuleConfig {
    private final File configFile;
    private final Gson gson;

    public ModuleConfig(String path) {
        this.configFile = new File(path);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save(List<Module> modules) {
        JsonObject config = new JsonObject();
        JsonArray modulesArray = new JsonArray();
        for (Module module : modules) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("name", module.getName());
            moduleObj.addProperty("enabled", module.isEnabled());
            moduleObj.addProperty("key", module.getKey());
            if (!module.getOptions().isEmpty()) {
                moduleObj.add("options", OptionSerializer.toJson(module.getOptions()));
            }
            modulesArray.add(moduleObj);
        }
        config.add("modules", modulesArray);
        writeConfig(config);
    }

    public void saveClickGui(JsonObject clickGuiData) {
        JsonObject config = loadRaw();
        config.add("clickgui", clickGuiData);
        writeConfig(config);
    }

    public JsonObject loadClickGui() {
        JsonObject config = loadRaw();
        if (config == null || !config.has("clickgui")) {
            return new JsonObject();
        }
        return config.getAsJsonObject("clickgui");
    }

    private JsonObject loadRaw() {
        if (!configFile.exists()) return new JsonObject();
        try (Reader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            return config != null ? config : new JsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    private void writeConfig(JsonObject config) {
        configFile.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(List<Module> modules) {
        if (!configFile.exists()) return;
        try (Reader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            if (config == null) return;
            JsonArray modulesArray = config.getAsJsonArray("modules");
            if (modulesArray == null) return;
            for (JsonElement element : modulesArray) {
                JsonObject moduleObj = element.getAsJsonObject();
                String name = moduleObj.get("name").getAsString();
                Module module = findModule(modules, name);
                if (module == null) continue;
                if (moduleObj.has("enabled") && moduleObj.get("enabled").getAsBoolean()) {
                    module.setEnabled(true);
                }
                if (moduleObj.has("key")) {
                    module.setKey(moduleObj.get("key").getAsInt());
                }
                if (moduleObj.has("options")) {
                    OptionSerializer.fromJson(moduleObj.getAsJsonObject("options"), module.getOptions());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Module findModule(List<Module> modules, String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}