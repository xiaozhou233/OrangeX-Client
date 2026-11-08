package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventRender2D;
import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.modules.ModuleManager;
import cn.xiaozhou233.orangex.render.api.Render2D;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.value.BooleanValue;
import cn.xiaozhou233.orangex.value.ModeValue;
import cn.xiaozhou233.orangex.value.Value;

import java.util.List;

/**
 * The main ClickGUI window.
 *
 * <p>A single rounded window with a category sidebar on the left and the
 * selected category's modules on the right. Left-clicking a module toggles it;
 * right-clicking a module opens its settings in the separate
 * {@link SettingsWindow}. The window itself can be dragged by its header.</p>
 *
 * <p>All coordinates are Minecraft GUI pixels. Input is polled from
 * {@link ClientInput} because the empty input screen does not forward
 * callbacks to this class directly. This class owns the window state and input
 * routing; drawing is delegated to the renderer classes in this package.</p>
 */
public final class MainPanel extends Component {

    private static final ModuleCategory[] CATEGORIES = ModuleCategory.values();

    private final ModuleManager moduleManager =
            OrangeX.getInstance().getModuleManager();

    private final ClickGuiAnimator animator = new ClickGuiAnimator();
    private final SettingsWindow settingsWindow = new SettingsWindow();

    private ModuleCategory selectedCategory = ModuleCategory.RENDER;

    private boolean hasPosition;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private int screenWidth = 854;
    private int screenHeight = 480;
    private float categoryRowHeight = ClickGuiStyle.CATEGORY_HEIGHT;

    private long lastFrameNanos;

