package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.module.value.Value;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class ConfigManager {

    private final Gson gson = new Gson();

    private HashMap<String, PanelPosition> panelConfigMap = new HashMap<>();

    private HashMap<String, ModuleConfig> moduleConfigMap = new HashMap<>();

    public void loadConfig() {
        try {
            // Load Panel Position
            File panelConfig = OrangeX.getInstance().getResourceManager().getPanelConfig();
            if (!panelConfig.exists()) panelConfig.createNewFile();
            if (panelConfig.length() != 0) {
                Type type = new TypeToken<HashMap<String, PanelPosition>>() {
                }.getType();
                try (FileReader reader = new FileReader(panelConfig)) {
                    HashMap<String, PanelPosition> map = gson.fromJson(reader, type);
                    if (map != null) this.panelConfigMap = map;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load panel config!");
            throw new RuntimeException(e);
        }

        try {
            // Load Module Config
            File moduleConfig = OrangeX.getInstance().getResourceManager().getModulesConfig();
            if (!moduleConfig.exists()) moduleConfig.createNewFile();
            if (moduleConfig.length() != 0) {
                Type type = new TypeToken<HashMap<String, ModuleConfig>>() {}.getType();
                try (FileReader reader = new FileReader(moduleConfig)) {
                    HashMap<String, ModuleConfig> map = gson.fromJson(reader, type);
                    if (map != null) this.moduleConfigMap = map;
                }
            }

            applyModuleConfig();

        } catch (Exception e) {
            System.out.println("Failed to load module config!");
            throw new RuntimeException(e);
        }
    }

    public PanelPosition getPanelPosition(String panelName) {
        return this.panelConfigMap.getOrDefault(panelName, null);
    }

    public void setPanelPosition(String panelName, PanelPosition position) {
        this.panelConfigMap.put(panelName, position);
    }

    public void saveConfig() {
        // Save panel config
        try(FileWriter writer = new FileWriter(
                OrangeX.getInstance().getResourceManager().getPanelConfig())) {
            writer.write(gson.toJson(this.panelConfigMap));
        } catch (IOException e) {
            System.out.println("Failed to save panel config");
            throw new RuntimeException(e);
        }

        // Save module config
        generateModuleConfig();
        try (FileWriter writer = new FileWriter(
                OrangeX.getInstance().getResourceManager().getModulesConfig())) {
            writer.write(gson.toJson(this.moduleConfigMap));
        } catch (IOException e) {
            System.out.println("Failed to save module config");
            throw new RuntimeException(e);
        }
    }

    private void generateModuleConfig() {
        moduleConfigMap.clear();

        for (Module module : OrangeX.getInstance().getModuleManager().getAllModules()) {
            ModuleConfig moduleConfig = new ModuleConfig();
            moduleConfig.setEnabled(module.isEnabled());
            moduleConfig.setKeybind(module.getKeyBind().getValue());

            for (Value<?> v : module.getValues()) {
                moduleConfig.getValues().put(
                        v.getName(),
                        gson.toJsonTree(v.getValue())
                );
            }

            moduleConfigMap.put(module.getName(), moduleConfig);
        }
    }

    private void applyModuleConfig() {
        for (Module module : OrangeX.getInstance().getModuleManager().getAllModules()) {
            ModuleConfig mc = moduleConfigMap.get(module.getName());
            if (mc == null) continue;

            module.setEnabled(mc.isEnabled());
            module.getKeyBind().setValue(mc.getKeybind());

            for (Value<?> value : module.getValues()) {
                JsonElement element = mc.getValues().get(value.getName());
                if (element == null) continue;

                value.fromJson(element);
            }
        }
    }


    private Object convertValue(Object raw, Value<?> targetValue) {
        if (raw == null) return null;

        if (targetValue instanceof KeybindValue) {
            if (raw instanceof Number) return ((Number) raw).intValue();
            if (raw instanceof String) return Integer.parseInt((String) raw);
        }

        if (targetValue instanceof BooleanValue) {
            if (raw instanceof Boolean) return raw;
            if (raw instanceof String) return Boolean.parseBoolean((String) raw);
        }

        if (targetValue instanceof NumberValue) {
            if (raw instanceof Number) return ((Number) raw).doubleValue();
            if (raw instanceof String) return Double.parseDouble((String) raw);
        }

        if (targetValue instanceof ModeValue) {
            if (raw instanceof String) {
                String mode = (String) raw;
                ModeValue mv = (ModeValue) targetValue;
                if (mv.isValidMode(mode)) return mode;
                return mv.getValue();
            }
        }

        if (targetValue.getValue().getClass().isInstance(raw)) {
            return raw;
        }

        return null;
    }
}
