package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class ModeComponent extends ValueComponent {

    private final ModeValue value;

    public ModeComponent(double x, double y, double width, ModeValue value, Component parent) {
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

        String text = value.getName() + ": " + value.getValue();
        GuiRenderUtils.drawString(text, ax + 6, ay + 4, 0xFFFFFFFF);
        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;

        if (button == 0) value.next();
        else if (button == 1) value.previous();

        applyValue();
    }

    @Override
    public void applyValue() {
        // no-op
    }
}
