package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.value.BooleanValue;
import cn.xiaozhou233.orangex.value.ModeValue;
import cn.xiaozhou233.orangex.value.Value;

import java.util.List;

/**
 * The settings page shown in the content area of {@link MainPanel}.
 *
 * <p>Opening a module's settings replaces the module list instead of spawning a
 * separate window. The page keeps the list's scroll viewport and is left with
 * the back button drawn next to the module name.</p>
 */
public final class SettingsSection {

    /** Height of one settings row. */
    public static final float ROW_HEIGHT = 22f;

    private static final float BACK_SIZE = 16f;
    private static final float BACK_GAP = 6f;

    private static final Object BACK_HOVER_KEY = new Object();

    private SettingsSection() {
    }

    /**
     * @return the total height of the rows, which may exceed the visible list.
     */
    public static float contentHeight(List<Value<?>> settings) {
        return settings.size() * ROW_HEIGHT;
    }

    public static float maxScroll(ClickGuiLayout layout, List<Value<?>> settings) {
        return Math.max(0.0f,
                contentHeight(settings)
                        - (layout.listBottom() - layout.listTop()));
    }

    public static void draw(Render2D graphics, ClickGuiLayout layout,
                            Module module, List<Value<?>> settings, float scroll,
                            ClickGuiAnimator animator, float delta,
                            double mouseX, double mouseY) {
        float contentX = layout.contentX();
        float contentWidth = layout.contentWidth();
        float headerY = layout.y() + ClickGuiStyle.HEADER_HEIGHT;

        drawBackButton(graphics, layout, animator, delta, mouseX, mouseY);

        FontRenderer title = ClickGuiStyle.font(graphics, 13.0f, true);
        title.drawString(module.getName(),
                contentX + BACK_SIZE + BACK_GAP + 4,
                headerY + 10, ClickGuiStyle.COLOR_TEXT);

        FontRenderer meta = ClickGuiStyle.font(graphics, 8.0f);
        String metaText = "SETTINGS";
        meta.drawString(metaText,
                contentX + contentWidth - meta.getStringWidth(metaText),
                headerY + 14, ClickGuiStyle.COLOR_TEXT_MUTED);

        graphics.fillRect(contentX, headerY + 30, contentWidth, 1,
                ClickGuiStyle.COLOR_DIVIDER);

        float listTop = layout.listTop();
        float listBottom = layout.listBottom();

        graphics.enableScissor(contentX - 2, listTop - 2,
                contentWidth + 4, listBottom - listTop + 4);

        float rowY = listTop - scroll;
        for (Value<?> value : settings) {
            if (rowY + ROW_HEIGHT >= listTop && rowY <= listBottom) {
                drawRow(graphics, layout, value, rowY, animator, delta,
                        mouseX, mouseY);
            }
            rowY += ROW_HEIGHT;
        }

        graphics.disableScissor();

        ClickGuiStyle.drawScrollbar(graphics, contentX + contentWidth - 3,
                listTop, listBottom - listTop, contentHeight(settings), scroll);
    }

    private static void drawBackButton(Render2D graphics, ClickGuiLayout layout,
                                       ClickGuiAnimator animator, float delta,
                                       double mouseX, double mouseY) {
        float backX = layout.contentX();
        float backY = backTop(layout);
        boolean hovered = ClickGuiStyle.isInside(mouseX, mouseY, backX, backY,
                BACK_SIZE, BACK_SIZE);
        float hover = animator.hover(BACK_HOVER_KEY, hovered, delta);

        if (hover > 0.01f) {
            graphics.fillRoundedRect(backX, backY, BACK_SIZE, BACK_SIZE, 4,
                    ClickGuiStyle.argb((int) (22.0f * hover),
                            ClickGuiStyle.COLOR_ACCENT_RGB));
        }

        FontRenderer arrow = ClickGuiStyle.font(graphics, 9.0f, true);
        arrow.drawCenteredString("<", backX + BACK_SIZE / 2.0,
                ClickGuiStyle.centeredY(arrow, backY, BACK_SIZE),
                hovered ? ClickGuiStyle.COLOR_ACCENT
                        : ClickGuiStyle.COLOR_TEXT_MUTED);
    }

