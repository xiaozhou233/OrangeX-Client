package cn.xiaozhou233.orangex.ui.altmanager;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.alts.Alt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GUIAltManager extends GuiScreen {

    private int selectedIndex = -1;
    private int scroll = 0;

    // Double click detect
    private int lastClickIndex = -1;
    private long lastClickTime = 0L;

    // Double click interval (ms)
    private static final long DOUBLE_CLICK_DELAY = 250L;

    @Override
    public void initGui() {
        this.buttonList.clear();

        // Reset click state
        selectedIndex = -1;
        lastClickIndex = -1;
        lastClickTime = 0L;

        // Add
        this.buttonList.add(new GuiButton(1, this.width - 110, 10, 100, 20, "Add Cracked"));
        this.buttonList.add(new GuiButton(2, this.width - 110, 35, 100, 20, "Add Microsoft"));

        // Back
        this.buttonList.add(new GuiButton(3, 10, this.height - 30, 60, 20, "Back"));

        // Delete
        this.buttonList.add(new GuiButton(5, 80, this.height - 30, 60, 20, "Delete"));

        // Login
        this.buttonList.add(new GuiButton(4, this.width - 70, this.height - 30, 60, 20, "Login"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        List<Alt> alts = OrangeX.getInstance().getAltManager().getAlts();

        switch (button.id) {
            case 1:
                mc.displayGuiScreen(new GUIAddCracked());
                break;

            case 2:
                mc.displayGuiScreen(new GUIAddMicrosoft());
                break;

            case 3:
                mc.displayGuiScreen(new GuiMultiplayer(null));
                break;

            case 4:
                if (selectedIndex >= 0 && selectedIndex < alts.size()) {
                    Alt alt = alts.get(selectedIndex);
                    OrangeX.getInstance().getAltManager().login(alt);
                }
                break;

            case 5:
                if (selectedIndex >= 0 && selectedIndex < alts.size()) {
                    Alt alt = alts.get(selectedIndex);
                    OrangeX.getInstance().getAltManager().removeAlt(alt);
                    selectedIndex = -1;
                    lastClickIndex = -1;
                }
                break;
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        drawCenteredString(this.fontRendererObj, "Alt Manager", this.width / 2, 10, Color.WHITE.getRGB());

        mc.fontRendererObj.drawString(
                "Current Account: §a" + Minecraft.getMinecraft().getSession().getUsername(),
                10, 30, Color.WHITE.getRGB()
        );

        List<Alt> alts = OrangeX.getInstance().getAltManager().getAlts();

        int startX = 10;
        int startY = 60;
        int listWidth = this.width - 20;
        int listHeight = this.height - 110;
        int lineHeight = 20;

        // Draw list background
        drawRect(
                startX - 2,
                startY - 2,
                startX + listWidth + 2,
                startY + listHeight + 2,
                0x55000000
        );

        // Scroll
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? -20 : 20;
        }

        int maxScroll = Math.max(0, alts.size() * lineHeight - listHeight);
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;

        // Render alts
        for (int i = 0; i < alts.size(); i++) {
            int y = startY + i * lineHeight - scroll;

            if (y < startY - lineHeight || y > startY + listHeight) {
                continue;
            }

            if (i == selectedIndex) {
                drawRect(startX, y, startX + listWidth, y + lineHeight, 0x55FFFFFF);
            }

            mc.fontRendererObj.drawString(
                    alts.get(i).getName(),
                    startX + 5,
                    y + 6,
                    Color.WHITE.getRGB()
            );
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;

        List<Alt> alts = OrangeX.getInstance().getAltManager().getAlts();

        int startX = 10;
        int startY = 60;
        int listWidth = this.width - 20;
        int listHeight = this.height - 110;
        int lineHeight = 20;

        for (int i = 0; i < alts.size(); i++) {
            int y = startY + i * lineHeight - scroll;

            // Only allow clicking visible items
            if (y < startY - lineHeight || y > startY + listHeight) {
                continue;
            }

            if (mouseX >= startX && mouseX <= startX + listWidth &&
                    mouseY >= y && mouseY <= y + lineHeight) {

                long now = System.currentTimeMillis();

                if (lastClickIndex == i && now - lastClickTime <= DOUBLE_CLICK_DELAY) {
                    // Double click -> login
                    OrangeX.getInstance().getAltManager().login(alts.get(i));
                    return;
                }

                // Single click -> select
                selectedIndex = i;
                lastClickIndex = i;
                lastClickTime = now;
                break;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new GuiMultiplayer(null));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
