package cn.xiaozhou233.orangex.render.internal;

import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontAtlas;
import cn.xiaozhou233.orangex.render.font.FontManager;

/**
 * Version-specific GPU operations for the font renderer.
 */
public interface FontBackend {

    FontTexture createTexture(FontAtlas atlas);

    /**
     * Bytes per atlas texel as uploaded by {@link #createTexture}, used to
     * enforce the texture memory budget. RGBA backends return 4; legacy
     * alpha backends can override this.
     */
    default int getBytesPerTexel() {
        return 4;
    }

    Render2D createRender2D(FontManager fonts, Object platformContext);

    String getName();
}
