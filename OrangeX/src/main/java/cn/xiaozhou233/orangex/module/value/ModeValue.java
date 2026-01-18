package cn.xiaozhou233.orangex.module.value;

import java.util.List;

public class ModeValue extends Value<String> {

    private final List<String> modes;

    public ModeValue(String name, String description, String defaultValue, List<String> modes) {
        super(name, description, defaultValue);
        this.modes = modes;
    }

    public List<String> getModes() {
        return modes;
    }

    public int getIndex() {
        return modes.indexOf(getValue());
    }

    public void setIndex(int index) {
        if (index < 0 || index >= modes.size()) return;
        setValue(modes.get(index));
    }
}
