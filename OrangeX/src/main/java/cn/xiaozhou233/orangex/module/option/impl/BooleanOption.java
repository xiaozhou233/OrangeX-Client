package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanOption extends Option<Boolean> {
    public BooleanOption(String name, String description, Module parent, Boolean defaultValue) {
        super(name, description, parent, defaultValue);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        this.value = element.getAsBoolean();
    }
}