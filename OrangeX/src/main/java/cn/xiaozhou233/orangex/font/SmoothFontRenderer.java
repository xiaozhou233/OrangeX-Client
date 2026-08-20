package cn.xiaozhou233.orangex.font;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class SmoothFontRenderer {
    protected Map<String, String> strippedStringCache;
    protected Map<String, Double> stringWidthCache = new SmoothFontRendererWidthCache(this, 16, 0.75f, true);
    protected boolean fontSelectorEnabled = true;
    protected int fontSize;

    public void drawString(String text, double x, double y, int color) {
        drawStringStripped(text, x, y, color, false);
    }

    public abstract void drawVerticalString(String text, double x, double y, Color color);

    protected void applyColorCode(char code, float[] rgb) {
        if (code == '4') { rgb[0] = 1.0f; rgb[1] = 0.0f; rgb[2] = 0.0f; }
        if (code == 'c') { rgb[0] = 1.0f; rgb[1] = 0.33f; rgb[2] = 0.33f; }
        if (code == '6') { rgb[0] = 1.0f; rgb[1] = 0.66f; rgb[2] = 0.0f; }
        if (code == 'e') { rgb[0] = 1.0f; rgb[1] = 1.0f; rgb[2] = 0.33f; }
        if (code == '2') { rgb[0] = 0.0f; rgb[1] = 0.66f; rgb[2] = 0.0f; }
        if (code == 'a') { rgb[0] = 0.33f; rgb[1] = 1.0f; rgb[2] = 0.33f; }
        if (code == 'b') { rgb[0] = 0.33f; rgb[1] = 1.0f; rgb[2] = 1.0f; }
        if (code == '3') { rgb[0] = 0.0f; rgb[1] = 0.66f; rgb[2] = 0.66f; }
        if (code == '1') { rgb[0] = 0.0f; rgb[1] = 0.0f; rgb[2] = 0.66f; }
        if (code == '9') { rgb[0] = 0.33f; rgb[1] = 0.33f; rgb[2] = 1.0f; }
        if (code == 'd') { rgb[0] = 1.0f; rgb[1] = 0.33f; rgb[2] = 1.0f; }
        if (code == '5') { rgb[0] = 0.66f; rgb[1] = 0.0f; rgb[2] = 0.66f; }
        if (code == 'f') { rgb[0] = 1.0f; rgb[1] = 1.0f; rgb[2] = 1.0f; }
        if (code == '7') { rgb[0] = 0.66f; rgb[1] = 0.66f; rgb[2] = 0.66f; }
        if (code == '8') { rgb[0] = 0.33f; rgb[1] = 0.33f; rgb[2] = 0.33f; }
        if (code == '0') { rgb[0] = 0.0f; rgb[1] = 0.0f; rgb[2] = 0.0f; }
    }

    public void clearCaches() {
        this.stringWidthCache.clear();
        this.strippedStringCache.clear();
    }

    public void drawString(String text, double x, double y, Color color) {
        drawString(text, x, y, color.getRGB());
    }

    public int getStrippedStringCacheSize() {
        return this.strippedStringCache.size();
    }

    public void drawCenteredString(String text, double x, double y, int color) {
        drawString(text, x - getStringWidth(text) / 2.0, y, color);
    }

    public void setFontSelectorEnabled(boolean enabled) {
        this.fontSelectorEnabled = enabled;
    }

    public abstract void drawStringWithShadow(String text, double x, double y, int color);

    public abstract void renderString(String text, double x, double y, int color, boolean fallbackToVanilla);

    public double getStringWidth(String text) {
        return getStringWidth(text, false);
    }

    public double getStringHeight(String text) {
        return getStringHeight(text, false);
    }

    public void drawString(String text, double x, double y, Color color, boolean fallbackToVanilla) {
        int rgb = color.getRGB();
        renderString(text, x, y, rgb, fallbackToVanilla);
    }

    public abstract double getStringHeight(String text, boolean fallbackToVanilla);

    public void drawStringStripped(String text, double x, double y, int color, boolean shadow) {
        String stripped = this.strippedStringCache.get(text);
        if (stripped == null) {
            StringBuilder sb = new StringBuilder();
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if (c == '\u00a7') {
                    ++i;
                    continue;
                }
                sb.append(c);
            }
            stripped = sb.toString();
            this.strippedStringCache.put(text, stripped);
        }
        text = stripped;
        renderString(text, x, y, color, shadow);
    }

    public int getWidthCacheSize() {
        return this.stringWidthCache.size();
    }

    public void drawStringWithShadow(String text, double x, double y, Color color) {
        drawString(text, x + 0.5, y + 0.5, Integer.MIN_VALUE);
        drawString(text, x, y, color.getRGB());
    }

    public abstract void renderStringWithShadow(String text, double x, double y, Color color, Color shadowColor, boolean fallbackToVanilla);

    public void drawCenteredString(String text, double x, double y, Color color) {
        drawCenteredString(text, x, y, color.getRGB());
    }

    public abstract void drawStringWithShadow(String text, double x, double y, Color color, boolean fallbackToVanilla);

    public int getFontSize() {
        return this.fontSize;
    }

    public void drawStringWithShadow(String text, double x, double y, Color color, Color shadowColor) {
        renderStringWithShadow(text, x, y, color, shadowColor, false);
    }

    public SmoothFontRenderer() {
        this.strippedStringCache = new SmoothFontRendererStringCache(this, 16, 0.75f, true);
    }

    public abstract double getStringWidth(String text, boolean fallbackToVanilla);
}