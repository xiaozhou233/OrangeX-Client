package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.OrangeX;
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

    public void loadConfig() {
        try {
            // Load Panel Position
            File panelConfig = OrangeX.getInstance().getResourceManager().getPanelConfig();
            if (!panelConfig.exists()) {
                panelConfig.createNewFile();
            }
            if (panelConfig.length() != 0) {
                Type type = new TypeToken<HashMap<String, PanelPosition>>() {}.getType();
                FileReader reader = new FileReader(panelConfig);
                HashMap<String, PanelPosition> map = gson.fromJson(reader, type);
                reader.close();
                if (map != null) {
                    this.panelConfigMap = map;
                }
            }
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
            // Panel Position
            FileWriter writer = new FileWriter(OrangeX.getInstance().getResourceManager().getPanelConfig());
            writer.write(gson.toJson(this.panelConfigMap));
            writer.close();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
