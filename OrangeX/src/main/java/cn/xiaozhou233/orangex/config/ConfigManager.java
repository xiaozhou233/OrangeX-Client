package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.module.value.Value;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
                Type type = new TypeToken<HashMap<String, PanelPosition>>() {}.getType();
                FileReader reader = new FileReader(panelConfig);
                HashMap<String, PanelPosition> map = gson.fromJson(reader, type);
                reader.close();
                if (map != null) this.panelConfigMap = map;
            }

            // Load Module Config
            File moduleConfig = OrangeX.getInstance().getResourceManager().getModulesConfig();
            if (!moduleConfig.exists()) moduleConfig.createNewFile();
            if (moduleConfig.length() != 0) {
                Type type = new TypeToken<HashMap<String, ModuleConfig>>() {}.getType();
                FileReader reader = new FileReader(moduleConfig);
                HashMap<String, ModuleConfig> map = gson.fromJson(reader, type);
                reader.close();
                if (map != null) this.moduleConfigMap = map;
            }

            applyModuleConfig();

        } catch (Exception e) {
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
        try {
            FileWriter writer = new FileWriter(OrangeX.getInstance().getResourceManager().getPanelConfig());
            writer.write(gson.toJson(this.panelConfigMap));
            writer.close();

            generateModuleConfig();
            FileWriter moduleWriter = new FileWriter(OrangeX.getInstance().getResourceManager().getModulesConfig());
            moduleWriter.write(gson.toJson(this.moduleConfigMap));
            moduleWriter.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void generateModuleConfig() {
        moduleConfigMap.clear();

        for (Module module : OrangeX.getInstance().getModuleManager().getAllModules()) {
            ModuleConfig mc = new ModuleConfig();
            mc.enabled = module.isEnabled();
            mc.keybind = module.getKeyBind().getValue();

            for (Value<?> v : module.getValues()) {
                mc.values.put(v.getName(), v.getValue());
            }

            moduleConfigMap.put(module.getName(), mc);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyModuleConfig() {
        for (Module module : OrangeX.getInstance().getModuleManager().getAllModules()) {
            ModuleConfig mc = moduleConfigMap.get(module.getName());
            if (mc == null) continue;

            module.setEnabled(mc.enabled);
            module.getKeyBind().setValue(mc.keybind);

            for (Value<?> v : module.getValues()) {
                Object raw = mc.values.get(v.getName());
                if (raw == null) continue;

                Object converted = convertValue(raw, v);
                if (converted != null) {
                    ((Value<Object>) v).setValue(converted);
                }
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
