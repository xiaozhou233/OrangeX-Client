package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.value.*;
import cn.xiaozhou233.orangex.ui.clickgui.panel.Panel;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.ModuleButton;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.BooleanComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.NumberComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.ModeComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.KeybindComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();

    @Override
    public void initGui() {
        panels.clear();

        // build panels for each category
        double startX = 20;
        double startY = 20;
        double panelWidth = 120;
        double gap = 10;

        for (ModuleCategory category : ModuleCategory.values()) {
            Panel panel = new Panel(category.name(), startX, startY, panelWidth);

            // add module buttons
            for (Module module : OrangeX.getInstance().getModuleManager().getModulesByCategory(category)) {
                ModuleButton button = new ModuleButton(0, 0, panelWidth, module, panel);

                // add all values
                for (Value<?> value : module.getValues()) {
                    if (value instanceof BooleanValue) {
                        button.addComponent(new BooleanComponent(0, 0, panelWidth, (BooleanValue) value, button));
                    } else if (value instanceof NumberValue) {
                        button.addComponent(new NumberComponent(0, 0, panelWidth, (NumberValue) value, button));
                    } else if (value instanceof ModeValue) {
                        button.addComponent(new ModeComponent(0, 0, panelWidth, (ModeValue) value, button));
                    } else if (value instanceof KeybindValue) {
                        button.addComponent(new KeybindComponent(0, 0, panelWidth, (KeybindValue) value, button));
                    }
                }

                panel.addComponent(button);
            }

            panels.add(panel);
            startX += panelWidth + gap;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // background
        Gui.drawRect(
                0,
                0,
                width,
                height,
                new Color(50, 50, 50, 100).getRGB()
        );

        // update and render panels
        for (Panel panel : panels) {
            panel.update(mouseX, mouseY);
            panel.render(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Panel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (Panel panel : panels) {
            panel.keyTyped(typedChar, keyCode);
        }

        // close GUI on ESC
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
        }

        super.keyTyped(typedChar, keyCode);
    }
}
