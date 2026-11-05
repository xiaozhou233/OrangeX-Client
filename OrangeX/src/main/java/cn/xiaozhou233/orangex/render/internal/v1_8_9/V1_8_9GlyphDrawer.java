package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import cn.xiaozhou233.orangex.render.font.Glyph;
import cn.xiaozhou233.orangex.render.internal.FontTexture;
import cn.xiaozhou233.orangex.render.internal.GlyphDrawer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

/**
 * Immediate-mode glyph drawer for the legacy (1.8.9) OpenGL pipeline.
 */
final class V1_8_9GlyphDrawer implements GlyphDrawer {

    private int boundTexture = -1;

    private boolean initialized;
    private boolean previousBlend;
    private boolean previousTexture2D;
    private boolean previousAlphaTest;
    private boolean previousDepthTest;
    private boolean previousCullFace;
    private boolean previousLighting;
    private boolean previousScissorTest;
    private boolean previousFog;
    private int previousTexture;
    private int previousTextureEnvMode;
    private int previousActiveTexture;
    private final FloatBuffer previousColor = BufferUtils.createFloatBuffer(16);

    @Override
    public void drawGlyph(FontTexture texture, Glyph glyph,
                          double penX, double baseline, double scale, int argb) {
        if (!(texture instanceof V1_8_9FontTexture)) {
            return;
        }
        initialize();

        int textureId = ((V1_8_9FontTexture) texture).getTextureId();
        if (textureId != boundTexture) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            boundTexture = textureId;
        }

        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);

        double left = penX + glyph.getX0() * scale;
        double right = penX + glyph.getX1() * scale;
        double top = baseline + glyph.getY0() * scale;
        double bottom = baseline + glyph.getY1() * scale;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(glyph.getU0(), glyph.getV0());
        GL11.glVertex2d(left, top);
        GL11.glTexCoord2f(glyph.getU0(), glyph.getV1());
        GL11.glVertex2d(left, bottom);
        GL11.glTexCoord2f(glyph.getU1(), glyph.getV1());
        GL11.glVertex2d(right, bottom);
        GL11.glTexCoord2f(glyph.getU1(), glyph.getV0());
        GL11.glVertex2d(right, top);
        GL11.glEnd();
    }

    void flush() {
        if (!initialized) {
            return;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        GL11.glColor4f(previousColor.get(0), previousColor.get(1),
                previousColor.get(2), previousColor.get(3));
        setEnabled(GL11.GL_BLEND, previousBlend);
        setEnabled(GL11.GL_TEXTURE_2D, previousTexture2D);
        setEnabled(GL11.GL_ALPHA_TEST, previousAlphaTest);
        setEnabled(GL11.GL_DEPTH_TEST, previousDepthTest);
        setEnabled(GL11.GL_CULL_FACE, previousCullFace);
        setEnabled(GL11.GL_LIGHTING, previousLighting);
        setEnabled(GL11.GL_SCISSOR_TEST, previousScissorTest);
        setEnabled(GL11.GL_FOG, previousFog);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE, previousTextureEnvMode);
        GL13.glActiveTexture(previousActiveTexture);
        initialized = false;
        boundTexture = -1;
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        previousBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        previousTexture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        previousAlphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        previousDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        previousCullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        previousLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        previousScissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        previousFog = GL11.glIsEnabled(GL11.GL_FOG);
        previousTextureEnvMode = GL11.glGetTexEnvi(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE);
        previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        previousColor.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, previousColor);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    }

    private static void setEnabled(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
    }
}
