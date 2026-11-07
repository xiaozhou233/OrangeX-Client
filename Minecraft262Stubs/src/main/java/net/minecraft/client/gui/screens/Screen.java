package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Compile-time stub only. Declares exactly the members OrangeX overrides or
 * calls; the real class comes from the game at runtime.
 */
public abstract class Screen {

    protected Screen(Component title) {
    }

    public void extractBackground(GuiGraphicsExtractor graphics,
                                  int mouseX, int mouseY, float partialTick) {
    }

    public void extractRenderState(GuiGraphicsExtractor graphics,
                                   int mouseX, int mouseY, float partialTick) {
    }

    public boolean isPauseScreen() {
        return true;
    }

    public void mouseMoved(double x, double y) {
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event,
                                double dragX, double dragY) {
        return false;
    }

    public boolean mouseScrolled(double x, double y,
                                 double scrollX, double scrollY) {
        return false;
    }
}
