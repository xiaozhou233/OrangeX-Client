package cn.xiaozhou233.orangex.render.api;

/**
 * The 2D drawing context for one frame.
 *
 * <p>Feature code receives this object through
 * {@code cn.xiaozhou233.orangex.event.impl.EventRender2D}. Coordinates and
 * sizes are expressed in Minecraft GUI pixels (the same space used by the
 * vanilla HUD after {@code ScaledResolution}).</p>
 */
public interface Render2D {

    int getWidth();

    int getHeight();

    double getGuiScale();

    FontRenderer getFont(float size);

    FontRenderer getFont(float size, boolean bold);

    /**
     * Framework-internal: restores any state the backend changed for this
     * frame. Feature code should not call it.
     */
    void flush();
}
