package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

@Getter
public class IntOption extends Option<Integer> {
    private final int min;
    private final int max;
    private final int step;

    public IntOption(String name, String description, Module parent,
                     Integer defaultValue, int min, int max, int step) {
        super(name, description, parent, clamp(defaultValue, min, max));
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void setValue(Integer value) {
        this.value = clamp(value, min, max);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        setValue(element.getAsInt());
    }

    private static int clamp(int v, int min, int max) {
        return Math.min(max, Math.max(min, v));
    }
}