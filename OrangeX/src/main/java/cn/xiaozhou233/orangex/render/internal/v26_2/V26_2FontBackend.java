package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontAtlas;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * 26.x backend.
 *
 * <p>The atlas is expanded to RGBA (white RGB + coverage alpha) and wrapped in
 * a {@code DynamicTexture}. Glyph drawing is deferred through
 * {@code GuiRenderState}, so no raw OpenGL calls are made here.</p>
 */
public final class V26_2FontBackend implements FontBackend {

    private final ClassLoader loader;

    public V26_2FontBackend(RuntimeInfo runtimeInfo) {
        this.loader = runtimeInfo.getMinecraftClassLoader();
    }

    @Override
    public FontTexture createTexture(FontAtlas atlas) {
        try {
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

            Class<?> nativeImageClass = Class.forName(
                    "com.mojang.blaze3d.platform.NativeImage", false, loader);
            Constructor<?> nativeImageConstructor = nativeImageClass
                    .getDeclaredConstructor(int.class, int.class, boolean.class);
            Object image = nativeImageConstructor.newInstance(width, height, false);
            ByteBuffer buffer = (ByteBuffer) nativeImageClass
                    .getMethod("getPixelBytes").invoke(image);
            buffer.put(rgba);

            Class<?> dynamicTextureClass = Class.forName(
                    "net.minecraft.client.renderer.texture.DynamicTexture", false, loader);
            Constructor<?> dynamicTextureConstructor = dynamicTextureClass
                    .getDeclaredConstructor(Supplier.class, nativeImageClass);
            Object dynamicTexture = dynamicTextureConstructor.newInstance(
                    new Supplier<String>() {
                        @Override
                        public String get() {
                            return "OrangeX font atlas";
                        }
                    },
                    image);

            Object textureView = dynamicTextureClass.getMethod("getTextureView")
                    .invoke(dynamicTexture);
            Object sampler = dynamicTextureClass.getMethod("getSampler")
                    .invoke(dynamicTexture);

            System.out.println("[OrangeX] 26.2 font texture created: "
                    + width + "x" + height);
            return new V26_2FontTexture(dynamicTexture, textureView, sampler,
                    width, height);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Cannot upload the 26.2 font atlas",
                    throwable);
        }
    }

    @Override
    public Render2D createRender2D(FontManager fonts, Object platformContext) {
        return new V26_2Render2D(fonts, platformContext, loader);
    }

    @Override
    public String getName() {
        return "26.2/GuiRenderState";
    }
}
