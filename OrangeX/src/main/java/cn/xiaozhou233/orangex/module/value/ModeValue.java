package cn.xiaozhou233.orangex.module.value;

import lombok.Getter;

import java.util.List;

@Getter
public class ModeValue extends Value<String> {

    private final List<String> modes;

    public ModeValue(String name, String description, String defaultValue, List<String> modes) {
        super(name, description, defaultValue);
        this.modes = modes;
    }

    public int getIndex() {
        return modes.indexOf(getValue());
    }

    public void setIndex(int index) {
        if (index < 0 || index >= modes.size()) return;
        setValue(modes.get(index));
    }

    public void next() {
        int index = getIndex();
        if (index < 0) index = 0;
        index++;
        if (index >= modes.size()) index = 0;
        setIndex(index);
    }

    public void previous() {
        int index = getIndex();
        if (index < 0) index = 0;
        index--;
        if (index < 0) index = modes.size() - 1;
        setIndex(index);
    }

    public String getCurrentMode() {
        return getValue();
    }

    public boolean isValidMode(String mode) {
        return modes.contains(mode);
    }


    @Override
    public Class<String> getValueClass() {
        return String.class;
    }
}
