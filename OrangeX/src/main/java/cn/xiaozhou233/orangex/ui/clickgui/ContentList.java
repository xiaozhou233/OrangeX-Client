package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;

import java.util.List;

/**
 * Row geometry for the module list.
 *
 * <p>Module rows are evenly sized, so a row's position is just its index times
 * the row stride minus the scroll offset. The renderer and the input code share
 * this class so both always agree on where a row is.</p>
 */
public final class ContentList {

    private final ClickGuiLayout layout;
    private final List<Module> modules;

    private float scroll;

    public ContentList(ClickGuiLayout layout, List<Module> modules,
                       float scroll) {
        this.layout = layout;
        this.modules = modules;
        this.scroll = scroll;
    }

    public List<Module> modules() {
        return modules;
    }

    public float scroll() {
        return scroll;
    }

    public void scrollBy(float amount) {
        scroll += amount;
    }

    /**
     * Clamps the scroll offset to the current content, so switching category
     * never leaves the list overscrolled.
     */
    public void clampScroll() {
        scroll = ClickGuiStyle.clamp(scroll, 0.0f, maxScroll());
    }

    public float rowStride() {
        return ClickGuiStyle.MODULE_HEIGHT + ClickGuiStyle.MODULE_GAP;
    }

    public float rowTop(int index) {
        return layout.listTop() + index * rowStride() - scroll;
    }

    public float contentHeight() {
        return modules.isEmpty() ? 0.0f
                : modules.size() * rowStride() - ClickGuiStyle.MODULE_GAP;
    }

    public float maxScroll() {
        return Math.max(0.0f,
                contentHeight() - (layout.listBottom() - layout.listTop()));
    }

    /**
     * @return the index of the module row under {@code mouseY}, or {@code -1}.
     */
    public int moduleIndexAt(float mouseY) {
        for (int i = 0; i < modules.size(); i++) {
            float top = rowTop(i);
            if (mouseY >= top && mouseY <= top + ClickGuiStyle.MODULE_HEIGHT) {
                return i;
            }
        }
        return -1;
    }
}
