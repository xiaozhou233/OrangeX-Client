package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.KeybindValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class KeybindComponent extends ValueComponent {

    private final KeybindValue value;
    private boolean listening = false;

    public KeybindComponent(double x, double y, double width, KeybindValue value, Component parent) {
        super(x, y, width, 16, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        int bgColor = 0xFF333333;
        if (isHovered(mouseX, mouseY)) {
            bgColor = GuiRenderUtils.blendColor(bgColor, 0xFFFFFFFF, 0.12f);
        }

        GuiRenderUtils.enableBlend();
        GuiRenderUtils.drawRect(ax, ay, width, height, bgColor);

        String keyName = listening ? "Press a key..." : GameSettings.getKeyDisplayString(value.getValue());
        if (keyName==GameSettings.getKeyDisplayString(Keyboard.KEY_DELETE))
            keyName = "None";
        GuiRenderUtils.drawString(value.getName() + ": " + keyName, ax + 6, ay + 4, 0xFFFFFFFF);
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

        // ESC
        if (keyCode == 1) {
            listening = false;
            return;
        }

        // DELETE
        if (keyCode == Keyboard.KEY_DELETE) {
            value.setValue(Keyboard.KEY_NONE);
            listening = false;
            applyValue();
            return;
        }

        value.setKey(keyCode);
        listening = false;
        applyValue();
    }

    @Override
    public void applyValue() {
        // no-op
    }
}
