package cn.xiaozhou233.orangex.ui.altmanager;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.alts.Alt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.UUID;

public class GUIAddCracked extends GuiScreen {

    private GuiTextField nameField;

    @Override
    public void initGui() {
        this.buttonList.clear();

        nameField = new GuiTextField(0, fontRendererObj, this.width / 2 - 100, this.height / 2 - 10, 200, 20);
        nameField.setMaxStringLength(32);

        this.buttonList.add(new GuiButton(1, this.width / 2 - 50, this.height / 2 + 20, 100, 20, "Save"));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height / 2 + 45, 100, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                save();
                break;
            case 0:
                mc.displayGuiScreen(new GUIAltManager());
                break;
        }
    }

    private void save() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            Alt alt = new Alt(
                    name,
                    UUID.randomUUID(),
                    "",
                    0L
            );
            OrangeX.getInstance().getAltManager().addAlt(alt);
            mc.displayGuiScreen(new GUIAltManager());
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        nameField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        nameField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == 28) {
            save();
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Add Cracked", width / 2, height / 2 - 30, 0xFFFFFF);

        nameField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
