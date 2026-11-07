package cn.xiaozhou233.orangex.modules;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.input.KeyCodes;
import cn.xiaozhou233.orangex.value.BooleanValue;
import cn.xiaozhou233.orangex.value.KeybindValue;
import cn.xiaozhou233.orangex.value.ModeValue;
import cn.xiaozhou233.orangex.value.Value;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class Module {
    protected final Minecraft mc = OrangeX.getInstance().getMinecraft();
    protected final String name;
    protected final ModuleCategory category;
    protected boolean enabled = false;

    private final Map<String, Value<?>> valueMap = new LinkedHashMap<>();
    private final KeybindValue keybind;

    public Module(String name, ModuleCategory category) {
        this(name, category, KeyCodes.NONE);
    }

    public Module(String name, ModuleCategory category, int defaultKey) {
        this.name = name;
        this.category = category;
        this.keybind = new KeybindValue("keybind", defaultKey);
        valueMap.put(keybind.getName(), keybind);
    }

    /**
     * Registers a value so it can be displayed by configuration UIs and
     * persisted with the module.
     *
     * @return the same value, for fluent field initialisation.
     */
    protected <V extends Value<?>> V addValue(V value) {
        if (valueMap.containsKey(value.getName())) {
            throw new IllegalArgumentException(
                    "Module '" + name + "' already has a value named '" + value.getName() + "'");
        }
        valueMap.put(value.getName(), value);
        return value;
    }

    protected BooleanValue addBoolean(String name, boolean defaultValue) {
        return addValue(new BooleanValue(name, defaultValue));
    }

    protected KeybindValue addKeybind(String name, int defaultKey) {
        return addValue(new KeybindValue(name, defaultKey));
    }

    protected ModeValue addMode(String name, String defaultMode, String... modes) {
        return addValue(new ModeValue(name, defaultMode, modes));
    }

    public Value<?> getValue(String name) {
        return valueMap.get(name);
    }

    public List<Value<?>> getValues() {
        return Collections.unmodifiableList(new ArrayList<>(valueMap.values()));
    }

    public List<Value<?>> getVisibleValues() {
        List<Value<?>> visible = new ArrayList<>();
        for (Value<?> value : valueMap.values()) {
            if (value.isVisible()) {
                visible.add(value);
            }
        }
        return visible;
    }

    public void resetValues() {
        for (Value<?> value : valueMap.values()) {
            value.reset();
        }
    }

    /**
     * Encodes every value (including the module keybind) for configuration
     * storage.
     */
    public JsonObject valuesToJson() {
        JsonObject json = new JsonObject();
        for (Value<?> value : valueMap.values()) {
            json.add(value.getName(), value.toJson());
        }
        return json;
    }

    /**
     * Restores previously encoded values. Unknown keys are ignored.
     */
    public void valuesFromJson(JsonObject json) {
        if (json == null) {
            return;
        }
        for (Value<?> value : valueMap.values()) {
            if (json.has(value.getName())) {
                value.fromJson(json.get(value.getName()));
            }
        }
    }

    protected void onEnable() {
        System.out.println("[OrangeX] Enable " + name);
    }

    protected void onDisable() {
        System.out.println("[OrangeX] Disable " + name);
    }

    public void setEnabled(boolean setEnabled) {
        if (this.enabled == setEnabled) return;

        this.enabled = setEnabled;

        if (this.enabled) {
            if (OrangeX.getInstance().getEventBus().isRegistered(this))
                return;
            try { OrangeX.getInstance().getEventBus().register(this); } catch (Exception ignored) {}
            onEnable();
        } else {
            if (!OrangeX.getInstance().getEventBus().isRegistered(this))
                return;
            try { OrangeX.getInstance().getEventBus().unregister(this); } catch (Exception ignored) {}
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

}