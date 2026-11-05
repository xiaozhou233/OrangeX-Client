package cn.xiaozhou233.orangex.render.internal;

import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontAtlas;
import cn.xiaozhou233.orangex.render.font.FontManager;

/**
 * Version-specific GPU operations for the font renderer.
 */
public interface FontBackend {

    FontTexture createTexture(FontAtlas atlas);

    Render2D createRender2D(FontManager fonts, Object platformContext);

    String getName();
}