    @Override
    public void render(EventRender2D event) {
        Render2D graphics = event.getRender2D();
        updateBounds(graphics);

        ClickGuiLayout layout = layout();
        float delta = updateDelta();
        double mouseX = ClientInput.getMouseX();
        double mouseY = ClientInput.getMouseY();

        PanelChrome.drawShadow(graphics, layout);
        graphics.fillRoundedRect(layout.x(), layout.y(), layout.width(),
                layout.height(), ClickGuiStyle.PANEL_RADIUS, 1.0,
                ClickGuiStyle.COLOR_PANEL, ClickGuiStyle.COLOR_BORDER);

        PanelChrome.drawHeader(graphics, layout);
        SidebarRenderer.draw(graphics, layout, selectedCategory,
                moduleManager.getAllModules(), animator, delta, mouseX, mouseY);
        ContentRenderer.draw(graphics, layout,
                modulesOf(selectedCategory), selectedCategory,
                animator, delta, mouseX, mouseY);

        settingsWindow.render(graphics, delta, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (settingsWindow.isOpen()) {
            if (settingsWindow.mouseClicked(mouseX, mouseY, button)) {
                return;
            }
            settingsWindow.close();
            return;
        }

        if (!ClickGuiStyle.isInside(mouseX, mouseY, x, y, width, height)) {
            return;
        }

        if (ClickGuiStyle.isInside(mouseX, mouseY, x, y, width,
                ClickGuiStyle.HEADER_HEIGHT)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return;
        }

        ClickGuiLayout layout = layout();
        ModuleCategory category = categoryAt(layout, mouseX, mouseY);
        if (category != null) {
            selectedCategory = category;
            return;
        }

        handleContentClick(layout, mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(float mouseX, float mouseY, int button) {
        if (settingsWindow.isOpen()) {
            settingsWindow.mouseDragged(mouseX, mouseY, button);
            return;
        }

        if (!dragging || button != 0) {
            return;
        }

        float maxX = Math.max(4.0f, screenWidth - width - 4.0f);
        float maxY = Math.max(4.0f, screenHeight - height - 4.0f);

        x = ClickGuiStyle.clamp(mouseX - dragOffsetX, 4.0f, maxX);
        y = ClickGuiStyle.clamp(mouseY - dragOffsetY, 4.0f, maxY);
        // 26.2 glyph blits snap to whole pixels; a fractional panel origin
        // makes every glyph round independently while dragging. Keep the
        // window on the pixel grid so text moves as one stable block.
        x = Math.round(x);
        y = Math.round(y);
        hasPosition = true;
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        settingsWindow.mouseReleased(mouseX, mouseY, button);
        if (button == 0) {
            dragging = false;
        }
    }

    public void resetInteraction() {
        dragging = false;
        settingsWindow.close();
    }

    /* ------------------------------------------------------------------ */
    /* Interaction                                                        */
    /* ------------------------------------------------------------------ */

    private boolean handleContentClick(ClickGuiLayout layout, float mouseX,
                                       float mouseY, int button) {
        if (mouseY < layout.listTop() || mouseY > layout.listBottom()) {
            return false;
        }

        float contentX = layout.contentX();
        float contentWidth = layout.contentWidth();
        float moduleHeight = ClickGuiStyle.MODULE_HEIGHT;
        float rowY = layout.listTop();

        for (Module module : modulesOf(selectedCategory)) {
            if (ClickGuiStyle.isInside(mouseX, mouseY, contentX, rowY,
                    contentWidth, moduleHeight)) {
                if (button == 0) {
                    module.toggle();
                } else if (button == 1
                        && !ClickGuiModules.settingsOf(module).isEmpty()) {
                    settingsWindow.open(module, layout);
                }
                return true;
            }

            rowY += moduleHeight + ClickGuiStyle.MODULE_GAP;
        }

        return false;
    }

    private ModuleCategory categoryAt(ClickGuiLayout layout, float mouseX,
                                      float mouseY) {
        float rowX = layout.categoryRowX();
        float rowWidth = layout.categoryRowWidth();
        float rowY = layout.categoryRowTop();
        float rowHeight = layout.categoryRowHeight();

        for (ModuleCategory category : CATEGORIES) {
            if (ClickGuiStyle.isInside(mouseX, mouseY, rowX, rowY,
                    rowWidth, rowHeight)) {
                return category;
            }
            rowY += rowHeight + ClickGuiStyle.CATEGORY_GAP;
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
    /* Layout helpers                                                     */
    /* ------------------------------------------------------------------ */

    private ClickGuiLayout layout() {
        return new ClickGuiLayout(x, y, width, height, categoryRowHeight,
                screenWidth, screenHeight);
    }

    private void updateBounds(Render2D graphics) {
        screenWidth = graphics.getWidth();
        screenHeight = graphics.getHeight();

        width = Math.min(ClickGuiStyle.DESIGN_WIDTH,
                Math.max(280.0f, screenWidth - ClickGuiStyle.MARGIN * 2.0f));
        height = Math.min(ClickGuiStyle.DESIGN_HEIGHT,
                Math.max(220.0f, screenHeight - ClickGuiStyle.MARGIN * 2.0f));

        // Keep all seven category rows inside the sidebar card even when the
        // panel is compressed by a small GUI-scaled screen.
        float cardHeight = height - ClickGuiStyle.HEADER_HEIGHT - 16.0f;
        float availableRows = cardHeight - 18.0f;
        float rowHeight = (availableRows
                - (CATEGORIES.length - 1) * ClickGuiStyle.CATEGORY_GAP)
                / CATEGORIES.length;
        categoryRowHeight = Math.max(12.0f,
                Math.min(ClickGuiStyle.CATEGORY_HEIGHT, rowHeight));

        if (!hasPosition) {
            x = (screenWidth - width) / 2.0f;
            y = (screenHeight - height) / 2.0f;
            hasPosition = true;
        }

        x = ClickGuiStyle.clamp(x, 4.0f,
                Math.max(4.0f, screenWidth - width - 4.0f));
        y = ClickGuiStyle.clamp(y, 4.0f,
                Math.max(4.0f, screenHeight - height - 4.0f));
        x = Math.round(x);
        y = Math.round(y);
    }

    private float updateDelta() {
        long now = System.nanoTime();
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return 0.016f;
        }
        float delta = (now - lastFrameNanos) / 1_000_000_000.0f;
        lastFrameNanos = now;
        return ClickGuiStyle.clamp(delta, 0.0f, 0.1f);
    }

    private List<Module> modulesOf(ModuleCategory category) {
        return moduleManager.getModulesByCategory(category);
    }
}
