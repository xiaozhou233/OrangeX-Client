package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.module.value.Value;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected static final FontRenderer fr = mc.fontRendererObj;

    protected final String name;
    @Getter
    @Setter
    protected KeybindValue keyBind = new KeybindValue("Bind", "KeyBinding", 0);
    protected final ModuleCategory category;
    protected boolean enabled = false;
    @Getter
    protected final List<Value<?>> values = new ArrayList<>();

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;

        addValue(keyBind);
    }

    public Module(String name, ModuleCategory category, int defaultKey) {
        this(name, category);
        this.keyBind.setKey(defaultKey);
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

    protected void addValue(Value<?> value) {
        values.add(value);
    }

    @SuppressWarnings("unchecked")
    public <T> Value<T> getValue(String name) {
        for (Value<?> v : values) {
            if (v.getName().equalsIgnoreCase(name)) {
                return (Value<T>) v;
            }
        }
        return null;
    }

}
