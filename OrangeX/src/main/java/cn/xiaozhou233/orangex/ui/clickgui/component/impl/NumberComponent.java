package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.NumberValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public class NumberComponent extends ValueComponent {

    private final NumberValue value;
    private boolean dragging;

    public NumberComponent(double x, double y, double width, NumberValue value, Component parent) {
        super(x, y, width, 16, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        GuiRenderUtils.enableBlend();

        // background
        GuiRenderUtils.drawRect(ax, ay, width, height, 0x80000000);

        // name + value text
        String text = value.getName() + ": " + String.format("%.1f", value.getValue());
        GuiRenderUtils.drawString(text, ax + 4, ay + 3, 0xFFFFFFFF);

        // slider background
        double barX = ax + 4;
        double barY = ay + 12;
        double barW = width - 8;
        double barH = 3;

        GuiRenderUtils.drawRect(barX, barY, barW, barH, 0xFF444444);

        // slider fill
        double ratio = (value.getValue() - value.getMin()) / (value.getMax() - value.getMin());
        double fillW = barW * ratio;
        GuiRenderUtils.drawRect(barX, barY, fillW, barH, 0xFF00AAFF);

        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            dragging = true;
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
    }

    @Override
    public void update(int mouseX, int mouseY) {
        if (dragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(int mouseX) {
        double rel = (mouseX - getAbsoluteX() - 4) / (width - 8);
        rel = Math.max(0, Math.min(1, rel));
        double newVal = value.getMin() + (value.getMax() - value.getMin()) * rel;

        // snap to increment
        double inc = value.getIncrement();
        newVal = Math.round(newVal / inc) * inc;

        value.setValue(newVal);
    }

    @Override
    public void applyValue() {
        // optional
    }
}
