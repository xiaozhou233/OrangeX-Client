package cn.xiaozhou233.orangex.wrapper.api;

/**
 * Cross-version view of {@code net.minecraft.client.Minecraft}.
 *
 * <p>Only the native client instance and the input-screen helpers are exposed
 * for now. More operations will be added incrementally as they are needed.</p>
 */
public interface Minecraft {

    /**
     * Returns the native {@code net.minecraft.client.Minecraft} instance.
     *
     * <p>Feature code should treat the result as opaque; version-specific
     * handling belongs to the implementation.</p>
     */
    Object getMinecraft();

    /**
     * {@code true} while a level is loaded (the title screen returns
     * {@code false}).
     */
    boolean isInWorld();

    /**
     * {@code true} when the OrangeX empty input screen is the current screen.
     */
    boolean isInputScreenOpen();

    /**
     * {@code true} when any Minecraft screen is open, including vanilla ones.
     */
    boolean isScreenOpen();

    /**
     * Identity check for injected code: returns {@code true} only for the
     * screen instance created by {@link #openInputScreen()}.
     */
    boolean isInputScreenInstance(Object screen);

    /**
     * Opens an empty screen. This releases the cursor and stops mouse and
     * keyboard input from reaching the player.
     */
    void openInputScreen();

    /**
     * Closes the input screen and gives mouse capture back to the game.
     */
    void closeInputScreen();

    /**
     * {@code true} while the GUI toggle key (M) is held.
     */
    boolean isToggleKeyDown();

    /**
     * {@code true} when the toggle is driven by key events (1.8.9) rather
     * than per-tick polling (26.2).
     */
    boolean isToggleEventDriven();

    /**
     * Current cursor X in Minecraft GUI pixels.
     */
    double getGuiMouseX();

    /**
     * Current cursor Y in Minecraft GUI pixels.
     */
    double getGuiMouseY();
}
