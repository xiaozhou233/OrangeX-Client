package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class NumberComponent extends ValueComponent {

    private final NumberValue value;
    private boolean dragging = false;

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
        double fillWidth = ratio * (width - 10);

        int bgColor = 0xFF333333;
        int fillColor = 0xFF2E8B57;

        if (isHovered(mouseX, mouseY)) {
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
    public void update(int mouseX, int mouseY) {
        if (!dragging) return;

        double ax = getAbsoluteX();
        double rel = mouseX - ax - 5;
        double ratio = rel / (width - 10);
        ratio = Math.max(0, Math.min(1, ratio));

        double newVal = value.getMin() + ratio * (value.getMax() - value.getMin());
        value.setValue(newVal);
        applyValue();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) dragging = true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
    }

    @Override
    public void applyValue() {
        // no-op
    }
}
