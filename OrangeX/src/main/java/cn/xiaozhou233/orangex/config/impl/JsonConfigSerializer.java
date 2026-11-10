package cn.xiaozhou233.orangex.config.impl;

import cn.xiaozhou233.orangex.config.ConfigSerializer;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonConfigSerializer implements ConfigSerializer {

    @Override
    public JsonObject serialize(ModuleManager moduleManager) {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        root.addProperty("version", CURRENT_VERSION);

        for (Module module : moduleManager.getAllModules()) {
            JsonObject moduleJson = new JsonObject();

            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.add("values", module.valuesToJson());

            modules.add(module.getConfigKey(), moduleJson);
        }

        root.add("modules", modules);

        return root;
    }

    @Override
    public void deserialize(ModuleManager moduleManager, JsonObject json) {
        if (json == null || !json.has("modules")
                || !json.get("modules").isJsonObject()) {
            return;
        }

        JsonObject modules = json.getAsJsonObject("modules");

        for (Module module : moduleManager.getAllModules()) {
            JsonElement element = modules.get(module.getConfigKey());

            // Older configs keyed modules by display name.
            if (element == null) {
                element = modules.get(module.getName());
            }

            if (element == null || !element.isJsonObject()) {
                continue;
            }

            JsonObject moduleJson = element.getAsJsonObject();

            // Restore values before enabling the module.
            if (moduleJson.has("values")
                    && moduleJson.get("values").isJsonObject()) {
                module.valuesFromJson(moduleJson.getAsJsonObject("values"));
            }

            if (moduleJson.has("enabled")
                    && moduleJson.get("enabled").isJsonPrimitive()
                    && moduleJson.get("enabled").getAsJsonPrimitive().isBoolean()) {
                module.setEnabled(moduleJson.get("enabled").getAsBoolean());
            }
        }
    }
}