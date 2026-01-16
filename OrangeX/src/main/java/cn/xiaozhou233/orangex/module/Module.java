package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

@Getter
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected static final FontRenderer fr = mc.fontRendererObj;

    protected final String name;
    @Setter
    protected int keyBind = -1;
    protected final ModuleCategory category;
    protected boolean enabled = false;

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
    }

    public Module(String name, ModuleCategory category, int defaultKey) {
        this(name, category);
        this.keyBind = defaultKey;
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
            if (OrangeX.getEventBus().isRegistered(this))
                return;
            try { OrangeX.getEventBus().register(this); } catch (Exception ignored) {}
            onEnable();
        } else {
            if (!OrangeX.getEventBus().isRegistered(this))
                return;
            try { OrangeX.getEventBus().unregister(this); } catch (Exception ignored) {}
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }
}
