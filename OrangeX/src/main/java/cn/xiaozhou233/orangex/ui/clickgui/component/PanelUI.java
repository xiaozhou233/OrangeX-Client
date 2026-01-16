package cn.xiaozhou233.orangex.ui.clickgui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import java.awt.*;

public class PanelUI {
    private int x, y, width, height;
    private String title;

    public PanelUI(int x, int y, int width, int height, String title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void draw() {
        Gui.drawRect(x, y, x+width, y+height, new Color(32, 32, 32, 204).getRGB());

    }
}
