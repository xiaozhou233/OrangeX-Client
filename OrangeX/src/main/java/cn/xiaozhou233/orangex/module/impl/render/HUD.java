package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.greenrobot.eventbus.Subscribe;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

public class HUD extends Module {

    private Minecraft mc;
    private FontRenderer fr;

    private final ArrayList<String> modules = new ArrayList<>();

    private float rainbowTick = 0f;

    public HUD() {
        modules.add("Juice uwu");
        modules.add("ExampleA");
        modules.add("ExampleB");
        modules.add("ExampleC");
        modules.add("114514");
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {

        // Lazy init, safe in MC thread
        if (mc == null) {
            mc = Minecraft.getMinecraft();
            if (mc == null || mc.fontRendererObj == null) return;
            fr = mc.fontRendererObj;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        float partialTicks = event.getPartialTicks();

        rainbowTick += partialTicks;

        renderLogo();
        renderModuleList(sr, rainbowTick);
    }

    private void renderLogo() {
        fr.drawStringWithShadow(
                "OrangeX Client",
                10,
                10,
                0xFFA500
        );
    }

    private void renderModuleList(ScaledResolution sr, float baseTick) {
        int margin = 6;
        int y = margin;
        int index = 0;

        modules.sort(
                Comparator.comparingInt(fr::getStringWidth).reversed()
        );

        for (String module : modules) {
            int textWidth = fr.getStringWidth(module);
            int x = sr.getScaledWidth() - textWidth - margin;

            int color = getRainbowColor(baseTick + index * 6f);

            drawHudString(module, x, y, color);

            y += fr.FONT_HEIGHT + 4;
            index++;
        }
    }

    private void drawHudString(String text, int x, int y, int color) {
        int padding = 2;

        Gui.drawRect(
                x - padding,
                y - padding,
                x + fr.getStringWidth(text) + padding,
                y + fr.FONT_HEIGHT + padding,
                0x70000000
        );

        fr.drawStringWithShadow(text, x, y, color);
    }

    private int getRainbowColor(float tick) {
        float cycle = 60f;
        float hue = (tick % cycle) / cycle;
        return Color.HSBtoRGB(hue, 0.9f, 1.0f);
    }
}

