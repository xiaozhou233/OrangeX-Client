package cn.xiaozhou233.orangex.ui.altmanager;

import cn.xiaozhou233.orangex.alts.AltLoginThread;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class GUIAddMicrosoft extends GuiScreen {

    private AltLoginThread loginThread;

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, this.width / 2 - 50, this.height / 2 + 20, 100, 20, "Start Login"));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height / 2 + 45, 100, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                if (loginThread == null || !loginThread.isAlive()) {
                    loginThread = new AltLoginThread(null);
                    loginThread.start();
                }
                break;

            case 0:
                mc.displayGuiScreen(new GUIAltManager());
                break;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "Add Microsoft", width / 2, height / 2 - 40, 0xFFFFFF);

        String status = "NONE";
        if (loginThread != null) {
            status = loginThread.getStatus();
        }

        drawCenteredString(fontRendererObj, "Status: " + status, width / 2, height / 2 - 10, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
