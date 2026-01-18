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

        int bgColor = value.getValue() ? 0xFF2E8B57 : 0xFF444444;
        if (isHovered(mouseX, mouseY)) {
            bgColor = GuiRenderUtils.blendColor(bgColor, 0xFFFFFFFF, 0.12f);
        }

        GuiRenderUtils.enableBlend();
        GuiRenderUtils.drawRect(ax, ay, width, height, bgColor);
        GuiRenderUtils.drawString(value.getName(), ax + 6, ay + 4, 0xFFFFFFFF);
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
        // no-op: value 已经直接设置
    }
}
