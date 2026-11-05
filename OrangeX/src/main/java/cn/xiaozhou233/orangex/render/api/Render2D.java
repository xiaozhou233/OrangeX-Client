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

    void fillRect(double x, double y, double width, double height, int argb);

    void fillGradient(double x, double y, double width, double height,
                      int topArgb, int bottomArgb);

    void drawRect(double x, double y, double width, double height,
                  double thickness, int argb);

    void fillRoundedRect(double x, double y, double width, double height,
                         double radius, int argb);

    void fillRoundedRect(double x, double y, double width, double height,
                         double radius, double borderWidth,
                         int argb, int borderArgb);

    void drawLine(double x1, double y1, double x2, double y2,
                  double thickness, int argb);

    void enableScissor(double x, double y, double width, double height);

    void disableScissor();

    /**
     * Framework-internal: restores any state the backend changed for this
     * frame. Feature code should not call it.
     */
    void flush();
}
