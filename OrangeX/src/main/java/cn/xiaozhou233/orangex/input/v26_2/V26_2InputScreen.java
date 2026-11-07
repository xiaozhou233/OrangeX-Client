package cn.xiaozhou233.orangex.input.v26_2;

import cn.xiaozhou233.orangex.input.ClientInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Empty input screen for 26.2.
 *
 * <p>The client GUI is still rendered by the HUD hook; this screen only exists
 * to move the game into its "screen open" state. Doing so makes
 * {@code Gui#setScreen} release the mouse and clear key mappings, and routes
 * mouse events to this instance instead of the player.</p>
 */
public final class V26_2InputScreen extends Screen {

    public V26_2InputScreen() {
        super(Component.empty());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics,
                                  int mouseX, int mouseY, float partialTick) {
        // No blur, panorama, menu texture or dim layer.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics,
                                   int mouseX, int mouseY, float partialTick) {
        // Drawn by the HUD hook instead.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void mouseMoved(double x, double y) {
        ClientInput.onMouseMove(x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ClientInput.onMouseButton(event.x(), event.y(), event.button(), true);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        ClientInput.onMouseButton(event.x(), event.y(), event.button(), false);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        ClientInput.onMouseMove(event.x(), event.y());
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y,
                                 double scrollX, double scrollY) {
        ClientInput.onMouseMove(x, y);
        return true;
    }
}
