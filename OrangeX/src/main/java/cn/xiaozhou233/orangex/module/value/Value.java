package cn.xiaozhou233.orangex.module.value;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Value<T> {

    private final String name;
    private final String description;
    @Setter
    private T value;

    public Value(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

}
