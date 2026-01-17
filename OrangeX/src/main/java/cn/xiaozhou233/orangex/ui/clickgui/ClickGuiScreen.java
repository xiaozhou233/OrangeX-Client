package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.ui.clickgui.component.ModuleButton;
import cn.xiaozhou233.orangex.ui.clickgui.component.PanelUI;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;

public class ClickGuiScreen extends GuiScreen {

    private PanelUI panel;

    @Override
    public void initGui() {
        panel = new PanelUI(10, 10, 120, 180, "Combat");

        for (Module module : OrangeX.getModuleManager().getAllModules()) {
            panel.addComponent(new ModuleButton(module, 120));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Background
        Gui.drawRect(
                0,
                0,
                width,
                height,
                new Color(50, 50, 50, 100).getRGB()
        );

        if (panel != null) {
            panel.draw(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (panel != null) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (panel != null) {
            panel.mouseReleased(state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
