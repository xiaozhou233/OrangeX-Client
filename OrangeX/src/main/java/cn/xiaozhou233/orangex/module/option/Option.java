package cn.xiaozhou233.orangex.module.option;

import cn.xiaozhou233.orangex.module.Module;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Option<T> {
    protected final String name;
    protected final String description;
    protected final Module parent;
    @Setter
    protected T value;
    protected final T defaultValue;

    public Option(String name, String description, Module parent, T defaultValue) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public abstract JsonElement serialize();
    public abstract void deserialize(JsonElement element);
}