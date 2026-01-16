package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.greenrobot.eventbus.Subscribe;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {

    private float rainbowTick = 0f;

    // HUD 常量
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
        fr.drawStringWithShadow("OrangeX Client", MARGIN, MARGIN, LOGO_COLOR);
    }

    private void renderModuleList(ScaledResolution sr) {
        List<Module> enabledModules = OrangeX.getModuleManager()
                .getAllModules()
                .stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> fr.getStringWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        int y = MARGIN;
        int screenWidth = sr.getScaledWidth();

        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            int textWidth = fr.getStringWidth(module.getName());
            int x = screenWidth - textWidth - MARGIN;

            int color = getRainbowColor(rainbowTick + i * 6f);
            drawHudString(module.getName(), x, y, color);

            y += fr.FONT_HEIGHT + MODULE_SPACING;
        }
    }

    private void drawHudString(String text, int x, int y, int color) {
        int textWidth = fr.getStringWidth(text);
        Gui.drawRect(
                x - PADDING,
                y - PADDING,
                x + textWidth + PADDING,
                y + fr.FONT_HEIGHT + PADDING,
                (BACKGROUND_ALPHA << 24) | 0x000000
        );
        fr.drawStringWithShadow(text, x, y, color);
    }

    private int getRainbowColor(float tick) {
        float hue = (tick % RAINBOW_CYCLE) / RAINBOW_CYCLE;
        return Color.HSBtoRGB(hue, 0.9f, 1.0f);
    }
}
