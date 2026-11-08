package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.value.KeybindValue;

import java.util.List;

/**
 * Draws the module list for the selected category.
 *
 * <p>Module settings are not drawn inline; right-clicking a module opens the
 * separate {@link SettingsWindow}.</p>
 */
public final class ContentRenderer {

    private ContentRenderer() {
    }

    public static void draw(Render2D graphics, ClickGuiLayout layout,
                            List<Module> modules, ModuleCategory category,
                            ClickGuiAnimator animator, float delta,
                            double mouseX, double mouseY) {
        float x = layout.x();
        float y = layout.y();
        float contentX = layout.contentX();
        float contentWidth = layout.contentWidth();

        FontRenderer title = ClickGuiStyle.font(graphics, 13.0f, true);
        title.drawString(ClickGuiModules.displayName(category), contentX,
                y + ClickGuiStyle.HEADER_HEIGHT + 10,
                ClickGuiStyle.COLOR_TEXT);

        FontRenderer meta = ClickGuiStyle.font(graphics, 8.0f);
        String metaText = modules.size()
                + (modules.size() == 1 ? " MODULE" : " MODULES");
        meta.drawString(metaText,
                contentX + contentWidth - meta.getStringWidth(metaText),
                y + ClickGuiStyle.HEADER_HEIGHT + 14,
                ClickGuiStyle.COLOR_TEXT_MUTED);

        graphics.fillRect(contentX, y + ClickGuiStyle.HEADER_HEIGHT + 30,
                contentWidth, 1, ClickGuiStyle.COLOR_DIVIDER);

        float listTop = layout.listTop();
        float listBottom = layout.listBottom();

        graphics.enableScissor(contentX - 2, listTop - 2,
                contentWidth + 4, listBottom - listTop + 4);

        if (modules.isEmpty()) {
            FontRenderer empty = ClickGuiStyle.font(graphics, 9.0f);
            String text = "No modules here yet";
            empty.drawString(text,
                    contentX + (contentWidth - empty.getStringWidth(text)) / 2.0,
                    listTop + 14, ClickGuiStyle.COLOR_TEXT_MUTED);
        } else {
            float rowY = listTop;

            for (Module module : modules) {
                boolean hovered = ClickGuiStyle.isInside(mouseX, mouseY,
                        contentX, rowY, contentWidth,
                        ClickGuiStyle.MODULE_HEIGHT);
                float hover = animator.hover(module, hovered, delta);

                drawModuleRow(graphics, layout, module, rowY, hover,
                        animator, delta);

                rowY += ClickGuiStyle.MODULE_HEIGHT + ClickGuiStyle.MODULE_GAP;
            }
        }

        graphics.disableScissor();
    }

    private static void drawModuleRow(Render2D graphics, ClickGuiLayout layout,
                                      Module module, float rowY, float hover,
                                      ClickGuiAnimator animator, float delta) {
        float contentX = layout.contentX();
        float contentWidth = layout.contentWidth();
        float rowRight = contentX + contentWidth;
        float moduleHeight = ClickGuiStyle.MODULE_HEIGHT;

        int background = module.isEnabled()
                ? ClickGuiStyle.argb((int) (14.0f + 14.0f * hover),
                        ClickGuiStyle.COLOR_ACCENT_RGB)
                : ClickGuiStyle.argb((int) (6.0f + 10.0f * hover),
                        0x00FFFFFF);
        graphics.fillRoundedRect(contentX, rowY, contentWidth, moduleHeight,
                5, background);

        if (module.isEnabled()) {
            graphics.fillRoundedRect(contentX + 2, rowY + 8, 2.0f,
                    moduleHeight - 16, 1.0f, ClickGuiStyle.COLOR_ACCENT);
        }

        FontRenderer name = ClickGuiStyle.font(graphics, 10.0f,
                module.isEnabled());
        name.drawString(module.getName(), contentX + 10,
                ClickGuiStyle.centeredY(name, rowY, moduleHeight),
                module.isEnabled() ? ClickGuiStyle.COLOR_TEXT
                        : ClickGuiStyle.COLOR_TEXT_DIM);

        KeybindValue keybind = module.getKeybind();
        if (keybind.isBound()) {
            FontRenderer keyFont = ClickGuiStyle.font(graphics, 8.0f);
            String key = KeyNames.keyName(keybind.getKey());
            keyFont.drawString(key,
                    rowRight - ClickGuiStyle.TOGGLE_WIDTH - 18
                            - keyFont.getStringWidth(key),
                    ClickGuiStyle.centeredY(keyFont, rowY, moduleHeight),
                    ClickGuiStyle.COLOR_TEXT_MUTED);
        }

        float toggleX = rowRight - ClickGuiStyle.TOGGLE_WIDTH - 10;
        float toggleY = rowY + (moduleHeight - ClickGuiStyle.TOGGLE_HEIGHT)
                / 2.0f;
        ClickGuiStyle.drawSwitch(graphics, toggleX, toggleY,
                ClickGuiStyle.TOGGLE_WIDTH, ClickGuiStyle.TOGGLE_HEIGHT,
                animator.state(module, module.isEnabled(), delta));
    }
}
