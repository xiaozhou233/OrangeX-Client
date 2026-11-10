
package cn.xiaozhou233.orangex.modules.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import org.greenrobot.eventbus.Subscribe;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {

    private static final float RAINBOW_CYCLE = 6000f;

    private static final int LOGO_FONT_SIZE = 22;
    private static final int MODULE_FONT_SIZE = 10;

    private static final int LOGO_START_X = 5;
    private static final int LOGO_START_Y = 5;

    private static final int MODULE_MARGIN = 6;
    private static final int MODULE_SPACING = 4;

    private static final int RAINBOW_OFFSET = 200;

    public HUD() {
        super("HUD", ModuleCategory.RENDER);
        setEnabled(true);
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        long currentTime = System.currentTimeMillis();

        renderLogo(event, currentTime);
        renderModuleList(event, currentTime);
    }

    private void renderLogo(EventRender2D event, long currentTime) {
        String title = "OrangeX";
        FontRenderer font = event.getRender2D().getFont(LOGO_FONT_SIZE);

        int x = LOGO_START_X;
        int y = LOGO_START_Y;

        for (int i = 0; i < title.length(); i++) {
            String character = String.valueOf(title.charAt(i));

            font.drawString(character, x, y, getRainbowColor(currentTime, i));

            x += (int) font.getStringWidth(character);
        }
    }

    private void renderModuleList(EventRender2D event, long currentTime) {
        FontRenderer font = event.getRender2D().getFont(MODULE_FONT_SIZE);
        List<Module> modules = OrangeX.getInstance()
                .getModuleManager()
                .getAllModules()
                .stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingDouble(
                        module -> -font.getStringWidth(module.getName())
                ))
                .collect(Collectors.toList());

        int screenWidth = event.getRender2D().getWidth();
        int y = MODULE_MARGIN;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            String name = module.getName();

            int textWidth = (int) font.getStringWidth(name);
            int x = screenWidth - textWidth - MODULE_MARGIN;

            font.drawStringWithShadow(name, x, y, getRainbowColor(currentTime, i));

            y += (int) (font.getStringHeight() + MODULE_SPACING);
        }
    }

    private int getRainbowColor(long currentTime, int index) {
        long offset = (long) index * RAINBOW_OFFSET;

        float hue = ((currentTime + offset) % (long) RAINBOW_CYCLE)
                / RAINBOW_CYCLE;

        return Color.HSBtoRGB(hue, 0.9f, 1.0f);
    }
}