    private static void drawRow(Render2D graphics, ClickGuiLayout layout,
                                Value<?> value, float rowY,
                                ClickGuiAnimator animator, float delta,
                                double mouseX, double mouseY) {
        float rowLeft = layout.contentX();
        float rowWidth = layout.contentWidth();
        boolean hovered = mouseY >= layout.listTop()
                && mouseY <= layout.listBottom()
                && ClickGuiStyle.isInside(mouseX, mouseY, rowLeft, rowY,
                        rowWidth, ROW_HEIGHT);
        float hover = animator.hover(value, hovered, delta);

        if (hover > 0.01f) {
            graphics.fillRoundedRect(rowLeft, rowY, rowWidth, ROW_HEIGHT, 4,
                    ClickGuiStyle.argb((int) (10.0f * hover), 0x00FFFFFF));
        }

        FontRenderer label = ClickGuiStyle.font(graphics, 9.0f);
        label.drawString(value.getName(), rowLeft + 8,
                ClickGuiStyle.centeredY(label, rowY, ROW_HEIGHT),
                ClickGuiStyle.COLOR_TEXT_DIM);

        float right = rowLeft + rowWidth - 8;

        if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            float toggleWidth = ClickGuiStyle.SMALL_TOGGLE_WIDTH;
            float toggleHeight = ClickGuiStyle.SMALL_TOGGLE_HEIGHT;
            ClickGuiStyle.drawSwitch(graphics,
                    right - toggleWidth,
                    rowY + (ROW_HEIGHT - toggleHeight) / 2.0f,
                    toggleWidth, toggleHeight,
                    animator.state(value, booleanValue.isEnabled(), delta));
        } else if (value instanceof ModeValue) {
            ModeValue modeValue = (ModeValue) value;
            FontRenderer modeFont = ClickGuiStyle.font(graphics, 9.0f);
            String text = "< " + modeValue.getMode() + " >";
            double pillWidth = modeFont.getStringWidth(text) + 10;
            double pillX = right - pillWidth;

            graphics.fillRoundedRect(pillX, rowY + 4, pillWidth,
                    ROW_HEIGHT - 8, 4,
                    ClickGuiStyle.argb(16, 0x00FFFFFF));
            modeFont.drawString(text, pillX + 5,
                    ClickGuiStyle.centeredY(modeFont, rowY, ROW_HEIGHT),
                    ClickGuiStyle.COLOR_ACCENT);
        } else {
            FontRenderer valueFont = ClickGuiStyle.font(graphics, 9.0f);
            String text = String.valueOf(value.getValue());
            valueFont.drawString(text, right - valueFont.getStringWidth(text),
                    ClickGuiStyle.centeredY(valueFont, rowY, ROW_HEIGHT),
                    ClickGuiStyle.COLOR_TEXT_MUTED);
        }
    }

    public static boolean isBackButton(ClickGuiLayout layout, double mouseX,
                                       double mouseY) {
        return ClickGuiStyle.isInside(mouseX, mouseY, layout.contentX(),
                backTop(layout), BACK_SIZE, BACK_SIZE);
    }

    /**
     * @return the index of the settings row under {@code mouseY}, or {@code -1}
     *         when the pointer is between rows.
     */
    public static int rowIndexAt(ClickGuiLayout layout,
                                 List<Value<?>> settings, float scroll,
                                 float mouseY) {
        if (mouseY < layout.listTop() || mouseY > layout.listBottom()) {
            return -1;
        }
        float local = mouseY - (layout.listTop() - scroll);
        int index = (int) Math.floor(local / ROW_HEIGHT);
        return index >= 0 && index < settings.size() ? index : -1;
    }

    /**
     * Applies a click to one settings row. Left-click toggles or advances,
     * right-click steps a mode back; other value types are display-only.
     */
    public static void click(List<Value<?>> settings, int index, int button) {
        if (index < 0 || index >= settings.size()) {
            return;
        }
        if (button != 0 && button != 1) {
            return;
        }

        Value<?> value = settings.get(index);
        if (value instanceof BooleanValue) {
            if (button == 0) {
                ((BooleanValue) value).toggle();
            }
        } else if (value instanceof ModeValue) {
            if (button == 0) {
                ((ModeValue) value).next();
            } else {
                ((ModeValue) value).previous();
            }
        }
    }

    private static float backTop(ClickGuiLayout layout) {
        return layout.y() + ClickGuiStyle.HEADER_HEIGHT + 8.0f;
    }
}
