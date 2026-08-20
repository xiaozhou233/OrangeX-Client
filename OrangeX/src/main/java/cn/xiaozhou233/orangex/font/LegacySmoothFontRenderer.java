package cn.xiaozhou233.orangex.font;

import cn.xiaozhou233.orangex.font.stb.StbTrueTypeFontInfo;
import cn.xiaozhou233.orangex.font.stb.StbTrueTypePackContext;
import cn.xiaozhou233.orangex.font.stb.StbTrueTypePackRange;
import cn.xiaozhou233.orangex.font.stb.StbTrueTypePackedChar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LegacySmoothFontRenderer extends SmoothFontRenderer {
    private float ascent;
    private Map<Integer, SmoothFontGlyph> glyphs = new HashMap<Integer, SmoothFontGlyph>();
    private int textureId;
    private float pixelScale;
    private float renderScale = 0.6f;
    private LegacyFontShaderProgram shader;

    public LegacySmoothFontRenderer(byte[] fontData, int fontSize, String fontName) {
        this.fontSize = fontSize;
        buildFont(fontData, fontSize, fontName);
    }

    public LegacySmoothFontRenderer(String resourcePath, int fontSize) {
        this.fontSize = fontSize;
        try {
            byte[] fontData = readResource(resourcePath);
            buildFont(fontData, fontSize, resourcePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readResource(String path) {
        try {
            String resourcePath = "fonts/" + path;
            InputStream is = LegacySmoothFontRenderer.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("fonts/" + path)).getInputStream();
            }
            if (is == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            is.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void buildFont(byte[] fontData, int fontSize, String fontName) {
        if (fontData == null) return;

        HashSet<Integer> charSet = new HashSet<Integer>();
        for (int i = 32; i <= 126; ++i) {
            charSet.add(i);
        }
        charSet.add(0);

        try {
            byte[] chineseData = readResource("chinese.properties.txt");
            if (chineseData != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(chineseData), StandardCharsets.UTF_8));
                int ch;
                while ((ch = reader.read()) != -1) {
                    if (ch >= 32) charSet.add(ch);
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] chars = charSet.stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(chars);

        int atlasSize = 4096;
        StbTrueTypeFontInfo fontInfo = new StbTrueTypeFontInfo();
        if (SmoothFontRasterState.z(fontInfo, fontData, 0) == 0) {
            System.err.println("Failed to init font: " + fontName);
            return;
        }

        int[] ascentArr = new int[1], descentArr = new int[1], lineGap = new int[1];
        SmoothFontRasterState.x(fontInfo, ascentArr, descentArr, lineGap);
        this.ascent = ascentArr[0];

        int[] fw = new int[1], fh = new int[1], fx = new int[1], fy = new int[1];
        SmoothFontRasterState.s(fontInfo, 72, fw, fh, fx, fy);
        float glyphHeight = fy[0] - fh[0];
        float fontHeight = ascentArr[0] - descentArr[0];
        float aspect = glyphHeight / fontHeight;
        float pixelHeight = (float) this.fontSize * (0.6f / aspect);

        byte[] atlasData = new byte[atlasSize * atlasSize];
        StbTrueTypePackContext packCtx = new StbTrueTypePackContext();
        if (SmoothFontRasterState.C(packCtx, atlasData, atlasSize, atlasSize, 0, 1) == 0) {
            System.err.println("Failed to begin packing font");
            return;
        }

        int oversampleX = 2, oversampleY = 2;
        SmoothFontRasterState.n(packCtx, oversampleX, oversampleY);

        try {
            StbTrueTypePackedChar[] packedChars = new StbTrueTypePackedChar[chars.length];
            for (int i = 0; i < packedChars.length; ++i) {
                packedChars[i] = new StbTrueTypePackedChar();
            }
            StbTrueTypePackRange range = new StbTrueTypePackRange();
            range.E = pixelHeight;
            range.R = 0;
            range.N = chars;
            range.b = chars.length;
            range.M = packedChars;
            range.i = (byte) oversampleX;
            range.y = (byte) oversampleY;
            SmoothFontRasterState.c(packCtx, fontData, 0, new StbTrueTypePackRange[]{range}, 1);
            SmoothFontRasterState.S(packCtx);

            this.pixelScale = SmoothFontRasterState.V(fontInfo, pixelHeight);
            for (int i = 0; i < chars.length; ++i) {
                int cp = chars[i];
                StbTrueTypePackedChar pc = packedChars[i];
                float invW = 1.0f / (float) atlasSize;
                float invH = 1.0f / (float) atlasSize;
                this.glyphs.put(cp, new SmoothFontGlyph(
                    pc.s, pc.G, pc.t, pc.R,
                    (float) pc.b * invW, (float) pc.k * invH,
                    (float) pc.q * invW, (float) pc.p * invH,
                    pc.A
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(atlasData.length + 128);
        buffer.put(atlasData);
        buffer.flip();

        this.textureId = GL11.glGenTextures();
        GlStateManager.bindTexture(this.textureId);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_LUMINANCE, atlasSize, atlasSize, 0, GL11.GL_LUMINANCE, GL11.GL_UNSIGNED_BYTE, buffer);

        shader = new LegacyFontShaderProgram();
    }

    @Override
    public void drawVerticalString(String text, double x, double y, Color color) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            drawStringWithShadow(String.valueOf(chars[i]), x, y, color);
            y += getStringHeight(String.valueOf(chars[i])) * 0.9;
        }
    }

    @Override
    public void renderString(String text, double x, double y, int color, boolean fallbackToVanilla) {
        if (!fallbackToVanilla) {
            if (text == null || text.length() == 0 || textureId == 0) return;

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;
            if (a < 0) a += 256;
            if (a == 0) a = 255;
            Color baseColor = new Color(r, g, b, a);

            char[] colorCodes = new char[text.length()];
            Arrays.fill(colorCodes, '\u0000');
            StringBuilder sb = new StringBuilder();
            char[] chars = text.toCharArray();
            int codeIdx = 0;
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if (c == '\u00a7') {
                    if (++i >= chars.length) continue;
                    colorCodes[codeIdx] = chars[i];
                    continue;
                }
                sb.append(c);
                ++codeIdx;
            }
            text = sb.toString();

            float f = renderScale;
            y += (double)(this.ascent * this.pixelScale * f);
            Color currentColor = baseColor;

            int prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean texEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
            boolean alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);

            if (!blendEnabled) GlStateManager.enableBlend();
            if (!texEnabled) GlStateManager.enableTexture2D();
            if (lightingEnabled) GlStateManager.disableLighting();
            if (!alphaEnabled) GlStateManager.enableAlpha();
            if (cullEnabled) GlStateManager.disableCull();

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.bindTexture(this.textureId);

            if (shader != null) shader.bind();

            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                SmoothFontGlyph glyph = this.glyphs.get((int) c);
                if (glyph == null) glyph = this.glyphs.get(32);
                if (glyph == null) continue;

                char cc = colorCodes[i];
                if (cc != '\u0000') {
                    float[] rgb = new float[3];
                    applyColorCode(cc, rgb);
                    currentColor = new Color(rgb[0], rgb[1], rgb[2]);
                    if (cc == 'r') currentColor = baseColor;
                }

                float x1 = (float) x + glyph.xOffset * f;
                float y1 = (float) y + glyph.yOffset * f;
                float x2 = (float) x + glyph.xMax * f;
                float y2 = (float) y + glyph.yMax * f;

                GL11.glColor4f(
                    (float) currentColor.getRed() / 255.0f,
                    (float) currentColor.getGreen() / 255.0f,
                    (float) currentColor.getBlue() / 255.0f,
                    (float) baseColor.getAlpha() / 255.0f
                );
                GL11.glTexCoord2f(glyph.uMin, glyph.vMin);
                GL11.glVertex2f(x1, y1);
                GL11.glTexCoord2f(glyph.uMin, glyph.vMax);
                GL11.glVertex2f(x1, y2);
                GL11.glTexCoord2f(glyph.uMax, glyph.vMax);
                GL11.glVertex2f(x2, y2);
                GL11.glTexCoord2f(glyph.uMax, glyph.vMin);
                GL11.glVertex2f(x2, y1);

                x += glyph.advance * f;
            }
            GL11.glEnd();

            if (shader != null) shader.restorePreviousProgram();

            GlStateManager.bindTexture(prevTex);
            if (!alphaEnabled) GlStateManager.disableAlpha();
            if (lightingEnabled) GlStateManager.enableLighting();
            if (!blendEnabled) GlStateManager.disableBlend();
            if (!texEnabled) GlStateManager.disableTexture2D();
            if (cullEnabled) GlStateManager.enableCull();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            float f = (float) this.fontSize / 16.0f;
            float inv = 1.0f / f;
            x = Math.ceil(x * inv);
            y = Math.ceil(y * inv);
            GL11.glPushMatrix();
            GL11.glScalef(f, f, f);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) x, (float) y, color);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void drawStringWithShadow(String text, double x, double y, int color) {
        drawString(text, x + 0.5, y + 0.5, Integer.MIN_VALUE);
        renderString(text, x, y, color, false);
    }

    @Override
    public void drawStringWithShadow(String text, double x, double y, Color color, boolean fallbackToVanilla) {
        if (!fallbackToVanilla) {
            drawString(text, x + 0.5, y + 0.5, Integer.MIN_VALUE);
            drawString(text, x, y, color);
        } else {
            float f = (float) this.fontSize / 16.0f;
            float inv = 1.0f / f;
            x = Math.ceil(x * inv);
            y = Math.ceil(y * inv);
            GL11.glPushMatrix();
            GL11.glScalef(f, f, f);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) x, (float) y, color.getRGB());
            GL11.glPopMatrix();
        }
    }

    @Override
    public double getStringHeight(String text, boolean fallbackToVanilla) {
        if (!fallbackToVanilla) {
            float f = 0.6f;
            return (float) this.fontSize * f;
        }
        float f = (float) this.fontSize / 16.0f;
        return 8.0f * f;
    }

    @Override
    public double getStringWidth(String text, boolean fallbackToVanilla) {
        String stripped = this.strippedStringCache.get(text);
        if (stripped == null) {
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == '\u00a7') continue;
                sb.append(c);
            }
            stripped = sb.toString();
            this.strippedStringCache.put(text, stripped);
        }
        text = stripped;

        if (!fallbackToVanilla) {
            Double cached = this.stringWidthCache.get(text);
            if (cached != null) return cached;

            float f = 0.6f;
            double width = 0.0;
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                SmoothFontGlyph glyph = this.glyphs.get((int) c);
                if (glyph == null) glyph = this.glyphs.get(32);
                if (glyph == null) continue;
                width += glyph.advance * f;
            }
            this.stringWidthCache.put(text, width);
            return width;
        }

        float f = (float) this.fontSize / 16.0f;
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * f;
    }

    @Override
    public void renderStringWithShadow(String text, double x, double y, Color color, Color shadowColor, boolean fallbackToVanilla) {
        int n = color.getRGB();
        int n2 = shadowColor.getRGB();
        if (!fallbackToVanilla) {
            drawString(text, x + 0.5, y + 0.5, n2);
            drawString(text, x, y, color);
        } else {
            float f = (float) this.fontSize / 16.0f;
            float inv = 1.0f / f;
            x = Math.ceil(x * inv);
            y = Math.ceil(y * inv);
            GL11.glPushMatrix();
            GL11.glScalef(f, f, f);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) x, (float) y, n);
            GL11.glPopMatrix();
        }
    }
}