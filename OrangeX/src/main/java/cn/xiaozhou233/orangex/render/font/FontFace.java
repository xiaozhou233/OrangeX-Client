package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.internal.FontTexture;

/**
 * A baked atlas plus its GPU texture and the scale needed to draw it at the
 * requested logical size.
 */
public final class FontFace {

    private final FontFamily family;
    private final FontAtlas atlas;
    private final FontTexture texture;
    private final float logicalSize;
    private final float drawScale;

    public FontFace(FontFamily family, FontAtlas atlas, FontTexture texture,
                    float logicalSize, float drawScale) {
        this.family = family;
        this.atlas = atlas;
        this.texture = texture;
        this.logicalSize = logicalSize;
        this.drawScale = drawScale;
    }

    public FontFamily getFamily() {
        return family;
    }

    public FontAtlas getAtlas() {
        return atlas;
    }

    public FontTexture getTexture() {
        return texture;
    }

    public float getLogicalSize() {
        return logicalSize;
    }

    public float getDrawScale() {
        return drawScale;
    }

    public Glyph getGlyph(int codePoint) {
        return atlas.getGlyph(codePoint);
    }

    public Glyph getFallbackGlyph() {
        return atlas.getFallbackGlyph();
    }

    public float getAscent() {
        return atlas.getAscent();
    }

    public float getLineHeight() {
        return atlas.getLineHeight();
    }
}
