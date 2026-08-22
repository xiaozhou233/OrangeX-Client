package cn.xiaozhou233.orangex.ui.clickgui.component;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Component {

    protected final Panel parent;

    protected final int offsetX;
    @Setter
    protected int offsetY;

    protected final int width;
    protected int height;


    public Component(
            Panel parent,
            int offsetX,
            int offsetY,
            int width,
            int height
    ) {
        this.parent = parent;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
    }


    public abstract void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    );


    public void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {

    }


    public void mouseReleased(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {

    }


    public void keyTyped(
            char typedChar,
            int keyCode
    ) {

    }


    public int getX() {
        return parent.getX() + offsetX;
    }


    public int getY() {
        return parent.getY() + offsetY - (int) parent.getScrollOffset();
    }


    protected boolean isHovered(
            int mouseX,
            int mouseY
    ) {

        return mouseX >= getX()
                && mouseX <= getX() + width
                && mouseY >= getY()
                && mouseY <= getY() + height;
    }


    protected void drawString(
            String text,
            int x,
            int y,
            int color
    ) {

        OrangeX.getInstance()
                .getStbFontManager()
                .getProxima(18)
                .drawString(
                        text,
                        x,
                        y,
                        color
                );
    }
}