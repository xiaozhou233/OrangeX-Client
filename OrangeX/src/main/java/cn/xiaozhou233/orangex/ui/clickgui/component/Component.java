package cn.xiaozhou233.orangex.ui.clickgui.component;

public abstract class Component {

    protected int x, y, width, height;

    public abstract void draw(int mouseX, int mouseY);

    public void mouseClicked(int mouseX, int mouseY, int button) {}

    public int getHeight() {
        return height;
    }

    protected boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }
}
