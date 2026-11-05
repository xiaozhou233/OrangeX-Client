package cn.xiaozhou233.orangex.render.font;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A baked font atlas.
 *
 * <p>{@link #getPixels()} contains one 8-bit coverage value per texel. The
 * backends decide how to upload that data (alpha-only on legacy OpenGL, RGBA
 * on modern Minecraft).</p>
 */
public final class FontAtlas {

    private final int width;
    private final int height;
    private byte[] pixels;
    private final Map<Integer, Glyph> glyphs;

    /** Distance from the top of a line to the baseline, in atlas pixels. */
    private final float ascent;
    /** Recommended line height, in atlas pixels. */
    private final float lineHeight;

    private final Glyph[] ascii = new Glyph[128];

    public FontAtlas(int width, int height, byte[] pixels,
                     Map<Integer, Glyph> glyphs,
                     float ascent, float lineHeight) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.glyphs = Collections.unmodifiableMap(new HashMap<Integer, Glyph>(glyphs));
        this.ascent = ascent;
        this.lineHeight = lineHeight;

        for (Map.Entry<Integer, Glyph> entry : glyphs.entrySet()) {
            int codePoint = entry.getKey();
            if (codePoint >= 0 && codePoint < ascii.length) {
                ascii[codePoint] = entry.getValue();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getPixels() {
        if (pixels == null) {
            throw new IllegalStateException("Atlas pixels have already been released");
        }
        return pixels;
    }

    /**
     * Drops the CPU-side coverage bitmap after the texture has been uploaded.
     */
    public void releasePixels() {
        this.pixels = null;
    }

    public float getAscent() {
        return ascent;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public Glyph getGlyph(int codePoint) {
        if (codePoint >= 0 && codePoint < ascii.length) {
            Glyph glyph = ascii[codePoint];
            if (glyph != null) {
                return glyph;
            }
        }
        return glyphs.get(codePoint);
    }

    public Glyph getFallbackGlyph() {
        Glyph space = getGlyph(' ');
        if (space != null) {
            return space;
        }
        return getGlyph('?');
    }

    public Map<Integer, Glyph> getGlyphs() {
        return glyphs;
    }
}
