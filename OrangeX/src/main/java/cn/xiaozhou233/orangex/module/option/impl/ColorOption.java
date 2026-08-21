package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorOption extends Option<Integer> {
    public ColorOption(String name, String description, Module parent, Integer defaultValue) {
        super(name, description, parent, defaultValue);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(String.format("0x%08X", value));
    }

    @Override
    public void deserialize(JsonElement element) {
        String raw = element.getAsString();
        if (raw.startsWith("0x") || raw.startsWith("0X")) {
            this.value = (int) Long.parseLong(raw.substring(2), 16);
        } else {
            this.value = Integer.parseUnsignedInt(raw);
        }
    }
}