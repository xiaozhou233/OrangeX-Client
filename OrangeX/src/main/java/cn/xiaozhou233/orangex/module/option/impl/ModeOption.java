package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class ModeOption extends Option<String> {
    private final String[] modes;

    public ModeOption(String name, String description, Module parent,
                      String defaultValue, String... modes) {
        super(name, description, parent, defaultValue);
        this.modes = modes;
    }

    @Override
    public void setValue(String value) {
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(value)) {
                this.value = mode;
                return;
            }
        }
        this.value = modes.length > 0 ? modes[0] : value;
    }

    public int getIndex() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) return i;
        }
        return 0;
    }

    public void cycle() {
        int next = (getIndex() + 1) % modes.length;
        this.value = modes[next];
    }

    public void cycleReverse() {
        int prev = (getIndex() - 1 + modes.length) % modes.length;
        this.value = modes[prev];
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        setValue(element.getAsString());
    }
}