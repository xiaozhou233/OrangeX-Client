package cn.xiaozhou233.orangex.ui.clickgui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelUI {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private int x, y, width, height;
    private final String title;

    /* ================= Drag ================= */

    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    /* ================= Layout ================= */

    private static final int TITLE_HEIGHT = 15;
    private final List<Component> components = new ArrayList<>();

    public PanelUI(int x, int y, int width, int height, String title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    /* ================= Render ================= */

    public void draw(int mouseX, int mouseY) {
        // Handle dragging
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }

        // Title bar
        Gui.drawRect(
                x,
                y,
                x + width,
                y + TITLE_HEIGHT,
                new Color(32, 32, 32, 200).getRGB()
        );

        mc.fontRendererObj.drawString(
                title,
                x + 4,
                y + 4,
                0xFFFFFFFF
        );

        // Auto layout components
        int offsetY = y + TITLE_HEIGHT;

        for (Component component : components) {
            component.x = x;
            component.y = offsetY;
            component.width = width;

            component.draw(mouseX, mouseY);

            offsetY += component.getHeight();
        }

        // Panel background (based on content height)
        height = offsetY - y;

        Gui.drawRect(
                x,
                y + TITLE_HEIGHT,
                x + width,
                y + height,
                new Color(60, 60, 60, 180).getRGB()
        );
    }

    /* ================= Mouse ================= */

    public void mouseClicked(int mouseX, int mouseY, int button) {
        // Drag panel
        if (button == 0 && isHoveringTitle(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return;
        }

        // Forward to components
        for (Component component : components) {
            component.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(int mouseButton) {
        if (mouseButton == 0) {
            dragging = false;
        }
    }

    /* ================= Utils ================= */

    public void addComponent(Component component) {
        components.add(component);
    }

    private boolean isHoveringTitle(int mouseX, int mouseY) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + TITLE_HEIGHT;
    }
}
