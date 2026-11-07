package cn.xiaozhou233.orangex.input;

import cn.xiaozhou233.orangex.wrapper.api.Minecraft;

/**
 * Cross-version GUI input state.
 *
 * <p>While the empty screen is open the game stops sending mouse and keyboard
 * input to the player. The screen itself forwards cursor and button events
 * here, and feature code can poll this class from {@code EventRender2D} to
 * draw hover effects, cursors, ClickGUI panels, etc.</p>
 *
 * <p>Coordinates are Minecraft GUI pixels (the same space as
 * {@code Render2D}).</p>
 */
public final class ClientInput {

    /**
     * GUI toggle key. The actual key code is version-specific and is handled
     * by {@code VersionInput}; this is only for documentation.
     */
    public static final String TOGGLE_KEY_NAME = "M";

    private static volatile boolean open;
    private static volatile double mouseX;
    private static volatile double mouseY;
    private static volatile boolean leftDown;
    private static volatile boolean rightDown;
    private static volatile boolean middleDown;

    private static boolean toggleWasDown;
    private static boolean started;
    private static boolean pendingOpen;
    private static boolean pendingClose;

    private ClientInput() {
    }

    /**
     * Called once per client tick by {@link InputController}.
     */
    public static void tick(Minecraft minecraft) {
        if (!started) {
            started = true;
            System.out.println("[OrangeX] Input controller active");
        }

        boolean screenOpen = minecraft.isInputScreenOpen();

        if (open && !screenOpen) {
            reset();
        } else if (!open && screenOpen) {
            onScreenOpened(minecraft.getGuiMouseX(), minecraft.getGuiMouseY());
        }

        boolean down = minecraft.isToggleKeyDown();

        if (minecraft.isToggleEventDriven()) {
            // 1.8.9 opens from KeyBinding#onTick (pendingOpen). Closing is
            // polled here and applied at the end of the tick so the same key
            // event is not also delivered to the freshly opened screen.
            if (open && down && !toggleWasDown) {
                pendingClose = true;
            }
        } else if (down && !toggleWasDown) {
            // 26.2: no injected Screen subclass key handling, so opening /
            // closing here cannot bounce.
            if (open) {
                minecraft.closeInputScreen();
            } else if (minecraft.isInWorld() && !minecraft.isScreenOpen()) {
                minecraft.openInputScreen();
            }
        }

        toggleWasDown = down;
    }

    /**
     * Applies a queued 1.8.9 toggle after the tick's keyboard/mouse handling.
     */
    public static void applyPending(Minecraft minecraft) {
        if (pendingOpen) {
            pendingOpen = false;
            if (!open && minecraft.isInWorld() && !minecraft.isScreenOpen()) {
                minecraft.openInputScreen();
                // The key event that opened the screen is still held down.
                toggleWasDown = true;
            }
        }

        if (pendingClose) {
            pendingClose = false;
            if (open) {
                minecraft.closeInputScreen();
                toggleWasDown = true;
            }
        }
    }

    public static void requestOpen() {
        pendingOpen = true;
    }

    public static void requestClose() {
        pendingClose = true;
    }

    public static void onMouseMove(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    /**
     * Marks the GUI as open immediately, without waiting for the next tick.
     */
    public static void onScreenOpened(double x, double y) {
        open = true;
        mouseX = x;
        mouseY = y;
        toggleWasDown = true;
        System.out.println("[OrangeX] Input screen opened");
    }

    public static void onMouseButton(double x, double y, int button, boolean pressed) {
        mouseX = x;
        mouseY = y;

        switch (button) {
            case 0:
                leftDown = pressed;
                break;
            case 1:
                rightDown = pressed;
                break;
            case 2:
                middleDown = pressed;
                break;
            default:
                break;
        }
    }

    public static void reset() {
        if (open) {
            System.out.println("[OrangeX] Input screen closed");
        }
        open = false;
        leftDown = false;
        rightDown = false;
        middleDown = false;
    }

    public static boolean isOpen() {
        return open;
    }

    public static double getMouseX() {
        return mouseX;
    }

    public static double getMouseY() {
        return mouseY;
    }

    public static boolean isLeftDown() {
        return leftDown;
    }

    public static boolean isRightDown() {
        return rightDown;
    }

    public static boolean isMiddleDown() {
        return middleDown;
    }
}
