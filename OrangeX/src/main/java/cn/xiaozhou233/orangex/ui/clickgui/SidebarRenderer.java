package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;

import java.util.List;

/**
 * Draws the category card on the left of the ClickGUI window.
 */
public final class SidebarRenderer {

    private static final ModuleCategory[] CATEGORIES = ModuleCategory.values();

    private SidebarRenderer() {
    }

    public static void draw(Render2D graphics, ClickGuiLayout layout,
                            ModuleCategory selectedCategory, List<Module> modules,
                            ClickGuiAnimator animator, float delta,
                            double mouseX, double mouseY) {
        float x = layout.x();
        float y = layout.y();

        float cardX = x + ClickGuiStyle.SIDEBAR_CARD_X;
        float cardY = y + ClickGuiStyle.SIDEBAR_CARD_Y;
        float cardWidth = ClickGuiStyle.SIDEBAR_WIDTH;
        float cardHeight = layout.height() - ClickGuiStyle.HEADER_HEIGHT
                - 16;

        graphics.fillRoundedRect(cardX, cardY, cardWidth, cardHeight, 6,
                1.0, ClickGuiStyle.COLOR_SIDEBAR_BG,
                ClickGuiStyle.COLOR_SIDEBAR_BORDER);

        int[] counts = new int[CATEGORIES.length];
        for (Module module : modules) {
            counts[module.getCategory().ordinal()]++;
        }

        float rowX = layout.categoryRowX();
        float rowWidth = layout.categoryRowWidth();
        float rowY = layout.categoryRowTop();
        float rowHeight = layout.categoryRowHeight();

        for (ModuleCategory category : CATEGORIES) {
            boolean selected = category == selectedCategory;
            boolean hovered = ClickGuiStyle.isInside(mouseX, mouseY, rowX, rowY,
                    rowWidth, rowHeight);
            float hover = animator.hover(category, hovered, delta);

            if (selected || hover > 0.01f) {
                int alpha = selected
                        ? (int) (22.0f + 10.0f * hover)
                        : (int) (10.0f * hover);
                graphics.fillRoundedRect(rowX, rowY, rowWidth, rowHeight, 5,
                        ClickGuiStyle.argb(alpha,
                                ClickGuiStyle.COLOR_ACCENT_RGB));
            }

            if (selected) {
                float barHeight = Math.max(4.0f, rowHeight - 12.0f);
                graphics.fillRoundedRect(rowX + 3,
                        rowY + (rowHeight - barHeight) / 2.0f,
                        2.5f, barHeight, 1.25f, ClickGuiStyle.COLOR_ACCENT);
            }

            FontRenderer label = ClickGuiStyle.font(graphics, 10.0f, selected);
            label.drawString(ClickGuiModules.displayName(category), rowX + 12,
                    ClickGuiStyle.centeredY(label, rowY, rowHeight),
                    selected ? ClickGuiStyle.COLOR_TEXT
                            : ClickGuiStyle.COLOR_TEXT_DIM);

            String count = String.valueOf(counts[category.ordinal()]);
            FontRenderer countFont = ClickGuiStyle.font(graphics, 8.0f);
            countFont.drawString(count,
                    rowX + rowWidth - 10 - countFont.getStringWidth(count),
                    ClickGuiStyle.centeredY(countFont, rowY, rowHeight),
                    selected ? ClickGuiStyle.COLOR_ACCENT
                            : ClickGuiStyle.COLOR_TEXT_MUTED);

            rowY += rowHeight + ClickGuiStyle.CATEGORY_GAP;
        }
    }
}
