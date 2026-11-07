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
     * GUI toggle key name. The canonical key code is {@link KeyCodes#M}.
     */
    public static final String TOGGLE_KEY_NAME = "M";

    private static volatile boolean open;
    private static volatile double mouseX;
    private static volatile double mouseY;
    private static volatile boolean leftDown;
    private static volatile boolean rightDown;
    private static volatile boolean middleDown;

    private static boolean started;
    private static boolean pendingOpen;
    private static boolean pendingClose;

    private ClientInput() {
    }

    /**
     * Called once per client tick by {@link InputController}. The toggle
     * itself is event-driven on every supported version; this only reconciles
     * the tracked state with the actual Minecraft screen.
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
    }

    /**
     * Applies queued open / close requests after the tick's keyboard and mouse
     * handling.
     */
    public static void applyPending(Minecraft minecraft) {
        if (pendingOpen) {
            pendingOpen = false;
            if (!open && minecraft.isInWorld() && !minecraft.isScreenOpen()) {
                minecraft.openInputScreen();
            }
        }

        if (pendingClose) {
            pendingClose = false;
            if (open) {
                minecraft.closeInputScreen();
            }
        }
    }

    public static void requestOpen() {
        pendingOpen = true;
    }

    public static void requestClose() {
        pendingClose = true;
    }

    /**
     * Handles a key event posted on the EventBus. Only the canonical toggle
     * key (M) changes the GUI state; all other keys are ignored here but stay
     * available to other subscribers.
     */
    public static void onKey(Minecraft minecraft, int key) {
        if (minecraft == null || key != KeyCodes.M) {
            return;
        }

        if (minecraft.isInputScreenOpen()) {
            requestClose();
        } else if (minecraft.isInWorld() && !minecraft.isScreenOpen()) {
            requestOpen();
        }
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
