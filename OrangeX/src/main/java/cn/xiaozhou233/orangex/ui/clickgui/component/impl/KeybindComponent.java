package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class KeybindComponent extends ValueComponent {

    private final KeybindValue value;
    private boolean listening;

    private final Minecraft mc = Minecraft.getMinecraft();

    public KeybindComponent(double x, double y, double width, KeybindValue value, Component parent) {
        super(x, y, width, 14, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        GuiRenderUtils.enableBlend();

        // background
        GuiRenderUtils.drawRect(ax, ay, width, height, 0x80000000);

        // name
        GuiRenderUtils.drawString(value.getName(), ax + 4, ay + 3, 0xFFFFFFFF);

        // key text
        String keyText = listening ? "Press..." : Keyboard.getKeyName(value.getValue());
        GuiRenderUtils.drawString(keyText, ax + width - 4 - mc.fontRendererObj.getStringWidth(keyText), ay + 3, 0xFFFFFFFF);

        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            listening = true;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!listening) return;

        value.setKey(keyCode);
        listening = false;
    }

    @Override
    public void applyValue() {
        // optional
    }
}
