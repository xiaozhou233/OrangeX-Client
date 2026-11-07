package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.internal.FontTexture;

import java.util.HashMap;
import java.util.Map;

/**
 * A font face bound to a shared {@link FontAtlasEntry} and a logical size.
 *
 * <p>The atlas is shared between sizes; {@link #getDrawScale()} converts atlas
 * pixels back to GUI pixels. Each face owns a small width cache because widths
 * also depend on the CJK fallback face that the primary face resolved.</p>
 */
public final class FontFace {

    private static final int MAX_CACHED_WIDTHS = 1024;

    private final FontFamily family;
    private final FontAtlasEntry entry;
    private final float logicalSize;
    private final float drawScale;
    private final Map<String, Double> widthCache = new HashMap<String, Double>();
    private long lastUsedNanos;

    FontFace(FontFamily family, FontAtlasEntry entry,
             float logicalSize, float drawScale, long lastUsedNanos) {
        this.family = family;
        this.entry = entry;
        this.logicalSize = logicalSize;
        this.drawScale = drawScale;
        this.lastUsedNanos = lastUsedNanos;
    }

    public FontFamily getFamily() {
        return family;
    }

    public FontAtlas getAtlas() {
        return entry.atlas;
    }

    public FontTexture getTexture() {
        return entry.texture;
    }

    public float getLogicalSize() {
        return logicalSize;
    }

    public float getDrawScale() {
        return drawScale;
    }

    public Glyph getGlyph(int codePoint) {
        return entry.atlas.getGlyph(codePoint);
    }

    public Glyph getFallbackGlyph() {
        return entry.atlas.getFallbackGlyph();
    }

    public float getAscent() {
        return entry.atlas.getAscent();
    }

    public float getLineHeight() {
        return entry.atlas.getLineHeight();
    }

    FontAtlasEntry getEntry() {
        return entry;
    }

    long getLastUsedNanos() {
        return lastUsedNanos;
    }

    void touch(long now) {
        lastUsedNanos = now;
    }

    /**
     * Shared across frames and renderers: {@code getStringWidth} is normally
     * called every frame for static text.
     */
    Double getCachedWidth(String text) {
        return widthCache.get(text);
    }

    void cacheWidth(String text, double width) {
        if (widthCache.size() >= MAX_CACHED_WIDTHS) {
            widthCache.clear();
        }
        widthCache.put(text, width);
    }
}
