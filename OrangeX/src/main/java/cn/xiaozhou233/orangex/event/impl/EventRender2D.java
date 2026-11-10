package cn.xiaozhou233.orangex.event.impl;

import cn.xiaozhou233.orangex.render.api.Render2D;

/**
 * Fired once per frame while the game collects its GUI. Listeners can queue
 * text through {@link #getRender2D()}.
 *
 * <p>{@link #isInWorld()} mirrors the game's own "should render level" flag.
 * 1.8.9 only runs the HUD hook while a world is loaded, but modern versions
 * also extract the GUI on the title screen, so listeners must not assume that
 * receiving this event means the player is in game.</p>
 */
public class EventRender2D {

    private final Render2D render2D;
    private final boolean inWorld;
    private final float partialTicks;

    public EventRender2D(Render2D render2D, boolean inWorld) {
        this(render2D, inWorld, Float.NaN);
    }

    public EventRender2D(Render2D render2D, boolean inWorld, float partialTicks) {
        this.render2D = render2D;
        this.inWorld = inWorld;
        this.partialTicks = partialTicks;
    }

    public Render2D getRender2D() {
        return render2D;
    }

    /**
     * {@code true} when the frame is rendered while a level is loaded.
     */
    public boolean isInWorld() {
        return inWorld;
    }

    /**
     * The partial tick the game used for this frame's world render, i.e. the
     * interpolation factor between the previous and the current tick.
     *
     * <p>Feature code must use this value instead of reading a timer of its own
     * so projected world positions line up with the rendered world. It is
     * {@link Float#NaN} when the hook could not supply one.</p>
     */
    public float getPartialTicks() {
        return partialTicks;
    }
}
