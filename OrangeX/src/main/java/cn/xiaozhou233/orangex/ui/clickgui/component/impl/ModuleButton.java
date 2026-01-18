package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Component {

    private final Module module;
    private final List<Component> components = new ArrayList<>();

    @Getter
    private boolean expanded = false;

    private final double baseHeight = 16;

    public ModuleButton(double x, double y, double width, Module module, Component parent) {
        super(x, y, width, 16, parent);
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        int bgColor = 0x80000000;

        if (module.isEnabled()) {
            bgColor = 0xFF2E8B57;
        }

        if (isHovered(mouseX, mouseY)) {
            bgColor = GuiRenderUtils.blendColor(bgColor, 0xFFFFFFFF, 0.15f);
        }

        // draw background
        GuiRenderUtils.enableBlend();
        GuiRenderUtils.drawRect(ax, ay, width, baseHeight, bgColor);

        // draw status bar (left)
        int statusColor = module.isEnabled() ? 0xFF00FF00 : 0xFF888888;
        GuiRenderUtils.drawRect(ax, ay, 2, baseHeight, statusColor);

        // draw module name
        GuiRenderUtils.drawString(module.getName(), ax + 6, ay + 3, 0xFFFFFFFF);
        GuiRenderUtils.disableBlend();

        if (!expanded) {
            this.height = baseHeight;
            return;
        }

        double offsetY = baseHeight;
        for (Component component : components) {
            component.setY(offsetY);
            component.setWidth(width);  // ensure same width
            component.render(mouseX, mouseY, partialTicks);
            offsetY += component.getHeight();
        }
        this.height = offsetY;
    }

    @Override
    public void update(int mouseX, int mouseY) {
        if (!expanded) return;

        for (Component component : components) {
            component.update(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;

        if (button == 0) {
            module.toggle();
        } else if (button == 1) {
            expanded = !expanded;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (!expanded) return;

        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void addComponent(Component component) {
        components.add(component);
    }

}
