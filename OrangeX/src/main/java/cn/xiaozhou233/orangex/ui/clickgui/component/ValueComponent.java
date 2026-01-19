package cn.xiaozhou233.orangex.ui.clickgui.component;

import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;

public abstract class ValueComponent extends Component {

    public ValueComponent(double x, double y, double width, double height, Component parent) {
        super(x, y, width, height, parent);
    }

    public abstract void applyValue();

    protected void drawBackground(int mouseX, int mouseY) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        int bgColor = 0xFF333333;
        if (isHovered(mouseX, mouseY)) {
            bgColor = GuiRenderUtils.blendColor(bgColor, 0xFFFFFFFF, 0.12f);
        }
        GuiRenderUtils.drawRect(ax, ay, width, height, bgColor);
    }

}
