package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.font.Glyph;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.render.internal.GlyphDrawer;

import java.lang.reflect.Method;

/**
 * Adds one deferred blit per glyph to the 26.x {@code GuiRenderState}.
 */
final class V26_2GlyphDrawer implements GlyphDrawer {

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

        int x0 = (int) Math.round(penX + glyph.getX0() * scale);
        int y0 = (int) Math.round(baseline + glyph.getY0() * scale);
        int x1 = (int) Math.round(penX + glyph.getX1() * scale);
        int y1 = (int) Math.round(baseline + glyph.getY1() * scale);
        if (x1 <= x0 || y1 <= y0) {
            return;
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
