package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.DragComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class NumberComponent extends DragComponent {

    private final NumberValue value;

    public NumberComponent(double x, double y, double width, NumberValue value, Component parent) {
        super(x, y, width, 14, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        double min = value.getMin();
        double max = value.getMax();
        double cur = value.getValue();

        double ratio = (cur - min) / (max - min);
        ratio = Math.max(0, Math.min(1, ratio));

        double fillWidth = ratio * (width - 10);

        int bgColor = 0xFF333333;
        int fillColor = 0xFF2E8B57;

        if (isHovered(mouseX, mouseY) || dragging) {
            bgColor = GuiRenderUtils.blendColor(bgColor, 0xFFFFFFFF, 0.12f);
        }

        GuiRenderUtils.enableBlend();
        GuiRenderUtils.drawRect(ax, ay, width, height, bgColor);
        GuiRenderUtils.drawRect(ax + 5, ay + 9, fillWidth, 2, fillColor);

        String text = value.getName() + ": " + String.format("%.2f", cur);
        GuiRenderUtils.drawString(text, ax + 6, ay + 3, 0xFFFFFFFF);
        GuiRenderUtils.disableBlend();
    }

    @Override
    protected void onDrag(int mouseX, int mouseY) {
        double ax = getAbsoluteX();
        double rel = mouseX - ax - 5;
        double ratio = rel / (width - 10);
        ratio = Math.max(0, Math.min(1, ratio));

        double newVal = value.getMin() + ratio * (value.getMax() - value.getMin());
        value.setValue(newVal);
        applyValue();
    }

    @Override
    public void applyValue() {
        // hook point
    }
}
