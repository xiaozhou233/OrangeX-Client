package cn.xiaozhou233.orangex.ui.clickgui.render;

import cn.xiaozhou233.orangex.OrangeX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class GuiRenderUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawRect(double x, double y, double w, double h, int color) {
        Gui.drawRect((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    public static void drawString(String text, double x, double y, int color) {
        OrangeX.getFontManager().MontserratRegular18
                .drawString(text, (float) x, (float) y, color);
    }

    public static void drawCenteredString(String text, double x, double y, int color) {
        OrangeX.getFontManager().MontserratRegular18
                .drawString(text, (float) x - (float) OrangeX.getFontManager().MontserratLight18.getStringWidth(text) / 2, (float) y, color);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static int blendColor(int color1, int color2, float ratio) {
        if (ratio <= 0f) return color1;
        if (ratio >= 1f) return color2;

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

}
