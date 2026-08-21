package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Panel {

    private final ModuleCategory category;

    private int x;
    private int y;

    private final int width;
    private final int height;

    private final int headerHeight = 20;

    private boolean open = true;

    private boolean dragging;

    private int dragX;
    private int dragY;

    private final List<Component> components = new ArrayList<>();


    public Panel(ModuleCategory category,int x,int y,int width,int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public void drawScreen(int mouseX,int mouseY,float partialTicks) {

        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }


        drawBackground();


        if (!open)
            return;


        for (Component component : components) {
            component.drawScreen(mouseX,mouseY,partialTicks);
        }
    }


    private void drawBackground() {

    }


    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if (isHovered(mouseX,mouseY)) {

            if (mouseButton == 0) {

                dragging = true;

                dragX = mouseX - x;
                dragY = mouseY - y;
            }

            if (mouseButton == 1) {
                open = !open;
            }
        }


        if (!open)
            return;


        for (Component component : components) {
            component.mouseClicked(mouseX,mouseY,mouseButton);
        }
    }


    public void mouseReleased(int mouseX,int mouseY,int mouseButton) {

        if (mouseButton == 0)
            dragging = false;


        for (Component component : components) {
            component.mouseReleased(mouseX,mouseY,mouseButton);
        }
    }


    public void keyTyped(char typedChar,int keyCode) {

        for (Component component : components) {
            component.keyTyped(typedChar,keyCode);
        }
    }


    private boolean isHovered(int mouseX,int mouseY) {

        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + headerHeight;
    }


    public void addComponent(Component component) {
        components.add(component);

        updateLayout();
    }

    public void updateLayout() {

        int offset = headerHeight;


        for(Component component : components) {

            component.setOffsetY(offset);

            offset += component.getHeight();
        }
    }
}