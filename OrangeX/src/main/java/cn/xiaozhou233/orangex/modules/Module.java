package cn.xiaozhou233.orangex.modules;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;
import lombok.Getter;

@Getter
public abstract class Module {
    protected final Minecraft mc = OrangeX.getInstance().getMinecraft();
    protected final String name;
    protected final ModuleCategory category;
    protected boolean enabled = false;

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
    }

    public Module(String name, ModuleCategory category, int defaultKey) {
        this(name, category);
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