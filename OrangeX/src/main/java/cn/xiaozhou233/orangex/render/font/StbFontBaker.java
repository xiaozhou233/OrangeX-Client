package cn.xiaozhou233.orangex.render.font;

import cn.xiaozhou233.orangex.render.font.stb.StbRasterizer;
import cn.xiaozhou233.orangex.render.font.stb.StbTrueTypeFontInfo;
import cn.xiaozhou233.orangex.render.font.stb.StbTrueTypePackContext;
import cn.xiaozhou233.orangex.render.font.stb.StbTrueTypePackRange;
import cn.xiaozhou233.orangex.render.font.stb.StbTrueTypePackedChar;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bakes a TrueType/OpenType font into a {@link FontAtlas}.
 *
 * <p>This is a thin adapter around the ported stb_truetype code in
 * {@code render.font.stb}. The original Vape renderer used a single range and
 * one fixed 4096x4096 sheet; here the character set is split into a Latin range
 * and an optional CJK range so oversampling/atlas pressure can be controlled
 * separately.</p>
 */
public final class StbFontBaker {

    private static final int DEFAULT_ATLAS_SIZE = 4096;
    private static final int LATIN_OVERSAMPLE = 2;
    private static final int CJK_OVERSAMPLE = 1;

    private StbFontBaker() {
    }

    public static FontAtlas bake(byte[] fontData, int pixelHeight,
                                 boolean includeCjk, boolean fullGbk) {
        if (fontData == null || fontData.length == 0) {
            throw new IllegalArgumentException("fontData is empty");
        }
        if (pixelHeight < 1) {
            throw new IllegalArgumentException("pixelHeight must be positive");
        }

        StbTrueTypeFontInfo info = new StbTrueTypeFontInfo();
        if (StbRasterizer.z(info, fontData, 0) == 0) {
            throw new IllegalArgumentException("Not a valid TrueType/OpenType font");
        }

        int[] ascent = new int[1];
        int[] descent = new int[1];
        int[] lineGap = new int[1];
        StbRasterizer.x(info, ascent, descent, lineGap);

        float fontScale = StbRasterizer.V(info, pixelHeight);
        float ascentPx = ascent[0] * fontScale;
        float lineHeightPx = (ascent[0] - descent[0] + lineGap[0]) * fontScale;

        int atlasSize = atlasSize();
        byte[] pixels = new byte[atlasSize * atlasSize];

        StbTrueTypePackContext packContext = new StbTrueTypePackContext();
        if (StbRasterizer.C(packContext, pixels, atlasSize, atlasSize, 0, 1) == 0) {
            throw new IllegalStateException("Unable to initialise the stb pack context");
        }
        StbRasterizer.n(packContext, LATIN_OVERSAMPLE, LATIN_OVERSAMPLE);

        int[] latin = latinCodepoints();
        StbTrueTypePackRange latinRange = createRange(latin, pixelHeight, LATIN_OVERSAMPLE);
        StbTrueTypePackRange[] ranges;

        int[] cjk = null;
        StbTrueTypePackRange cjkRange = null;
        if (includeCjk) {
            cjk = cjkCodepoints(fullGbk);
            if (cjk.length > 0) {
                cjkRange = createRange(cjk, pixelHeight, CJK_OVERSAMPLE);
            }
        }

        if (cjkRange != null) {
            ranges = new StbTrueTypePackRange[]{latinRange, cjkRange};
        } else {
            ranges = new StbTrueTypePackRange[]{latinRange};
        }

        int packed = StbRasterizer.c(packContext, fontData, 0, ranges, ranges.length);
        StbRasterizer.S(packContext);

        Map<Integer, Glyph> glyphs = new HashMap<Integer, Glyph>();
        collectGlyphs(glyphs, latinRange, latin, atlasSize, packed != 0);
        if (cjkRange != null) {
            collectGlyphs(glyphs, cjkRange, cjk, atlasSize, packed != 0);
        }

        // Missing glyph: '?' or space. The renderer falls back to it for
        // unsupported code points.
        if (!glyphs.containsKey((int) '?')) {
            Glyph space = glyphs.get((int) ' ');
            if (space != null) {
                glyphs.put((int) '?', space);
            }
        }

        return new FontAtlas(atlasSize, atlasSize, pixels, glyphs,
                ascentPx, lineHeightPx);
    }

