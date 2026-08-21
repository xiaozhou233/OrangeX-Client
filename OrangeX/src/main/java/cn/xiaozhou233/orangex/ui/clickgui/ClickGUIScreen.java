package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.ui.clickgui.component.impl.ModuleButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

public class ClickGUIScreen extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();


    @Override
    public void initGui() {

        panels.clear();


        int x = 20;


        for(ModuleCategory category : ModuleCategory.values()) {

            Panel panel = new Panel(
                    category,
                    x,
                    30,
                    120,
                    20
            );


            int y = 20;


            for(Module module :
                    OrangeX.getInstance()
                            .getModuleManager()
                            .getModules()) {


                if(module.getCategory() != category)
                    continue;


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
    }


    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {


        for(Panel panel : panels) {

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


        for(Panel panel : panels) {

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


        for(Panel panel : panels) {

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


        for(Panel panel : panels) {

            panel.keyTyped(
                    typedChar,
                    keyCode
            );
        }


        if(keyCode == 1) {
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
}