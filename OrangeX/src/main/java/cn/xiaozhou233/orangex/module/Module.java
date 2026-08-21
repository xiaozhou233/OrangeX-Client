package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.option.Option;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected final String name;
    protected final String description;
    protected final ModuleCategory category;
    @Setter
    protected int key = Keyboard.KEY_NONE;
    protected boolean enabled = false;
    protected final List<Option<?>> options = new ArrayList<>();

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, String description, ModuleCategory category, int key) {
        this(name, description, category);
        this.key = key;
    }

    public Module(String name, String description, ModuleCategory category, int key, boolean enabled) {
        this(name, description, category, key);
        this.enabled = enabled;
    }

    public void addOption(Option<?> option) {
        options.add(option);
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
