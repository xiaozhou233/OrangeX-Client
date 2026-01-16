package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.ui.clickgui.component.PanelUI;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;

public class ClickGuiScreen extends GuiScreen {
    private PanelUI panel;

    @Override
    public void initGui() {
        panel = new PanelUI(10, 10, 100, 150, "Example");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Background
        int color = new Color(50, 50, 50, 100).getRGB();
        Gui.drawRect(0, 0, this.width, this.height, color);

        if (panel != null) {
            panel.draw();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
