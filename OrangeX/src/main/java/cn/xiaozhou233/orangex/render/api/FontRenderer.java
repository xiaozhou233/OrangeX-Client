package cn.xiaozhou233.orangex.render.api;

/**
 * A font face bound to the current 2D render pass.
 *
 * <p>Instances are created by {@link Render2D#getFont(float)} and must only be
 * used during the frame that produced the {@code Render2D} instance.</p>
 */
public interface FontRenderer {

    void drawString(String text, double x, double y, int argb);

    void drawStringWithShadow(String text, double x, double y, int argb);

    void drawCenteredString(String text, double centerX, double y, int argb);

    void drawCenteredStringWithShadow(String text, double centerX, double y, int argb);

    double getStringWidth(String text);

    double getStringHeight();
}
