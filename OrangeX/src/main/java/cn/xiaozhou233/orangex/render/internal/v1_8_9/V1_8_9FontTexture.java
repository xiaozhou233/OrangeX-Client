package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import cn.xiaozhou233.orangex.render.internal.FontTexture;
import org.lwjgl.opengl.GL11;

public final class V1_8_9FontTexture implements FontTexture {

    private final int textureId;
    private final int width;
    private final int height;

    V1_8_9FontTexture(int textureId, int width, int height) {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
    }

    int getTextureId() {
        return textureId;
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
        GL11.glDeleteTextures(textureId);
    }
}
