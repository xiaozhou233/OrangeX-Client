package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.render.api.FontRenderer;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.value.BooleanValue;
import cn.xiaozhou233.orangex.value.ModeValue;
import cn.xiaozhou233.orangex.value.Value;

import java.util.List;

/**
 * A small draggable window that shows the settings of one module.
 *
 * <p>Opened by right-clicking a module row in {@link MainPanel}. Clicking
 * outside the window, pressing its close button or closing the GUI dismisses
 * it.</p>
 */
public final class SettingsWindow {

    private static final Object CLOSE_HOVER_KEY = new Object();

    private static final float WIDTH = 236f;
    private static final float HEADER_HEIGHT = 30f;
    private static final float ROW_HEIGHT = 22f;
    private static final float BODY_PADDING = 8f;
    private static final float RADIUS = 7f;
    private static final float CLOSE_SIZE = 16f;

    private final ClickGuiAnimator animator = new ClickGuiAnimator();

    private Module module;
    private List<Value<?>> settings;

    private float x;
    private float y;
    private float width;
    private float height;

    private int screenWidth = 854;
    private int screenHeight = 480;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public boolean isOpen() {
        return module != null;
    }

    public void open(Module module, ClickGuiLayout layout) {
        List<Value<?>> moduleSettings = ClickGuiModules.settingsOf(module);
        if (moduleSettings.isEmpty()) {
            close();
            return;
        }

        this.module = module;
        this.settings = moduleSettings;
        this.width = WIDTH;
        this.height = HEADER_HEIGHT + BODY_PADDING * 2.0f
                + moduleSettings.size() * ROW_HEIGHT;
        this.screenWidth = layout.screenWidth();
        this.screenHeight = layout.screenHeight();
        this.dragging = false;

        this.x = layout.x() + (layout.width() - width) / 2.0f;
        this.y = layout.y() + (layout.height() - height) / 2.0f;
        this.x = ClickGuiStyle.clamp(x, 4.0f,
                Math.max(4.0f, screenWidth - width - 4.0f));
        this.y = ClickGuiStyle.clamp(y, 4.0f,
                Math.max(4.0f, screenHeight - height - 4.0f));
        this.x = Math.round(x);
        this.y = Math.round(y);
    }

    public void close() {
        module = null;
        settings = null;
        dragging = false;
    }

    public void render(Render2D graphics, float delta,
                       double mouseX, double mouseY) {
        if (!isOpen()) {
            return;
        }

        graphics.fillRoundedRect(x - 3, y + 2, width + 6, height + 4,
                RADIUS + 3, 0x26000000);
        graphics.fillRoundedRect(x, y, width, height, RADIUS, 1.0,
                ClickGuiStyle.COLOR_PANEL, ClickGuiStyle.COLOR_BORDER);

        FontRenderer title = ClickGuiStyle.font(graphics, 11.0f, true);
        title.drawString(module.getName(), x + 12, y + 7,
                ClickGuiStyle.COLOR_TEXT);

        FontRenderer subtitle = ClickGuiStyle.font(graphics, 7.0f, true);
        subtitle.drawString("SETTINGS", x + 12, y + 19,
                ClickGuiStyle.COLOR_TEXT_MUTED);

        drawCloseButton(graphics, delta, mouseX, mouseY);

        graphics.fillRect(x + 10, y + HEADER_HEIGHT - 1, width - 20, 1,
                ClickGuiStyle.COLOR_DIVIDER);

        float rowY = y + HEADER_HEIGHT + BODY_PADDING;
        for (Value<?> value : settings) {
            drawSettingRow(graphics, value, rowY, delta, mouseX, mouseY);
            rowY += ROW_HEIGHT;
        }
    }

