package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontFamily;

/**
 * Shared colours, metrics and drawing primitives for the ClickGUI.
 *
 * <p>All colours are ARGB and all coordinates are Minecraft GUI pixels.</p>
 */
public final class ClickGuiStyle {

    /**
     * Font family used only by the ClickGUI. Other screens keep using the
     * globally configured default family.
     */
    public static final FontFamily FONT_FAMILY = FontFamily.MONTSERRAT;

    public static final float DESIGN_WIDTH = 448f;
    public static final float DESIGN_HEIGHT = 306f;
    public static final float MARGIN = 12f;
    public static final float PANEL_RADIUS = 9f;

    public static final float HEADER_HEIGHT = 46f;

    public static final float SIDEBAR_WIDTH = 112f;
    public static final float SIDEBAR_CARD_X = 10f;
    public static final float SIDEBAR_CARD_Y = HEADER_HEIGHT + 8f;
    public static final float CATEGORY_HEIGHT = 26f;
    public static final float CATEGORY_GAP = 2f;

    public static final float MODULE_HEIGHT = 27f;
    public static final float MODULE_GAP = 3f;

    public static final float TOGGLE_WIDTH = 26f;
    public static final float TOGGLE_HEIGHT = 12f;
    public static final float SMALL_TOGGLE_WIDTH = 20f;
    public static final float SMALL_TOGGLE_HEIGHT = 9f;

    public static final int COLOR_PANEL = 0xF218181D;
    public static final int COLOR_BORDER = 0xFF2C2C37;
    public static final int COLOR_TEXT = 0xFFEDEDF2;
    public static final int COLOR_TEXT_DIM = 0xFF9A9AA8;
    public static final int COLOR_TEXT_MUTED = 0xFF61616F;
    public static final int COLOR_ACCENT = 0xFFFF8A3D;
    public static final int COLOR_ACCENT_RGB = 0x00FF8A3D;
    public static final int COLOR_DIVIDER = 0xFF272730;
    public static final int COLOR_SIDEBAR_BG = 0xFF15151A;
    public static final int COLOR_SIDEBAR_BORDER = 0xFF24242C;
    public static final int COLOR_SWITCH_OFF = 0xFF34343F;
    public static final int COLOR_KNOB_OFF = 0xFF9A9AA8;

    private ClickGuiStyle() {
    }

    public static FontRenderer font(Render2D graphics, float size) {
        return graphics.getFont(FONT_FAMILY, size);
    }

    public static FontRenderer font(Render2D graphics, float size, boolean bold) {
        return graphics.getFont(FONT_FAMILY, size, bold);
    }

    public static void drawSwitch(Render2D graphics, float switchX, float switchY,
                                  float switchWidth, float switchHeight,
                                  float progress) {
        int track = lerpColor(COLOR_SWITCH_OFF, COLOR_ACCENT, progress);
        graphics.fillRoundedRect(switchX, switchY, switchWidth, switchHeight,
                switchHeight / 2.0f, track);

        float knobSize = switchHeight - 4.0f;
        float knobX = switchX + 2.0f
                + (switchWidth - knobSize - 4.0f) * progress;
        int knob = lerpColor(COLOR_KNOB_OFF, 0xFFFFFFFF, progress);
        graphics.fillRoundedRect(knobX, switchY + 2.0f, knobSize, knobSize,
                knobSize / 2.0f, knob);
    }

    public static double centeredY(FontRenderer font, float top, float height) {
        return top + (height - font.getStringHeight()) / 2.0;
    }

    public static boolean isInside(double mouseX, double mouseY,
                                   double x, double y,
                                   double width, double height) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int argb(int alpha, int rgb) {
        int clamped = Math.max(0, Math.min(255, alpha));
        return (clamped << 24) | (rgb & 0x00FFFFFF);
    }

    public static int lerpColor(int from, int to, float progress) {
        float t = clamp(progress, 0.0f, 1.0f);
        int a = lerpChannel(from >>> 24, to >>> 24, t);
        int r = lerpChannel(from >> 16, to >> 16, t);
        int g = lerpChannel(from >> 8, to >> 8, t);
        int b = lerpChannel(from, to, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        int fromChannel = from & 0xFF;
        int toChannel = to & 0xFF;
        return Math.round(fromChannel + (toChannel - fromChannel) * t);
    }
}
