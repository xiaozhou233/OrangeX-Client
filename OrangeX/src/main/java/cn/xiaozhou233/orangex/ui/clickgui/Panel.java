package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Panel {

    private final ModuleCategory category;

    @Setter
    private int x;
    @Setter
    private int y;

    private final int width;
    private final int height;

    private final int headerHeight = 20;

    @Setter
    private boolean open = true;

    private boolean dragging;

    private int dragX;
    private int dragY;

    private final List<Component> components = new ArrayList<>();

    private int scrollOffset;

    private int maxScroll;


    public Panel(ModuleCategory category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        drawBackground(mouseX, mouseY);

        if (!open)
            return;

        for (Component component : components) {
            component.drawScreen(mouseX, mouseY, partialTicks);
        }
    }


    private void drawBackground(int mouseX, int mouseY) {
        int contentHeight = calculateContentHeight();
        int panelHeight = headerHeight + (open ? contentHeight : 0);

        Gui.drawRect(x, y, x + width, y + panelHeight, 0xff1a1a1a);

        Gui.drawRect(x, y, x + width, y + 1, 0xff3d3d3d);
        Gui.drawRect(x, y + panelHeight - 1, x + width, y + panelHeight, 0xff3d3d3d);
        Gui.drawRect(x, y, x + 1, y + panelHeight, 0xff3d3d3d);
        Gui.drawRect(x + width - 1, y, x + width, y + panelHeight, 0xff3d3d3d);

        boolean headerHovered = isHovered(mouseX, mouseY);
        Gui.drawRect(x + 1, y + 1, x + width - 1, y + headerHeight, headerHovered ? 0xff353535 : 0xff2d2d2d);

        OrangeX.getInstance()
                .getStbFontManager()
                .getProxima(18)
                .drawString(category.getName(), x + 5, y + 5, 0xffffffff);

        String indicator = open ? "-" : "+";
        int indicatorX = x + width - 5 -
                (int) OrangeX.getInstance()
                        .getStbFontManager()
                        .getProxima(18)
                        .getStringWidth(indicator);
        OrangeX.getInstance()
                .getStbFontManager()
                .getProxima(18)
                .drawString(indicator, indicatorX, y + 5, 0xffaaaaaa);
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
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
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }


    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0)
            dragging = false;

        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }


    public void keyTyped(char typedChar, int keyCode) {
        for (Component component : components) {
            component.keyTyped(typedChar, keyCode);
        }
    }


    public void handleMouseInput(int amount) {
        if (!open) return;

        scrollOffset += amount;
        updateMaxScroll();
        scrollOffset = Math.max(Math.min(scrollOffset, 0), -maxScroll);
    }


    private void updateMaxScroll() {
        int contentHeight = calculateContentHeight();
        maxScroll = Math.max(0, contentHeight - (height - headerHeight));
    }


    private int calculateContentHeight() {
        int total = 0;
        for (Component component : components) {
            total += component.getHeight();
        }
        return total;
    }


    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + headerHeight;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int contentHeight = calculateContentHeight();
        int panelHeight = headerHeight + (open ? contentHeight : 0);
        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + panelHeight;
    }


    public void addComponent(Component component) {
        components.add(component);
        updateLayout();
    }

    public void updateLayout() {
        int offset = headerHeight;

        for (Component component : components) {
            component.setOffsetY(offset);
            offset += component.getHeight();
        }

        updateMaxScroll();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }
}