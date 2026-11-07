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

    double getGuiMouseX();

    double getGuiMouseY();
}
