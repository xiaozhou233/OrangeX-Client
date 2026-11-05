package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontAtlas;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;

/**
 * 26.x backend.
 *
 * <p>The atlas is expanded to RGBA (white RGB + coverage alpha) and wrapped in
 * a {@code DynamicTexture}. Glyph drawing is deferred through
 * {@code GuiRenderState}, so no raw OpenGL calls are made here.</p>
 */
public final class V26_2FontBackend implements FontBackend {

    private final ClassLoader loader;
    private final V26_2ShapeCache shapes;

    public V26_2FontBackend(RuntimeInfo runtimeInfo) {
        this.loader = runtimeInfo.getMinecraftClassLoader();
        this.shapes = new V26_2ShapeCache(loader);
    }

    @Override
    public FontTexture createTexture(FontAtlas atlas) {
        int width = atlas.getWidth();
        int height = atlas.getHeight();
        byte[] coverage = atlas.getPixels();

        byte[] rgba = new byte[coverage.length * 4];
        for (int i = 0, j = 0; i < coverage.length; i++, j += 4) {
            rgba[j] = (byte) 0xFF;
            rgba[j + 1] = (byte) 0xFF;
            rgba[j + 2] = (byte) 0xFF;
            rgba[j + 3] = coverage[i];
        }

        V26_2GpuTexture texture = V26_2TextureHelper.create(
                loader, width, height, rgba);
        return new V26_2FontTexture(texture.texture, texture.textureView,
                texture.sampler, width, height);
    }

    @Override
    public Render2D createRender2D(FontManager fonts, Object platformContext) {
        return new V26_2Render2D(fonts, platformContext, loader, shapes);
    }

    @Override
    public String getName() {
        return "26.2/GuiRenderState";
    }
}
