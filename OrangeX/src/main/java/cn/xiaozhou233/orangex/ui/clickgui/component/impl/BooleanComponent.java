package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class BooleanComponent extends ValueComponent {

    private final BooleanValue value;

    public BooleanComponent(double x, double y, double width, BooleanValue value, Component parent) {
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

        // name text
        GuiRenderUtils.drawString(value.getName(), ax + 4, ay + 3, 0xFFFFFFFF);

        // toggle indicator
        int color = value.getValue() ? 0xFF00FF00 : 0xFFAAAAAA;
        GuiRenderUtils.drawRect(ax + width - 14, ay + 3, 10, 8, color);

        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            value.setValue(!value.getValue());
        }
    }

    @Override
    public void applyValue() {
        // optional
    }
}
