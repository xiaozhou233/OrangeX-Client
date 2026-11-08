package cn.xiaozhou233.orangex.ui.clickgui;

/**
 * Immutable geometry for one ClickGUI frame.
 *
 * <p>The panel keeps its own bounds and category-row height; this class turns
 * them into the positions and sizes used by the individual renderers.</p>
 */
public final class ClickGuiLayout {

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float categoryRowHeight;
    private final int screenWidth;
    private final int screenHeight;

    public ClickGuiLayout(float x, float y, float width, float height,
                          float categoryRowHeight, int screenWidth,
                          int screenHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.categoryRowHeight = categoryRowHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float categoryRowHeight() {
        return categoryRowHeight;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public float contentX() {
        return x + ClickGuiStyle.SIDEBAR_CARD_X
                + ClickGuiStyle.SIDEBAR_WIDTH + 14.0f;
    }

    public float contentWidth() {
        return x + width - 12.0f - contentX();
    }

    public float listTop() {
        return y + ClickGuiStyle.HEADER_HEIGHT + 38.0f;
    }

    public float listBottom() {
        return y + height - 8.0f;
    }

    public float categoryRowX() {
        return x + ClickGuiStyle.SIDEBAR_CARD_X + 6.0f;
    }

    public float categoryRowWidth() {
        return ClickGuiStyle.SIDEBAR_WIDTH - 12.0f;
    }

    public float categoryRowTop() {
        return y + ClickGuiStyle.SIDEBAR_CARD_Y + 9.0f;
    }
}
