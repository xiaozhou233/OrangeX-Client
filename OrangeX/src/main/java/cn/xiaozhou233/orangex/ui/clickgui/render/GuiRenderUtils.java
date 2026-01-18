package cn.xiaozhou233.orangex.ui.clickgui.render;

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
        FontRenderer fr = mc.fontRendererObj;
        fr.drawString(text, (int) x, (int) y, color);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }
}
