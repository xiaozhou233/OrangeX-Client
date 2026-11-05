package cn.xiaozhou233.orangex.render.font;

/**
 * One glyph inside a {@link FontAtlas}.
 *
 * <p>All offsets are expressed in atlas pixels relative to the pen position on
 * the baseline. UVs are normalised to {@code [0, 1]}.</p>
 */
public final class Glyph {

    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;

    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;

    private final float advance;

    public Glyph(float x0, float y0, float x1, float y1,
                 float u0, float v0, float u1, float v1,
                 float advance) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.advance = advance;
    }

    public float getX0() {
        return x0;
    }

    public float getY0() {
        return y0;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getU0() {
        return u0;
    }

    public float getV0() {
        return v0;
    }

    public float getU1() {
        return u1;
    }

    public float getV1() {
        return v1;
    }

    public float getAdvance() {
        return advance;
    }

    public boolean isBlank() {
        return x0 == x1 || y0 == y1;
    }
}
