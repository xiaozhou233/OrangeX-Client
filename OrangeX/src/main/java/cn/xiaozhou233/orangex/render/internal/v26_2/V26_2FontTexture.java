package cn.xiaozhou233.orangex.render.internal.v26_2;

import cn.xiaozhou233.orangex.render.internal.FontTexture;

/**
 * Wraps a 26.x {@code DynamicTexture}. All fields are kept as {@link Object}
 * because OrangeX is compiled against Java 8 without Mojang/LWJGL3 on the
 * compile classpath.
 */
public final class V26_2FontTexture implements FontTexture {

    private final Object texture;
    private final Object textureView;
    private final Object sampler;
    private final int width;
    private final int height;

    V26_2FontTexture(Object texture, Object textureView, Object sampler,
                     int width, int height) {
        this.texture = texture;
        this.textureView = textureView;
        this.sampler = sampler;
        this.width = width;
        this.height = height;
    }

    Object getTexture() {
        return texture;
    }

    Object getTextureView() {
        return textureView;
    }

    Object getSampler() {
        return sampler;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void close() {
        try {
            texture.getClass().getMethod("close").invoke(texture);
        } catch (Throwable throwable) {
            System.err.println("[OrangeX] Failed to close a 26.2 font texture: "
                    + throwable);
        }
    }
}
