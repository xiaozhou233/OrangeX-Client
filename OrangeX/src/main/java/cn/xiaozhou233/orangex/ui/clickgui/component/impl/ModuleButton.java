package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.option.KeybindComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.option.OptionComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.option.OptionComponentFactory;
import lombok.Getter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Component {

    private final Module module;

    @Getter
    private boolean expanded;
    @Getter
    private final List<OptionComponent> options = new ArrayList<>();

    public ModuleButton(Panel parent, Module module, int offsetX, int offsetY) {
        super(parent, offsetX, offsetY, 120, 20);

        this.module = module;

        int optionY = 20;

        for (Option<?> option : module.getOptions()) {
            OptionComponent component =
                    OptionComponentFactory.create(
                            parent,
                            option,
                            offsetX,
                            offsetY + optionY
                    );

            if (component != null)
                options.add(component);

            optionY += 18;
        }

        options.add(new KeybindComponent(parent, module, offsetX, offsetY + optionY));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (module.isEnabled()) {
            GlStateManager.resetColor();
            Gui.drawRect(getX(), getY(), getX() + width, getY() + height, 0xff1a4a2a);
        }

        drawString(
                module.getName(),
                getX() + 5,
                getY() + 5,
                0xffffffff
        );

        if (expanded) {
            int optionY = 20;
            for (OptionComponent option : options) {
                option.setOffsetY(getOffsetY() + optionY);
                option.drawScreen(mouseX, mouseY, partialTicks);
                optionY += 18;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseButton == 0)
                module.toggle();

            if (mouseButton == 1) {
                expanded = !expanded;
                parent.updateLayout();
            }
        }

        if (expanded) {
            int optionY = 20;
            for (OptionComponent option : options) {
                option.setOffsetY(getOffsetY() + optionY);
                option.mouseClicked(mouseX, mouseY, mouseButton);
                optionY += 18;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (expanded) {
            for (OptionComponent option : options) {
                option.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (OptionComponent option : options) {
                if (option.keyTyped(typedChar, keyCode))
                    return true;
            }
        }
        return false;
    }

    @Override
    public int getHeight() {
        if (!expanded)
            return 20;

        return 20 + options.size() * 18;
    }
}