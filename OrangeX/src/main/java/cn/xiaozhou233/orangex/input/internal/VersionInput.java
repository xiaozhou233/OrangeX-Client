package cn.xiaozhou233.orangex.input.internal;

/**
 * Version-specific implementation of the input screen and the cursor helpers.
 *
 * <p>Implementations live in version packages and may reference that
 * version's classes directly. They are loaded lazily through
 * {@link cn.xiaozhou233.orangex.wrapper.api.Minecraft} so that a 1.8.9 runtime
 * never loads 26.2 classes and vice versa.</p>
 */
public interface VersionInput {

    boolean isInWorld();

    /**
     * {@code true} when our own empty screen is the current Minecraft screen.
     */
    boolean isInputScreenOpen();

    /**
     * {@code true} when any Minecraft screen is open, including vanilla ones.
     */
    boolean isScreenOpen();

    /**
     * Identity check used by injected 1.8.9 code, which runs for every
     * {@code GuiScreen} instance.
     */
    boolean isInputScreenInstance(Object screen);

    /**
     * Opens the empty screen. On both versions this releases the mouse and
     * stops keyboard / mouse input from reaching the player.
     */
    void openInputScreen();

    void closeInputScreen();

    /**
     * M: {@code Keyboard.KEY_M} on 1.8.9, GLFW 77 on 26.2.
     */
    boolean isToggleKeyDown();

    /**
     * {@code true} when opening/closing is driven by injected key events
     * instead of polling {@link #isToggleKeyDown()} once per tick. 1.8.9 uses
     * this so a very short key tap is not missed between ticks.
     */
    boolean isToggleEventDriven();

    double getGuiMouseX();

    double getGuiMouseY();
}
