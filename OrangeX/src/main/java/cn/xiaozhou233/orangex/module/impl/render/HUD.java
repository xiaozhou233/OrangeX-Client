package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.font.FontRenderer;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.greenrobot.eventbus.Subscribe;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {

    private float rainbowTick = 0f;

    private static final int MARGIN = 6;
    private static final int PADDING = 2;
    private static final int MODULE_SPACING = 4;
    private static final int LOGO_COLOR = 0xFFA500;
    private static final int BACKGROUND_ALPHA = 0x70; // 112
    private static final float RAINBOW_CYCLE = 60f;

    public HUD() {
        super("HUD", ModuleCategory.RENDER);

        // TODO: Release 时移除
        keyBind = 41;
        setEnabled(true);
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(mc);
        rainbowTick += event.getPartialTicks();

        renderLogo();
        renderModuleList(sr);
    }

    private void renderLogo() {
        OrangeX.getFontManager().JelloMedium32.drawStringWithShadow("OrangeX", 5, 5, LOGO_COLOR);
    }

    private void renderModuleList(ScaledResolution sr) {
        List<Module> enabledModules = OrangeX.getInstance().getModuleManager()
                .getAllModules()
                .stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) ->
                        OrangeX.getFontManager().MontserratMedium18.getStringWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        int y = MARGIN;
        int screenWidth = sr.getScaledWidth();

        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            int textWidth = OrangeX.getFontManager().MontserratMedium18.getStringWidth(module.getName());
            int x = screenWidth - textWidth - MARGIN;

            int color = getRainbowColor(rainbowTick + i * 6f);
            drawHudString(module.getName(), x, y, color);

            y += OrangeX.getFontManager().MontserratMedium18.getStringHeight(module.getName()) + MODULE_SPACING;
        }
    }

    private void drawHudString(String text, int x, int y, int color) {
        FontRenderer font = OrangeX.getFontManager().MontserratMedium18;
        int textWidth = font.getStringWidth(text);

        Gui.drawRect(
                x - PADDING,
                y - PADDING,
                x + textWidth + PADDING,
                y + font.getStringHeight(text) + PADDING,
                (BACKGROUND_ALPHA << 24) | 0x000000
        );

        font.drawStringWithShadow(text, x, y, color);
    }

    private int getRainbowColor(float tick) {
        float hue = (tick % RAINBOW_CYCLE) / RAINBOW_CYCLE;
        return Color.HSBtoRGB(hue, 0.9f, 1.0f);
    }
}
