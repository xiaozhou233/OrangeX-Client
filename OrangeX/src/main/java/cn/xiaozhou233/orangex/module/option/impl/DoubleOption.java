package cn.xiaozhou233.orangex.module.option.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

@Getter
public class DoubleOption extends Option<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final int precision;

    public DoubleOption(String name, String description, Module parent,
                        Double defaultValue, double min, double max, double step, int precision) {
        super(name, description, parent, clamp(defaultValue, min, max));
        this.min = min;
        this.max = max;
        this.step = step;
        this.precision = precision;
    }

    @Override
    public void setValue(Double value) {
        double clamped = clamp(value, min, max);
        double stepped = Math.round(clamped / step) * step;
        double factor = Math.pow(10, precision);
        this.value = Math.round(stepped * factor) / factor;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        setValue(element.getAsDouble());
    }

    private static double clamp(double v, double min, double max) {
        return Math.min(max, Math.max(min, v));
    }
}