    private static int atlasSize() {
        int configured = Integer.getInteger("orangex.font.atlasSize", DEFAULT_ATLAS_SIZE);
        if (configured < 512) {
            configured = 512;
        } else if (configured > 8192) {
            configured = 8192;
        }
        return configured;
    }

    private static StbTrueTypePackRange createRange(int[] codepoints, int pixelHeight,
                                                    int oversample) {
        StbTrueTypePackedChar[] packedChars = new StbTrueTypePackedChar[codepoints.length];
        for (int i = 0; i < packedChars.length; i++) {
            packedChars[i] = new StbTrueTypePackedChar();
        }

        StbTrueTypePackRange range = new StbTrueTypePackRange();
        range.E = pixelHeight;
        range.R = 0;
        range.N = codepoints;
        range.b = codepoints.length;
        range.M = packedChars;
        range.i = (byte) oversample;
        range.y = (byte) oversample;
        return range;
    }

    private static void collectGlyphs(Map<Integer, Glyph> out,
                                      StbTrueTypePackRange range,
                                      int[] codepoints,
                                      int atlasSize,
                                      boolean packedOk) {
        float inverse = 1.0f / (float) atlasSize;
        for (int i = 0; i < codepoints.length; i++) {
            StbTrueTypePackedChar packedChar = range.M[i];
            if (packedChar.A == 0.0f && packedChar.q == 0 && packedChar.p == 0) {
                // Glyph did not fit or has no advance; keep spaces (A > 0).
                continue;
            }
            out.put(codepoints[i], new Glyph(
                    packedChar.s,
                    packedChar.G,
                    packedChar.t,
                    packedChar.R,
                    packedChar.b * inverse,
                    packedChar.k * inverse,
                    packedChar.q * inverse,
                    packedChar.p * inverse,
                    packedChar.A
            ));
        }
        if (!packedOk) {
            System.out.println("[OrangeX] Font pack reported overflow (atlas "
                    + atlasSize + "x" + atlasSize + ")");
        }
    }

    private static int[] latinCodepoints() {
        Set<Integer> codepoints = new LinkedHashSet<Integer>();
        for (int c = 32; c <= 126; c++) {
            codepoints.add(c);
        }
        for (int c = 160; c <= 255; c++) {
            codepoints.add(c);
        }
        return toArray(codepoints);
    }

    /**
     * Decodes GBK double-byte ranges. Level 1 (common simplified Chinese) is
     * used by default; {@code fullGbk} widens it to the whole GBK range that
     * Vape covered.
     */
    private static int[] cjkCodepoints(boolean fullGbk) {
        Set<Integer> codepoints = new LinkedHashSet<Integer>();
        int highStart = fullGbk ? 0xA1 : 0xB0;
        int highEnd = fullGbk ? 0xF7 : 0xD7;
        Charset gbk;
        try {
            gbk = Charset.forName("GBK");
        } catch (Exception ignored) {
            return new int[0];
        }

        for (int high = highStart; high <= highEnd; high++) {
            for (int low = 0xA1; low <= 0xFE; low++) {
                String decoded = new String(
                        new byte[]{(byte) high, (byte) low}, gbk);
                if (decoded.length() != 1) {
                    continue;
                }
                char c = decoded.charAt(0);
                if (c >= '\u4e00' && c <= '\u9fff') {
                    codepoints.add((int) c);
                }
            }
        }
        return toArray(codepoints);
    }

    private static int[] toArray(Set<Integer> values) {
        int[] result = new int[values.size()];
        int index = 0;
        for (Integer value : values) {
            result[index++] = value;
        }
        return result;
    }
}
