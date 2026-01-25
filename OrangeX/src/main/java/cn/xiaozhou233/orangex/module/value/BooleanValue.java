package cn.xiaozhou233.orangex.module.value;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, String description, Boolean defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }
}
