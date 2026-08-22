package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.ModuleButton;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUIScreen extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();

    @Override
    public void initGui() {
        panels.clear();

        int x = 20;
        int y = 30;
        int panelWidth = 120;
        int gap = 20;
        int rowHeight = 30;

        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> categoryModules = OrangeX.getInstance()
                    .getModuleManager()
                    .getModulesByCategory(category);

            if (categoryModules.isEmpty())
                continue;

            if (x + panelWidth > this.width) {
                x = 20;
                y += 300 + rowHeight;
            }

            Panel panel = new Panel(
                    category,
                    x,
                    y,
                    panelWidth,
                    20
            );

            int moduleY = 20;

            for (Module module : categoryModules) {
                panel.addComponent(
                        new ModuleButton(
                                panel,
                                module,
                                0,
                                moduleY
                        )
                );

                moduleY += 20;
            }

            panels.add(panel);

            x += panelWidth + gap;
        }

        loadPanelPositions();
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        Gui.drawRect(0, 0, width, height, new Color(50, 50, 50, 100).getRGB());

        for (Panel panel : panels) {
            panel.drawScreen(
                    mouseX,
                    mouseY,
                    partialTicks
            );
        }

        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws java.io.IOException {
        for (Panel panel : panels) {
            panel.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton
            );
        }

        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    protected void mouseReleased(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        for (Panel panel : panels) {
            panel.mouseReleased(
                    mouseX,
                    mouseY,
                    mouseButton
            );
        }

        super.mouseReleased(
                mouseX,
                mouseY,
                mouseButton
        );
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode
    ) throws java.io.IOException {
        boolean consumed = false;
        for (Panel panel : panels) {
            if (panel.keyTyped(typedChar, keyCode))
                consumed = true;
        }

        if (keyCode == 1 && !consumed) {
            mc.displayGuiScreen(null);
        }

        if (!consumed) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            for (Panel panel : panels) {
                if (panel.isMouseOver(mx, my)) {
                    panel.mouseWheel(wheel);
                    break;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        savePanelPositions();
    }

    private void savePanelPositions() {
        JsonObject data = new JsonObject();

        for (Panel panel : panels) {
            JsonObject panelObj = new JsonObject();
            panelObj.addProperty("x", panel.getX());
            panelObj.addProperty("y", panel.getY());
            panelObj.addProperty("open", panel.isOpen());
            data.add(panel.getCategory().getName(), panelObj);
        }

        OrangeX.getInstance()
                .getModuleManager()
                .getConfig()
                .saveClickGui(data);
    }

    private void loadPanelPositions() {
        JsonObject data = OrangeX.getInstance()
                .getModuleManager()
                .getConfig()
                .loadClickGui();

        for (Panel panel : panels) {
            String categoryName = panel.getCategory().getName();
            if (data.has(categoryName)) {
                JsonObject panelObj = data.getAsJsonObject(categoryName);
                panel.setX(panelObj.get("x").getAsInt());
                panel.setY(panelObj.get("y").getAsInt());
                if (panelObj.has("open")) {
                    panel.setOpen(panelObj.get("open").getAsBoolean());
                }
            }
        }
    }
}