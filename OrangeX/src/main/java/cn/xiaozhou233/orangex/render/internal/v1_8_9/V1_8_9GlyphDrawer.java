package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import cn.xiaozhou233.orangex.render.font.Glyph;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.render.internal.GlyphDrawer;
import org.lwjgl.opengl.GL11;

/**
 * Immediate-mode glyph drawer for the legacy (1.8.9) OpenGL pipeline.
 */
final class V1_8_9GlyphDrawer implements GlyphDrawer {

    private final V1_8_9RenderState state;
    private int boundTexture = -1;

    V1_8_9GlyphDrawer(V1_8_9RenderState state) {
        this.state = state;
    }

    @Override
    public void drawGlyph(FontTexture texture, Glyph glyph,
                          double penX, double baseline, double scale, int argb) {
        if (!(texture instanceof V1_8_9FontTexture)) {
            return;
        }
        state.begin();
        state.enableTexture();

        int textureId = ((V1_8_9FontTexture) texture).getTextureId();
        if (textureId != boundTexture) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            boundTexture = textureId;
        }
        state.setGlyphColor(argb);

        double left = penX + glyph.getX0() * scale;
        double right = penX + glyph.getX1() * scale;
        double top = baseline + glyph.getY0() * scale;
        double bottom = baseline + glyph.getY1() * scale;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(glyph.getU0(), glyph.getV0());
        GL11.glVertex2d(left, top);
        GL11.glTexCoord2f(glyph.getU0(), glyph.getV1());
        GL11.glVertex2d(left, bottom);
        GL11.glTexCoord2f(glyph.getU1(), glyph.getV1());
        GL11.glVertex2d(right, bottom);
        GL11.glTexCoord2f(glyph.getU1(), glyph.getV0());
        GL11.glVertex2d(right, top);
        GL11.glEnd();
    }

    void reset() {
        boundTexture = -1;
    }
}
