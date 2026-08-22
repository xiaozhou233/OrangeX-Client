package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

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

    private double scrollOffset;
    private double targetScrollOffset;

    private static final int BOTTOM_PADDING = 20;


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

        int contentHeight = calculateContentHeight();
        int maxVisibleContentHeight = getMaxVisibleContentHeight();

        if (open) {
            animateScroll(contentHeight, maxVisibleContentHeight);

            Gui.drawRect(x, y + headerHeight, x + width, y + headerHeight + maxVisibleContentHeight, 0xff1a1a1a);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int scale = sr.getScaleFactor();
            int visibleHeight = Math.min(maxVisibleContentHeight, contentHeight);
            GL11.glScissor(
                    x * scale,
                    Minecraft.getMinecraft().displayHeight - ((y + headerHeight + visibleHeight) * scale),
                    width * scale,
                    visibleHeight * scale
            );

            for (Component component : components) {
                component.drawScreen(mouseX, mouseY, partialTicks);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        // Header background
        boolean headerHovered = isHovered(mouseX, mouseY);
        Gui.drawRect(x + 1, y + 1, x + width - 1, y + headerHeight, headerHovered ? 0xff353535 : 0xff2d2d2d);

        // Category name
        OrangeX.getInstance()
                .getStbFontManager()
                .getProxima(18)
                .drawString(category.getName(), x + 5, y + 5, 0xffffffff);

        // Collapse indicator
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

        // Borders
        int panelHeight = headerHeight + (open ? maxVisibleContentHeight : 0);
        Gui.drawRect(x, y, x + width, y + 1, 0xff3d3d3d);
        Gui.drawRect(x, y + panelHeight - 1, x + width, y + panelHeight, 0xff3d3d3d);
        Gui.drawRect(x, y, x + 1, y + panelHeight, 0xff3d3d3d);
        Gui.drawRect(x + width - 1, y, x + width, y + panelHeight, 0xff3d3d3d);
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

        int maxVisibleContentHeight = getMaxVisibleContentHeight();
        if (mouseY < y + headerHeight || mouseY > y + headerHeight + maxVisibleContentHeight)
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


    public void mouseWheel(int delta) {
        if (!open) return;
        int contentHeight = calculateContentHeight();
        int maxVisibleContentHeight = getMaxVisibleContentHeight();
        int maxScroll = Math.max(0, contentHeight - maxVisibleContentHeight);
        targetScrollOffset -= delta * 0.5;
        targetScrollOffset = Math.max(0, Math.min(maxScroll, targetScrollOffset));
    }


    private void animateScroll(int contentHeight, int maxVisibleContentHeight) {
        int maxScroll = Math.max(0, contentHeight - maxVisibleContentHeight);
        targetScrollOffset = Math.max(0, Math.min(maxScroll, targetScrollOffset));

        double diff = targetScrollOffset - scrollOffset;
        if (Math.abs(diff) > 0.5) {
            scrollOffset += diff * 0.3;
        } else {
            scrollOffset = targetScrollOffset;
        }
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }


    private int calculateContentHeight() {
        int total = 0;
        for (Component component : components) {
            total += component.getHeight();
        }
        return total;
    }


    private int getMaxVisibleContentHeight() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int screenHeight = sr.getScaledHeight();
        return Math.max(0, screenHeight - y - headerHeight - BOTTOM_PADDING);
    }


    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + headerHeight;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int maxVisibleContentHeight = getMaxVisibleContentHeight();
        int panelHeight = headerHeight + (open ? maxVisibleContentHeight : 0);
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

        int contentHeight = calculateContentHeight();
        int maxVisibleContentHeight = getMaxVisibleContentHeight();
        int maxScroll = Math.max(0, contentHeight - maxVisibleContentHeight);
        targetScrollOffset = Math.max(0, Math.min(maxScroll, targetScrollOffset));
    }

    }