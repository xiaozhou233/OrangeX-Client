package cn.xiaozhou233.orangex.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A value limited to a fixed list of named modes, for example
 * "Vanilla" / "Packet" / "Spoof".
 */
public class ModeValue extends Value<String> {

    private final List<String> modes;

    public ModeValue(String name, String defaultMode, String... modes) {
        this(name, defaultMode, Arrays.asList(modes));
    }

    public ModeValue(String name, String defaultMode, Collection<String> modes) {
        super(name, defaultMode);
        if (modes == null || modes.isEmpty()) {
            throw new IllegalArgumentException("Mode value '" + name + "' needs at least one mode");
        }
        this.modes = Collections.unmodifiableList(new ArrayList<>(modes));
        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException(
                    "Default mode '" + defaultMode + "' is not in " + this.modes);
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public String getMode() {
        return getValue();
    }

    public boolean is(String mode) {
        return getValue().equals(mode);
    }

    @Override
    public void setValue(String newValue) {
        if (!modes.contains(newValue)) {
            throw new IllegalArgumentException(
                    "Unknown mode '" + newValue + "' for value '" + getName() + "'");
        }
        super.setValue(newValue);
    }

    public int getIndex() {
        return modes.indexOf(getValue());
    }

    /**
     * Selects the next mode, wrapping around at the end.
     *
     * @return the newly selected mode.
     */
    public String next() {
        String next = modes.get((getIndex() + 1) % modes.size());
        setValue(next);
        return next;
    }

    /**
     * Selects the previous mode, wrapping around at the start.
     *
     * @return the newly selected mode.
     */
    public String previous() {
        String previous = modes.get((getIndex() - 1 + modes.size()) % modes.size());
        setValue(previous);
        return previous;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (isString(json)) {
            String mode = json.getAsString();
            if (modes.contains(mode)) {
                setValue(mode);
            }
        }
    }
}
