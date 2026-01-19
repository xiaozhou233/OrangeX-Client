package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.BooleanValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class BooleanComponent extends ValueComponent {

    private final BooleanValue value;

    public BooleanComponent(double x, double y, double width, BooleanValue value, Component parent) {
        super(x, y, width, 16, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        double boxSize = 8;
        double boxX = ax + 4;
        double boxY = ay + (height - boxSize) / 2;

        int boxColor = value.getValue() ? 0xFF2E8B57 : 0xFF444444;

        if (isHovered(mouseX, mouseY)) {
            boxColor = GuiRenderUtils.blendColor(boxColor, 0xFFFFFFFF, 0.12f);
        }

        GuiRenderUtils.enableBlend();

        drawBackground(mouseX, mouseY);
        GuiRenderUtils.drawRect(boxX, boxY, boxSize, boxSize, boxColor);

        GuiRenderUtils.drawString(value.getName(), ax + 4 + boxSize + 6, ay + 6, 0xFFFFFFFF);

        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            value.setValue(!value.getValue());
            applyValue();
        }
    }

    @Override
    public void applyValue() {
    }
}
