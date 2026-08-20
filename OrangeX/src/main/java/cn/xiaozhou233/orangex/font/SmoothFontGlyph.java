package cn.xiaozhou233.orangex.font;

public class SmoothFontGlyph {
    public float uMin;
    public float vMin;
    public float uMax;
    public float vMax;
    public float xOffset;
    public float yOffset;
    public float xMax;
    public float yMax;
    public float advance;

    public SmoothFontGlyph(float xOffset, float yOffset, float xMax, float yMax, float uMin, float vMin, float uMax, float vMax, float advance) {
        this.xOffset = xOffset;
        this.xMax = xMax;
        this.yOffset = yOffset;
        this.yMax = yMax;
        this.uMin = uMin;
        this.uMax = uMax;
        this.vMin = vMin;
        this.vMax = vMax;
        this.advance = advance;
    }
}