package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.internal.GlyphDrawer;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws UTF-16 text with a primary face and an optional CJK fallback face.
 *
 * <p>Supported formatting: Minecraft {@code §} colour codes ({@code 0-9},
 * {@code a-f}, {@code r}). Shadow text is drawn as MC does: a dimmed copy
 * offset by one GUI pixel.</p>
 */
public final class TrueTypeFontRenderer implements FontRenderer {

    private static final int[] COLORS = new int[]{
            0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA,
            0xFFAA0000, 0xFFAA00AA, 0xFFFFAA00, 0xFFAAAAAA,
            0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF,
            0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF
    };

    private final FontManager manager;
    private final FontFace primary;
    private final GlyphDrawer drawer;
    private final boolean bold;
    private final double guiScale;

    private final Map<String, Double> widthCache = new HashMap<String, Double>();
    private FontFace fallback;

    public TrueTypeFontRenderer(FontManager manager, FontFace primary,
                                boolean bold, double guiScale,
                                GlyphDrawer drawer) {
        this.manager = manager;
        this.primary = primary;
        this.bold = bold;
        this.guiScale = guiScale;
        this.drawer = drawer;
    }

    @Override
    public void drawString(String text, double x, double y, int argb) {
        draw(text, x, y, argb, false);
    }

    @Override
    public void drawStringWithShadow(String text, double x, double y, int argb) {
        draw(text, x, y, argb, true);
    }

    @Override
    public void drawCenteredString(String text, double centerX, double y, int argb) {
        drawString(text, centerX - getUncachedWidth(text) / 2.0, y, argb);
    }

    @Override
    public void drawCenteredStringWithShadow(String text, double centerX,
                                             double y, int argb) {
        drawStringWithShadow(text, centerX - getUncachedWidth(text) / 2.0, y, argb);
    }

    @Override
    public double getStringWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        Double cached = widthCache.get(text);
        if (cached != null) {
            return cached;
        }
        double width = getUncachedWidth(text);
        if (widthCache.size() > 4096) {
            widthCache.clear();
        }
        widthCache.put(text, width);
        return width;
    }

    @Override
    public double getStringHeight() {
        float height = primary.getLineHeight() * primary.getDrawScale();
        return height > 0.0f ? height : primary.getLogicalSize();
    }

    private void draw(String text, double x, double y, int argb, boolean shadow) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int color = normalizeAlpha(argb);
        if (shadow) {
            drawPass(text, x + 1.0, y + 1.0, color, true);
        }
        drawPass(text, x, y, color, false);
    }

    private void drawPass(String text, double x, double y,
                          int baseColor, boolean shadow) {
        int color = baseColor;
        double penX = x;

        for (int i = 0; i < text.length(); ) {
            char ch = text.charAt(i);

            if (ch == '\u00a7' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                color = colorFor(code, baseColor);
                i += 2;
                continue;
            }

            int codePoint;
            if (Character.isHighSurrogate(ch) && i + 1 < text.length()) {
                codePoint = Character.toCodePoint(ch, text.charAt(i + 1));
                i += 2;
            } else {
                codePoint = ch;
                i++;
            }

            ResolvedGlyph resolved = resolve(codePoint);
            if (resolved == null) {
                continue;
            }

            float scale = resolved.face.getDrawScale();
            double baseline = y + resolved.face.getAscent() * scale;
            int drawColor = shadow ? shadowColor(color) : color;

            if (!resolved.glyph.isBlank()) {
                drawer.drawGlyph(resolved.face.getTexture(), resolved.glyph,
                        penX, baseline, scale, drawColor);
            }
            penX += resolved.glyph.getAdvance() * scale;
        }
    }

    private double getUncachedWidth(String text) {
        double width = 0.0;
        for (int i = 0; i < text.length(); ) {
            char ch = text.charAt(i);

            if (ch == '\u00a7' && i + 1 < text.length()) {
                i += 2;
                continue;
            }

            int codePoint;
            if (Character.isHighSurrogate(ch) && i + 1 < text.length()) {
                codePoint = Character.toCodePoint(ch, text.charAt(i + 1));
                i += 2;
            } else {
                codePoint = ch;
                i++;
            }

            ResolvedGlyph resolved = resolve(codePoint);
            if (resolved == null) {
                continue;
            }
            width += resolved.glyph.getAdvance() * resolved.face.getDrawScale();
        }
        return width;
    }

    private ResolvedGlyph resolve(int codePoint) {
        Glyph glyph = primary.getGlyph(codePoint);
        if (glyph != null) {
            return new ResolvedGlyph(primary, glyph);
        }

        // ASCII/Latin faces do not contain CJK. Lazy-load Noto only when a
        // missing code point is actually encountered.
        if (!primary.getFamily().isCjkCapable()) {
            if (fallback == null) {
                fallback = manager.getFace(FontFamily.NOTO,
                        primary.getLogicalSize(), bold, guiScale);
            }
            glyph = fallback.getGlyph(codePoint);
            if (glyph != null) {
                return new ResolvedGlyph(fallback, glyph);
            }
        }

        glyph = primary.getFallbackGlyph();
        return glyph != null ? new ResolvedGlyph(primary, glyph) : null;
    }

    private static int normalizeAlpha(int argb) {
        if ((argb >>> 24) == 0) {
            return 0xFF000000 | (argb & 0x00FFFFFF);
        }
        return argb;
    }

    private static int colorFor(char code, int baseColor) {
        if (code == 'r' || code == 'R') {
            return baseColor;
        }
        if (code >= '0' && code <= '9') {
            return applyAlpha(COLORS[code - '0'], baseColor);
        }
        if (code >= 'a' && code <= 'f') {
            return applyAlpha(COLORS[10 + code - 'a'], baseColor);
        }
        if (code >= 'A' && code <= 'F') {
            return applyAlpha(COLORS[10 + code - 'A'], baseColor);
        }
        return baseColor;
    }

    private static int applyAlpha(int rgb, int baseColor) {
        return (baseColor & 0xFF000000) | (rgb & 0x00FFFFFF);
    }

    private static int shadowColor(int color) {
        int alpha = color & 0xFF000000;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return alpha
                | ((red / 4) << 16)
                | ((green / 4) << 8)
                | (blue / 4);
    }

    private static final class ResolvedGlyph {
        private final FontFace face;
        private final Glyph glyph;

        private ResolvedGlyph(FontFace face, Glyph glyph) {
            this.face = face;
            this.glyph = glyph;
        }
    }
}
