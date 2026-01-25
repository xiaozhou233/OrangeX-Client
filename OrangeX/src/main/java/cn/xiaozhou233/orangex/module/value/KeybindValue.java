package cn.xiaozhou233.orangex.module.value;

public class KeybindValue extends Value<Integer> {

    public KeybindValue(String name, String description, Integer defaultValue) {
        super(name, description, defaultValue);
    }

    public void setKey(int key) {
        setValue(key);
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }
}
