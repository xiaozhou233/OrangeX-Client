package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringOption extends Option<String> {
    private final int maxLength;

    public StringOption(String name, String description, Module parent,
                        String defaultValue, int maxLength) {
        super(name, description, parent, defaultValue);
        this.maxLength = maxLength;
    }

    @Override
    public void setValue(String value) {
        if (maxLength > 0 && value.length() > maxLength) {
            this.value = value.substring(0, maxLength);
        } else {
            this.value = value;
        }
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