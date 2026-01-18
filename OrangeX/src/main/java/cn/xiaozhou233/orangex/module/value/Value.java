package cn.xiaozhou233.orangex.module.value;

public abstract class Value<T> {

    private final String name;
    private final String description;
    private T value;

    public Value(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
