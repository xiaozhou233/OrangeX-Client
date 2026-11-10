package cn.xiaozhou233.orangex.value;

import com.google.gson.JsonElement;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A named, typed configuration value owned by a module.
 *
 * <p>Values keep their default so a module can be reset to a known state.
 * Subclasses add type-specific helpers and decide how the value is encoded for
 * configuration storage via {@link #toJson()} and {@link #fromJson(JsonElement)}.</p>
 *
 * @param <T> stored value type.
 */
public abstract class Value<T> {

    private static volatile Runnable globalChangeHook;

    private final String name;
    private final T defaultValue;

    private T value;

    private Supplier<Boolean> visibility = () -> true;
    private Consumer<T> changeListener;

    protected Value(String name, T defaultValue) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Value name must not be empty");
        }
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        this.value = newValue;
        try {
            if (changeListener != null) {
                changeListener.accept(newValue);
            }
        } finally {
            Runnable hook = globalChangeHook;
            if (hook != null) {
                hook.run();
            }
        }
    }

    /**
     * Registers a process-wide callback invoked whenever any value changes.
     * Used by the config manager to track unsaved changes.
     */
    public static void setGlobalChangeHook(Runnable hook) {
        globalChangeHook = hook;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefault() {
        return Objects.equals(value, defaultValue);
    }

    public void reset() {
        setValue(defaultValue);
    }

    /**
     * Hides the value from configuration UIs while the condition is false.
     */
    public Value<T> visibleWhen(Supplier<Boolean> condition) {
        this.visibility = condition == null ? () -> true : condition;
        return this;
    }

    public boolean isVisible() {
        return Boolean.TRUE.equals(visibility.get());
    }

    /**
     * Registers a callback invoked after the value actually changes.
     */
    public Value<T> onChange(Consumer<T> listener) {
        this.changeListener = listener;
        return this;
    }

    /**
     * Encodes the current value for configuration storage.
     */
    public abstract JsonElement toJson();

    /**
     * Restores a previously encoded value. Invalid input is ignored so a
     * malformed config never leaves a module in an unusable state.
     */
    public abstract void fromJson(JsonElement json);

    protected static boolean isBoolean(JsonElement json) {
        return json != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean();
    }

    protected static boolean isNumber(JsonElement json) {
        return json != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber();
    }

    protected static boolean isString(JsonElement json) {
        return json != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isString();
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