    private void drawCloseButton(Render2D graphics, float delta,
                                 double mouseX, double mouseY) {
        float closeX = x + width - 12 - CLOSE_SIZE;
        float closeY = y + (HEADER_HEIGHT - CLOSE_SIZE) / 2.0f;
        boolean hovered = ClickGuiStyle.isInside(mouseX, mouseY, closeX,
                closeY, CLOSE_SIZE, CLOSE_SIZE);
        float hover = animator.hover(CLOSE_HOVER_KEY, hovered, delta);

        if (hover > 0.01f) {
            graphics.fillRoundedRect(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE,
                    4, ClickGuiStyle.argb((int) (22.0f * hover),
                            ClickGuiStyle.COLOR_ACCENT_RGB));
        }

        FontRenderer close = ClickGuiStyle.font(graphics, 9.0f, true);
        close.drawCenteredString("x", closeX + CLOSE_SIZE / 2.0,
                closeY + (CLOSE_SIZE - close.getStringHeight()) / 2.0,
                hovered ? ClickGuiStyle.COLOR_ACCENT
                        : ClickGuiStyle.COLOR_TEXT_MUTED);
    }

    private void drawSettingRow(Render2D graphics, Value<?> value, float rowY,
                                float delta, double mouseX, double mouseY) {
        boolean hovered = ClickGuiStyle.isInside(mouseX, mouseY, x + 6, rowY,
                width - 12, ROW_HEIGHT);
        float hover = animator.hover(value, hovered, delta);

        if (hover > 0.01f) {
            graphics.fillRoundedRect(x + 6, rowY, width - 12, ROW_HEIGHT, 4,
                    ClickGuiStyle.argb((int) (10.0f * hover), 0x00FFFFFF));
        }

        FontRenderer label = ClickGuiStyle.font(graphics, 9.0f);
        label.drawString(value.getName(), x + 14,
                ClickGuiStyle.centeredY(label, rowY, ROW_HEIGHT),
                ClickGuiStyle.COLOR_TEXT_DIM);

        float right = x + width - 12;

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

            graphics.fillRoundedRect(pillX, rowY + 3, pillWidth,
                    ROW_HEIGHT - 6, 4,
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

    /**
     * @return {@code true} when the click was inside the window (and has been
     *         consumed), {@code false} when the caller should dismiss it.
     */
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!isOpen()
                || !ClickGuiStyle.isInside(mouseX, mouseY, x, y,
                        width, height)) {
            return false;
        }

        float closeX = x + width - 12 - CLOSE_SIZE;
        float closeY = y + (HEADER_HEIGHT - CLOSE_SIZE) / 2.0f;
        if (button == 0 && ClickGuiStyle.isInside(mouseX, mouseY, closeX,
                closeY, CLOSE_SIZE, CLOSE_SIZE)) {
            close();
            return true;
        }

        if (ClickGuiStyle.isInside(mouseX, mouseY, x, y,
                width, HEADER_HEIGHT)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return true;
        }

        float rowY = y + HEADER_HEIGHT + BODY_PADDING;
        for (Value<?> value : settings) {
            if (ClickGuiStyle.isInside(mouseX, mouseY, x + 6, rowY,
                    width - 12, ROW_HEIGHT)) {
                applySetting(value, button);
                return true;
            }
            rowY += ROW_HEIGHT;
        }

        return true;
    }

    public void mouseDragged(float mouseX, float mouseY, int button) {
        if (!isOpen() || !dragging || button != 0) {
            return;
        }

        x = ClickGuiStyle.clamp(mouseX - dragOffsetX, 4.0f,
                Math.max(4.0f, screenWidth - width - 4.0f));
        y = ClickGuiStyle.clamp(mouseY - dragOffsetY, 4.0f,
                Math.max(4.0f, screenHeight - height - 4.0f));
        x = Math.round(x);
        y = Math.round(y);
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    private void applySetting(Value<?> value, int button) {
        if (button != 0 && button != 1) {
            return;
        }

        if (value instanceof BooleanValue) {
            if (button == 0) {
                ((BooleanValue) value).toggle();
            }
        } else if (value instanceof ModeValue) {
            ModeValue modeValue = (ModeValue) value;
            if (button == 0) {
                modeValue.next();
            } else {
                modeValue.previous();
            }
        }
    }
}
