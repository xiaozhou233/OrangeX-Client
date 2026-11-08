package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.font.Glyph;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.render.internal.GlyphDrawer;

import java.lang.reflect.Method;

/**
 * Adds one deferred blit per glyph to the 26.x {@code GuiRenderState}.
 */
final class V26_2GlyphDrawer implements GlyphDrawer {

    /**
     * Maximum distance (in logical pixels) that still counts as "sitting on the
     * baseline" rather than being a descender or a raised glyph.
     *
     * <p>The atlas is packed with 2x oversampling, so a normal glyph's baked
     * bottom is 0.25/0.75px away from the baseline. 26.2 blits use integer
     * coordinates, and rounding those offsets per glyph made some letters drop
     * onto the next pixel while their neighbours stayed put. Snapping
     * near-baseline bottoms to one shared value keeps a line visually level.</p>
     */
    private static final double BASELINE_SNAP = 0.9;

    private final Object extractor;
    private final Method innerBlit;
    private final Object guiPipeline;

    V26_2GlyphDrawer(Object extractor, Method innerBlit, Object guiPipeline) {
        this.extractor = extractor;
        this.innerBlit = innerBlit;
        this.guiPipeline = guiPipeline;
    }

    @Override
    public void drawGlyph(FontTexture texture, Glyph glyph,
                          double penX, double baseline, double scale, int argb) {
        if (!(texture instanceof V26_2FontTexture)) {
            return;
        }

        // Snap both horizontal edges the same way (downwards). Rounding each
        // edge independently made neighbouring glyphs drift apart or together
        // by one pixel, which is most visible around narrow letters such as I.
        int x0 = (int) Math.floor(penX + glyph.getX0() * scale + 0.0001);
        int x1 = (int) Math.floor(penX + glyph.getX1() * scale + 0.0001);

        // Anchor both edges to one rounded baseline so every glyph in a line
        // shares the same pixel origin.
        int base = (int) Math.round(baseline);
        // Round the top down: curved letters (C, G, o...) overshoot the flat
        // cap/x-height by half a pixel in the atlas. Rounding split them onto
        // different rows, which made "CLICK GUI" look uneven.
        int y0 = base + (int) Math.floor(glyph.getY0() * scale + 0.0001);
        double bottomOffset = glyph.getY1() * scale;
        int y1;
        if (bottomOffset >= 0.0 && bottomOffset < BASELINE_SNAP) {
            // Normal letter/punctuation: sit exactly on the baseline.
            y1 = base;
        } else {
            // Descenders (g, p, y...) and raised glyphs keep their offset.
            y1 = base + (int) Math.round(bottomOffset);
        }

        // Thin strokes (l, I, | ...) can be narrower than one GUI pixel at high
        // GUI scales. Keep them visible with at least one pixel.
        if (x1 <= x0) {
            x1 = x0 + 1;
        }
        if (y1 <= y0) {
            y1 = y0 + 1;
        }

        V26_2FontTexture fontTexture = (V26_2FontTexture) texture;
        try {
            innerBlit.invoke(extractor, guiPipeline,
                    fontTexture.getTextureView(), fontTexture.getSampler(),
                    x0, y0, x1, y1,
                    glyph.getU0(), glyph.getU1(),
                    glyph.getV0(), glyph.getV1(),
                    argb);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to submit a GUI glyph", throwable);
        }
    }
}

