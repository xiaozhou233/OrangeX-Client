package cn.xiaozhou233.orangex.ui.clickgui.component;

import org.lwjgl.input.Mouse;

public abstract class DragComponent extends ValueComponent {

    protected boolean dragging = false;

    public DragComponent(double x, double y, double width, double height, Component parent) {
        super(x, y, width, height, parent);
    }

    // Called when dragging starts
    protected void onDragStart(int mouseX, int mouseY) {}

    // Called every tick while dragging
    protected abstract void onDrag(int mouseX, int mouseY);

    // Called when dragging ends
    protected void onDragEnd(int mouseX, int mouseY) {}

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        if (!isHovered(mouseX, mouseY)) return;

        dragging = true;
        onDragStart(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            onDragEnd(mouseX, mouseY);
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        // Safety release if mouseReleased event is lost
        if (dragging && !Mouse.isButtonDown(0)) {
            dragging = false;
            onDragEnd(mouseX, mouseY);
            return;
        }

        if (dragging) {
            onDrag(mouseX, mouseY);
        }
    }
}
