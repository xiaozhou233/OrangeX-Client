package cn.xiaozhou233.orangex.event.facade;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

/**
 * Entry point used by injected 1.8.9 {@code GuiScreen} / {@code KeyBinding}
 * code.
 *
 * <p>Every vanilla screen goes through these methods, so each callback first
 * checks whether the instance is the empty input screen created by OrangeX.</p>
 */
public final class InputEvents {

    /** LWJGL 2 {@code Keyboard.KEY_M}. */
    private static final int KEY_M = 50;

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
     * screen is open. This queues the 1.8.9 open; it is applied at the end of
     * the tick so the same key event is not also delivered to the new screen.
     */
    public static void onKeyPressed(int keyCode) {
        if (keyCode != KEY_M) {
            return;
        }
        try {
            Minecraft minecraft = minecraft();
            if (minecraft == null || minecraft.isScreenOpen()) {
                return;
            }
            boolean inWorld = minecraft.isInWorld();
            if (DEBUG) {
                System.out.println("[OrangeX] M pressed: inWorld=" + inWorld);
            }
            if (inWorld) {
                ClientInput.requestOpen();
            }
        } catch (Throwable throwable) {
            logFailure(throwable);
        }
    }

    /**
     * Called from {@code GuiScreen#keyTyped(char, int)}. This queues the
     * 1.8.9 close; it is applied at the end of the tick.
     */
    public static boolean onKeyTyped(Object screen, int keyCode) {
        if (!isOurs(screen) || keyCode != KEY_M) {
            return false;
        }
        try {
            ClientInput.requestClose();
        } catch (Throwable throwable) {
            logFailure(throwable);
        }
        return true;
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
