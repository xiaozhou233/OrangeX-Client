package cn.xiaozhou233.orangex.ui.clickgui.component;

import cn.xiaozhou233.orangex.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class ModuleButton extends Component {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Module module;
    private boolean expanded;

    private static final int BUTTON_HEIGHT = 14;
    private static final int EXPANDED_HEIGHT = 20; // placeholder

    public ModuleButton(Module module, int width) {
        this.module = module;
        this.width = width;
        this.height = BUTTON_HEIGHT;
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        boolean hover = isHovering(mouseX, mouseY);

        int bgColor;
        if (module.isEnabled()) {
            bgColor = hover
                    ? new Color(70, 130, 180, 220).getRGB()
                    : new Color(60, 120, 170, 200).getRGB();
        } else {
            bgColor = hover
                    ? new Color(80, 80, 80, 200).getRGB()
                    : new Color(60, 60, 60, 180).getRGB();
        }

        // Button background
        Gui.drawRect(x, y, x + width, y + BUTTON_HEIGHT, bgColor);

        // Module name
        mc.fontRendererObj.drawString(
                module.getName(),
                x + 4,
                y + 3,
                0xFFFFFFFF
        );

        // Expanded area (future settings)
        if (expanded) {
            Gui.drawRect(
                    x,
                    y + BUTTON_HEIGHT,
                    x + width,
                    y + height,
                    new Color(45, 45, 45, 200).getRGB()
            );
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isHovering(mouseX, mouseY)) return;

        // Left click: toggle
        if (button == 0) {
            module.toggle();
        }

        // Right click: expand
        if (button == 1) {
            expanded = !expanded;
            height = expanded
                    ? BUTTON_HEIGHT + EXPANDED_HEIGHT
                    : BUTTON_HEIGHT;
        }
    }
}
