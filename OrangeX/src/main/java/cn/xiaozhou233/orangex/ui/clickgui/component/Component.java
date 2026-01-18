package cn.xiaozhou233.orangex.ui.clickgui.component;

import lombok.Getter;
import lombok.Setter;

public abstract class Component {

    @Setter
    protected double x, y;
    @Getter
    protected double width, height;

    protected Component parent;
    @Getter
    @Setter
    protected boolean visible = true;

    public Component(double x, double y, double width, double height, Component parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
    }

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public void update(int mouseX, int mouseY) {

    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    public void mouseReleased(int mouseX, int mouseY, int button) {

    }

    public void keyTyped(char typedChar, int keyCode) {

    }

    public boolean isHovered(int mouseX, int mouseY) {
        double px = getAbsoluteX();
        double py = getAbsoluteY();
        return mouseX >= px && mouseX <= px + width
                && mouseY >= py && mouseY <= py + height;
    }

    public double getAbsoluteX() {
        return parent == null ? x : parent.getAbsoluteX() + x;
    }

    public double getAbsoluteY() {
        return parent == null ? y : parent.getAbsoluteY() + y;
    }

}
