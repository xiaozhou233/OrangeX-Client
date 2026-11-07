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
 *
 * <p>Atlas size is chosen automatically from the requested glyph set (a fixed
 * 4096x4096 sheet wastes 4x-16x the memory for Latin-only faces). The grown
 * sizes are retried on overflow, so the estimate only affects bake time, never
 * correctness. {@code -Dorangex.font.atlasSize=<n>} pins an exact size for
 * compatibility with the old behaviour.</p>
 */
public final class StbFontBaker {

    private static final int MIN_ATLAS_SIZE = 512;
    private static final int MAX_ATLAS_SIZE = 8192;
    private static final int LATIN_OVERSAMPLE = 2;
    private static final int CJK_OVERSAMPLE = 1;

    /**
     * Fraction of the sheet expected to be occupied by glyph boxes. The
     * per-glyph areas below are calibrated against stb's real packing output
     * for the bundled fonts; overflow still doubles and retries, so the
     * estimate only trades a possible extra bake against wasted VRAM.
     */
    private static final double PACK_FILL = 0.92;
    private static final double LATIN_AREA_FACTOR = 0.55;
    private static final double CJK_AREA_FACTOR = 0.36;

    /** CJK glyph sets never fit a 512 sheet in practice; skip the retry. */
    private static final int MIN_CJK_ATLAS_SIZE = 1024;

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

        int[] latin = latinCodepoints();
        int[] cjk = includeCjk ? cjkCodepoints(fullGbk) : new int[0];

        Integer pinned = Integer.getInteger("orangex.font.atlasSize");
        int atlasSize = pinned != null
                ? clampAtlasSize(pinned)
                : estimateAtlasSize(latin.length, cjk.length, pixelHeight);

        while (true) {
            Result result = bakeAt(fontData, pixelHeight, latin, cjk,
                    atlasSize, ascentPx, lineHeightPx);
            if (!result.overflow) {
                return result.atlas;
            }
            if (pinned != null || atlasSize >= MAX_ATLAS_SIZE) {
                // Keep drawing with the glyphs that did fit rather than
                // failing the whole frame.
                System.out.println("[OrangeX] Font atlas overflow at "
                        + atlasSize + "x" + atlasSize
                        + "; some glyphs are missing. Raise "
                        + "-Dorangex.font.atlasSize.");
                return result.atlas;
            }
            atlasSize = Math.min(MAX_ATLAS_SIZE, atlasSize * 2);
        }
    }

    private static Result bakeAt(byte[] fontData, int pixelHeight,
                                 int[] latin, int[] cjk, int atlasSize,
                                 float ascentPx, float lineHeightPx) {
        byte[] pixels = new byte[atlasSize * atlasSize];

        StbTrueTypePackContext packContext = new StbTrueTypePackContext();
        if (StbRasterizer.C(packContext, pixels, atlasSize, atlasSize, 0, 1) == 0) {
            throw new IllegalStateException("Unable to initialise the stb pack context");
        }

        // stb packs every range with the context-wide oversampling, so the
        // ranges are packed in two passes to give Latin 2x2 and CJK 1x1 (the
        // latter alone would otherwise quadruple CJK atlas memory).
        StbTrueTypePackRange latinRange = createRange(
                latin, pixelHeight, LATIN_OVERSAMPLE);
        StbTrueTypePackRange cjkRange = cjk.length > 0
                ? createRange(cjk, pixelHeight, CJK_OVERSAMPLE)
                : null;

        StbRasterizer.n(packContext, LATIN_OVERSAMPLE, LATIN_OVERSAMPLE);
        boolean packed = StbRasterizer.c(packContext, fontData, 0,
                new StbTrueTypePackRange[]{latinRange}, 1) != 0;

        if (cjkRange != null) {
            StbRasterizer.n(packContext, CJK_OVERSAMPLE, CJK_OVERSAMPLE);
            packed &= StbRasterizer.c(packContext, fontData, 0,
                    new StbTrueTypePackRange[]{cjkRange}, 1) != 0;
        }
        StbRasterizer.S(packContext);

        Map<Integer, Glyph> glyphs = new HashMap<Integer, Glyph>();
        collectGlyphs(glyphs, latinRange, latin, atlasSize);
        if (cjkRange != null) {
            collectGlyphs(glyphs, cjkRange, cjk, atlasSize);
        }

        // Missing glyph: '?' or space. The renderer falls back to it for
        // unsupported code points.
        if (!glyphs.containsKey((int) '?')) {
            Glyph space = glyphs.get((int) ' ');
            if (space != null) {
                glyphs.put((int) '?', space);
            }
        }

        FontAtlas atlas = new FontAtlas(atlasSize, atlasSize, pixels, glyphs,
                ascentPx, lineHeightPx);
        return new Result(atlas, !packed);
    }

    private static void collectGlyphs(Map<Integer, Glyph> out,
                                      StbTrueTypePackRange range,
                                      int[] codepoints,
                                      int atlasSize) {
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
    }

    private static int estimateAtlasSize(int latinCount, int cjkCount,
                                         int pixelHeight) {
        double latinCell = LATIN_OVERSAMPLE * (double) pixelHeight;
        double cjkCell = CJK_OVERSAMPLE * (double) pixelHeight;
        double area = latinCount * latinCell * latinCell * LATIN_AREA_FACTOR
                + cjkCount * cjkCell * cjkCell * CJK_AREA_FACTOR;
        int side = (int) Math.ceil(Math.sqrt(area / PACK_FILL));
        if (cjkCount > 0 && side < MIN_CJK_ATLAS_SIZE) {
            side = MIN_CJK_ATLAS_SIZE;
        }
        return nextPowerOfTwo(clampAtlasSize(side));
    }

    private static int nextPowerOfTwo(int value) {
        int result = MIN_ATLAS_SIZE;
        while (result < value && result < MAX_ATLAS_SIZE) {
            result <<= 1;
        }
        return Math.min(result, MAX_ATLAS_SIZE);
    }

    private static int clampAtlasSize(int size) {
        if (size < MIN_ATLAS_SIZE) {
            return MIN_ATLAS_SIZE;
        }
        if (size > MAX_ATLAS_SIZE) {
            return MAX_ATLAS_SIZE;
        }
        return size;
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

    private static final class Result {
        private final FontAtlas atlas;
        private final boolean overflow;

        private Result(FontAtlas atlas, boolean overflow) {
            this.atlas = atlas;
            this.overflow = overflow;
        }
    }
}
