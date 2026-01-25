package cn.xiaozhou233.orangex.module.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
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
        try {
            setValue(gson.fromJson(element, getValueClass()));
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to parse json for value " + name);
            e.printStackTrace();
        }
    }

    public abstract Class<T> getValueClass();

}
