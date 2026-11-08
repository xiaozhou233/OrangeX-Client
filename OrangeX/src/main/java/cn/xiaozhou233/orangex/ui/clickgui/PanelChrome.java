package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;

/**
 * Draws the parts of the window that never change with the selected category:
 * the drop shadow and the header.
 */
public final class PanelChrome {

    private PanelChrome() {
    }

    public static void drawShadow(Render2D graphics, ClickGuiLayout layout) {
        float x = layout.x();
        float y = layout.y();
        float width = layout.width();
        float height = layout.height();

        graphics.fillRoundedRect(x - 4, y + 3, width + 8, height + 6,
                ClickGuiStyle.PANEL_RADIUS + 4, 0x1C000000);
        graphics.fillRoundedRect(x - 2, y + 1, width + 4, height + 3,
                ClickGuiStyle.PANEL_RADIUS + 2, 0x26000000);
    }

    public static void drawHeader(Render2D graphics, ClickGuiLayout layout) {
        float x = layout.x();
        float y = layout.y();
        float width = layout.width();

        FontRenderer logo = ClickGuiStyle.font(graphics, 16.0f, true);
        String brand = "Orange";
        double brandX = x + 16;
        double brandY = y + 9;

        logo.drawString(brand, brandX, brandY, ClickGuiStyle.COLOR_TEXT);
        logo.drawString("X", brandX + logo.getStringWidth(brand), brandY,
                ClickGuiStyle.COLOR_ACCENT);

        FontRenderer subtitle = ClickGuiStyle.font(graphics, 7.5f, true);
        subtitle.drawString("CLICK GUI", x + 17, y + 28,
                ClickGuiStyle.COLOR_TEXT_MUTED);

        graphics.fillRect(x + 14, y + ClickGuiStyle.HEADER_HEIGHT - 1,
                width - 28, 1, ClickGuiStyle.COLOR_DIVIDER);
    }
}
