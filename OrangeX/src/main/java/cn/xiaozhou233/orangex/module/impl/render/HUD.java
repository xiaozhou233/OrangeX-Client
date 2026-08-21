package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.font.SmoothFontRenderer;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import org.greenrobot.eventbus.Subscribe;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {

    private SmoothFontRenderer font;
    private SmoothFontRenderer listFont;

    @Getter
    @Setter
    private int x = 10;

    @Getter
    @Setter
    private int y = 10;

    @Getter
    @Setter
    private int listOffsetY = 10;

    @Getter
    @Setter
    private boolean rainbow = true;

    @Getter
    @Setter
    private boolean showList = true;

    @Getter
    @Setter
    private boolean listBackground = true;

    @Getter
    @Setter
    private int backgroundAlpha = 140;

    @Getter
    @Setter
    private int barWidth = 3;

    public HUD() {
        super("HUD", "Render HUD", ModuleCategory.RENDER);
        setEnabled(true);
    }

    private void renderLogo() {
        font.drawString("Orange",x,y,0xFFFFA500);
        font.drawString("X",x + font.getStringWidth("Orange"),y,getRainbow(1500));
    }

    private void renderList() {
        if (!showList)
            return;

        List<Module> modules =
                OrangeX.getInstance()
                        .getModuleManager()
                        .getModules()
                        .stream()
                        .filter(Module::isEnabled)
                        .sorted(Comparator.comparingDouble(m -> -listFont.getStringWidth(m.getName())))
                        .collect(Collectors.toList());

        int lineHeight = (int) listFont.getStringHeight("LOL");
        int listY = y + (int) font.getStringHeight("Orange") + listOffsetY;

        int height = modules.size() * lineHeight;
        int width = 0;

        for (Module module : modules) {
            width = (int) Math.max(width,listFont.getStringWidth(module.getName()));
        }

        width += 12;

        if (listBackground) {
            Gui.drawRect(
                    x - 5,
                    listY - 3,
                    x + width,
                    listY + height + 3,
                    (backgroundAlpha << 24)
            );

            for (int i = 0; i < height + 6; i++) {
                Gui.drawRect(
                        x - 5,
                        listY - 3 + i,
                        x - 5 + barWidth,
                        listY - 2 + i,
                        getRainbow(i * 20)
                );
            }
        }

        int index = 0;

        for (Module module : modules) {
            int color = rainbow ? getRainbow(index * 350) : 0xFFFFFFFF;

            listFont.drawString(module.getName(),x + 5,listY,color);

            listY += lineHeight;
            index++;
        }
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        if (font == null) {
            font = OrangeX.getInstance().getStbFontManager().getJelloMedium(44);
            listFont = OrangeX.getInstance().getStbFontManager().getProxima(22);
        }

        renderLogo();
        renderList();
    }

    private int getRainbow(long offset) {
        float hue = ((System.currentTimeMillis() + offset) % 8000L) / 8000.0f;
        return Color.HSBtoRGB(hue,0.7f,1.0f);
    }
}