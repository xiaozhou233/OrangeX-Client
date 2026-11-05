package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.render.font.FontAtlas;
import cn.xiaozhou233.orangex.render.font.FontManager;
import cn.xiaozhou233.orangex.render.internal.FontBackend;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

/**
 * Legacy OpenGL backend used by Minecraft 1.8.9.
 *
 * <p>The atlas is uploaded as {@code GL_ALPHA}; the fixed-function pipeline
 * modulates the current colour with the glyph coverage, which is exactly what
 * the old Minecraft font renderer does.</p>
 */
public final class V1_8_9FontBackend implements FontBackend {

    private final RuntimeInfo runtimeInfo;
    private final MappingResolver resolver;
    private final Object minecraft;
    private final V1_8_9RenderState state = new V1_8_9RenderState();
    private final V1_8_9GlyphDrawer drawer = new V1_8_9GlyphDrawer(state);

    public V1_8_9FontBackend(RuntimeInfo runtimeInfo,
                             MappingResolver resolver,
                             Object minecraft) {
        this.runtimeInfo = runtimeInfo;
        this.resolver = resolver;
        this.minecraft = minecraft;
    }

    @Override
    public FontTexture createTexture(FontAtlas atlas) {
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int previousAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        ByteBuffer buffer = BufferUtils.createByteBuffer(
                atlas.getWidth() * atlas.getHeight() * 4);
        byte[] coverage = atlas.getPixels();
        for (int i = 0; i < coverage.length; i++) {
            // RGBA is the most compatible fixed-function format: white RGB
            // keeps the vertex colour, alpha carries the glyph coverage.
            buffer.put((byte) 0xFF);
            buffer.put((byte) 0xFF);
            buffer.put((byte) 0xFF);
            buffer.put(coverage[i]);
        }
        buffer.flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                atlas.getWidth(), atlas.getHeight(), 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, previousAlignment);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        GL13.glActiveTexture(previousActiveTexture);
        return new V1_8_9FontTexture(textureId, atlas.getWidth(), atlas.getHeight());
    }

    @Override
    public Render2D createRender2D(FontManager fonts, Object platformContext) {
        return new V1_8_9Render2D(fonts, runtimeInfo, resolver, minecraft, drawer, state);
    }

    @Override
    public String getName() {
        return "1.8.9/GL11";
    }
}
