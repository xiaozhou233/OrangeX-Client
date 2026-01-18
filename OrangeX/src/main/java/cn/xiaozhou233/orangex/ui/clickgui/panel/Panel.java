package cn.xiaozhou233.orangex.ui.clickgui.panel;

import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Panel extends Component {

    @Getter
    private final String title;
    @Getter
    private final List<Component> components = new ArrayList<>();

    private boolean dragging;
    private double dragX, dragY;

    @Getter
    private boolean open = true;
    private final double headerHeight = 18;

    public Panel(String title, double x, double y, double width) {
        super(x, y, width, 0, null);
        this.title = title;
        this.height = headerHeight;
    }

    /* ================= 渲染 ================= */

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        // draw header background
        GuiRenderUtils.enableBlend();
        GuiRenderUtils.drawRect(ax, ay, width, headerHeight, 0x80000000);

        // draw title
        GuiRenderUtils.drawString(title, ax + 4, ay + 4, 0xFFFFFFFF);
        GuiRenderUtils.disableBlend();

        if (!open) return;

        double offsetY = headerHeight;

        for (Component component : components) {
            if (!component.isVisible()) continue;

            component.setY(offsetY);
            component.render(mouseX, mouseY, partialTicks);
            offsetY += component.getHeight();
        }

        this.height = offsetY;
    }


    /* ================= 更新 ================= */

    @Override
    public void update(int mouseX, int mouseY) {
        if (dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }

        if (!open) return;

        for (Component component : components) {
            component.update(mouseX, mouseY);
        }
    }

    /* ================= 鼠标 ================= */

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHeaderHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }

            if (button == 1) {
                open = !open;
            }
            return;
        }

        if (!open) return;

        for (Component component : components) {
            component.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;

        if (!open) return;

        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
    }

    /* ================= 工具 ================= */

    private boolean isHeaderHovered(int mouseX, int mouseY) {
        double px = getAbsoluteX();
        double py = getAbsoluteY();
        return mouseX >= px && mouseX <= px + width
                && mouseY >= py && mouseY <= py + headerHeight;
    }

    public void addComponent(Component component) {
        components.add(component);
    }

}
