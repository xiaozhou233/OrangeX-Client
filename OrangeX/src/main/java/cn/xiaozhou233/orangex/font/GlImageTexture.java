package cn.xiaozhou233.orangex.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GlImageTexture {
    public int width;
    public int height;
    public int textureId;

    public GlImageTexture() {
        this.textureId = GL11.glGenTextures();
    }

    public GlImageTexture(int textureId) {
        this.textureId = textureId;
    }

    public void bind() {
        GlStateManager.bindTexture(this.textureId);
    }

    public void delete() {
        GL11.glDeleteTextures(this.textureId);
    }
}