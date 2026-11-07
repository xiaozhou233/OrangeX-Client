package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.internal.FontTexture;

/**
 * One baked atlas and its GPU texture, shared by every {@link FontFace} that
 * was baked at the same height and resource.
 *
 * <p>Logical sizes do not own atlases: {@link FontFace#getDrawScale()} maps
 * atlas pixels back to GUI pixels, so many sizes reuse a single texture.</p>
 */
final class FontAtlasEntry {

    final String resource;
    final int bakeHeight;
    final FontAtlas atlas;
    final FontTexture texture;
    final int texels;
    long lastUsedNanos;

    FontAtlasEntry(String resource, int bakeHeight, FontAtlas atlas,
                   FontTexture texture, long lastUsedNanos) {
        this.resource = resource;
        this.bakeHeight = bakeHeight;
        this.atlas = atlas;
        this.texture = texture;
        this.texels = atlas.getWidth() * atlas.getHeight();
        this.lastUsedNanos = lastUsedNanos;
    }
}
