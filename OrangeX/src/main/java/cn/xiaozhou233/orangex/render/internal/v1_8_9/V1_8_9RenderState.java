package cn.xiaozhou233.orangex.render.internal.v1_8_9;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Shared fixed-function state for one 2D frame.
 *
 * <p>Shapes and glyphs both use this object so that interleaved draw calls do
 * not save/restore each other's GL state. {@link #begin()} is idempotent and
 * {@link #end()} restores the state that existed before the first draw.</p>
 */
final class V1_8_9RenderState {

    private final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(16);
    private final IntBuffer scissorBuffer = BufferUtils.createIntBuffer(16);

    private boolean active;
    private boolean premultipliedBlend;
    private boolean previousBlend;
    private boolean previousTexture2D;
    private boolean previousAlphaTest;
    private boolean previousDepthTest;
    private boolean previousCullFace;
    private boolean previousLighting;
    private boolean previousFog;
    private boolean previousScissorTest;
    private int previousTexture;
    private int previousTextureEnvMode;
    private int previousActiveTexture;
    private int previousShadeModel;
    private int previousBlendSrc;
    private int previousBlendDst;
    private float previousLineWidth;
    private int previousScissorX;
    private int previousScissorY;
    private int previousScissorWidth;
    private int previousScissorHeight;

    void begin() {
        if (active) {
            return;
        }
        active = true;

        previousBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        previousTexture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        previousAlphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        previousDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        previousCullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        previousLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        previousFog = GL11.glIsEnabled(GL11.GL_FOG);
        previousScissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        previousTextureEnvMode = GL11.glGetTexEnvi(
                GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE);
        previousShadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        previousBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        previousBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        previousLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        scissorBuffer.clear();
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, scissorBuffer);
        previousScissorX = scissorBuffer.get(0);
        previousScissorY = scissorBuffer.get(1);
        previousScissorWidth = scissorBuffer.get(2);
        previousScissorHeight = scissorBuffer.get(3);

        colorBuffer.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, colorBuffer);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        premultipliedBlend = false;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    }

    void end() {
        if (!active) {
            return;
        }
        active = false;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        GL11.glColor4f(colorBuffer.get(0), colorBuffer.get(1),
                colorBuffer.get(2), colorBuffer.get(3));
        setEnabled(GL11.GL_BLEND, previousBlend);
        setEnabled(GL11.GL_TEXTURE_2D, previousTexture2D);
        setEnabled(GL11.GL_ALPHA_TEST, previousAlphaTest);
        setEnabled(GL11.GL_DEPTH_TEST, previousDepthTest);
        setEnabled(GL11.GL_CULL_FACE, previousCullFace);
        setEnabled(GL11.GL_LIGHTING, previousLighting);
        setEnabled(GL11.GL_FOG, previousFog);
        setEnabled(GL11.GL_SCISSOR_TEST, previousScissorTest);
        GL11.glScissor(previousScissorX, previousScissorY,
                previousScissorWidth, previousScissorHeight);
        GL11.glBlendFunc(previousBlendSrc, previousBlendDst);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                GL11.GL_TEXTURE_ENV_MODE, previousTextureEnvMode);
        GL11.glShadeModel(previousShadeModel);
        GL11.glLineWidth(previousLineWidth);
        GL13.glActiveTexture(previousActiveTexture);
    }

    void enableTexture() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    void disableTexture() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    void setColor(int argb) {
        useStandardBlend();
        GL11.glColor4f(
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f,
                ((argb >>> 24) & 0xFF) / 255.0f
        );
    }

    /**
     * Colours for {@code LUMINANCE_ALPHA} glyph atlases.
     *
     * <p>That atlas format stores the coverage in both luminance and alpha, so
     * {@code GL_MODULATE} produces {@code Cf * coverage} twice. Premultiplying
     * the vertex colour and blending with {@code (ONE, ONE_MINUS_SRC_ALPHA)}
     * keeps the result identical to the old RGBA upload while halving the
     * texture size.</p>
     */
    void setGlyphColor(int argb) {
        usePremultipliedBlend();
        float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
        GL11.glColor4f(
                ((argb >> 16) & 0xFF) / 255.0f * alpha,
                ((argb >> 8) & 0xFF) / 255.0f * alpha,
                (argb & 0xFF) / 255.0f * alpha,
                alpha
        );
    }

    private void useStandardBlend() {
        if (premultipliedBlend) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            premultipliedBlend = false;
        }
    }

    private void usePremultipliedBlend() {
        if (!premultipliedBlend) {
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            premultipliedBlend = true;
        }
    }

    private static void setEnabled(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
    }
}
