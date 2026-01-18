package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.value.ModeValue;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.ValueComponent;
import cn.xiaozhou233.orangex.ui.clickgui.render.GuiRenderUtils;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ModeComponent extends ValueComponent {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final ModeValue value;

    public ModeComponent(double x, double y, double width, ModeValue value, Component parent) {
        super(x, y, width, 14, parent);
        this.value = value;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double ax = getAbsoluteX();
        double ay = getAbsoluteY();

        GuiRenderUtils.enableBlend();

        // background
        GuiRenderUtils.drawRect(ax, ay, width, height, 0x80000000);

        // name text
        GuiRenderUtils.drawString(value.getName(), ax + 4, ay + 3, 0xFFFFFFFF);

        // mode text
        String mode = value.getValue();
        GuiRenderUtils.drawString(mode, ax + width - 4 - mc.fontRendererObj.getStringWidth(mode), ay + 3, 0xFFFFFFFF);

        GuiRenderUtils.disableBlend();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            List<String> modes = value.getModes();
            int idx = (value.getIndex() + 1) % modes.size();
            value.setIndex(idx);
        }
    }

    @Override
    public void applyValue() {
        // optional
    }
}
