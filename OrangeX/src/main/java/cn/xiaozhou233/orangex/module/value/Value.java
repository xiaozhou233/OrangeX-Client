package cn.xiaozhou233.orangex.module.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Value<T> {
    private static final Gson gson = new Gson();

    private final String name;
    private final String description;
    @Setter
    private T value;

    public Value(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public void fromJson(JsonElement element) {
        setValue(gson.fromJson(element, getValueClass()));
    }

    public abstract Class<T> getValueClass();

}
