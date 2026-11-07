package cn.xiaozhou233.orangex.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * An on/off value, for example "enabled" or "show background".
 */
public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean isEnabled() {
        return getValue();
    }

    public void setEnabled(boolean enabled) {
        setValue(enabled);
    }

    public void toggle() {
        setValue(!isEnabled());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(isEnabled());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (isBoolean(json)) {
            setValue(json.getAsBoolean());
        }
    }
}
