package cn.xiaozhou233.orangex.ui.clickgui.component;

import cn.xiaozhou233.orangex.event.impl.EventRender2D;

public abstract class Component {

    protected float x;
    protected float y;

    protected float width;
    protected float height;

    protected boolean visible = true;
    protected boolean enabled = true;

    public abstract void render(EventRender2D event);

    public void update() {
    }

    public void mouseClicked(float mouseX, float mouseY, int button) {
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
    }

    public void mouseDragged(float mouseX, float mouseY, int button) {
    }

    public void keyTyped(char character, int keyCode) {
    }

    public void setBounds(
            float x,
            float y,
            float width,
            float height
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }
}
