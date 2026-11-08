package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.input.ClientInput;

/**
 * Renders and drives the ClickGUI while the empty input screen is open.
 *
 * <p>The input screen only records the raw mouse state in
 * {@link ClientInput}; this class turns button transitions into component
 * callbacks and then renders {@link MainPanel}.</p>
 */
public final class ClickGuiRender {

    private static final MainPanel MAIN_PANEL = new MainPanel();

    private static boolean lastLeftDown;
    private static boolean lastRightDown;

    private ClickGuiRender() {
    }

    public static void render(EventRender2D event) {
        if (!ClientInput.isOpen()) {
            reset();
            return;
        }

        float mouseX = (float) ClientInput.getMouseX();
        float mouseY = (float) ClientInput.getMouseY();
        boolean leftDown = ClientInput.isLeftDown();
        boolean rightDown = ClientInput.isRightDown();

        if (leftDown && !lastLeftDown) {
            MAIN_PANEL.mouseClicked(mouseX, mouseY, 0);
        }
        if (rightDown && !lastRightDown) {
            MAIN_PANEL.mouseClicked(mouseX, mouseY, 1);
        }
        if (leftDown && lastLeftDown) {
            MAIN_PANEL.mouseDragged(mouseX, mouseY, 0);
        }
        if (!leftDown && lastLeftDown) {
            MAIN_PANEL.mouseReleased(mouseX, mouseY, 0);
        }
        if (!rightDown && lastRightDown) {
            MAIN_PANEL.mouseReleased(mouseX, mouseY, 1);
        }

        MAIN_PANEL.render(event);

        lastLeftDown = leftDown;
        lastRightDown = rightDown;
    }

    /**
     * Drops transient input state. Called when the GUI closes or reopens so a
     * button held across screens does not produce a stale drag.
     */
    public static void reset() {
        MAIN_PANEL.resetInteraction();
        lastLeftDown = false;
        lastRightDown = false;
    }
}
