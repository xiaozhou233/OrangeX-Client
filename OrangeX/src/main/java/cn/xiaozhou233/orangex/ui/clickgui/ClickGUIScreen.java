package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.ModuleButton;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class ClickGUIScreen extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();

    @Override
    public void initGui() {
        panels.clear();

        int x = 20;

        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> categoryModules = OrangeX.getInstance()
                    .getModuleManager()
                    .getModulesByCategory(category);

            if (categoryModules.isEmpty())
                continue;

            Panel panel = new Panel(
                    category,
                    x,
                    30,
                    120,
                    20
            );

            int y = 20;

            for (Module module : categoryModules) {
                panel.addComponent(
                        new ModuleButton(
                                panel,
                                module,
                                0,
                                y
                        )
                );

                y += 20;
            }

            panels.add(panel);

            x += 140;
        }

        loadPanelPositions();
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
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
        for (Panel panel : panels) {
            panel.keyTyped(
                    typedChar,
                    keyCode
            );
        }

        if (keyCode == 1) {
            mc.displayGuiScreen(null);
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
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