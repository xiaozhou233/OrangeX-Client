package cn.xiaozhou233.orangex.render.internal;

import cn.xiaozhou233.orangex.render.font.Glyph;

/**
 * Draws one glyph for the current frame. A drawer is created per frame because
 * modern Minecraft renders the GUI through a deferred render-state object.
 */
public interface GlyphDrawer {

    void drawGlyph(FontTexture texture, Glyph glyph,
                   double x, double y, double scale, int argb);
}
