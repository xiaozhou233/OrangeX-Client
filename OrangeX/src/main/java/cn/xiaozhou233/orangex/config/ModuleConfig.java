package cn.xiaozhou233.orangex.config;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class ModuleConfig {
    private boolean enabled;
    private int keybind;
    private Map<String, JsonElement> values = new HashMap<>();
}
