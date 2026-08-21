package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.font.SmoothFontRenderer;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import org.greenrobot.eventbus.Subscribe;

import java.awt.*;

public class HUD extends Module {

    private SmoothFontRenderer font;

    private static final int LOGO_COLOR = 0xFFA500;

    public HUD() {
        super("HUD", "Render HUD", ModuleCategory.RENDER);
        setEnabled(true);
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {

        if (font == null) {
            font = OrangeX.getInstance()
                    .getStbFontManager()
                    .getJelloMedium(44);
        }

        font.drawString(
                "Orange",
                10,
                10,
                LOGO_COLOR
        );

        font.drawString(
                "X",
                10 + font.getStringWidth("Orange"),
                10,
                getRainbow(1500)
        );
    }

    private int getRainbow(long offset) {
        float hue = ((System.currentTimeMillis() + offset) % 8000L)
                / 8000.0f;

        return Color.HSBtoRGB(hue, 0.7f, 1.0f) & 0xFFFFFF;
    }
}