package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.input.KeyCodes;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

/**
 * Input entry point shared by injected 1.8.9 code and the 26.2 input screen.
 *
 * <p>Every vanilla screen goes through these methods, so each callback first
 * checks whether the instance is the empty input screen created by OrangeX.
 * Key events are not handled here: they are posted as
 * {@link cn.xiaozhou233.orangex.event.impl.EventKey} so features can subscribe
 * to them, and {@link cn.xiaozhou233.orangex.input.InputController} consumes
 * the GUI toggle.</p>
 */
public final class InputEvents {

    private static final boolean DEBUG = Boolean.getBoolean("orangex.debugInput");

    private static boolean failureLogged;

    private static Minecraft minecraft() {
        return OrangeX.getInstance().getMinecraft();
    }

    private static boolean isOurs(Object screen) {
        try {
            Minecraft minecraft = minecraft();
            return minecraft != null && minecraft.isInputScreenInstance(screen);
        } catch (Throwable throwable) {
            logFailure(throwable);
            return false;
        }
    }

    /**
     * Called from {@code KeyBinding#onTick(int)} for every key press while no
     * screen is open. The injected code already dropped auto-repeat events.
     */
    public static void onKeyPressed(int keyCode) {
        if (DEBUG) {
            System.out.println("[OrangeX] key pressed: " + keyCode);
        }
        KeyEvents.fire(keyCode);
    }

    /**
     * Called from the 26.2 {@code KeyboardHandler#keyPress} hook. Translates
     * the GLFW key code to the canonical 1.8.9 numbering before posting.
     */
    public static void onGlfwKeyPressed(int glfwKeyCode) {
        onKeyPressed(KeyCodes.fromGlfw(glfwKeyCode));
    }

    /**
     * Called from {@code GuiScreen#keyTyped(char, int)}. Returns {@code true}
     * only for the toggle key, so other keys (ESC, etc.) still reach vanilla
     * screen handling.
     */
    public static boolean onKeyTyped(Object screen, int keyCode) {
        if (!isOurs(screen)) {
            return false;
        }
        KeyEvents.fire(keyCode);
        return keyCode == KeyCodes.M;
    }

    public static void onDrawScreen(Object screen, int mouseX, int mouseY) {
        if (isOurs(screen)) {
            ClientInput.onMouseMove(mouseX, mouseY);
        }
    }

    public static boolean onMouseClicked(Object screen, int mouseX,
                                         int mouseY, int button) {
        if (!isOurs(screen)) {
            return false;
        }
        ClientInput.onMouseButton(mouseX, mouseY, button, true);
        return true;
    }

    public static boolean onMouseReleased(Object screen, int mouseX,
                                          int mouseY, int button) {
        if (!isOurs(screen)) {
            return false;
        }
        ClientInput.onMouseButton(mouseX, mouseY, button, false);
        return true;
    }

    public static void onMouseDragged(Object screen, int mouseX,
                                      int mouseY, int button) {
        if (isOurs(screen)) {
            ClientInput.onMouseMove(mouseX, mouseY);
        }
    }

    public static boolean isOurInputScreen(Object screen) {
        return isOurs(screen);
    }

    private static void logFailure(Throwable throwable) {
        if (!failureLogged) {
            failureLogged = true;
            System.err.println("[OrangeX] Input callback failed");
            throwable.printStackTrace();
        }
    }

    private InputEvents() {
    }
}
