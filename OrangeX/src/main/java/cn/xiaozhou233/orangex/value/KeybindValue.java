package cn.xiaozhou233.orangex.value;

import cn.xiaozhou233.orangex.input.KeyCodes;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * A single key binding. The stored key always uses the canonical 1.8.9 /
 * LWJGL 2 numbering used by
 * {@link cn.xiaozhou233.orangex.event.impl.EventKey}; {@link KeyCodes#NONE}
 * means the binding is unbound.
 */
public class KeybindValue extends Value<Integer> {

    public KeybindValue(String name, int defaultKey) {
        super(name, normalize(defaultKey));
    }

    public int getKey() {
        return getValue();
    }

    public void setKey(int key) {
        setValue(normalize(key));
    }

    public boolean isBound() {
        return getKey() != KeyCodes.NONE;
    }

    public void clear() {
        setKey(KeyCodes.NONE);
    }

    /**
     * @param key canonical 1.8.9 (LWJGL 2) key code.
     * @return whether this binding is set and matches {@code key}.
     */
    public boolean matches(int key) {
        return isBound() && getKey() == key;
    }

    private static int normalize(int key) {
        return key < 0 ? KeyCodes.NONE : key;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getKey());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (isNumber(json)) {
            setKey(json.getAsInt());
        }
    }
}
